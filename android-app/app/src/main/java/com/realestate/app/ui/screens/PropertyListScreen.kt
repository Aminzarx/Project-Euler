package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.CaseType
import com.realestate.app.data.DealType
import com.realestate.app.data.MIN_PASSWORD_LENGTH
import com.realestate.app.data.PasswordStrengthLevel
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.evaluatePasswordStrength
import com.realestate.app.data.matchesPriceRange
import com.realestate.app.data.passwordStrengthHelperText
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.GlassAlertDialog
import com.realestate.app.ui.components.SwipeablePropertyRow
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.CaseTransferViewModel
import com.realestate.app.viewmodel.ImportApplyState
import com.realestate.app.viewmodel.ImportPreviewState
import com.realestate.app.viewmodel.ImportReadStage
import com.realestate.app.viewmodel.ExportStage
import com.realestate.app.viewmodel.PropertyFilter
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.ShareBundleState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    viewModel: PropertyViewModel,
    profileViewModel: ProfileViewModel,
    caseTransferViewModel: CaseTransferViewModel,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    // What ends up in the encrypted bundle's metadata as "who made this" — agency name first
    // since that's what a colleague receiving the file would recognize, falling back to the
    // agent's own name, and finally blank (never a raw phone number or device identifier).
    val exporterName = profile.agencyName.ifBlank { profile.fullName }
    val properties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val cities by viewModel.availableCities.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val selectionMode = selectedIds.isNotEmpty()

    // Leaving selection mode (X button or system back) always clears the selection — there's no
    // "exit but keep it for later" concept here, so the two must stay in lockstep.
    BackHandler(enabled = selectionMode) { viewModel.clearSelection() }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onAction?.invoke()
        }
    }

    var showSharePasswordDialog by remember { mutableStateOf(false) }
    val shareState by caseTransferViewModel.shareState.collectAsStateWithLifecycle()

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    val importPreview by caseTransferViewModel.importPreview.collectAsStateWithLifecycle()
    val importApply by caseTransferViewModel.importApply.collectAsStateWithLifecycle()
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportPasswordDialog = true
        }
    }

    // The bundle file is ready — hand it to the system Share Sheet, then reset so a
    // recomposition (e.g. rotation) doesn't re-fire the same share intent.
    LaunchedEffect(shareState) {
        val state = shareState
        if (state is ShareBundleState.Ready) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, state.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری بسته رمزنگاری‌شده"))
            viewModel.clearSelection()
            caseTransferViewModel.resetShareState()
        } else if (state is ShareBundleState.Failed) {
            showSnackbar(state.message, actionLabel = "تلاش دوباره") { showSharePasswordDialog = true }
            caseTransferViewModel.resetShareState()
        } else if (state is ShareBundleState.Cancelled) {
            showSnackbar("اشتراک‌گذاری لغو شد")
            caseTransferViewModel.resetShareState()
        }
    }

    LaunchedEffect(importApply) {
        val state = importApply
        if (state is ImportApplyState.Success) {
            val message = when {
                state.insertedCount > 0 && state.updatedCount > 0 ->
                    "${state.insertedCount} پرونده جدید افزوده و ${state.updatedCount} پرونده به‌روزرسانی شد"
                state.insertedCount > 0 -> "${state.insertedCount} پرونده جدید افزوده شد"
                state.updatedCount > 0 -> "${state.updatedCount} پرونده به‌روزرسانی شد"
                else -> "چیزی برای اعمال وجود نداشت"
            }
            if (state.canUndo) {
                showSnackbar(message, actionLabel = "بازگردانی") { caseTransferViewModel.undoLastImport() }
            } else {
                showSnackbar(message)
            }
            caseTransferViewModel.resetImportApplyState()
        } else if (state is ImportApplyState.Failed) {
            showSnackbar(state.message, actionLabel = "تلاش دوباره") {
                pendingImportUri?.let { uri -> showImportPasswordDialog = true }
            }
            caseTransferViewModel.resetImportApplyState()
        } else if (state is ImportApplyState.Undone) {
            showSnackbar("تغییرات بازگردانده شد")
            caseTransferViewModel.resetImportApplyState()
        }
    }

    LaunchedEffect(importPreview) {
        val state = importPreview
        if (state is ImportPreviewState.Failed) {
            showSnackbar(state.message, actionLabel = "تلاش دوباره") {
                pendingImportUri?.let { showImportPasswordDialog = true }
            }
            caseTransferViewModel.dismissImportPreview()
        } else if (state is ImportPreviewState.Cancelled) {
            showSnackbar("ورود اطلاعات لغو شد")
            caseTransferViewModel.dismissImportPreview()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // A plain if/else swap between two TopAppBars would pop instantly; Crossfade makes
            // entering/leaving selection mode read as one continuous transition instead of a jump.
            Crossfade(targetState = selectionMode, animationSpec = tween(200), label = "topbar-mode") { inSelectionMode ->
                if (inSelectionMode) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} انتخاب شده") },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Rounded.Close, contentDescription = "لغو انتخاب")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.selectAll(properties.map { it.id }) }) {
                                Icon(Icons.Rounded.SelectAll, contentDescription = "انتخاب همه")
                            }
                            IconButton(onClick = { showSharePasswordDialog = true }) {
                                Icon(Icons.Rounded.Share, contentDescription = "اشتراک‌گذاری رمزنگاری‌شده")
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "حذف موارد انتخاب‌شده")
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                when (filter.caseType) {
                                    null -> "همه پرونده‌ها"
                                    CaseType.OWNER -> "املاک"
                                    CaseType.CLIENT_REQUEST -> "درخواست‌های مشتریان"
                                }
                            )
                        },
                        actions = {
                            IconButton(onClick = { importPicker.launch("*/*") }) {
                                Icon(Icons.Rounded.FileOpen, contentDescription = "وارد کردن بسته رمزنگاری‌شده")
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = androidx.compose.foundation.shape.CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "افزودن ملک")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // A tab, not another filter chip: owner listings and client requests are different
            // kinds of records (a thing you're selling vs. a thing a client wants), so they get
            // their own independent section instead of blending into one undifferentiated list.
            val caseTypeTabs = remember { listOf(null, CaseType.OWNER, CaseType.CLIENT_REQUEST) }
            val selectedTabIndex = caseTypeTabs.indexOf(filter.caseType).coerceAtLeast(0)
            TabRow(selectedTabIndex = selectedTabIndex) {
                caseTypeTabs.forEachIndexed { index, caseType ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.updateFilter(filter.copy(caseType = caseType)) },
                        text = {
                            Text(
                                when (caseType) {
                                    null -> "همه"
                                    CaseType.OWNER -> "املاک"
                                    CaseType.CLIENT_REQUEST -> "درخواست‌ها"
                                }
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = filter.query,
                onValueChange = { viewModel.updateFilter(filter.copy(query = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = com.realestate.app.ui.components.PillShape,
                placeholder = { Text("جستجو بر اساس عنوان، شهر، آدرس یا کد ملک") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.recordSearch(filter.query) }
                ),
                trailingIcon = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Rounded.FilterList, contentDescription = "فیلترها")
                    }
                }
            )

            if (filter.query.isBlank() && recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentSearches.forEach { recent ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.updateFilter(filter.copy(query = recent)) },
                            label = { Text(recent) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.dealType == null,
                    onClick = { viewModel.updateFilter(filter.copy(dealType = null)) },
                    label = { Text("همه") }
                )
                FilterChip(
                    selected = filter.dealType == DealType.SALE,
                    onClick = { viewModel.updateFilter(filter.copy(dealType = DealType.SALE)) },
                    label = { Text("فروش") }
                )
                FilterChip(
                    selected = filter.dealType == DealType.RENT,
                    onClick = { viewModel.updateFilter(filter.copy(dealType = DealType.RENT)) },
                    label = { Text("اجاره") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (properties.isEmpty()) {
                // Distinguishes "nothing of this case type exists yet" (a tab-specific, honest
                // empty state) from "your search/filters are just too narrow" (the existing
                // generic one) — otherwise picking the درخواست‌ها tab before adding any client
                // requests would misleadingly suggest the filters, not the tab, are the problem.
                val casesInSelectedTab = remember(allProperties, filter.caseType) {
                    allProperties.filter { filter.caseType == null || it.caseType == filter.caseType }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (allProperties.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "هنوز ملکی ثبت نکردی",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "با دکمه + پایین صفحه اولین ملک رو اضافه کن",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    } else if (casesInSelectedTab.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (filter.caseType == CaseType.CLIENT_REQUEST) "هنوز درخواست مشتری ثبت نکردی" else "هنوز پرونده مالکی ثبت نکردی",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "با دکمه + پایین صفحه اولین مورد رو اضافه کن",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "با این مشخصات ملکی پیدا نشد",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "جستجو یا فیلترها رو تغییر بده",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            com.realestate.app.ui.components.SecondaryButton(
                                text = "حذف جستجو و فیلترها",
                                onClick = { viewModel.updateFilter(PropertyFilter(caseType = filter.caseType)) }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "${properties.size} ملک",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(properties, key = { it.id }) { property ->
                        SwipeablePropertyRow(
                            property = property,
                            selectionMode = selectionMode,
                            isSelected = selectedIds.contains(property.id),
                            onClick = {
                                if (selectionMode) {
                                    viewModel.toggleSelection(property.id)
                                } else {
                                    onPropertyClick(property.id)
                                }
                            },
                            onLongPress = { viewModel.toggleSelection(property.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(property) },
                            onCall = {
                                if (property.ownerPhone.isNotBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.ownerPhone}")))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            FilterSheetContent(
                cities = cities,
                filter = filter,
                allProperties = allProperties,
                onApply = { newFilter ->
                    viewModel.updateFilter(newFilter)
                    showFilterSheet = false
                },
                onReset = {
                    viewModel.updateFilter(PropertyFilter(query = filter.query))
                    showFilterSheet = false
                }
            )
        }
    }

    if (showDeleteConfirm) {
        val selected = properties.filter { selectedIds.contains(it.id) }
        com.realestate.app.ui.components.ConfirmationDialog(
            title = "حذف ${selected.size} ملک؟",
            text = "این ملک‌ها برای همیشه حذف می‌شوند و این کار قابل بازگشت نیست.",
            confirmLabel = "حذف",
            danger = true,
            onConfirm = { viewModel.deleteSelected(selected) },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showSharePasswordDialog) {
        PasswordPromptDialog(
            title = "رمز عبور بسته اشتراکی",
            description = "این رمز عبور برای رمزگشایی روی گوشی مقصد لازم است — آن را جدا (نه در همان پیام) به گیرنده بدهید.",
            confirmLabel = "اشتراک‌گذاری",
            isNewPassword = true,
            onConfirm = { password ->
                showSharePasswordDialog = false
                val selected = allProperties.filter { selectedIds.contains(it.id) }
                caseTransferViewModel.shareSelected(selected, password.toCharArray(), exporterName)
            },
            onDismiss = { showSharePasswordDialog = false }
        )
    }

    if (showImportPasswordDialog) {
        PasswordPromptDialog(
            title = "رمز عبور بسته",
            description = "رمز عبوری که هنگام اشتراک‌گذاری این فایل تعیین شده را وارد کنید.",
            confirmLabel = "ادامه",
            isNewPassword = false,
            onConfirm = { password ->
                showImportPasswordDialog = false
                pendingImportUri?.let { uri -> caseTransferViewModel.peekImport(uri, password.toCharArray()) }
            },
            onDismiss = {
                showImportPasswordDialog = false
                pendingImportUri = null
            }
        )
    }

    val shareStateNow = shareState
    if (shareStateNow is ShareBundleState.Working) {
        WorkingDialog(stageLabel = shareStateNow.stage.label, onCancel = { caseTransferViewModel.cancelShare() })
    }

    val previewState = importPreview
    if (previewState is ImportPreviewState.Loading) {
        WorkingDialog(stageLabel = previewState.stage.label, onCancel = { caseTransferViewModel.cancelImportPeek() })
    } else if (previewState is ImportPreviewState.Ready) {
        ImportPreviewDialog(
            preview = previewState,
            onConfirm = { selectedUids -> caseTransferViewModel.confirmImport(selectedUids) },
            onDismiss = { caseTransferViewModel.dismissImportPreview() }
        )
    }

    // confirmImport() itself isn't cancellable (see CaseTransferViewModel doc comment — it's a
    // single fast batched write, and undo is the safety net instead), so this dialog never gets
    // a cancel button; it's only ever on screen for the brief moment that write takes.
    if (importApply is ImportApplyState.Working) {
        WorkingDialog(stageLabel = "در حال اعمال تغییرات…")
    }
}

@Composable
private fun WorkingDialog(stageLabel: String, onCancel: (() -> Unit)? = null) {
    GlassAlertDialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        confirmButton = {
            if (onCancel != null) {
                TextButton(onClick = onCancel) { Text("لغو") }
            }
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stageLabel)
            }
        }
    )
}

private fun strengthColor(level: PasswordStrengthLevel): Color = when (level) {
    PasswordStrengthLevel.WEAK -> Color(0xFFE53935)
    PasswordStrengthLevel.MEDIUM -> Color(0xFFFB8C00)
    PasswordStrengthLevel.STRONG -> Color(0xFF43A047)
    PasswordStrengthLevel.VERY_STRONG -> Color(0xFF2E7D32)
}

/**
 * Reused for both directions of the flow, which need different rules: [isNewPassword] = true
 * (export) enforces [MIN_PASSWORD_LENGTH], shows the live strength meter, and requires a matching
 * confirmation field; [isNewPassword] = false (import) is just "type the password this file was
 * already encrypted with" — no minimum beyond non-empty, no confirmation field, since an older
 * bundle may have used a shorter password from before this minimum existed, and we must not lock
 * agents out of their own previously-exported files.
 */
@Composable
private fun PasswordPromptDialog(
    title: String,
    description: String,
    confirmLabel: String,
    isNewPassword: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val strength = remember(password) { evaluatePasswordStrength(password) }
    val confirmMismatch = isNewPassword && confirmPassword.isNotEmpty() && confirmPassword != password
    val isValid = if (isNewPassword) strength.meetsMinimum && confirmPassword == password else password.isNotEmpty()
    val visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    val visibilityToggle: @Composable () -> Unit = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (passwordVisible) "پنهان کردن رمز عبور" else "نمایش رمز عبور"
            )
        }
    }

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور") },
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    trailingIcon = visibilityToggle,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isNewPassword) ImeAction.Next else ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isNewPassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { strength.score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .semantics { contentDescription = "قدرت رمز عبور: ${strength.level.label}" },
                        color = strengthColor(strength.level)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            passwordStrengthHelperText(strength, password),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (password.isNotEmpty()) {
                            Text(strength.level.label, style = MaterialTheme.typography.labelSmall, color = strengthColor(strength.level))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("تکرار رمز عبور") },
                        singleLine = true,
                        isError = confirmMismatch,
                        visualTransformation = visualTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (confirmMismatch) {
                        Text(
                            "رمز عبور و تکرار آن یکسان نیستند",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = isValid) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

private val importPreviewDateFormat by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) }

@Composable
private fun ImportPreviewDialog(
    preview: ImportPreviewState.Ready,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val plan = preview.plan
    val actionableUids = remember(plan) {
        (plan.toInsert.map { it.uid } + plan.toUpdate.map { it.updated.uid }).toSet()
    }
    var selectedUids by remember(plan) { mutableStateOf(actionableUids) }

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیش‌نمایش ورود اطلاعات") },
        text = {
            Column(modifier = Modifier.heightIn(max = 440.dp)) {
                AppCard(contentPadding = PaddingValues(12.dp)) {
                    Column {
                        if (preview.exportedAt > 0) {
                            MetadataRow("تاریخ ساخت بسته", importPreviewDateFormat.format(Date(preview.exportedAt)))
                        }
                        if (preview.exporterName.isNotBlank()) MetadataRow("ارسال‌کننده", preview.exporterName)
                        if (preview.appVersion.isNotBlank()) MetadataRow("نسخه اپ مبدا", preview.appVersion)
                        if (preview.deviceModel.isNotBlank()) MetadataRow("دستگاه مبدا", preview.deviceModel)
                        if (preview.androidVersion.isNotBlank()) MetadataRow("نسخه اندروید مبدا", "Android ${preview.androidVersion}")
                        MetadataRow("تعداد کل پرونده‌ها", "${plan.totalIncoming}")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (actionableUids.isEmpty()) {
                    // Empty state: nothing this bundle has is newer than what's already here.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "همه ${plan.unchanged.size} پرونده این بسته از قبل به‌روز هستند",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "چیزی برای وارد کردن وجود ندارد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    if (plan.toUpdate.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "موارد «به‌روزرسانی می‌شود» نسخه محلی‌شان جایگزین خواهد شد",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedUids.size} از ${actionableUids.size} مورد انتخاب شده",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row {
                            TextButton(onClick = { selectedUids = actionableUids }) { Text("انتخاب همه") }
                            TextButton(onClick = { selectedUids = emptySet() }) { Text("پاک کردن") }
                        }
                    }
                    if (selectedUids.isEmpty()) {
                        Text(
                            "هیچ موردی انتخاب نشده — دکمه اعمال غیرفعال است",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        if (plan.toInsert.isNotEmpty()) {
                            item { ImportSectionLabel("پرونده‌های جدید (${plan.toInsert.size})") }
                            items(plan.toInsert, key = { it.uid }) { property ->
                                SelectableImportRow(
                                    title = property.title,
                                    subtitle = property.city,
                                    checked = property.uid in selectedUids,
                                    onCheckedChange = { checked ->
                                        selectedUids = if (checked) selectedUids + property.uid else selectedUids - property.uid
                                    }
                                )
                            }
                        }
                        if (plan.toUpdate.isNotEmpty()) {
                            item { ImportSectionLabel("به‌روزرسانی می‌شود (${plan.toUpdate.size})") }
                            items(plan.toUpdate, key = { it.updated.uid }) { update ->
                                SelectableImportRow(
                                    title = update.updated.title,
                                    subtitle = update.updated.city,
                                    checked = update.updated.uid in selectedUids,
                                    onCheckedChange = { checked ->
                                        val uid = update.updated.uid
                                        selectedUids = if (checked) selectedUids + uid else selectedUids - uid
                                    }
                                )
                            }
                        }
                        if (plan.unchanged.isNotEmpty()) {
                            item {
                                Text(
                                    "${plan.unchanged.size} مورد بدون تغییر (نسخه محلی جدیدتر یا هم‌زمان است)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedUids) }, enabled = selectedUids.isNotEmpty()) {
                Text(if (selectedUids.isEmpty()) "اعمال" else "اعمال (${selectedUids.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelSmall)
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ImportSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SelectableImportRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val displayTitle = title.ifBlank { "بدون عنوان" }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$displayTitle، $subtitle، ${if (checked) "انتخاب شده" else "انتخاب نشده"}"
            }
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(displayTitle, style = MaterialTheme.typography.bodyMedium)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheetContent(
    cities: List<String>,
    filter: PropertyFilter,
    allProperties: List<com.realestate.app.data.Property>,
    onApply: (PropertyFilter) -> Unit,
    onReset: () -> Unit
) {
    var selectedCity by remember(filter) { mutableStateOf(filter.city) }
    var selectedType by remember(filter) { mutableStateOf(filter.propertyType) }
    var selectedStatus by remember(filter) { mutableStateOf(filter.status) }
    var minPrice by remember(filter) { mutableStateOf(filter.minPrice?.toString() ?: "") }
    var maxPrice by remember(filter) { mutableStateOf(filter.maxPrice?.toString() ?: "") }
    var cityMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val matchCount = remember(allProperties, selectedCity, selectedType, selectedStatus, minPrice, maxPrice) {
        val min = minPrice.toLongOrNull()
        val max = maxPrice.toLongOrNull()
        allProperties.count { property ->
            (selectedCity == null || property.city == selectedCity) &&
                (selectedType == null || property.propertyType == selectedType) &&
                (selectedStatus == null || property.status == selectedStatus) &&
                property.matchesPriceRange(min, max)
        }
    }

    // Kept deliberately compact — City/Type share a row and Status/price fields are tight on
    // vertical spacing — so Apply/Reset land on screen without scrolling on a typical phone.
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()) {
        Text("فیلترها", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = cityMenuExpanded,
                onExpandedChange = { cityMenuExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCity ?: "همه شهرها",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("شهر") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityMenuExpanded) }
                )
                DropdownMenu(expanded = cityMenuExpanded, onDismissRequest = { cityMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("همه شهرها") }, onClick = {
                        selectedCity = null
                        cityMenuExpanded = false
                    })
                    cities.forEach { city ->
                        DropdownMenuItem(text = { Text(city) }, onClick = {
                            selectedCity = city
                            cityMenuExpanded = false
                        })
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedType?.label() ?: "همه انواع",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع ملک") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                )
                DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("همه انواع") }, onClick = {
                        selectedType = null
                        typeMenuExpanded = false
                    })
                    PropertyType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.label()) }, onClick = {
                            selectedType = type
                            typeMenuExpanded = false
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = it }) {
            OutlinedTextField(
                value = selectedStatus?.label() ?: "همه وضعیت‌ها",
                onValueChange = {},
                readOnly = true,
                label = { Text("وضعیت") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) }
            )
            DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("همه وضعیت‌ها") }, onClick = {
                    selectedStatus = null
                    statusMenuExpanded = false
                })
                PropertyStatus.entries.forEach { entry ->
                    DropdownMenuItem(text = { Text(entry.label()) }, onClick = {
                        selectedStatus = entry
                        statusMenuExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                label = { Text("حداقل قیمت") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                label = { Text("حداکثر قیمت") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$matchCount ملک با این فیلترها",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            com.realestate.app.ui.components.SecondaryButton(
                text = "حذف فیلترها",
                onClick = onReset,
                modifier = Modifier.weight(1f)
            )
            com.realestate.app.ui.components.PrimaryButton(
                text = "اعمال",
                onClick = {
                    onApply(
                        filter.copy(
                            city = selectedCity,
                            propertyType = selectedType,
                            status = selectedStatus,
                            minPrice = minPrice.toLongOrNull(),
                            maxPrice = maxPrice.toLongOrNull()
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
