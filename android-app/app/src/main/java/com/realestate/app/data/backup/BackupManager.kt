package com.realestate.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.CaseFlag
import com.realestate.app.data.CasePriority
import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.CaseType
import com.realestate.app.data.DealType
import com.realestate.app.data.MortgageStatus
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.RequestValidityType
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

// Bumped for the Case redesign (data/Property.kt) — restoreBackup still reads version-1 files
// fine (every new field is read with a has()/isNull() guard, defaulting exactly the way the
// Room migration does for existing rows: caseType OWNER, everything else null/empty/its own
// enum default).
private const val BACKUP_FORMAT_VERSION = 2

data class BackupResult(
    val propertyCount: Int,
    val noteCount: Int,
    val eventCount: Int,
    val transactionCount: Int,
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

        val json = JSONObject().apply {
            put("version", BACKUP_FORMAT_VERSION)
            put("properties", JSONArray(properties.map { it.toJson() }))
            put("notes", JSONArray(notes.map { it.toJson() }))
            put("timelineEvents", JSONArray(events.map { it.toJson() }))
            put("walletTransactions", JSONArray(transactions.map { it.toJson() }))
        }
        val bytes = json.toString(2).toByteArray(Charsets.UTF_8)
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("امکان نوشتن فایل پشتیبان وجود ندارد")
        BackupResult(properties.size, notes.size, events.size, transactions.size, bytes.size.toLong())
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

        // Wrapped in a single database transaction so a crash or force-close mid-restore can't
        // leave the database half-wiped/half-restored — either the whole swap lands, or none of it does.
        db.withTransaction {
            db.propertyDao().deleteAll()
            db.noteDao().deleteAll()
            db.timelineDao().deleteAll()
            db.walletDao().deleteAll()

            db.propertyDao().insertAll(properties)
            db.noteDao().insertAll(notes)
            db.timelineDao().insertAll(events)
            db.walletDao().insertAll(transactions)
        }

        BackupResult(properties.size, notes.size, events.size, transactions.size, text.toByteArray(Charsets.UTF_8).size.toLong())
    }

    private fun readBackupText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("امکان خواندن فایل پشتیبان وجود ندارد")
}

private inline fun <T> JSONArray.toObjectList(map: (JSONObject) -> T): List<T> =
    (0 until length()).map { map(getJSONObject(it)) }

private fun Property.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("price", price)
    put("area", area)
    put("rooms", rooms)
    put("city", city)
    put("address", address)
    put("ownerName", ownerName)
    put("ownerPhone", ownerPhone)
    put("dealType", dealType.name)
    put("propertyType", propertyType.name)
    put("status", status.name)
    put("tags", JSONArray(tags))
    put("imageUri", imageUri ?: JSONObject.NULL)
    put("isFavorite", isFavorite)
    put("isPinned", isPinned)
    put("favoriteFolder", favoriteFolder ?: JSONObject.NULL)
    put("dateAdded", dateAdded)
    put("lastModifiedAt", lastModifiedAt)
    put("lastViewedAt", lastViewedAt ?: JSONObject.NULL)
    put("lastSharedAt", lastSharedAt ?: JSONObject.NULL)
    put("viewCount", viewCount)
    put("followUpAt", followUpAt ?: JSONObject.NULL)

    // ---- Case redesign fields ----
    put("caseType", caseType.name)
    put("transactionType", transactionType?.name ?: JSONObject.NULL)
    put("caseFlags", JSONArray(caseFlags.map { it.name }))
    put("priority", priority.name)
    put("expiryType", expiryType.name)
    put("customExpiryAt", customExpiryAt ?: JSONObject.NULL)
    put("mortgageStatus", mortgageStatus.name)
    put("titleDeedReady", titleDeedReady ?: JSONObject.NULL)
    put("reasonForSelling", reasonForSelling ?: JSONObject.NULL)
    put("viewingHours", viewingHours ?: JSONObject.NULL)
    put("keyHolder", keyHolder ?: JSONObject.NULL)
    put("paymentConditions", paymentConditions ?: JSONObject.NULL)
    put("constructionAge", constructionAge ?: JSONObject.NULL)
    put("legalStatus", legalStatus ?: JSONObject.NULL)
    put("hiddenNotes", hiddenNotes ?: JSONObject.NULL)
    put("floor", floor ?: JSONObject.NULL)
    put("totalFloors", totalFloors ?: JSONObject.NULL)
    put("landZoning", landZoning ?: JSONObject.NULL)
    put("hasBusinessLicense", hasBusinessLicense ?: JSONObject.NULL)
    put("budgetMin", budgetMin ?: JSONObject.NULL)
    put("budgetMax", budgetMax ?: JSONObject.NULL)
    put("desiredMinArea", desiredMinArea ?: JSONObject.NULL)
    put("desiredMaxArea", desiredMaxArea ?: JSONObject.NULL)
    put("desiredBedrooms", desiredBedrooms ?: JSONObject.NULL)
    put("preferredAreas", JSONArray(preferredAreas))
    put("floorPreference", floorPreference ?: JSONObject.NULL)
    put("viewPreference", viewPreference ?: JSONObject.NULL)
    put("cashAvailable", cashAvailable ?: JSONObject.NULL)
    put("maxDeposit", maxDeposit ?: JSONObject.NULL)
    put("maxMonthlyRent", maxMonthlyRent ?: JSONObject.NULL)
}

