package com.realestate.app.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.code
import com.realestate.app.ui.components.buildShareMessage
import com.realestate.app.ui.components.color
import com.realestate.app.ui.components.formatPrice
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    propertyId: Long,
    viewModel: PropertyViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onStoryCard: (Long) -> Unit
) {
    val context = LocalContext.current
    val property by viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    val notes by viewModel.getNotesForProperty(propertyId).collectAsStateWithLifecycle(initialValue = emptyList())
    val timeline by viewModel.getTimelineForProperty(propertyId).collectAsStateWithLifecycle(initialValue = emptyList())
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val favoriteFolders by viewModel.favoriteFolders.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var hasMarkedViewed by remember(propertyId) { mutableStateOf(false) }

    LaunchedEffect(property) {
        val p = property
        if (p != null && !hasMarkedViewed) {
            hasMarkedViewed = true
            viewModel.markViewed(p)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.title ?: "جزئیات ملک") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    property?.let { p ->
                        IconButton(onClick = { viewModel.toggleFavorite(p) }) {
                            Icon(
                                imageVector = if (p.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "علاقه‌مندی"
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "بیشتر")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("کارت استوری") },
                                    onClick = { showMenu = false; onStoryCard(p.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text("ویرایش") },
                                    onClick = { showMenu = false; onEdit(p.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (p.isPinned) "برداشتن سنجاق" else "سنجاق کردن") },
                                    onClick = { showMenu = false; viewModel.togglePinned(p) }
                                )
                                DropdownMenuItem(
                                    text = { Text("پوشه علاقه‌مندی") },
                                    onClick = { showMenu = false; showFolderDialog = true }
                                )
                                if (p.status == PropertyStatus.ARCHIVED) {
                                    DropdownMenuItem(
                                        text = { Text("بازگردانی از بایگانی") },
                                        onClick = { showMenu = false; viewModel.restoreProperty(p) }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("بایگانی") },
                                        onClick = { showMenu = false; viewModel.archiveProperty(p) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("حذف") },
                                    onClick = { showMenu = false; showDeleteDialog = true }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = property
        if (current == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ملک یافت نشد")
            }
        } else {
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
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    if (current.imageUri != null) {
                        AsyncImage(
                            model = current.imageUri,
                            contentDescription = current.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(64.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = current.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (current.isPinned) {
                        Icon(Icons.Filled.PushPin, contentDescription = "سنجاق‌شده", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${current.propertyType.label()} · ${current.dealType.label()} · ${current.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                AssistChip(
                    onClick = { showStatusSheet = true },
                    label = { Text(current.status.label()) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(current.status.color())
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = formatPrice(current.price, current.dealType), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "شهر", value = current.city)
                DetailRow(label = "آدرس", value = current.address)
                DetailRow(label = "متراژ", value = "${current.area} متر مربع")
                DetailRow(label = "تعداد اتاق", value = current.rooms.toString())
                if (current.ownerName.isNotBlank()) {
                    DetailRow(label = "مالک", value = current.ownerName)
                }
                DetailRow(label = "شماره تماس", value = current.ownerPhone)

                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = "برچسب‌ها") { showAddTagDialog = true }
                Spacer(modifier = Modifier.height(8.dp))
                if (current.tags.isEmpty()) {
                    Text(
                        "برچسبی اضافه نشده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        current.tags.forEach { tag ->
                            AssistChip(
                                onClick = { viewModel.removeTag(current, tag) },
                                label = { Text(tag) },
                                trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "حذف برچسب", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "توضیحات", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = current.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, buildShareMessage(current))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ملک"))
                        viewModel.markShared(current)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اشتراک‌گذاری ملک")
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "یادداشت‌ها") { showAddNoteDialog = true }
                Spacer(modifier = Modifier.height(8.dp))
                if (notes.isEmpty()) {
                    Text(
                        "یادداشتی ثبت نشده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notes.forEach { note ->
                            Card {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatDateTime(note.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "تاریخچه", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (timeline.isEmpty()) {
                    Text(
                        "هنوز رویدادی ثبت نشده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        timeline.forEach { event ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        formatDateTime(event.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showStatusSheet) {
                ModalBottomSheet(onDismissRequest = { showStatusSheet = false }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("وضعیت ملک", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        PropertyStatus.entries.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateStatus(current, status)
                                        showStatusSheet = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(status.color())
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    status.label(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (status == current.status) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "انتخاب‌شده",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (showAddTagDialog) {
                AddTagDialog(
                    suggestions = allTags.filterNot { current.tags.contains(it) },
                    onAdd = { tag -> viewModel.addTag(current, tag) },
                    onDismiss = { showAddTagDialog = false }
                )
            }

            if (showAddNoteDialog) {
                AddNoteDialog(
                    onAdd = { content -> viewModel.addNote(current.id, content) },
                    onDismiss = { showAddNoteDialog = false }
                )
            }

            if (showFolderDialog) {
                FavoriteFolderDialog(
                    currentFolder = current.favoriteFolder,
                    existingFolders = favoriteFolders,
                    onSelect = { folder -> viewModel.setFavoriteFolder(current, folder) },
                    onDismiss = { showFolderDialog = false }
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("حذف ملک") },
                    text = { Text("آیا از حذف این ملک مطمئن هستید؟ این عمل قابل بازگشت نیست.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteProperty(current)
                            showDeleteDialog = false
                            onDeleted()
                        }) { Text("حذف") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("انصراف") }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAddClick) { Text("افزودن") }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTagDialog(suggestions: List<String>, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن برچسب") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("مثلاً فوری، VIP") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.take(8).forEach { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onAdd(suggestion)
                                    onDismiss()
                                },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) onAdd(text)
                onDismiss()
            }) { Text("افزودن") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun AddNoteDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن یادداشت") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) onAdd(text)
                onDismiss()
            }) { Text("ثبت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteFolderDialog(
    currentFolder: String?,
    existingFolders: List<String>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentFolder ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پوشه علاقه‌مندی") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("مثلاً مشتریان امروز") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (existingFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        existingFolders.forEach { folder ->
                            FilterChip(
                                selected = folder == text,
                                onClick = { text = folder },
                                label = { Text(folder) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSelect(text.trim().ifBlank { null })
                onDismiss()
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
