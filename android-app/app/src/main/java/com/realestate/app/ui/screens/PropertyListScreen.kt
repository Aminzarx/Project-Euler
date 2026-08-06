package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.matchesPriceRange
import com.realestate.app.ui.components.SwipeablePropertyRow
import com.realestate.app.ui.components.buildShareMessage
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.CaseTransferViewModel
import com.realestate.app.viewmodel.ImportApplyState
import com.realestate.app.viewmodel.ImportPreviewState
import com.realestate.app.viewmodel.PropertyFilter
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.ShareBundleState

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
    val agentPhone = profileViewModel.profile.collectAsStateWithLifecycle().value.mobileNumber
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
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
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
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            caseTransferViewModel.resetImportApplyState()
        } else if (state is ImportApplyState.Failed) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            caseTransferViewModel.resetImportApplyState()
        }
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} انتخاب شده") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "لغو انتخاب")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll(properties.map { it.id }) }) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "انتخاب همه")
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
                    title = { Text("املاک") },
                    actions = {
                        IconButton(onClick = { importPicker.launch("*/*") }) {
                            Icon(Icons.Rounded.Download, contentDescription = "وارد کردن بسته رمزنگاری‌شده")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "فیلترها")
                        }
                    }
                )
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
                )
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
                                onClick = { viewModel.updateFilter(PropertyFilter()) }
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
                            },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, buildShareMessage(property, agentPhone))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ملک"))
                                viewModel.markShared(property)
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
            onConfirm = { password ->
                showSharePasswordDialog = false
                val selected = allProperties.filter { selectedIds.contains(it.id) }
                caseTransferViewModel.shareSelected(selected, password.toCharArray())
            },
            onDismiss = { showSharePasswordDialog = false }
        )
    }
    if (shareState is ShareBundleState.Working) {
        WorkingDialog(text = "در حال آماده‌سازی و رمزنگاری بسته…")
    }

    if (showImportPasswordDialog) {
        PasswordPromptDialog(
            title = "رمز عبور بسته",
            description = "رمز عبوری که هنگام اشتراک‌گذاری این فایل تعیین شده را وارد کنید.",
            confirmLabel = "ادامه",
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
    if (importPreview is ImportPreviewState.Loading) {
        WorkingDialog(text = "در حال خواندن و رمزگشایی فایل…")
    }
    if (importApply is ImportApplyState.Working) {
        WorkingDialog(text = "در حال اعمال تغییرات…")
    }

    val preview = importPreview
    if (preview is ImportPreviewState.Ready) {
        ImportPreviewDialog(
            preview = preview,
            onConfirm = { caseTransferViewModel.confirmImport() },
            onDismiss = { caseTransferViewModel.dismissImportPreview() }
        )
    }
    LaunchedEffect(importPreview) {
        val state = importPreview
        if (state is ImportPreviewState.Failed) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            caseTransferViewModel.dismissImportPreview()
        }
    }
}

@Composable
private fun WorkingDialog(text: String) {
    AlertDialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        confirmButton = {},
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text)
            }
        }
    )
}

@Composable
private fun PasswordPromptDialog(
    title: String,
    description: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    // A short PIN is fine — PBKDF2's 210,000 iterations are what actually make brute-forcing it
    // expensive, not the character count — but under 4 digits is trivial to shoulder-surf and
    // easy to mistype without noticing, so a minimum is worth enforcing at the door.
    val isValid = password.length >= 4
    AlertDialog(
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
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
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

@Composable
private fun ImportPreviewDialog(
    preview: ImportPreviewState.Ready,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val plan = preview.plan
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیش‌نمایش ورود اطلاعات") },
        text = {
            Column {
                Text(
                    "این بسته ${plan.totalIncoming} پرونده دارد" +
                        if (preview.appVersion.isNotBlank()) " — ساخته‌شده با نسخه ${preview.appVersion} اپ" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                ImportCountRow(label = "پرونده جدید", count = plan.toInsert.size)
                ImportCountRow(label = "به‌روزرسانی می‌شود", count = plan.toUpdate.size)
                ImportCountRow(label = "بدون تغییر (نسخه محلی جدیدتر یا هم‌زمان است)", count = plan.unchanged.size)
                if (plan.toUpdate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "پرونده‌های «به‌روزرسانی می‌شود» نسخه محلی‌شان با نسخه داخل این بسته جایگزین خواهد شد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = plan.toInsert.isNotEmpty() || plan.toUpdate.isNotEmpty()) {
                Text("اعمال")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun ImportCountRow(label: String, count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$count", style = MaterialTheme.typography.bodySmall)
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

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("فیلترها", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = cityMenuExpanded, onExpandedChange = { cityMenuExpanded = it }) {
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

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = it }) {
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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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
        Spacer(modifier = Modifier.height(16.dp))
    }
}
