package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.Property
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.data.dealassistant.RankingMetric
import com.realestate.app.data.dealassistant.averagePriceStats
import com.realestate.app.data.dealassistant.calculatePricePerMeter
import com.realestate.app.data.dealassistant.estimateMarketValue
import com.realestate.app.data.dealassistant.neighborhoodStats
import com.realestate.app.data.dealassistant.rankProperties
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.formatPrice
import com.realestate.app.ui.components.label
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.NumberFormat
import java.util.Locale

private fun money(value: Double): String = NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
private fun percent(value: Double): String = "%.1f٪".format(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyAnalysisScreen(
    toolId: DealToolId,
    propertyId: Long?,
    viewModel: DealAssistantViewModel,
    propertyViewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val allProperties by propertyViewModel.allProperties.collectAsStateWithLifecycle()
    LaunchedEffect(toolId) { viewModel.recordToolUsage(toolId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolId.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (toolId) {
                DealToolId.AVERAGE_PRICE -> AveragePriceAnalysis(allProperties)
                DealToolId.MARKET_VALUE -> MarketValueAnalysis(allProperties, propertyId)
                DealToolId.NEIGHBORHOOD -> NeighborhoodAnalysis(allProperties)
                DealToolId.RANKING -> RankingAnalysis(allProperties)
                DealToolId.COMPARISON -> ComparisonAnalysis(allProperties)
                else -> Text("این ابزار در دسترس نیست.", modifier = Modifier.padding(Spacing.screen))
            }
        }
    }
}

@Composable
private fun AveragePriceAnalysis(properties: List<Property>) {
    Column(modifier = Modifier.padding(Spacing.screen)) {
        if (properties.isEmpty()) {
            Text("هنوز ملکی ثبت نشده است.")
            return@Column
        }
        val stats = averagePriceStats(properties)
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                StatRow("تعداد املاک", stats.count.toString())
                Spacer(modifier = Modifier.height(10.dp))
                StatRow("میانگین قیمت", "${money(stats.averagePrice)} تومان")
                Spacer(modifier = Modifier.height(10.dp))
                StatRow("میانگین قیمت هر متر", "${money(stats.averagePricePerMeter)} تومان")
            }
        }
    }
}

@Composable
private fun MarketValueAnalysis(properties: List<Property>, propertyId: Long?) {
    val target = properties.find { it.id == propertyId }
    Column(modifier = Modifier.padding(Spacing.screen)) {
        if (target == null) {
            Text("ملکی برای برآورد انتخاب نشده است.")
            return@Column
        }
        Text(target.title, style = MaterialTheme.typography.titleLarge)
        Text("${target.city} · ${target.propertyType.label()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.md))
        val estimate = estimateMarketValue(target, properties)
        if (estimate.comparableCount == 0) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text("ملک مشابهی (همان شهر و نوع ملک) در پایگاه داده شما یافت نشد.")
            }
        } else {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    StatRow("تعداد ملک مشابه", estimate.comparableCount.toString())
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRow("میانگین قیمت هر متر مشابه‌ها", "${money(estimate.averagePricePerMeter)} تومان")
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRow("ارزش تخمینی این ملک", "${money(estimate.estimatedValue)} تومان")
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRow(
                        "اختلاف با میانگین بازار",
                        "${if (estimate.differencePercent >= 0) "+" else ""}${percent(estimate.differencePercent)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun NeighborhoodAnalysis(properties: List<Property>) {
    if (properties.isEmpty()) {
        Text("هنوز ملکی ثبت نشده است.", modifier = Modifier.padding(Spacing.screen))
        return
    }
    val stats = neighborhoodStats(properties)
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(stats, key = { it.city }) { stat ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stat.city, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("تعداد آگهی", stat.count.toString())
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow("میانگین قیمت", "${money(stat.averagePrice)} تومان")
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow("میانگین قیمت هر متر", "${money(stat.averagePricePerMeter)} تومان")
                }
            }
        }
    }
}

@Composable
private fun RankingAnalysis(properties: List<Property>) {
    var metric by remember { mutableStateOf(RankingMetric.MOST_VIEWED) }
    Column(modifier = Modifier.padding(top = Spacing.screen, start = Spacing.screen, end = Spacing.screen)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RankingMetric.entries.forEach { option ->
                FilterChip(selected = metric == option, onClick = { metric = option }, label = { Text(option.label) })
            }
        }
    }
    Spacer(modifier = Modifier.height(Spacing.md))
    val ranked = rankProperties(properties, metric)
    if (ranked.isEmpty()) {
        Text("هنوز ملکی ثبت نشده است.", modifier = Modifier.padding(horizontal = Spacing.screen))
        return
    }
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.screen, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        itemsIndexed(ranked, key = { _, property -> property.id }) { index, property ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(property.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            when (metric) {
                                RankingMetric.MOST_VIEWED -> "${property.viewCount} بازدید"
                                RankingMetric.HIGHEST_PRICE, RankingMetric.LOWEST_PRICE -> formatPrice(property.price, property.dealType)
                                RankingMetric.NEWEST -> property.city
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonAnalysis(properties: List<Property>) {
    var selected by remember { mutableStateOf(setOf<Long>()) }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "دو تا چهار ملک را برای مقایسه انتخاب کنید",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.sm)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(properties, key = { it.id }) { property ->
                val isSelected = selected.contains(property.id)
                AppCard(
                    onClick = {
                        selected = if (isSelected) {
                            selected - property.id
                        } else if (selected.size < 4) {
                            selected + property.id
                        } else {
                            selected
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(property.title, style = MaterialTheme.typography.titleSmall)
                            Text(property.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        val selectedProperties = properties.filter { selected.contains(it.id) }
        if (selectedProperties.size >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(Spacing.screen),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                selectedProperties.forEach { property ->
                    AppCard(modifier = Modifier.width(200.dp)) {
                        Column {
                            Text(property.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(8.dp))
                            ComparisonRow("شهر", property.city)
                            ComparisonRow("نوع", property.propertyType.label())
                            ComparisonRow("قیمت", formatPrice(property.price, property.dealType))
                            ComparisonRow("متراژ", "${property.area.toInt()} متر")
                            ComparisonRow("قیمت هر متر", "${money(calculatePricePerMeter(property.price, property.area))}")
                            ComparisonRow("اتاق", property.rooms.toString())
                            ComparisonRow("بازدید", property.viewCount.toString())
                            ComparisonRow("وضعیت", property.status.label())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
