package com.realestate.app.data.backup

import android.content.Context
import android.net.Uri
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.property.Note
import com.realestate.app.data.property.TimelineEvent
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.data.wallet.TransactionStatus
import com.realestate.app.data.wallet.TransactionType
import com.realestate.app.data.wallet.WalletTransaction
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class BackupResult(val propertyCount: Int, val sizeBytes: Long)

/** Offline JSON backup/restore for the whole local database. No network involved. */
class BackupManager(private val context: Context) {
    private val db = AppDatabase.getInstance(context)

    suspend fun createBackup(uri: Uri): BackupResult {
        val properties = db.propertyDao().getAllProperties().first()
        val notes = db.noteDao().getAllNotes().first()
        val events = db.timelineDao().getAllEvents().first()
        val transactions = db.walletDao().getAllTransactions().first()

        val json = JSONObject().apply {
            put("version", 1)
            put("properties", JSONArray(properties.map { it.toJson() }))
            put("notes", JSONArray(notes.map { it.toJson() }))
            put("timelineEvents", JSONArray(events.map { it.toJson() }))
            put("walletTransactions", JSONArray(transactions.map { it.toJson() }))
        }
        val bytes = json.toString(2).toByteArray(Charsets.UTF_8)
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("امکان نوشتن فایل پشتیبان وجود ندارد")
        return BackupResult(properties.size, bytes.size.toLong())
    }

    suspend fun restoreBackup(uri: Uri): BackupResult {
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("امکان خواندن فایل پشتیبان وجود ندارد")

        val json = JSONObject(text)
        val properties = json.getJSONArray("properties").toObjectList { it.toProperty() }
        val notes = json.getJSONArray("notes").toObjectList { it.toNote() }
        val events = json.getJSONArray("timelineEvents").toObjectList { it.toTimelineEvent() }
        val transactions = json.getJSONArray("walletTransactions").toObjectList { it.toWalletTransaction() }

        db.propertyDao().deleteAll()
        db.noteDao().deleteAll()
        db.timelineDao().deleteAll()
        db.walletDao().deleteAll()

        properties.forEach { db.propertyDao().insert(it) }
        notes.forEach { db.noteDao().insert(it) }
        events.forEach { db.timelineDao().insert(it) }
        transactions.forEach { db.walletDao().insert(it) }

        return BackupResult(properties.size, text.toByteArray(Charsets.UTF_8).size.toLong())
    }
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
    lastSharedAt = if (isNull("lastSharedAt")) null else getLong("lastSharedAt")
)

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
