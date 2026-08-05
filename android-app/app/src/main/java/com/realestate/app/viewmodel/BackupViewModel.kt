package com.realestate.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.backup.BackupManager
import com.realestate.app.data.backup.BackupPreview
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

/** What the restore confirmation dialog is showing, keyed to the file the user just picked. */
sealed class RestorePreviewState {
    object Idle : RestorePreviewState()
    object Loading : RestorePreviewState()
    data class Ready(val uri: Uri, val preview: BackupPreview) : RestorePreviewState()
    data class Failed(val message: String) : RestorePreviewState()
}

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val backupManager = BackupManager(application)
    private val historyRepository = BackupHistoryRepository(application)

    val history: StateFlow<List<BackupHistoryEntry>> = historyRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _status = MutableStateFlow<BackupUiStatus>(BackupUiStatus.Idle)
    val status: StateFlow<BackupUiStatus> = _status

    private val _restorePreview = MutableStateFlow<RestorePreviewState>(RestorePreviewState.Idle)
    val restorePreview: StateFlow<RestorePreviewState> = _restorePreview

    fun backup(uri: Uri) = viewModelScope.launch {
        _status.value = BackupUiStatus.Working
        runCatching {
            val result = backupManager.createBackup(uri)
            historyRepository.addEntry(
                BackupHistoryEntry(
                    timestamp = System.currentTimeMillis(),
                    propertyCount = result.propertyCount,
                    sizeBytes = result.sizeBytes,
                    noteCount = result.noteCount,
                    transactionCount = result.transactionCount
                )
            )
            result
        }.onSuccess { result ->
            _status.value = BackupUiStatus.Success("پشتیبان‌گیری با موفقیت انجام شد (${result.propertyCount} ملک)")
        }.onFailure { e ->
            _status.value = BackupUiStatus.Failure(e.message ?: "خطا در پشتیبان‌گیری")
        }
    }

    /** Step 1 of restore: read the file and show what it contains, before touching the database. */
    fun peekRestore(uri: Uri) = viewModelScope.launch {
        _restorePreview.value = RestorePreviewState.Loading
        runCatching { backupManager.peekBackup(uri) }
            .onSuccess { preview -> _restorePreview.value = RestorePreviewState.Ready(uri, preview) }
            .onFailure { e -> _restorePreview.value = RestorePreviewState.Failed(e.message ?: "فایل پشتیبان قابل خواندن نیست") }
    }

    fun dismissRestorePreview() {
        _restorePreview.value = RestorePreviewState.Idle
    }

    /** Step 2: the user has seen the preview and confirmed — now actually replace the database. */
    fun confirmRestore(uri: Uri) = viewModelScope.launch {
        _restorePreview.value = RestorePreviewState.Idle
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
