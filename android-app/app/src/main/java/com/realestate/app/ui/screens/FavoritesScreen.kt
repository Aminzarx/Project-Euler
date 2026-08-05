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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.Property
import com.realestate.app.ui.components.SwipeablePropertyRow
import com.realestate.app.ui.components.buildShareMessage
import com.realestate.app.viewmodel.PropertyViewModel

private enum class FavoriteSort(val label: String) {
    RECENT("جدیدترین"),
    PRICE_LOW("ارزان‌ترین"),
    PRICE_HIGH("گران‌ترین"),
    TITLE("نام")
}

private fun List<Property>.sortedFor(sort: FavoriteSort): List<Property> = when (sort) {
    FavoriteSort.RECENT -> sortedByDescending { it.dateAdded }
    FavoriteSort.PRICE_LOW -> sortedBy { it.price }
    FavoriteSort.PRICE_HIGH -> sortedByDescending { it.price }
    FavoriteSort.TITLE -> sortedBy { it.title }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: PropertyViewModel,
    onPropertyClick: (Long) -> Unit
) {
    val favorites by viewModel.favoriteProperties.collectAsStateWithLifecycle()
    val folders by viewModel.favoriteFolders.collectAsStateWithLifecycle()
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(FavoriteSort.RECENT) }

    val visibleFavorites = (
        if (selectedFolder == null) favorites else favorites.filter { it.favoriteFolder == selectedFolder }
        ).sortedFor(sortOption)

    Scaffold(topBar = { TopAppBar(title = { Text("علاقه‌مندی‌ها") }) }) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "هنوز چیزی اینجا نیست — با ضربه روی ♡ کنار هر ملک، به علاقه‌مندی‌هات اضافه‌اش کن",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FavoriteSort.entries.forEach { option ->
                        FilterChip(
                            selected = sortOption == option,
                            onClick = { sortOption = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                if (folders.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFolder == null,
                            onClick = { selectedFolder = null },
                            label = { Text("همه") }
                        )
                        folders.forEach { folder ->
                            FilterChip(
                                selected = selectedFolder == folder,
                                onClick = { selectedFolder = folder },
                                label = { Text(folder) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                FavoritesList(visibleFavorites, onPropertyClick, viewModel)
            }
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Property>,
    onPropertyClick: (Long) -> Unit,
    viewModel: PropertyViewModel
) {
    val context = LocalContext.current
    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "ملکی در این پوشه نیست",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites, key = { it.id }) { property ->
                SwipeablePropertyRow(
                    property = property,
                    selectionMode = false,
                    isSelected = false,
                    onClick = { onPropertyClick(property.id) },
                    onLongPress = {},
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
