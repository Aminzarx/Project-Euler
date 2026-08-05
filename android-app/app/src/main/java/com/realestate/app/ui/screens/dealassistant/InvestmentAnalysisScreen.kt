package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.realestate.app.data.dealassistant.InvestmentAnalysisInputs
import com.realestate.app.data.dealassistant.calculateInvestmentAnalysis
import com.realestate.app.data.dealassistant.investmentVerdicts
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.theme.Spacing

@Composable
internal fun InvestmentAnalysisCalculator(prefillPurchasePrice: Long?) {
    var purchasePrice by remember { mutableStateOf(prefillPurchasePrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf("") }
    var holdingYears by remember { mutableStateOf("2") }
    var annualAppreciation by remember { mutableStateOf("15") }
    var annualInflation by remember { mutableStateOf("30") }
    var maintenanceCosts by remember { mutableStateOf("0") }
    var renovationCosts by remember { mutableStateOf("0") }
    var taxes by remember { mutableStateOf("0") }
    var transactionCosts by remember { mutableStateOf("0") }
    var opportunityCost by remember { mutableStateOf("") }

    Text(
        "اگر ملک هنوز فروخته نشده، قیمت فروش را خالی بگذارید تا بر اساس رشد سالانه تخمین زده شود.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.md))

    MoneyField("قیمت خرید (تومان)", purchasePrice, { purchasePrice = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField(
        "قیمت فروش (تومان، اگر فروخته شده)",
        sellingPrice,
        { sellingPrice = it },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("مدت نگهداری", holdingYears, { holdingYears = it }, "سال", modifier = Modifier.weight(1f))
        NumberField("رشد سالانه تخمینی", annualAppreciation, { annualAppreciation = it }, "٪", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(Spacing.md))
    CollapsibleSection(title = "هزینه‌ها و تورم (پیشرفته)", subtitle = "نگهداری، بازسازی، مالیات و مقایسه با تورم") {
        NumberField("نرخ تورم سالانه", annualInflation, { annualInflation = it }, "٪")
        MoneyField("هزینه نگهداری (تومان، کل دوره)", maintenanceCosts, { maintenanceCosts = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.sm))
        MoneyField("هزینه بازسازی (تومان، کل دوره)", renovationCosts, { renovationCosts = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.sm))
        MoneyField("مالیات (تومان، کل دوره)", taxes, { taxes = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.sm))
        MoneyField("هزینه‌های معامله (تومان، کل)", transactionCosts, { transactionCosts = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.sm))
        NumberField(
            "بازده سالانه گزینه جایگزین (اختیاری)",
            opportunityCost,
            { opportunityCost = it },
            "٪"
        )
    }

    val inputs = InvestmentAnalysisInputs(
        purchasePrice = purchasePrice.toAmount(),
        sellingPrice = sellingPrice.toAmount().takeIf { sellingPrice.isNotBlank() },
        holdingYears = holdingYears.toAmount(),
        annualInflationPercent = annualInflation.toAmount(),
        annualAppreciationPercent = annualAppreciation.toAmount(),
        maintenanceCosts = maintenanceCosts.toAmount(),
        renovationCosts = renovationCosts.toAmount(),
        taxes = taxes.toAmount(),
        transactionCosts = transactionCosts.toAmount(),
        opportunityCostPercent = opportunityCost.toAmount().takeIf { opportunityCost.isNotBlank() }
    )
    val result = calculateInvestmentAnalysis(inputs)

    val verdicts = investmentVerdicts(result, inputs)
    Spacer(modifier = Modifier.height(Spacing.lg))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            verdicts.forEachIndexed { index, message ->
                Text(message, style = MaterialTheme.typography.bodyMedium)
                if (index != verdicts.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
    ResultsCard(
        listOf(
            "قیمت فروش مؤثر" to "${money(result.effectiveSellingPrice)} تومان",
            "سود اسمی" to "${money(result.nominalProfit)} تومان",
            "سود واقعی (تعدیل‌شده با تورم)" to "${money(result.realProfit)} تومان",
            "بازده سرمایه (ROI)" to percent(result.roiPercent),
            "بازده سالانه" to percent(result.annualizedRoiPercent),
            "رشد سرمایه" to "×${"%.2f".format(result.capitalGrowthMultiplier)}",
            "ارزش به قدرت خرید امروز" to "${money(result.purchasingPowerAdjustedValue)} تومان"
        ) + (result.breakEvenYear?.let { listOf("سال سربه‌سری" to "%.1f سال".format(it)) } ?: emptyList())
    )
}
