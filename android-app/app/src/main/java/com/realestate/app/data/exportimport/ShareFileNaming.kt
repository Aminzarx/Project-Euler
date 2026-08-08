package com.realestate.app.data.exportimport

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pure (no Context) so it's unit-testable — strips filesystem-reserved characters and collapses
 *  whitespace, but deliberately keeps non-Latin letters: this app is Persian-first, and an agency
 *  name is very often Persian text, so an ASCII-only sanitizer would silently fall back to the
 *  generic name for most real users. */
private fun sanitizeForFileName(raw: String): String {
    val cleaned = raw.trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "")
        .replace(Regex("""\s+"""), "_")
        .take(40)
    return cleaned.ifBlank { "Properties" }
}

/** "OfficeName_2026-08-06.enc" when an exporter name is set, "Properties_2026-08-06.enc"
 *  otherwise. [now] is a parameter (not read internally) purely so this stays deterministic and
 *  testable — the caller always passes the real current time. */
fun buildShareFileName(exporterName: String, now: Long): String {
    val datePart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
    val prefix = sanitizeForFileName(exporterName)
    return "${prefix}_$datePart.enc"
}
