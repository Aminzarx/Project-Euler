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
import com.realestate.app.data.dealassistant.investmentAnalysisWarnings
import com.realestate.app.data.dealassistant.investmentScore
import com.realestate.app.data.dealassistant.investmentVerdicts
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors

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
    val score = investmentScore(result, inputs)
    val warnings = investmentAnalysisWarnings(inputs)
    val verdictColor = when {
        result.annualizedRoiPercent >= 10 -> MaterialTheme.extendedColors.success
        result.annualizedRoiPercent >= 0 -> MaterialTheme.extendedColors.warning
        else -> MaterialTheme.extendedColors.danger
    }

    Spacer(modifier = Modifier.height(Spacing.lg))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            verdicts.forEachIndexed { index, message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (index == 0) verdictColor else MaterialTheme.colorScheme.onSurface
                )
                if (index != verdicts.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "امتیاز سرمایه‌گذاری: $score از ۱۰۰",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (warnings.isNotEmpty()) {
        Spacer(modifier = Modifier.height(Spacing.sm))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                warnings.forEachIndexed { index, message ->
                    Text("⚠ $message", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extendedColors.warning)
                    if (index != warnings.lastIndex) Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    ResultsCard(
        listOf(
            "قیمت فروش مؤثر" to "${money(result.effectiveSellingPrice)} تومان",
            "سود اسمی (معادل «سود خالص» در امکان‌سنجی ساخت)" to "${money(result.nominalProfit)} تومان",
            "سود واقعی (تعدیل‌شده با تورم)" to "${money(result.realProfit)} تومان",
            "بازده سرمایه (ROI)" to percent(result.roiPercent),
            "بازده سالانه" to percent(result.annualizedRoiPercent),
            "رشد سرمایه" to "×${"%.2f".format(result.capitalGrowthMultiplier)}",
            "ارزش به قدرت خرید امروز" to "${money(result.purchasingPowerAdjustedValue)} تومان"
        ) + (result.breakEvenYear?.let { listOf("سال سربه‌سری" to "%.1f سال".format(it)) } ?: emptyList())
    )

    Spacer(modifier = Modifier.height(Spacing.md))
    CollapsibleSection(title = "تحلیل حساسیت (چه می‌شود اگر...)", subtitle = "تأثیر تغییر فرضیات کلیدی بر بازده") {
        val scenarios = listOf(
            "رشد سالانه ۵ واحد کمتر" to inputs.copy(annualAppreciationPercent = (inputs.annualAppreciationPercent - 5).coerceAtLeast(0.0)),
            "یک سال نگهداری بیشتر" to inputs.copy(holdingYears = inputs.holdingYears + 1),
            "تورم ۵ واحد بیشتر" to inputs.copy(annualInflationPercent = inputs.annualInflationPercent + 5)
        )
        scenarios.forEachIndexed { index, (label, scenarioInputs) ->
            val scenarioResult = calculateInvestmentAnalysis(scenarioInputs)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text(
                    "بازده سالانه: ${percent(scenarioResult.annualizedRoiPercent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (scenarioResult.annualizedRoiPercent >= 0) MaterialTheme.extendedColors.success else MaterialTheme.extendedColors.danger
                )
            }
            if (index != scenarios.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }

    MethodologyNote(
        "اگر قیمت فروش وارد نشده باشد، با فرمول رشد مرکب برآورد می‌شود: قیمت خرید × (۱ + درصد رشد سالانه) ^ سال. " +
            "سود اسمی = قیمت فروش مؤثر − قیمت خرید − مجموع هزینه‌ها (نگهداری، بازسازی، مالیات، هزینه معامله) — از نظر مفهومی همان چیزی است که در ابزار «امکان‌سنجی ساخت‌وساز» «سود خالص» نامیده می‌شود. " +
            "بازده سرمایه (ROI) = سود اسمی ÷ (قیمت خرید + مجموع هزینه‌ها) × ۱۰۰. " +
            "بازده سالانه با فرمول نرخ رشد مرکب سالانه (CAGR) محاسبه می‌شود — این یک برآورد ساده‌شده است، نه محاسبه دقیق نرخ بازده داخلی (IRR). " +
            "سود واقعی با تعدیل تورم به‌دست می‌آید: سود اسمی ÷ (۱ + درصد تورم سالانه) ^ سال. " +
            "ارزش به قدرت خرید امروز = قیمت فروش مؤثر ÷ (۱ + درصد تورم سالانه) ^ سال. " +
            "سال سربه‌سری (در صورت وجود رشد سالانه مثبت) با فرمول لگاریتمی برآورد می‌شود: چند سال طول می‌کشد تا رشد ارزش ملک، سرمایه واقعی وارد‌شده را جبران کند. " +
            "امتیاز سرمایه‌گذاری از ۱۰۰: حداکثر ۷۰ امتیاز از بازده سالانه (سقف در ۲۵٪) و حداکثر ۳۰ امتیاز از فاصلهٔ آن با نرخ تورم — یک معیار مقایسه‌ای شفاف است، نه تضمین مالی."
    )
}
