package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.CaseType
import com.realestate.app.data.Property
import com.realestate.app.data.dealassistant.DealCategory
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.GlassAlertDialog
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.PropertyViewModel

/**
 * The productivity center of the app: every standalone tool lives here, grouped by what it's
 * for, plus a shortcut to whatever the agent used most recently. Contextual tools that operate
 * on a single open property (commission, price/m², construction cost) live on the property page
 * itself instead — see PropertyDetailScreen's "دستیار معامله" section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealAssistantScreen(
    viewModel: DealAssistantViewModel,
    propertyViewModel: PropertyViewModel,
    onBack: () -> Unit,
    onOpenCalculator: (DealToolId, Long?) -> Unit,
    onOpenAnalysis: (DealToolId, Long?) -> Unit,
    onOpenQuickNotes: () -> Unit,
    onOpenAdText: (Long) -> Unit,
    onOpenStoryCard: (Long) -> Unit
) {
    val recentTools by viewModel.recentTools.collectAsStateWithLifecycle()
    val allProperties by propertyViewModel.allProperties.collectAsStateWithLifecycle()
    var pickerTarget by remember { mutableStateOf<DealToolId?>(null) }

    fun openTool(tool: DealToolId) {
        if (!tool.available) return
        viewModel.recordToolUsage(tool)
        when (tool) {
            DealToolId.STORY_GENERATOR, DealToolId.AD_TEXT, DealToolId.MARKET_VALUE -> pickerTarget = tool
            DealToolId.COMPARISON -> onOpenAnalysis(tool, null)
            DealToolId.AVERAGE_PRICE, DealToolId.NEIGHBORHOOD, DealToolId.RANKING -> onOpenAnalysis(tool, null)
            DealToolId.QUICK_NOTES -> onOpenQuickNotes()
            else -> onOpenCalculator(tool, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دستیار هوشمند معامله") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(Spacing.screen),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (recentTools.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("اخیراً استفاده‌شده", style = MaterialTheme.typography.titleLarge)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        items(recentTools, key = { it.name }) { tool ->
                            RecentToolChip(tool = tool, onClick = { openTool(tool) })
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }

            DealCategory.entries.forEach { category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(category.label, style = MaterialTheme.typography.titleLarge)
                    }
                }
                val tools = DealToolId.entries.filter { it.category == category }
                items(tools, key = { it.name }) { tool ->
                    DealToolCard(tool = tool, onClick = { openTool(tool) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }
        }
    }

    val target = pickerTarget
    if (target != null) {
        // Story Card, Ad Text, and Market Value all assume an owner listing (price, address, a
        // selling pitch) — a Client Request case has none of those, so it never belongs in this
        // picker.
        PropertyPickerDialog(
            properties = allProperties.filter { it.caseType == CaseType.OWNER },
            onDismiss = { pickerTarget = null },
            onPick = { property ->
                pickerTarget = null
                when (target) {
                    DealToolId.STORY_GENERATOR -> onOpenStoryCard(property.id)
                    DealToolId.AD_TEXT -> onOpenAdText(property.id)
                    DealToolId.MARKET_VALUE -> onOpenAnalysis(target, property.id)
                    else -> Unit
                }
            }
        )
    }
}

@Composable
private fun DealToolCard(tool: DealToolId, onClick: () -> Unit) {
    AppCard(
        onClick = if (tool.available) onClick else null,
        contentPadding = PaddingValues(Spacing.md),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (tool.available) MaterialTheme.extendedColors.accent.copy(alpha = 0.3f)
                        else MaterialTheme.extendedColors.disabled
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tool.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (tool.available) MaterialTheme.extendedColors.onAccent else MaterialTheme.extendedColors.onDisabled
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(tool.label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                if (tool.available) tool.description else "به‌زودی",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentToolChip(tool: DealToolId, onClick: () -> Unit) {
    AppCard(onClick = onClick, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(tool.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(tool.label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PropertyPickerDialog(properties: List<Property>, onDismiss: () -> Unit, onPick: (Property) -> Unit) {
    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب ملک") },
        text = {
            if (properties.isEmpty()) {
                Text("هنوز پرونده مالکی ثبت نشده است. این ابزار فقط برای پرونده‌های مالک در دسترس است.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(properties, key = { it.id }) { property ->
                        AppCard(
                            onClick = { onPick(property) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(property.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(property.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
