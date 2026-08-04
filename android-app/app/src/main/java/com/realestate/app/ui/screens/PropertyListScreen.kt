package com.realestate.app.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.ui.components.PropertyCard
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
    val properties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val cities by viewModel.availableCities.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("املاک") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "فیلترها")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن ملک")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = { viewModel.updateFilter(filter.copy(query = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("جستجو بر اساس عنوان، شهر، آدرس یا کد ملک") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
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
                        .padding(horizontal = 16.dp),
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
                    Text(
                        "با این مشخصات ملکی پیدا نشد — فیلترها رو یه بار امتحان کن",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(properties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(property) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheetContent(
    cities: List<String>,
    filter: PropertyFilter,
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text("حذف فیلترها")
            }
            Button(
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
            ) {
                Text("اعمال")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
