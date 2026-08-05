package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.label
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPropertyScreen(
    propertyId: Long?,
    viewModel: PropertyViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val existing by if (propertyId != null) {
        viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf<Property?>(null) }
    }
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val smartDefaults by viewModel.smartDefaults.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var dealType by remember { mutableStateOf(DealType.SALE) }
    var propertyType by remember { mutableStateOf(PropertyType.APARTMENT) }
    var status by remember { mutableStateOf(PropertyStatus.NEW) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagInput by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var loadedExisting by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val p = existing
        if (p != null && !loadedExisting) {
            title = p.title
            description = p.description
            price = p.price.toString()
            area = p.area.toString()
            rooms = p.rooms.toString()
            city = p.city
            address = p.address
            ownerName = p.ownerName
            ownerPhone = p.ownerPhone
            dealType = p.dealType
            propertyType = p.propertyType
            status = p.status
            tags = p.tags
            imageUri = p.imageUri
            loadedExisting = true
        }
    }

    LaunchedEffect(smartDefaults) {
        if (propertyId == null && city.isBlank()) {
            smartDefaults.city?.let { city = it }
            smartDefaults.propertyType?.let { propertyType = it }
            smartDefaults.dealType?.let { dealType = it }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // برخی منابع اجازه دسترسی دائمی نمی‌دهند؛ عکس فقط تا پایان این نشست قابل نمایش خواهد بود
            }
            imageUri = uri.toString()
        }
    }

    val isEditMode = propertyId != null
    // Only a title is required — save fast, fill everything else in later.
    val isFormValid = title.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "ویرایش ملک" else "افزودن ملک") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(Spacing.screen)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("افزودن تصویر")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.cardGap))

            Text("اطلاعات پایه", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان ملک") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(Spacing.cardGap))

            Text("قیمت و مشخصات", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = dealType == DealType.SALE,
                        onClick = { dealType = DealType.SALE },
                        label = { Text("فروش") }
                    )
                    FilterChip(
                        selected = dealType == DealType.RENT,
                        onClick = { dealType = DealType.RENT },
                        label = { Text("اجاره") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() } },
                        label = { Text("قیمت (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("متراژ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rooms,
                    onValueChange = { rooms = it.filter { c -> c.isDigit() } },
                    label = { Text("تعداد اتاق") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = it }) {
                    OutlinedTextField(
                        value = propertyType.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع ملک") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                    )
                    DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        PropertyType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.label()) }, onClick = {
                                propertyType = type
                                typeMenuExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = it }) {
                    OutlinedTextField(
                        value = status.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وضعیت") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) }
                    )
                    DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                        PropertyStatus.entries.forEach { entry ->
                            DropdownMenuItem(text = { Text(entry.label()) }, onClick = {
                                status = entry
                                statusMenuExpanded = false
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.cardGap))

            Text("موقعیت و مالک", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("شهر") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("آدرس") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("نام مالک") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("شماره تماس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.cardGap))

            Text("برچسب‌ها", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                if (tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            AssistChip(
                                onClick = { tags = tags - tag },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(Icons.Rounded.Close, contentDescription = "حذف برچسب", modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("افزودن برچسب") },
                    placeholder = { Text("مثلاً فوری، VIP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                val tagSuggestions = allTags.filterNot { tags.contains(it) }
                if (tagInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        modifier = Modifier.clickable {
                            tags = tags + tagInput.trim()
                            tagInput = ""
                        },
                        text = "افزودن «${tagInput.trim()}»",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (tagSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tagSuggestions.take(8).forEach { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = { tags = tags + suggestion },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            PrimaryButton(
                text = if (isEditMode) "ذخیره تغییرات" else "افزودن ملک",
                onClick = {
                    val newProperty = Property(
                        id = propertyId ?: 0,
                        title = title.trim(),
                        description = description.trim(),
                        price = price.toLongOrNull() ?: 0,
                        area = area.toDoubleOrNull() ?: 0.0,
                        rooms = rooms.toIntOrNull() ?: 0,
                        city = city.trim(),
                        address = address.trim(),
                        ownerName = ownerName.trim(),
                        ownerPhone = ownerPhone.trim(),
                        dealType = dealType,
                        propertyType = propertyType,
                        status = status,
                        tags = tags,
                        imageUri = imageUri,
                        isFavorite = existing?.isFavorite ?: false,
                        isPinned = existing?.isPinned ?: false,
                        favoriteFolder = existing?.favoriteFolder,
                        dateAdded = existing?.dateAdded ?: System.currentTimeMillis(),
                        lastViewedAt = existing?.lastViewedAt,
                        lastSharedAt = existing?.lastSharedAt,
                        viewCount = existing?.viewCount ?: 0,
                        followUpAt = existing?.followUpAt
                    )
                    if (isEditMode) {
                        viewModel.updateProperty(newProperty)
                    } else {
                        viewModel.addProperty(newProperty)
                    }
                    onDone()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
