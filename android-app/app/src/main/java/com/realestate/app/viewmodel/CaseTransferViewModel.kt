package com.realestate.app.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
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
import com.realestate.app.data.exportimport.buildShareFileName
import com.realestate.app.data.exportimport.filterToSelected
import com.realestate.app.data.exportimport.parseExportEnvelope
import com.realestate.app.data.exportimport.planImport
import com.realestate.app.data.exportimport.toJson
import com.realestate.app.data.exportimport.undoSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Real, distinct stages of building a bundle — not a fake percentage. A PBKDF2+AES-GCM pass over
 *  a few properties' worth of JSON finishes in well under a second even on a modest device, so
 *  there is no meaningful "37%" to show; naming what's actually happening right now is more honest
 *  and, for a security-sensitive operation, more reassuring than a bar that would mostly just jump
 *  from empty to full. */
enum class ExportStage(val label: String) {
    PREPARING("در حال آماده‌سازی…"),
    ENCRYPTING("در حال رمزنگاری…"),
    WRITING_PACKAGE("در حال نوشتن بسته…"),
    LAUNCHING_SHARE_SHEET("در حال باز کردن اشتراک‌گذاری…")
}

enum class ImportReadStage(val label: String) {
    READING("در حال خواندن فایل…"),
    DECRYPTING("در حال رمزگشایی…"),
    VALIDATING("در حال بررسی اعتبار فایل…"),
    PLANNING("در حال مقایسه با اطلاعات محلی…")
}

sealed class ShareBundleState {
    object Idle : ShareBundleState()
    data class Working(val stage: ExportStage) : ShareBundleState()
    data class Ready(val uri: Uri) : ShareBundleState()
    object Cancelled : ShareBundleState()
    data class Failed(val message: String) : ShareBundleState()
}

/** What the import preview dialog shows, keyed to the file the agent just picked and decrypted. */
sealed class ImportPreviewState {
    object Idle : ImportPreviewState()
    data class Loading(val stage: ImportReadStage) : ImportPreviewState()
    data class Ready(
        val plan: ImportPlan,
        val exportedAt: Long,
        val appVersion: String,
        val deviceModel: String,
        val androidVersion: String,
        val exporterName: String
    ) : ImportPreviewState()
    object Cancelled : ImportPreviewState()
    data class Failed(val message: String) : ImportPreviewState()
}

sealed class ImportApplyState {
    object Idle : ImportApplyState()
    object Working : ImportApplyState()
    data class Success(val insertedCount: Int, val updatedCount: Int, val canUndo: Boolean) : ImportApplyState()
    object Undone : ImportApplyState()
    data class Failed(val message: String) : ImportApplyState()
}

/** What [CaseTransferViewModel] needs to reverse a just-applied import — held only from a
 *  successful [CaseTransferViewModel.confirmImport] until [CaseTransferViewModel.undoLastImport]
 *  is used or a new import/share cycle starts. Never persisted: this is a same-session,
 *  same-process "oops" button, not a durable history. */
private data class AppliedImportUndo(val insertedUids: List<String>, val previousValues: List<Property>)

/**
 * Owns the "share selected cases as an encrypted bundle" and "import an encrypted bundle" flows —
 * kept separate from PropertyViewModel (which already owns selection state) the same way
 * BackupViewModel is kept separate from it for the full-database backup, so each ViewModel's state
 * machine stays about one thing.
 *
 * Mirrors BackupViewModel's preview-then-confirm shape for import: [peekImport] only decrypts and
 * plans, touching nothing in the database, so the agent always sees real counts before anything
 * is written.
 *
 * Cancellation is offered for [shareSelected] and [peekImport] — both run entirely before anything
 * touches the database, so cancelling them mid-flight is always safe. [confirmImport] itself is not
 * cancellable: by the time it runs, the agent has already committed, the write is a single fast
 * batched transaction per table, and interrupting a database write partway through risks a
 * half-applied import — a worse outcome than letting an already-in-flight write finish. [undoLastImport]
 * is the safety net for that step instead.
 */
