package com.realestate.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

enum class ThemePreference { SYSTEM, LIGHT, DARK }

private object Keys {
    val MOBILE_NUMBER = stringPreferencesKey("mobile_number")
    val DEVICE_ID = stringPreferencesKey("device_id")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    val FULL_NAME = stringPreferencesKey("full_name")
    val AGENCY_NAME = stringPreferencesKey("agency_name")
    val AGENCY_LOGO_URI = stringPreferencesKey("agency_logo_uri")
    val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
    val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
    val BIOGRAPHY = stringPreferencesKey("biography")
    val INSTAGRAM = stringPreferencesKey("instagram")
    val TELEGRAM = stringPreferencesKey("telegram")
    val WEBSITE = stringPreferencesKey("website")
    val SLOGAN = stringPreferencesKey("slogan")
    val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    val BACKUP_HISTORY = stringPreferencesKey("backup_history")
    val RECENT_DEAL_TOOLS = stringPreferencesKey("recent_deal_tools")
}

/**
 * Local session store. Persists which mobile number is signed in on this device.
 * Real cross-device single-session enforcement (rejecting a second device for the
 * same number) requires a backend service to arbitrate between devices; that part
 * is intentionally left as a seam ([DeviceSessionValidator]) rather than faked here.
 */
class SessionManager(private val context: Context) {
    val isLoggedIn: Flow<Boolean> = context.appDataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val mobileNumber: Flow<String?> = context.appDataStore.data.map { it[Keys.MOBILE_NUMBER] }

    suspend fun getOrCreateDeviceId(): String {
        val existing = context.appDataStore.data.first()[Keys.DEVICE_ID]
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        context.appDataStore.edit { it[Keys.DEVICE_ID] = newId }
        return newId
    }

    suspend fun login(mobile: String, deviceId: String) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.MOBILE_NUMBER] = mobile
            prefs[Keys.DEVICE_ID] = deviceId
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.appDataStore.edit { prefs -> prefs[Keys.IS_LOGGED_IN] = false }
    }
}

/** Seam for real, server-backed single-device enforcement. Always allows locally for now. */
interface DeviceSessionValidator {
    suspend fun canActivate(mobile: String, deviceId: String): Boolean
}

class LocalDeviceSessionValidator : DeviceSessionValidator {
    override suspend fun canActivate(mobile: String, deviceId: String): Boolean = true
}

data class ProfileData(
    val fullName: String = "",
    val agencyName: String = "",
    val agencyLogoUri: String? = null,
    val profilePhotoUri: String? = null,
    val businessAddress: String = "",
    val biography: String = "",
    val instagram: String = "",
    val telegram: String = "",
    val website: String = "",
    val slogan: String = "",
    val mobileNumber: String = "",
    val themePreference: ThemePreference = ThemePreference.SYSTEM
)

/** Which optional branding fields are still empty, in the order it's most useful to fill them in. */
private val COMPLETION_FIELDS: List<Pair<String, (ProfileData) -> Boolean>> = listOf(
    "نام و نام‌خانوادگی" to { p: ProfileData -> p.fullName.isNotBlank() },
    "نام آژانس" to { p: ProfileData -> p.agencyName.isNotBlank() },
    "عکس پروفایل" to { p: ProfileData -> p.profilePhotoUri != null },
    "لوگوی آژانس" to { p: ProfileData -> p.agencyLogoUri != null },
    "آدرس محل کسب‌وکار" to { p: ProfileData -> p.businessAddress.isNotBlank() },
    "بیوگرافی" to { p: ProfileData -> p.biography.isNotBlank() },
    "شعار کسب‌وکار" to { p: ProfileData -> p.slogan.isNotBlank() },
    "شبکه اجتماعی یا وب‌سایت" to { p: ProfileData ->
        p.instagram.isNotBlank() || p.telegram.isNotBlank() || p.website.isNotBlank()
    }
)

/** 0-100. Purely a function of which optional profile fields the agent has filled in — no hidden scoring. */
fun ProfileData.completionPercent(): Int {
    val filled = COMPLETION_FIELDS.count { (_, isFilled) -> isFilled(this) }
    return (filled * 100) / COMPLETION_FIELDS.size
}

/** Up to 3 concrete, actionable suggestions for what to fill in next. */
fun ProfileData.missingFieldSuggestions(limit: Int = 3): List<String> =
    COMPLETION_FIELDS.filterNot { (_, isFilled) -> isFilled(this) }.map { (label, _) -> label }.take(limit)

