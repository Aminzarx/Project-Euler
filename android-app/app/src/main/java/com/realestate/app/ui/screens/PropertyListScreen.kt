package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.ui.components.SwipeablePropertyRow
import com.realestate.app.ui.components.buildShareMessage
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.PropertyFilter
import com.realestate.app.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    viewModel: PropertyViewModel,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val properties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val cities by viewModel.availableCities.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val selectionMode = selectedIds.isNotEmpty()

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
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "حذف موارد انتخاب‌شده")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("املاک") },
                    actions = {
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
                                    putExtra(Intent.EXTRA_TEXT, buildShareMessage(property))
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
                (min == null || property.price >= min) &&
                (max == null || property.price <= max)
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
