package com.realestate.app.data.update

import org.json.JSONObject

/** What the update dialog needs: the build being offered, and where to download it. */
data class LatestRelease(
    val versionCode: Int,
    val downloadUrl: String,
    val releaseName: String
)

private val VERSION_CODE_REGEX = Regex("""versionCode\s*=\s*(\d+)""")

/**
 * Parses the JSON body of GitHub's `GET /repos/{owner}/{repo}/releases/latest` response — pure
 * (no networking) so it's unit-testable on plain JVM, same reasoning as FileCrypto/ExportEnvelope.
 * The CI workflow embeds a `versionCode=<n>` marker line in the release body specifically for this
 * to find, since [Property versionCode][com.realestate.app.BuildConfig.VERSION_CODE] — not the
 * release's tag name, which stays a fixed "latest-debug-build" so the download link never changes
 * — is what the app can actually compare itself against. Never throws: a response shaped
 * differently than what CI publishes (missing the marker, no APK asset, malformed JSON) is treated
 * as "nothing usable found" rather than a crash — an update check failing silently is the right
 * default for a background, best-effort feature.
 */
fun parseLatestRelease(json: String): LatestRelease? = try {
    val obj = JSONObject(json)
    val body = obj.optString("body", "")
    val versionCode = VERSION_CODE_REGEX.find(body)?.groupValues?.get(1)?.toIntOrNull()
    val assets = obj.optJSONArray("assets")
    var downloadUrl: String? = null
    if (assets != null) {
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            if (asset.optString("name").endsWith(".apk")) {
                downloadUrl = asset.optString("browser_download_url").ifBlank { null }
                break
            }
        }
    }
    if (versionCode == null || downloadUrl == null) {
        null
    } else {
        LatestRelease(versionCode, downloadUrl, obj.optString("name", ""))
    }
} catch (e: Exception) {
    null
}