class ProfileRepository(private val context: Context) {
    val profile: Flow<ProfileData> = context.appDataStore.data.map { prefs ->
        ProfileData(
            fullName = prefs[Keys.FULL_NAME] ?: "",
            agencyName = prefs[Keys.AGENCY_NAME] ?: "",
            agencyLogoUri = prefs[Keys.AGENCY_LOGO_URI]?.takeIf { it.isNotBlank() },
            profilePhotoUri = prefs[Keys.PROFILE_PHOTO_URI]?.takeIf { it.isNotBlank() },
            businessAddress = prefs[Keys.BUSINESS_ADDRESS] ?: "",
            biography = prefs[Keys.BIOGRAPHY] ?: "",
            instagram = prefs[Keys.INSTAGRAM] ?: "",
            telegram = prefs[Keys.TELEGRAM] ?: "",
            website = prefs[Keys.WEBSITE] ?: "",
            slogan = prefs[Keys.SLOGAN] ?: "",
            mobileNumber = prefs[Keys.MOBILE_NUMBER] ?: "",
            themePreference = ThemePreference.entries.find { it.name == prefs[Keys.THEME_PREFERENCE] }
                ?: ThemePreference.SYSTEM
        )
    }

    suspend fun updateProfile(update: (ProfileData) -> ProfileData) {
        val updated = update(profile.first())
        context.appDataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = updated.fullName
            prefs[Keys.AGENCY_NAME] = updated.agencyName
            prefs[Keys.AGENCY_LOGO_URI] = updated.agencyLogoUri ?: ""
            prefs[Keys.PROFILE_PHOTO_URI] = updated.profilePhotoUri ?: ""
            prefs[Keys.BUSINESS_ADDRESS] = updated.businessAddress
            prefs[Keys.BIOGRAPHY] = updated.biography
            prefs[Keys.INSTAGRAM] = updated.instagram
            prefs[Keys.TELEGRAM] = updated.telegram
            prefs[Keys.WEBSITE] = updated.website
            prefs[Keys.SLOGAN] = updated.slogan
        }
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.appDataStore.edit { prefs -> prefs[Keys.THEME_PREFERENCE] = preference.name }
    }
}

class SearchHistoryRepository(private val context: Context) {
    val recentSearches: Flow<List<String>> = context.appDataStore.data.map { prefs ->
        prefs[Keys.RECENT_SEARCHES]?.split(SEARCH_DELIMITER)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val current = recentSearches.first()
        val updated = (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }).take(5)
        context.appDataStore.edit { prefs -> prefs[Keys.RECENT_SEARCHES] = updated.joinToString(SEARCH_DELIMITER) }
    }

    suspend fun clear() {
        context.appDataStore.edit { prefs -> prefs[Keys.RECENT_SEARCHES] = "" }
    }

    private companion object {
        const val SEARCH_DELIMITER = "|||"
    }
}

/** Tracks which Smart Deal Assistant tools were opened most recently, most-recent first. */
class RecentToolsRepository(private val context: Context) {
    val recentToolKeys: Flow<List<String>> = context.appDataStore.data.map { prefs ->
        prefs[Keys.RECENT_DEAL_TOOLS]?.split(TOOL_DELIMITER)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun recordUsage(toolKey: String) {
        val current = recentToolKeys.first()
        val updated = (listOf(toolKey) + current.filterNot { it == toolKey }).take(6)
        context.appDataStore.edit { prefs -> prefs[Keys.RECENT_DEAL_TOOLS] = updated.joinToString(TOOL_DELIMITER) }
    }

    private companion object {
        const val TOOL_DELIMITER = "|||"
    }
}

data class BackupHistoryEntry(
    val timestamp: Long,
    val propertyCount: Int,
    val sizeBytes: Long,
    val noteCount: Int = 0,
    val transactionCount: Int = 0
)

class BackupHistoryRepository(private val context: Context) {
    val history: Flow<List<BackupHistoryEntry>> = context.appDataStore.data.map { prefs ->
        val raw = prefs[Keys.BACKUP_HISTORY]
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching {
                val array = org.json.JSONArray(raw)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    BackupHistoryEntry(
                        timestamp = obj.getLong("timestamp"),
                        propertyCount = obj.getInt("propertyCount"),
                        sizeBytes = obj.getLong("sizeBytes"),
                        noteCount = obj.optInt("noteCount", 0),
                        transactionCount = obj.optInt("transactionCount", 0)
                    )
                }
            }.getOrDefault(emptyList())
        }
    }

    suspend fun addEntry(entry: BackupHistoryEntry) {
        val current = history.first()
        val updated = (listOf(entry) + current).take(5)
        val array = org.json.JSONArray()
        updated.forEach { e ->
            array.put(
                org.json.JSONObject().apply {
                    put("timestamp", e.timestamp)
                    put("propertyCount", e.propertyCount)
                    put("sizeBytes", e.sizeBytes)
                    put("noteCount", e.noteCount)
                    put("transactionCount", e.transactionCount)
                }
            )
        }
        context.appDataStore.edit { prefs -> prefs[Keys.BACKUP_HISTORY] = array.toString() }
    }
}
