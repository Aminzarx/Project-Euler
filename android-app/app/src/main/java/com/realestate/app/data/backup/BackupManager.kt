package com.realestate.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.contact.Contact
import com.realestate.app.data.contact.PreferredContactTime
import com.realestate.app.data.toJson
import com.realestate.app.data.toProperty
import com.realestate.app.data.property.Note
import com.realestate.app.data.property.TimelineEvent
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.data.wallet.TransactionStatus
import com.realestate.app.data.wallet.TransactionType
import com.realestate.app.data.wallet.WalletTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// Bumped for the Contact entity (data/contact/Contact.kt) — restoreBackup still reads version-1
// and version-2 files fine; a backup without a "contacts" key simply restores zero contacts,
// exactly like an old backup already restores zero notes/events for keys it predates.
private const val BACKUP_FORMAT_VERSION = 3

data class BackupResult(
    val propertyCount: Int,
    val noteCount: Int,
    val eventCount: Int,
    val transactionCount: Int,
    val contactCount: Int,
    val sizeBytes: Long
)

/**
 * A read-only look at a backup file's contents (counts, format version, size) before committing
 * to a restore. Restoring replaces the entire local database, so agents need to know what
 * they're about to load before they confirm it — this is what powers that confirmation dialog.
 */
data class BackupPreview(
    val formatVersion: Int,
    val propertyCount: Int,
    val noteCount: Int,
    val eventCount: Int,
    val transactionCount: Int,
    val contactCount: Int,
    val sizeBytes: Long
)

/**
 * Offline JSON backup/restore for the whole local database. No network involved — the file is
 * handed off through Android's Storage Access Framework (the system document picker), which
 * already lets a user save to or open from any cloud-backed provider installed on the device
 * (Google Drive, Dropbox, OneDrive, ...) without this app needing its own cloud integration.
 * If a dedicated cloud sync feature is ever added, it plugs in here as an alternate source/sink
 * for the same JSON payload this class already produces and consumes.
 *
 * The backup file is plain, unencrypted JSON and may contain customer names and phone numbers.
 * Encrypting it (e.g. a passphrase-derived key via Android's Keystore/EncryptedFile) is a
 * reasonable future addition, but doing that safely needs its own dedicated design and testing —
 * left as a deliberate seam rather than bolted on here.
 */
class BackupManager(private val context: Context) {
    private val db = AppDatabase.getInstance(context)

    suspend fun createBackup(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        val properties = db.propertyDao().getAllProperties().first()
        val notes = db.noteDao().getAllNotes().first()
        val events = db.timelineDao().getAllEvents().first()
        val transactions = db.walletDao().getAllTransactions().first()
        val contacts = db.contactDao().getAllContacts().first()

        val json = JSONObject().apply {
            put("version", BACKUP_FORMAT_VERSION)
            put("properties", JSONArray(properties.map { it.toJson() }))
            put("notes", JSONArray(notes.map { it.toJson() }))
            put("timelineEvents", JSONArray(events.map { it.toJson() }))
            put("walletTransactions", JSONArray(transactions.map { it.toJson() }))
            put("contacts", JSONArray(contacts.map { it.toJson() }))
        }
        val bytes = json.toString(2).toByteArray(Charsets.UTF_8)
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("امکان نوشتن فایل پشتیبان وجود ندارد")
        BackupResult(properties.size, notes.size, events.size, transactions.size, contacts.size, bytes.size.toLong())
    }

    /** Reads and parses the file to report what it contains, without touching the database. */
    suspend fun peekBackup(uri: Uri): BackupPreview = withContext(Dispatchers.IO) {
        val text = readBackupText(uri)
        val json = JSONObject(text)
        BackupPreview(
            formatVersion = json.optInt("version", 1),
            propertyCount = json.getJSONArray("properties").length(),
            noteCount = json.optJSONArray("notes")?.length() ?: 0,
            eventCount = json.optJSONArray("timelineEvents")?.length() ?: 0,
            transactionCount = json.optJSONArray("walletTransactions")?.length() ?: 0,
            contactCount = json.optJSONArray("contacts")?.length() ?: 0,
            sizeBytes = text.toByteArray(Charsets.UTF_8).size.toLong()
        )
    }