private fun JSONObject.toProperty(): Property = Property(
    id = getLong("id"),
    title = getString("title"),
    description = optString("description", ""),
    price = getLong("price"),
    area = getDouble("area"),
    rooms = getInt("rooms"),
    city = getString("city"),
    address = getString("address"),
    ownerName = optString("ownerName", ""),
    ownerPhone = optString("ownerPhone", ""),
    dealType = DealType.valueOf(getString("dealType")),
    propertyType = PropertyType.valueOf(getString("propertyType")),
    status = PropertyStatus.valueOf(optString("status", PropertyStatus.NEW.name)),
    tags = getJSONArray("tags").let { arr -> (0 until arr.length()).map { i -> arr.getString(i) } },
    imageUri = if (isNull("imageUri")) null else getString("imageUri"),
    isFavorite = optBoolean("isFavorite", false),
    isPinned = optBoolean("isPinned", false),
    favoriteFolder = if (isNull("favoriteFolder")) null else optString("favoriteFolder"),
    dateAdded = getLong("dateAdded"),
    lastModifiedAt = optLong("lastModifiedAt", System.currentTimeMillis()),
    lastViewedAt = if (isNull("lastViewedAt")) null else getLong("lastViewedAt"),
    lastSharedAt = if (isNull("lastSharedAt")) null else getLong("lastSharedAt"),
    viewCount = optInt("viewCount", 0),
    followUpAt = if (has("followUpAt") && !isNull("followUpAt")) getLong("followUpAt") else null,

    // ---- Case redesign fields — every one defaults exactly like MIGRATION_6_7 does, so a
    // version-1 backup (no such keys at all) restores as a well-formed OWNER case. ----
    caseType = if (has("caseType")) CaseType.valueOf(getString("caseType")) else CaseType.OWNER,
    transactionType = optEnumOrNull("transactionType", CaseTransactionType::valueOf),
    caseFlags = optJSONArray("caseFlags")?.let { arr -> (0 until arr.length()).map { CaseFlag.valueOf(arr.getString(it)) } } ?: emptyList(),
    priority = if (has("priority")) CasePriority.valueOf(getString("priority")) else CasePriority.NORMAL,
    expiryType = if (has("expiryType")) RequestValidityType.valueOf(getString("expiryType")) else RequestValidityType.NO_EXPIRATION,
    customExpiryAt = optLongOrNull("customExpiryAt"),
    mortgageStatus = if (has("mortgageStatus")) MortgageStatus.valueOf(getString("mortgageStatus")) else MortgageStatus.NONE,
    titleDeedReady = optBooleanOrNull("titleDeedReady"),
    reasonForSelling = optStringOrNull("reasonForSelling"),
    viewingHours = optStringOrNull("viewingHours"),
    keyHolder = optStringOrNull("keyHolder"),
    paymentConditions = optStringOrNull("paymentConditions"),
    constructionAge = optIntOrNull("constructionAge"),
    legalStatus = optStringOrNull("legalStatus"),
    hiddenNotes = optStringOrNull("hiddenNotes"),
    floor = optIntOrNull("floor"),
    totalFloors = optIntOrNull("totalFloors"),
    landZoning = optStringOrNull("landZoning"),
    hasBusinessLicense = optBooleanOrNull("hasBusinessLicense"),
    budgetMin = optLongOrNull("budgetMin"),
    budgetMax = optLongOrNull("budgetMax"),
    desiredMinArea = optDoubleOrNull("desiredMinArea"),
    desiredMaxArea = optDoubleOrNull("desiredMaxArea"),
    desiredBedrooms = optIntOrNull("desiredBedrooms"),
    preferredAreas = optJSONArray("preferredAreas")?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList(),
    floorPreference = optStringOrNull("floorPreference"),
    viewPreference = optStringOrNull("viewPreference"),
    cashAvailable = optLongOrNull("cashAvailable"),
    maxDeposit = optLongOrNull("maxDeposit"),
    maxMonthlyRent = optLongOrNull("maxMonthlyRent")
)

// ---- Small nullable-read helpers so every optional field above reads the same way whether the
// key is entirely absent (an old backup) or present-but-JSON-null (a new backup, field unset). ----
private fun JSONObject.optStringOrNull(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null
private fun JSONObject.optIntOrNull(key: String): Int? = if (has(key) && !isNull(key)) getInt(key) else null
private fun JSONObject.optLongOrNull(key: String): Long? = if (has(key) && !isNull(key)) getLong(key) else null
private fun JSONObject.optDoubleOrNull(key: String): Double? = if (has(key) && !isNull(key)) getDouble(key) else null
private fun JSONObject.optBooleanOrNull(key: String): Boolean? = if (has(key) && !isNull(key)) getBoolean(key) else null
private fun <T> JSONObject.optEnumOrNull(key: String, valueOf: (String) -> T): T? =
    if (has(key) && !isNull(key)) valueOf(getString(key)) else null

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
