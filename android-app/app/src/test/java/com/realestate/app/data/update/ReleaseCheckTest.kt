package com.realestate.app.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseCheckTest {

    private fun releaseJson(body: String, assetName: String = "app-debug.apk", downloadUrl: String = "https://example.com/app-debug.apk"): String = """
        {
          "name": "Latest debug build (#52)",
          "body": "$body",
          "assets": [
            { "name": "$assetName", "browser_download_url": "$downloadUrl" }
          ]
        }
    """.trimIndent()

    @Test
    fun `a well-formed release with a versionCode marker and an apk asset parses`() {
        val release = parseLatestRelease(releaseJson("Automatically updated on every push.\\nversionCode=52"))

        assertEquals(52, release?.versionCode)
        assertEquals("https://example.com/app-debug.apk", release?.downloadUrl)
        assertEquals("Latest debug build (#52)", release?.releaseName)
    }

    @Test
    fun `a body without the versionCode marker parses to nothing usable`() {
        val release = parseLatestRelease(releaseJson("Just a commit sha, no marker."))

        assertNull(release)
    }

    @Test
    fun `a release with no apk among its assets parses to nothing usable`() {
        val release = parseLatestRelease(releaseJson("versionCode=52", assetName = "notes.txt"))

        assertNull(release)
    }

    @Test
    fun `garbage that is not JSON at all does not throw`() {
        val release = parseLatestRelease("not json { at all")

        assertNull(release)
    }

    @Test
    fun `an empty response does not throw`() {
        val release = parseLatestRelease("")

        assertNull(release)
    }
}