    suspend fun restoreBackup(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        val text = readBackupText(uri)

        val json = JSONObject(text)
        val properties = json.getJSONArray("properties").toObjectList { it.toProperty() }
        val notes = json.optJSONArray("notes")?.toObjectList { it.toNote() } ?: emptyList()
        val events = json.optJSONArray("timelineEvents")?.toObjectList { it.toTimelineEvent() } ?: emptyList()
        val transactions = json.optJSONArray("walletTransactions")?.toObjectList { it.toWalletTransaction() } ?: emptyList()
        val contacts = json.optJSONArray("contacts")?.toObjectList { it.toContact() } ?: emptyList()

        // Wrapped in a single database transaction so a crash or force-close mid-restore can't
        // leave the database half-wiped/half-restored — either the whole swap lands, or none of it does.
        db.withTransaction {
            db.propertyDao().deleteAll()
            db.noteDao().deleteAll()
            db.timelineDao().deleteAll()
            db.walletDao().deleteAll()
            db.contactDao().deleteAll()

            db.propertyDao().insertAll(properties)
            db.noteDao().insertAll(notes)
            db.timelineDao().insertAll(events)
            db.walletDao().insertAll(transactions)
            db.contactDao().insertAll(contacts)
        }

        BackupResult(properties.size, notes.size, events.size, transactions.size, contacts.size, text.toByteArray(Charsets.UTF_8).size.toLong())
    }

    private fun readBackupText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("امکان خواندن فایل پشتیبان وجود ندارد")
}

private inline fun <T> JSONArray.toObjectList(map: (JSONObject) -> T): List<T> =
    (0 until length()).map { map(getJSONObject(it)) }

private fun Note.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("propertyId", propertyId)
    put("content", content)
    put("createdAt", createdAt)
}

private fun JSONObject.toNote(): Note = Note(
    id = getLong("id"),
    propertyId = getLong("propertyId"),
    content = getString("content"),
    createdAt = getLong("createdAt")
)

private fun TimelineEvent.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("propertyId", propertyId)
    put("type", type.name)
    put("description", description)
    put("createdAt", createdAt)
}

private fun JSONObject.toTimelineEvent(): TimelineEvent = TimelineEvent(
    id = getLong("id"),
    propertyId = getLong("propertyId"),
    type = TimelineEventType.valueOf(getString("type")),
    description = getString("description"),
    createdAt = getLong("createdAt")
)

private fun WalletTransaction.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("amount", amount)
    put("type", type.name)
    put("status", status.name)
    put("description", description)
    put("referenceId", referenceId ?: JSONObject.NULL)
    put("createdAt", createdAt)
}

private fun JSONObject.toWalletTransaction(): WalletTransaction = WalletTransaction(
    id = getLong("id"),
    amount = getLong("amount"),
    type = TransactionType.valueOf(getString("type")),
    status = TransactionStatus.valueOf(getString("status")),
    description = getString("description"),
    referenceId = if (isNull("referenceId")) null else getString("referenceId"),
    createdAt = getLong("createdAt")
)

private fun Contact.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("uid", uid)
    put("fullName", fullName)
    put("primaryPhone", primaryPhone)
    put("secondaryPhone", secondaryPhone ?: JSONObject.NULL)
    put("landlinePhone", landlinePhone ?: JSONObject.NULL)
    put("whatsappNumber", whatsappNumber ?: JSONObject.NULL)
    put("preferredContactTime", preferredContactTime?.name ?: JSONObject.NULL)
    put("email", email ?: JSONObject.NULL)
    put("note", note ?: JSONObject.NULL)
    put("createdAt", createdAt)
    put("lastCaseAt", lastCaseAt ?: JSONObject.NULL)
}

private fun JSONObject.toContact(): Contact = Contact(
    id = getLong("id"),
    uid = optString("uid", java.util.UUID.randomUUID().toString()),
    fullName = getString("fullName"),
    primaryPhone = getString("primaryPhone"),
    secondaryPhone = if (isNull("secondaryPhone")) null else optString("secondaryPhone"),
    landlinePhone = if (isNull("landlinePhone")) null else optString("landlinePhone"),
    whatsappNumber = if (isNull("whatsappNumber")) null else optString("whatsappNumber"),
    preferredContactTime = if (isNull("preferredContactTime")) null else PreferredContactTime.valueOf(getString("preferredContactTime")),
    email = if (isNull("email")) null else optString("email"),
    note = if (isNull("note")) null else optString("note"),
    createdAt = getLong("createdAt"),
    lastCaseAt = if (isNull("lastCaseAt")) null else getLong("lastCaseAt")
)
