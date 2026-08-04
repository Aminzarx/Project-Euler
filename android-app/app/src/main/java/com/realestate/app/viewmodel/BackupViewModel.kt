package com.realestate.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.backup.BackupManager
import com.realestate.app.data.datastore.BackupHistoryEntry
import com.realestate.app.data.datastore.BackupHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BackupUiStatus {
    object Idle : BackupUiStatus()
    object Working : BackupUiStatus()
    data class Success(val message: String) : BackupUiStatus()
    data class Failure(val message: String) : BackupUiStatus()
}

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val backupManager = BackupManager(application)
    private val historyRepository = BackupHistoryRepository(application)

    val history: StateFlow<List<BackupHistoryEntry>> = historyRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _status = MutableStateFlow<BackupUiStatus>(BackupUiStatus.Idle)
    val status: StateFlow<BackupUiStatus> = _status

    fun backup(uri: Uri) = viewModelScope.launch {
        _status.value = BackupUiStatus.Working
        runCatching {
            val result = backupManager.createBackup(uri)
            historyRepository.addEntry(
                BackupHistoryEntry(System.currentTimeMillis(), result.propertyCount, result.sizeBytes)
            )
            result
        }.onSuccess { result ->
            _status.value = BackupUiStatus.Success("پشتیبان‌گیری با موفقیت انجام شد (${result.propertyCount} ملک)")
        }.onFailure { e ->
            _status.value = BackupUiStatus.Failure(e.message ?: "خطا در پشتیبان‌گیری")
        }
    }

    fun restore(uri: Uri) = viewModelScope.launch {
        _status.value = BackupUiStatus.Working
        runCatching { backupManager.restoreBackup(uri) }
            .onSuccess { result ->
                _status.value = BackupUiStatus.Success("بازیابی با موفقیت انجام شد (${result.propertyCount} ملک)")
            }
            .onFailure { e ->
                _status.value = BackupUiStatus.Failure(e.message ?: "خطا در بازیابی")
            }
    }

    fun resetStatus() {
        _status.value = BackupUiStatus.Idle
    }
}
