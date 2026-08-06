package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.BuildConfig
import com.realestate.app.data.update.LatestRelease
import com.realestate.app.data.update.parseLatestRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object UpToDate : UpdateCheckState()
    data class UpdateAvailable(val release: LatestRelease) : UpdateCheckState()
}

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/Aminzarx/Project-Euler/releases/latest"

/**
 * Checks, once per app launch, whether the rolling GitHub Release CI publishes on every push
 * (tag `latest-debug-build`) carries a newer build than the one currently installed. There's no
 * Play Store distribution here, so this is the update channel — see ReleaseCheck.kt for why the
 * comparison is against a `versionCode=` marker embedded in the release body rather than the
 * release's tag (which stays fixed so the download link never changes).
 *
 * A failed check (no network, GitHub unreachable, unexpected response shape) settles into
 * [UpdateCheckState.UpToDate] rather than an error state — this is a best-effort background
 * courtesy, not a feature the user should ever see fail loudly.
 */
class UpdateCheckViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val state: StateFlow<UpdateCheckState> = _state

    private var checkedThisSession = false

    fun checkForUpdateOnce() {
        if (checkedThisSession) return
        checkedThisSession = true
        viewModelScope.launch {
            val release = withContext(Dispatchers.IO) {
                runCatching { parseLatestRelease(fetchLatestReleaseJson()) }.getOrNull()
            }
            _state.value = if (release != null && release.versionCode > BuildConfig.VERSION_CODE) {
                UpdateCheckState.UpdateAvailable(release)
            } else {
                UpdateCheckState.UpToDate
            }
        }
    }

    fun dismiss() {
        _state.value = UpdateCheckState.UpToDate
    }

    private fun fetchLatestReleaseJson(): String {
        val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return ""
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
