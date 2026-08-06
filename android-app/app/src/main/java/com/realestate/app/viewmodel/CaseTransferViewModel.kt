package com.realestate.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.BuildConfig
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyRepository
import com.realestate.app.data.crypto.FileCrypto
import com.realestate.app.data.exportimport.CaseBundleFileService
import com.realestate.app.data.exportimport.EXPORT_SCHEMA_VERSION
import com.realestate.app.data.exportimport.ExportEnvelope
import com.realestate.app.data.exportimport.ExportImportError
import com.realestate.app.data.exportimport.ImportPlan
import com.realestate.app.data.exportimport.parseExportEnvelope
import com.realestate.app.data.exportimport.planImport
import com.realestate.app.data.exportimport.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ShareBundleState {
    object Idle : ShareBundleState()
    object Working : ShareBundleState()
    data class Ready(val uri: Uri) : ShareBundleState()
    data class Failed(val message: String) : ShareBundleState()
}

/** What the import preview dialog shows, keyed to the file the agent just picked and decrypted. */
sealed class ImportPreviewState {
    object Idle : ImportPreviewState()
    object Loading : ImportPreviewState()
    data class Ready(val plan: ImportPlan, val exportedAt: Long, val appVersion: String) : ImportPreviewState()
    data class Failed(val message: String) : ImportPreviewState()
}

sealed class ImportApplyState {
    object Idle : ImportApplyState()
    object Working : ImportApplyState()
    data class Success(val insertedCount: Int, val updatedCount: Int) : ImportApplyState()
    data class Failed(val message: String) : ImportApplyState()
}

/**
 * Owns the "share selected cases as an encrypted bundle" and "import an encrypted bundle" flows —
 * kept separate from PropertyViewModel (which already owns selection state) the same way
 * BackupViewModel is kept separate from it for the full-database backup, so each ViewModel's state
 * machine stays about one thing.
 *
 * Mirrors BackupViewModel's preview-then-confirm shape for import: [peekImport] only decrypts and
 * plans, touching nothing in the database, so the agent always sees real counts before anything
 * is written.
 */
class CaseTransferViewModel(application: Application) : AndroidViewModel(application) {
    private val fileService = CaseBundleFileService(application)
    private val repository = PropertyRepository(AppDatabase.getInstance(application).propertyDao())

    private val _shareState = MutableStateFlow<ShareBundleState>(ShareBundleState.Idle)
    val shareState: StateFlow<ShareBundleState> = _shareState

    private val _importPreview = MutableStateFlow<ImportPreviewState>(ImportPreviewState.Idle)
    val importPreview: StateFlow<ImportPreviewState> = _importPreview

    private val _importApply = MutableStateFlow<ImportApplyState>(ImportApplyState.Idle)
    val importApply: StateFlow<ImportApplyState> = _importApply

    // Held only between a successful peekImport and the agent's confirm/cancel — never persisted,
    // never written anywhere but into the database itself on confirm.
    private var pendingPlan: ImportPlan? = null

    fun shareSelected(properties: List<Property>, password: CharArray) = viewModelScope.launch {
        _shareState.value = ShareBundleState.Working
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val envelope = ExportEnvelope(
                    schemaVersion = EXPORT_SCHEMA_VERSION,
                    exportedAt = System.currentTimeMillis(),
                    appVersion = BuildConfig.VERSION_NAME,
                    properties = properties
                )
                val plaintext = envelope.toJson().toString().toByteArray(Charsets.UTF_8)
                val encrypted = FileCrypto.encrypt(plaintext, password)
                fileService.writeShareFile(encrypted, "cases-${System.currentTimeMillis()}.recx")
            }
        }
        result.onSuccess { uri -> _shareState.value = ShareBundleState.Ready(uri) }
            .onFailure { e -> _shareState.value = ShareBundleState.Failed(e.message ?: "خطا در آماده‌سازی فایل") }
    }

    fun resetShareState() {
        _shareState.value = ShareBundleState.Idle
    }

    /** Step 1 of import: read, decrypt, validate, and plan — nothing here touches the database. */
    fun peekImport(uri: Uri, password: CharArray) = viewModelScope.launch {
        _importPreview.value = ImportPreviewState.Loading
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val encrypted = fileService.readFile(uri)
                val plaintext = FileCrypto.decrypt(encrypted, password)
                val envelope = parseExportEnvelope(plaintext)
                val existing = repository.allProperties.first()
                val plan = planImport(existing, envelope.properties)
                Triple(plan, envelope.exportedAt, envelope.appVersion)
            }
        }
        result.onSuccess { (plan, exportedAt, appVersion) ->
            pendingPlan = plan
            _importPreview.value = ImportPreviewState.Ready(plan, exportedAt, appVersion)
        }.onFailure { e ->
            val message = when (e) {
                is FileCrypto.DecryptionException -> e.message
                is ExportImportError -> e.message
                else -> null
            } ?: "خطا در خواندن فایل"
            _importPreview.value = ImportPreviewState.Failed(message)
        }
    }

    fun dismissImportPreview() {
        _importPreview.value = ImportPreviewState.Idle
        pendingPlan = null
    }

    /** Step 2: the agent has seen the new/updated/unchanged counts and confirmed — now actually
     *  write to the database. */
    fun confirmImport() = viewModelScope.launch {
        val plan = pendingPlan ?: return@launch
        _importPreview.value = ImportPreviewState.Idle
        _importApply.value = ImportApplyState.Working
        runCatching {
            withContext(Dispatchers.IO) {
                if (plan.toInsert.isNotEmpty()) repository.insertAll(plan.toInsert)
                if (plan.toUpdate.isNotEmpty()) repository.updateAll(plan.toUpdate)
            }
        }.onSuccess {
            _importApply.value = ImportApplyState.Success(plan.toInsert.size, plan.toUpdate.size)
        }.onFailure { e ->
            _importApply.value = ImportApplyState.Failed(e.message ?: "خطا در اعمال ورود اطلاعات")
        }
        pendingPlan = null
    }

    fun resetImportApplyState() {
        _importApply.value = ImportApplyState.Idle
    }
}
