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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.Property
import com.realestate.app.ui.components.PropertyCard
import com.realestate.app.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: PropertyViewModel,
    onPropertyClick: (Long) -> Unit
) {
    val favorites by viewModel.favoriteProperties.collectAsStateWithLifecycle()
    val folders by viewModel.favoriteFolders.collectAsStateWithLifecycle()
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    val visibleFavorites = if (selectedFolder == null) {
        favorites
    } else {
        favorites.filter { it.favoriteFolder == selectedFolder }
    }

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
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (folders.isEmpty()) {
                    FavoritesList(favorites, onPropertyClick, viewModel)
                } else {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        FavoritesList(visibleFavorites, onPropertyClick, viewModel)
                    }
                }
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites, key = { it.id }) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property.id) },
                    onFavoriteClick = { viewModel.toggleFavorite(property) }
                )
            }
        }
    }
}
