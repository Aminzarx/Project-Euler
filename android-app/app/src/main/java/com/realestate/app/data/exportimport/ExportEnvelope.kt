package com.realestate.app.data.exportimport

import com.realestate.app.data.Property
import com.realestate.app.data.toJson
import com.realestate.app.data.toProperty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Bump only when the *envelope shape itself* changes (a top-level key added/removed/renamed) —
 *  not for every new Property field, which is already handled inside Property's own JSON mapping
 *  the same way the full-database backup handles it (has()/isNull() guards, see PropertyJson.kt). */
const val EXPORT_SCHEMA_VERSION = 1

/** The newest schema version this build of the app knows how to read. A file from a future app
 *  version that bumped [EXPORT_SCHEMA_VERSION] is refused with a clear message instead of being
 *  guessed at — "unsupported version" is a far better failure than silently misreading a shape
 *  this code was never taught. */
private const val MAX_SUPPORTED_SCHEMA_VERSION = EXPORT_SCHEMA_VERSION

/** A standalone export bundle of specific Cases (as opposed to the full-database
 *  data/backup/BackupManager.kt, which always covers every table). Every field except
 *  [schemaVersion] and [properties] is informational only — surfaced in the import preview so the
 *  receiving agent can see when, from what, and (optionally) whom the file came from, never used
 *  to gate whether import is allowed. [deviceModel]/[androidVersion]/[exporterName] are read with
 *  empty-string defaults (see [parseExportEnvelope]) so a bundle made before these fields existed
 *  still imports fine — nothing about this is a breaking schema change.
 *
 *  Deliberately *not* included: anything about the device or agent that isn't already visible
 *  elsewhere in the app (no device ID, no phone number, no location) — this metadata answers
 *  "what/when/who made this", not "identify this device". */
data class ExportEnvelope(
    val schemaVersion: Int,
    val exportedAt: Long,
    val appVersion: String,
    val deviceModel: String = "",
    val androidVersion: String = "",
    val exporterName: String = "",
    val properties: List<Property>
)

/** Every way parsing/validating an incoming file can fail — always one of these, never a raw
 *  JSONException or ArrayIndexOutOfBounds escaping to the UI as a crash. */
sealed class ExportImportError(message: String) : Exception(message) {
    class NotValidFile(message: String = "این فایل یک بسته معتبر پرونده نیست") : ExportImportError(message)
    class UnsupportedSchemaVersion(val fileVersion: Int) :
        ExportImportError("این فایل با نسخه‌ی جدیدتری از اپ ساخته شده و با این نسخه سازگار نیست")
    class MissingField(val field: String) : ExportImportError("فایل ناقص است — بخش «$field» یافت نشد")
    class NoRecords : ExportImportError("این بسته هیچ پرونده‌ای ندارد")
}

fun ExportEnvelope.toJson(): JSONObject = JSONObject().apply {
    put("schemaVersion", schemaVersion)
    put("exportedAt", exportedAt)
    put("appVersion", appVersion)
    put("deviceModel", deviceModel)
    put("androidVersion", androidVersion)
    put("exporterName", exporterName)
    put(
        "data",
        JSONObject().apply {
            put("properties", JSONArray(properties.map { it.toJson() }))
        }
    )
}

/** Parses and fully validates an export bundle's decrypted plaintext bytes. Never throws anything
 *  other than [ExportImportError] — every malformed-input path (not JSON, wrong shape, missing
 *  fields, an unreadable individual record, an empty bundle) is caught and translated here so the
 *  UI layer only ever has one exception type to show a message for. */
fun parseExportEnvelope(plaintext: ByteArray): ExportEnvelope {
    val text = plaintext.toString(Charsets.UTF_8)
    val json = try {
        JSONObject(text)
    } catch (e: JSONException) {
        throw ExportImportError.NotValidFile()
    }

    if (!json.has("schemaVersion")) throw ExportImportError.MissingField("schemaVersion")
    val schemaVersion = json.optInt("schemaVersion", -1)
    if (schemaVersion < 1) throw ExportImportError.NotValidFile()
    if (schemaVersion > MAX_SUPPORTED_SCHEMA_VERSION) throw ExportImportError.UnsupportedSchemaVersion(schemaVersion)

    if (!json.has("data")) throw ExportImportError.MissingField("data")
    val data = try {
        json.getJSONObject("data")
    } catch (e: JSONException) {
        throw ExportImportError.NotValidFile()
    }

    if (!data.has("properties")) throw ExportImportError.MissingField("properties")
    val propertiesArray = try {
        data.getJSONArray("properties")
    } catch (e: JSONException) {
        throw ExportImportError.NotValidFile()
    }

    val properties = (0 until propertiesArray.length()).map { index ->
        try {
            propertiesArray.getJSONObject(index).toProperty()
        } catch (e: Exception) {
            throw ExportImportError.NotValidFile("رکورد شماره ${index + 1} در فایل قابل خواندن نیست")
        }
    }
    if (properties.isEmpty()) throw ExportImportError.NoRecords()

    return ExportEnvelope(
        schemaVersion = schemaVersion,
        exportedAt = json.optLong("exportedAt", 0L),
        appVersion = json.optString("appVersion", ""),
        deviceModel = json.optString("deviceModel", ""),
        androidVersion = json.optString("androidVersion", ""),
        exporterName = json.optString("exporterName", ""),
        properties = properties
    )
}
