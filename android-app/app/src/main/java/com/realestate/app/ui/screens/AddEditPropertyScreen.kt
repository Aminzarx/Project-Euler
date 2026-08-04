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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyType
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var dealType by remember { mutableStateOf(DealType.SALE) }
    var propertyType by remember { mutableStateOf(PropertyType.APARTMENT) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var loadedExisting by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

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
            ownerPhone = p.ownerPhone
            dealType = p.dealType
            propertyType = p.propertyType
            imageUri = p.imageUri
            loadedExisting = true
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
    val isFormValid = title.isNotBlank() && city.isNotBlank() && address.isNotBlank() &&
        price.toLongOrNull() != null && area.toDoubleOrNull() != null && rooms.toIntOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "ویرایش ملک" else "افزودن ملک") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
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
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("افزودن تصویر")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(8.dp))

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rooms,
                    onValueChange = { rooms = it.filter { c -> c.isDigit() } },
                    label = { Text("تعداد اتاق") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("شماره تماس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

            Text("نوع معامله", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
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

            ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = it }) {
                OutlinedTextField(
                    value = propertyType.label(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع ملک") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    PropertyType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.label()) }, onClick = {
                            propertyType = type
                            typeMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
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
                        ownerPhone = ownerPhone.trim(),
                        dealType = dealType,
                        propertyType = propertyType,
                        imageUri = imageUri,
                        isFavorite = existing?.isFavorite ?: false
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
            ) {
                Text(if (isEditMode) "ذخیره تغییرات" else "افزودن ملک")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