class CaseTransferViewModel(application: Application) : AndroidViewModel(application) {
    private val fileService = CaseBundleFileService(application)
    private val repository = PropertyRepository(AppDatabase.getInstance(application).propertyDao())

    private val _shareState = MutableStateFlow<ShareBundleState>(ShareBundleState.Idle)
    val shareState: StateFlow<ShareBundleState> = _shareState
    private var shareJob: Job? = null

    private val _importPreview = MutableStateFlow<ImportPreviewState>(ImportPreviewState.Idle)
    val importPreview: StateFlow<ImportPreviewState> = _importPreview
    private var importPeekJob: Job? = null

    private val _importApply = MutableStateFlow<ImportApplyState>(ImportApplyState.Idle)
    val importApply: StateFlow<ImportApplyState> = _importApply

    // Held only between a successful peekImport and the agent's confirm/cancel — never persisted,
    // never written anywhere but into the database itself on confirm.
    private var pendingPlan: ImportPlan? = null
    private var lastAppliedUndo: AppliedImportUndo? = null

    fun shareSelected(properties: List<Property>, password: CharArray, exporterName: String) {
        shareJob?.cancel()
        shareJob = viewModelScope.launch {
            try {
                _shareState.value = ShareBundleState.Working(ExportStage.PREPARING)
                val plaintext = withContext(Dispatchers.IO) {
                    ensureActive()
                    val envelope = ExportEnvelope(
                        schemaVersion = EXPORT_SCHEMA_VERSION,
                        exportedAt = System.currentTimeMillis(),
                        appVersion = BuildConfig.VERSION_NAME,
                        deviceModel = Build.MODEL.orEmpty(),
                        androidVersion = Build.VERSION.RELEASE.orEmpty(),
                        exporterName = exporterName,
                        properties = properties
                    )
                    envelope.toJson().toString().toByteArray(Charsets.UTF_8)
                }

                _shareState.value = ShareBundleState.Working(ExportStage.ENCRYPTING)
                val encrypted = withContext(Dispatchers.IO) {
                    ensureActive()
                    FileCrypto.encrypt(plaintext, password)
                }

                _shareState.value = ShareBundleState.Working(ExportStage.WRITING_PACKAGE)
                val uri = withContext(Dispatchers.IO) {
                    ensureActive()
                    val fileName = buildShareFileName(exporterName, System.currentTimeMillis())
                    fileService.writeShareFile(encrypted, fileName)
                }

                _shareState.value = ShareBundleState.Working(ExportStage.LAUNCHING_SHARE_SHEET)
                yield() // let the dialog actually paint this label before the terminal state fires
                _shareState.value = ShareBundleState.Ready(uri)
            } catch (e: CancellationException) {
                _shareState.value = ShareBundleState.Cancelled
                throw e
            } catch (e: Exception) {
                _shareState.value = ShareBundleState.Failed(e.message ?: "خطا در آماده‌سازی فایل")
            } finally {
                // Best-effort only — the Compose String this CharArray was copied from is immutable
                // and can't itself be zeroed, so this doesn't make the password unrecoverable from a
                // full heap dump. It does mean this CharArray copy, specifically, doesn't linger.
                password.fill('\u0000')
            }
        }
    }

    fun cancelShare() {
        shareJob?.cancel()
    }

    fun resetShareState() {
        _shareState.value = ShareBundleState.Idle
    }

