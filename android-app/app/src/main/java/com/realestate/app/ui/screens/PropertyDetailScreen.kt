package com.realestate.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.ui.components.formatPrice
import com.realestate.app.ui.components.label
import com.realestate.app.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    propertyId: Long,
    viewModel: PropertyViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onStoryCard: (Long) -> Unit
) {
    val property by viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                        IconButton(onClick = { onStoryCard(p.id) }) {
                            Icon(Icons.Filled.Share, contentDescription = "کارت استوری")
                        }
                        IconButton(onClick = { onEdit(p.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف")
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
            return@Scaffold
        }

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

            Text(text = current.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "${current.propertyType.label()} · ${current.dealType.label()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatPrice(current.price, current.dealType), style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(label = "شهر", value = current.city)
            DetailRow(label = "آدرس", value = current.address)
            DetailRow(label = "متراژ", value = "${current.area} متر مربع")
            DetailRow(label = "تعداد اتاق", value = current.rooms.toString())
            DetailRow(label = "شماره تماس", value = current.ownerPhone)

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "توضیحات", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = current.description, style = MaterialTheme.typography.bodyMedium)
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف ملک") },
                text = { Text("آیا از حذف این ملک مطمئن هستید؟") },
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
