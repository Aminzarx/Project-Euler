package com.realestate.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.components.CircleIconButton
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.PropertyMiniCard
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home is a working desk, not a dashboard: search first, then the properties the agent is
 * already touching (recently opened, frequently used, pinned), and what needs attention today.
 * Nothing else competes for attention here — wallet lives in Profile, favorites has its own tab.
 */
@Composable
fun HomeScreen(
    viewModel: PropertyViewModel,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewedProperties.collectAsStateWithLifecycle()
    val frequentProperties by viewModel.frequentProperties.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedProperties.collectAsStateWithLifecycle()
    val todayFollowUps by viewModel.todayFollowUps.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

    Scaffold { padding ->
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(
                    icon = Icons.Rounded.FilterList,
                    onClick = onSearchClick,
                    contentDescription = "فیلترها",
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                AppCard(
                    modifier = Modifier.weight(1f),
                    onClick = onSearchClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "جستجوی سریع ملک...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            PrimaryButton(
                text = "افزودن ملک جدید",
                onClick = onAddClick,
                icon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )

            if (todayFollowUps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "پیگیری‌های امروز", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    todayFollowUps.forEach { property ->
                        AppListRow(
                            icon = Icons.Rounded.EventAvailable,
                            title = property.title,
                            subtitle = "${property.city} · ${formatActivityTime(property.followUpAt ?: 0L)}",
                            onClick = { onPropertyClick(property.id) }
                        )
                    }
                }
            }

            if (recentlyViewed.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xl))
                Text(text = "به‌تازگی دیده‌شده", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recentlyViewed, key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            if (frequentProperties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "پرکاربردترین ملک‌ها", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(frequentProperties, key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            if (pinned.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سنجاق‌شده", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pinned, key = { it.id }) { property ->
                        PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                }
            }

            if (recentActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xl))
                Text(text = "فعالیت‌های اخیر", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    recentActivities.forEach { activity ->
                        AppListRow(
                            icon = Icons.Rounded.History,
                            title = activity.event.description,
                            subtitle = "${activity.propertyTitle} · ${formatActivityTime(activity.event.createdAt)}",
                            onClick = { onPropertyClick(activity.propertyId) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

private fun formatActivityTime(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun EmptyHomeState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.extendedColors.accent.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.extendedColors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.extendedColors.onAccent
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFlorist,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.extendedColors.onAccent
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.extendedColors.onAccent
                    )
                }
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
                icon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
