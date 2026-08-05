package com.realestate.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.components.CircleIconButton
import com.realestate.app.ui.components.PillShape
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.PropertyMiniCard
import com.realestate.app.ui.components.StatusPillBadge
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
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
    onSearchClick: () -> Unit,
    onOpenQuickNotes: () -> Unit = {}
) {
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewedProperties.collectAsStateWithLifecycle()
    val frequentProperties by viewModel.frequentProperties.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedProperties.collectAsStateWithLifecycle()
    val todayFollowUps by viewModel.todayFollowUps.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    Scaffold { padding ->
        if (allProperties.isEmpty()) {
            EmptyHomeState(modifier = Modifier.padding(padding), onAddClick = onAddClick)
            return@Scaffold
        }

        val now = System.currentTimeMillis()
        val staleProperty = remember(pinned, frequentProperties) {
            (pinned + frequentProperties)
                .distinctBy { it.id }
                .maxByOrNull { now - it.lastModifiedAt }
                ?.takeIf { now - it.lastModifiedAt > STALE_THRESHOLD_MILLIS }
        }
        val visible = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
        val groupedActivities = remember(recentActivities) { groupActivitiesByRecency(recentActivities) }

        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it / 12 }
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item(key = "header") {
                    HomeHeader(
                        onFilterClick = onSearchClick,
                        query = filter.query,
                        onQueryChange = { viewModel.updateFilter(filter.copy(query = it)) },
                        onSearchSubmit = {
                            if (filter.query.isNotBlank()) viewModel.recordSearch(filter.query)
                            onSearchClick()
                        },
                        recentSearches = if (filter.query.isBlank()) recentSearches else emptyList(),
                        onRecentSearchClick = { recent -> viewModel.updateFilter(filter.copy(query = recent)) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    QuickActionsRow(
                        onAddClick = onAddClick,
                        onSearchClick = onSearchClick,
                        onQuickNotesClick = onOpenQuickNotes
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    SmartSummaryStrip(
                        propertyCount = allProperties.size,
                        followUpCount = todayFollowUps.size,
                        pinnedCount = pinned.size
                    )
                }

                if (staleProperty != null) {
                    item(key = "insight") {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        val days = ((now - staleProperty.lastModifiedAt) / DAY_MILLIS).toInt()
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPropertyClick(staleProperty.id) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.extendedColors.warning
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "«${staleProperty.title}» $days روزه بروزرسانی نشده",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "شاید وقتشه دوباره سر بزنی",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (todayFollowUps.isNotEmpty()) {
                    item(key = "followups-header") {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        val overdueCount = todayFollowUps.count { (it.followUpAt ?: 0L) < now }
                        SectionHeader(
                            icon = Icons.Rounded.EventAvailable,
                            title = "پیگیری‌های امروز",
                            subtitle = if (overdueCount > 0) {
                                "${todayFollowUps.size} مورد · $overdueCount مورد دیرکرد"
                            } else {
                                "${todayFollowUps.size} مورد"
                            },
                            emphasized = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(todayFollowUps, key = { "followup-${it.id}" }) { property ->
                        val isOverdue = (property.followUpAt ?: 0L) < now
                        Column {
                            AppListRow(
                                icon = Icons.Rounded.EventAvailable,
                                title = property.title,
                                subtitle = "${property.city} · ${formatActivityTime(property.followUpAt ?: 0L)}",
                                onClick = { onPropertyClick(property.id) },
                                trailing = {
                                    StatusPillBadge(
                                        text = if (isOverdue) "دیرکرد" else "امروز",
                                        color = if (isOverdue) MaterialTheme.extendedColors.danger else MaterialTheme.extendedColors.warning
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }
                    }
                }

                if (recentlyViewed.isNotEmpty()) {
                    item(key = "recent-header") {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        SectionHeader(icon = Icons.Rounded.Search, title = "به‌تازگی دیده‌شده")
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recentlyViewed, key = { it.id }) { property ->
                                PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                            }
                        }
                    }
                }

                if (frequentProperties.isNotEmpty()) {
                    item(key = "frequent-header") {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        SectionHeader(icon = Icons.Rounded.TrendingUp, title = "پرکاربردترین ملک‌ها")
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(frequentProperties, key = { it.id }) { property ->
                                PropertyMiniCard(
                                    property = property,
                                    onClick = { onPropertyClick(property.id) },
                                    subtitle = "${property.viewCount} بار بازدید"
                                )
                            }
                        }
                    }
                }

                if (pinned.isNotEmpty()) {
                    item(key = "pinned-header") {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        SectionHeader(icon = Icons.Rounded.PushPin, title = "سنجاق‌شده")
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pinned, key = { it.id }) { property ->
                                PropertyMiniCard(property = property, onClick = { onPropertyClick(property.id) })
                            }
                        }
                    }
                }

                if (recentActivities.isNotEmpty()) {
                    item(key = "activity-header") {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        SectionHeader(icon = Icons.Rounded.History, title = "فعالیت‌های اخیر")
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    groupedActivities.forEach { (bucketLabel, activities) ->
                        item(key = "activity-bucket-$bucketLabel") {
                            Text(
                                text = bucketLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        items(activities, key = { "activity-${it.event.id}" }) { activity ->
                            Column {
                                AppListRow(
                                    icon = activity.event.type.icon(),
                                    title = activity.event.description,
                                    subtitle = "${activity.propertyTitle} · ${formatActivityTime(activity.event.createdAt)}",
                                    onClick = { onPropertyClick(activity.propertyId) }
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                        }
                    }
                }

                item(key = "bottom-spacer") {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
private const val STALE_THRESHOLD_MILLIS = 14 * DAY_MILLIS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeader(
    onFilterClick: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit
) {
    val greeting = remember { greetingForCurrentTime() }
    val dayLabel = remember { todayLabel() }

    Text(text = greeting, style = MaterialTheme.typography.headlineSmall)
    Text(
        text = dayLabel,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.md))

    Row(verticalAlignment = Alignment.CenterVertically) {
        CircleIconButton(
            icon = Icons.Rounded.FilterList,
            onClick = onFilterClick,
            contentDescription = "فیلترها",
            size = 52.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            shape = PillShape,
            placeholder = { Text("جستجوی سریع ملک...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() })
        )
    }

    if (recentSearches.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recentSearches.forEach { recent ->
                FilterChip(selected = false, onClick = { onRecentSearchClick(recent) }, label = { Text(recent) })
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onQuickNotesClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        QuickActionTile(
            icon = Icons.Rounded.Add,
            label = "افزودن ملک",
            onClick = onAddClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            icon = Icons.Rounded.Search,
            label = "جستجو",
            onClick = onSearchClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            icon = Icons.Rounded.StickyNote2,
            label = "یادداشت سریع",
            onClick = onQuickNotesClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SmartSummaryStrip(propertyCount: Int, followUpCount: Int, pinnedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryStat(label = "کل املاک", value = propertyCount.toString())
        SummaryStat(label = "پیگیری امروز", value = followUpCount.toString())
        SummaryStat(label = "سنجاق‌شده", value = pinnedCount.toString())
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, subtitle: String? = null, emphasized: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (emphasized) 22.dp else 18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                style = if (emphasized) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun groupActivitiesByRecency(
    activities: List<com.realestate.app.viewmodel.RecentActivity>
): List<Pair<String, List<com.realestate.app.viewmodel.RecentActivity>>> {
    val startOfToday = startOfDay(Calendar.getInstance()).timeInMillis
    val startOfYesterday = startOfToday - DAY_MILLIS
    val startOfWeek = startOfToday - 7 * DAY_MILLIS

    val today = mutableListOf<com.realestate.app.viewmodel.RecentActivity>()
    val yesterday = mutableListOf<com.realestate.app.viewmodel.RecentActivity>()
    val thisWeek = mutableListOf<com.realestate.app.viewmodel.RecentActivity>()
    val older = mutableListOf<com.realestate.app.viewmodel.RecentActivity>()

    activities.forEach { activity ->
        val t = activity.event.createdAt
        when {
            t >= startOfToday -> today += activity
            t >= startOfYesterday -> yesterday += activity
            t >= startOfWeek -> thisWeek += activity
            else -> older += activity
        }
    }

    return buildList {
        if (today.isNotEmpty()) add("امروز" to today)
        if (yesterday.isNotEmpty()) add("دیروز" to yesterday)
        if (thisWeek.isNotEmpty()) add("این هفته" to thisWeek)
        if (older.isNotEmpty()) add("قدیمی‌تر" to older)
    }
}

private fun startOfDay(calendar: Calendar): Calendar {
    val c = calendar.clone() as Calendar
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}

private fun greetingForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..10 -> "صبح بخیر"
        hour in 11..15 -> "ظهر بخیر"
        hour in 16..18 -> "عصر بخیر"
        else -> "شب بخیر"
    }
}

private val PERSIAN_WEEKDAYS = mapOf(
    Calendar.SATURDAY to "شنبه",
    Calendar.SUNDAY to "یک‌شنبه",
    Calendar.MONDAY to "دوشنبه",
    Calendar.TUESDAY to "سه‌شنبه",
    Calendar.WEDNESDAY to "چهارشنبه",
    Calendar.THURSDAY to "پنج‌شنبه",
    Calendar.FRIDAY to "جمعه"
)

private fun todayLabel(): String {
    val calendar = Calendar.getInstance()
    val weekday = PERSIAN_WEEKDAYS[calendar.get(Calendar.DAY_OF_WEEK)] ?: ""
    val numeric = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
    return "امروز $weekday، $numeric"
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
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "نکته: بعداً می‌تونی ملک‌های مهم رو سنجاق کنی تا همیشه بالای صفحه اصلی در دسترس باشن.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
