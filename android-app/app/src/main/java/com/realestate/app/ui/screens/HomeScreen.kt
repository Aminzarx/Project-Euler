package com.realestate.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.PropertyMiniCard
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: PropertyViewModel,
    walletViewModel: WalletViewModel,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllFavoritesClick: () -> Unit,
    onOpenWallet: () -> Unit
) {
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewedProperties.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteProperties.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedProperties.collectAsStateWithLifecycle()
    val balance by walletViewModel.balance.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = androidx.compose.foundation.shape.CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن ملک")
            }
        }
    ) { padding ->
        if (allProperties.isEmpty()) {
            EmptyHomeState(modifier = Modifier.padding(padding), onAddClick = onAddClick)
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.screen)
        ) {
            Text(text = "سلام 👋", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "بیا سریع به کارت برسی",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSearchClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "جستجوی سریع ملک...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.cardGap))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(heroGradient())
                    .clickable(onClick = onOpenWallet)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("کیف پول", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.US).format(balance)} تومان",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                TextButton(onClick = onOpenWallet) { Text("مشاهده", color = Color.White) }
            }

            if (pinned.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سنجاق‌شده", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pinned, key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            if (recentlyViewed.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "به‌تازگی دیده‌شده", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recentlyViewed, key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            if (favorites.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "علاقه‌مندی‌ها", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onSeeAllFavoritesClick) {
                        Text("مشاهده همه")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favorites.take(6), key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyHomeState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth(0.5f)
                    .clip(MaterialTheme.shapes.large)
                    .background(heroGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.height(40.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "هنوز ملکی ثبت نکردی",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "با افزودن اولین ملک، همه‌چیز اینجا مرتب و در دسترس می‌مونه.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "افزودن اولین ملک",
                onClick = onAddClick,
                icon = Icons.Filled.Add,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
