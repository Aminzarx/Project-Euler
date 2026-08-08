package com.realestate.app.data.exportimport

import com.realestate.app.data.toJson
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportEnvelopeTest {

    @Test
    fun `round trip through JSON preserves everything, including metadata`() {
        val original = ExportEnvelope(
            schemaVersion = EXPORT_SCHEMA_VERSION,
            exportedAt = 1_700_000_000_000L,
            appVersion = "1.0",
            deviceModel = "Pixel 8",
            androidVersion = "14",
            exporterName = "املاک آریا",
            properties = listOf(testProperty(title = "ملک الف"), testProperty(title = "ملک ب"))
        )

        val parsed = parseExportEnvelope(original.toJson().toString().toByteArray(Charsets.UTF_8))

        assertEquals(original.schemaVersion, parsed.schemaVersion)
        assertEquals(original.exportedAt, parsed.exportedAt)
        assertEquals(original.appVersion, parsed.appVersion)
        assertEquals(original.deviceModel, parsed.deviceModel)
        assertEquals(original.androidVersion, parsed.androidVersion)
        assertEquals(original.exporterName, parsed.exporterName)
        assertEquals(original.properties.map { it.uid }.toSet(), parsed.properties.map { it.uid }.toSet())
        assertEquals(original.properties.map { it.title }.toSet(), parsed.properties.map { it.title }.toSet())
    }

    @Test
    fun `a bundle from before metadata existed still imports, with empty metadata defaults`() {
        val legacyBundle = JSONObject().apply {
            put("schemaVersion", EXPORT_SCHEMA_VERSION)
            put("exportedAt", 1_700_000_000_000L)
            put("appVersion", "0.9")
            // no deviceModel/androidVersion/exporterName keys at all — this is exactly the shape
            // every bundle produced before this round had.
            put(
                "data",
                JSONObject().apply {
                    put("properties", org.json.JSONArray().apply { put(testProperty().toJson()) })
                }
            )
        }

        val parsed = parseExportEnvelope(legacyBundle.toString().toByteArray(Charsets.UTF_8))

        assertEquals("", parsed.deviceModel)
        assertEquals("", parsed.androidVersion)
        assertEquals("", parsed.exporterName)
        assertEquals(1, parsed.properties.size)
    }

    @Test
    fun `garbage bytes are rejected as not a valid file, not a crash`() {
        val garbage = "این یک فایل json نیست {{{".toByteArray(Charsets.UTF_8)

        assertThrows(ExportImportError.NotValidFile::class.java) {
            parseExportEnvelope(garbage)
        }
    }

    @Test
    fun `a schema version newer than this app supports is rejected by name`() {
        val futureEnvelope = JSONObject().apply {
            put("schemaVersion", EXPORT_SCHEMA_VERSION + 1)
            put("exportedAt", 0L)
            put("appVersion", "99.0")
            put("data", JSONObject().apply { put("properties", org.json.JSONArray()) })
        }

        val error = assertThrows(ExportImportError.UnsupportedSchemaVersion::class.java) {
            parseExportEnvelope(futureEnvelope.toString().toByteArray(Charsets.UTF_8))
        }
        assertEquals(EXPORT_SCHEMA_VERSION + 1, error.fileVersion)
    }

    @Test
    fun `a missing data section is reported as a missing field, not a crash`() {
        val incomplete = JSONObject().apply {
            put("schemaVersion", EXPORT_SCHEMA_VERSION)
            put("exportedAt", 0L)
            put("appVersion", "1.0")
        }

        assertThrows(ExportImportError.MissingField::class.java) {
            parseExportEnvelope(incomplete.toString().toByteArray(Charsets.UTF_8))
        }
    }

    @Test
    fun `an empty properties array is rejected as nothing to import`() {
        val empty = JSONObject().apply {
            put("schemaVersion", EXPORT_SCHEMA_VERSION)
            put("exportedAt", 0L)
            put("appVersion", "1.0")
            put("data", JSONObject().apply { put("properties", org.json.JSONArray()) })
        }

        assertThrows(ExportImportError.NoRecords::class.java) {
            parseExportEnvelope(empty.toString().toByteArray(Charsets.UTF_8))
        }
    }

    @Test
    fun `one corrupt record among valid ones fails the whole file with a clear message`() {
        val mixed = JSONObject().apply {
            put("schemaVersion", EXPORT_SCHEMA_VERSION)
            put("exportedAt", 0L)
            put("appVersion", "1.0")
            put(
                "data",
                JSONObject().apply {
                    put(
                        "properties",
                        org.json.JSONArray().apply {
                            put(testProperty().toJson())
                            put(JSONObject().apply { put("title", "بدون فیلدهای اجباری") })
                        }
                    )
                }
            )
        }

        val error = assertThrows(ExportImportError.NotValidFile::class.java) {
            parseExportEnvelope(mixed.toString().toByteArray(Charsets.UTF_8))
        }
        assertTrue(error.message!!.isNotBlank())
    }
}
