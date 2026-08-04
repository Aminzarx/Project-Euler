package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.datastore.ProfileData
import com.realestate.app.data.datastore.ProfileRepository
import com.realestate.app.data.datastore.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(application)

    val profile: StateFlow<ProfileData> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileData())

    fun updateProfile(update: (ProfileData) -> ProfileData) = viewModelScope.launch {
        repository.updateProfile(update)
    }

    fun setThemePreference(preference: ThemePreference) = viewModelScope.launch {
        repository.setThemePreference(preference)
    }
}