    /** Step 1 of import: read, decrypt, validate, and plan — nothing here touches the database. */
    fun peekImport(uri: Uri, password: CharArray) {
        importPeekJob?.cancel()
        importPeekJob = viewModelScope.launch {
            try {
                _importPreview.value = ImportPreviewState.Loading(ImportReadStage.READING)
                val encrypted = withContext(Dispatchers.IO) {
                    ensureActive()
                    fileService.readFile(uri)
                }

                _importPreview.value = ImportPreviewState.Loading(ImportReadStage.DECRYPTING)
                val plaintext = withContext(Dispatchers.IO) {
                    ensureActive()
                    FileCrypto.decrypt(encrypted, password)
                }

                _importPreview.value = ImportPreviewState.Loading(ImportReadStage.VALIDATING)
                val envelope = withContext(Dispatchers.IO) {
                    ensureActive()
                    parseExportEnvelope(plaintext)
                }

                _importPreview.value = ImportPreviewState.Loading(ImportReadStage.PLANNING)
                val plan = withContext(Dispatchers.IO) {
                    ensureActive()
                    val existing = repository.allProperties.first()
                    planImport(existing, envelope.properties)
                }

                pendingPlan = plan
                _importPreview.value = ImportPreviewState.Ready(
                    plan = plan,
                    exportedAt = envelope.exportedAt,
                    appVersion = envelope.appVersion,
                    deviceModel = envelope.deviceModel,
                    androidVersion = envelope.androidVersion,
                    exporterName = envelope.exporterName
                )
            } catch (e: CancellationException) {
                _importPreview.value = ImportPreviewState.Cancelled
                throw e
            } catch (e: Exception) {
                val message = when (e) {
                    is FileCrypto.DecryptionException -> e.message
                    is ExportImportError -> e.message
                    else -> null
                } ?: "خطا در خواندن فایل"
                _importPreview.value = ImportPreviewState.Failed(message)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun cancelImportPeek() {
        importPeekJob?.cancel()
    }

    fun dismissImportPreview() {
        _importPreview.value = ImportPreviewState.Idle
        pendingPlan = null
    }

    /** Step 2: the agent has seen the new/updated/unchanged counts, checked/unchecked individual
     *  records, and confirmed — now actually write to the database. [selectedUids] narrows the
     *  full plan down to what was actually left checked in the selective-import list. */
    fun confirmImport(selectedUids: Set<String>) = viewModelScope.launch {
        val plan = pendingPlan?.filterToSelected(selectedUids) ?: return@launch
        _importPreview.value = ImportPreviewState.Idle
        _importApply.value = ImportApplyState.Working
        runCatching {
            withContext(Dispatchers.IO) {
                if (plan.toInsert.isNotEmpty()) repository.insertAll(plan.toInsert)
                if (plan.toUpdate.isNotEmpty()) repository.updateAll(plan.toUpdate.map { it.updated })
            }
        }.onSuccess {
            val (insertedUids, previousValues) = plan.undoSnapshot()
            lastAppliedUndo = AppliedImportUndo(insertedUids, previousValues)
            _importApply.value = ImportApplyState.Success(
                insertedCount = plan.toInsert.size,
                updatedCount = plan.toUpdate.size,
                canUndo = plan.toInsert.isNotEmpty() || plan.toUpdate.isNotEmpty()
            )
        }.onFailure { e ->
            _importApply.value = ImportApplyState.Failed(e.message ?: "خطا در اعمال ورود اطلاعات")
        }
        pendingPlan = null
    }

    /** Reverses the most recently applied import: deletes the records it inserted (by uid — the
     *  local ids Room assigned on insert were never captured, since insertAll() doesn't return
     *  them) and restores the pre-import values of the records it updated. Only available for a
     *  short window after a successful import — surfaced as a Snackbar action, so its natural
     *  lifetime is the Snackbar's own visible duration; there is no separate timer to defeat. */
    fun undoLastImport() = viewModelScope.launch {
        val undo = lastAppliedUndo ?: return@launch
        lastAppliedUndo = null
        runCatching {
            withContext(Dispatchers.IO) {
                if (undo.insertedUids.isNotEmpty()) repository.deleteByUids(undo.insertedUids)
                if (undo.previousValues.isNotEmpty()) repository.updateAll(undo.previousValues)
            }
        }.onSuccess {
            _importApply.value = ImportApplyState.Undone
        }.onFailure { e ->
            _importApply.value = ImportApplyState.Failed(e.message ?: "بازگردانی ناموفق بود")
        }
    }

    fun resetImportApplyState() {
        _importApply.value = ImportApplyState.Idle
    }
}
