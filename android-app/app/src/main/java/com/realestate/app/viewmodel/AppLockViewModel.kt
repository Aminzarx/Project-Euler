package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.datastore.SecurityPreferencesRepository
import com.realestate.app.data.datastore.SecuritySettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppLockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SecurityPreferencesRepository(application)

    val settings: StateFlow<SecuritySettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecuritySettings())

    // Deliberately in-memory only (not persisted): a lock's whole point is that it re-locks on
    // every fresh process start, and again whenever the app is backgrounded (see RootApp's
    // lifecycle observer) — persisting "unlocked" would defeat both.
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    fun setPin(pin: String) = viewModelScope.launch {
        repository.setPin(pin)
        _isUnlocked.value = true
    }

    fun clearPin() = viewModelScope.launch {
        repository.clearPin()
        _isUnlocked.value = true
    }

    fun verifyPin(pin: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = repository.verifyPin(pin)
        if (ok) _isUnlocked.value = true
        onResult(ok)
    }

    fun unlockWithBiometric() {
        _isUnlocked.value = true
    }

    fun lock() {
        if (settings.value.hasPin) _isUnlocked.value = false
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    fun setScreenSecurityEnabled(enabled: Boolean) = viewModelScope.launch { repository.setScreenSecurityEnabled(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    fun setCalendarJalali(enabled: Boolean) = viewModelScope.launch { repository.setCalendarJalali(enabled) }

    /** At most one "پیگیری امروز" notification per calendar day, keyed by a yyyy-MM-dd string. */
    suspend fun shouldNotifyFollowUpsToday(todayKey: String): Boolean =
        repository.getLastFollowUpNotifyDate() != todayKey

    fun markFollowUpsNotified(todayKey: String) = viewModelScope.launch {
        repository.setLastFollowUpNotifyDate(todayKey)
    }
}
