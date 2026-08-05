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
import com.realestate.app.data.dealassistant.ConstructionFeasibilityInputs
import com.realestate.app.data.dealassistant.calculateConstructionFeasibility
import com.realestate.app.data.dealassistant.constructionFeasibilityVerdict
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors

@Composable
internal fun ConstructionFeasibilityCalculator(prefillLandPrice: Long?, prefillLandArea: Double?) {
    var landArea by remember { mutableStateOf(prefillLandArea?.toString() ?: "") }
    var landPrice by remember { mutableStateOf(prefillLandPrice?.toString() ?: "") }
    var farPercent by remember { mutableStateOf("200") }
    var floors by remember { mutableStateOf("4") }
    var totalBuiltAreaOverride by remember { mutableStateOf("") }
    var efficiencyPercent by remember { mutableStateOf("82") }
    var costPerMeter by remember { mutableStateOf("") }
    var permitCost by remember { mutableStateOf("") }
    var engineeringFeePercent by remember { mutableStateOf("3") }
    var municipalityCharges by remember { mutableStateOf("") }
    var insurancePercent by remember { mutableStateOf("0.5") }
    var unexpectedPercent by remember { mutableStateOf("5") }
    var financingCost by remember { mutableStateOf("0") }
    var sellingPricePerMeter by remember { mutableStateOf("") }

    Text(
        "امکان‌سنجی کامل یک پروژه ساخت‌وساز: از قیمت زمین تا سود خالص و بازده سرمایه.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.md))

    Text("زمین و تراکم", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("متراژ زمین", landArea, { landArea = it }, "متر", modifier = Modifier.weight(1f))
    }
    MoneyField("قیمت کل زمین (تومان)", landPrice, { landPrice = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("تراکم مجاز (FAR)", farPercent, { farPercent = it }, "٪", modifier = Modifier.weight(1f))
        NumberField("تعداد طبقات", floors, { floors = it }, modifier = Modifier.weight(1f))
    }

    val autoBuiltArea = landArea.toAmount() * (farPercent.toAmount() / 100.0)
    val totalBuiltArea = totalBuiltAreaOverride.toAmount().takeIf { totalBuiltAreaOverride.isNotBlank() } ?: autoBuiltArea

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(
            "متراژ کل بنا (خودکار، قابل تغییر)",
            if (totalBuiltAreaOverride.isNotBlank()) totalBuiltAreaOverride else "%.0f".format(autoBuiltArea),
            { totalBuiltAreaOverride = it },
            "متر",
            modifier = Modifier.weight(1f)
        )
        NumberField("بازده ساخت (متراژ خالص)", efficiencyPercent, { efficiencyPercent = it }, "٪", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(Spacing.md))
    Text("هزینه ساخت", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField("هزینه ساخت هر متر (تومان)", costPerMeter, { costPerMeter = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField("هزینه پروانه ساخت (تومان، کل)", permitCost, { permitCost = it }, modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(Spacing.md))
    CollapsibleSection(title = "هزینه‌های جانبی (پیشرفته)", subtitle = "حق‌الزحمه مهندسی، عوارض، بیمه، پیش‌بینی‌نشده و تأمین مالی") {
        NumberField("حق‌الزحمه مهندسی (از هزینه ساخت)", engineeringFeePercent, { engineeringFeePercent = it }, "٪")
        MoneyField("عوارض شهرداری (تومان، کل)", municipalityCharges, { municipalityCharges = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.sm))
        NumberField("بیمه (از هزینه ساخت)", insurancePercent, { insurancePercent = it }, "٪")
        NumberField("پیش‌بینی‌نشده", unexpectedPercent, { unexpectedPercent = it }, "٪")
        MoneyField("هزینه تأمین مالی (تومان، اختیاری)", financingCost, { financingCost = it }, modifier = Modifier.fillMaxWidth())
    }

    Spacer(modifier = Modifier.height(Spacing.md))
    Text("فروش", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField(
        "قیمت فروش تخمینی هر متر پس از ساخت (تومان)",
        sellingPricePerMeter,
        { sellingPricePerMeter = it },
        modifier = Modifier.fillMaxWidth()
    )

    val result = calculateConstructionFeasibility(
        ConstructionFeasibilityInputs(
            landArea = landArea.toAmount(),
            landPrice = landPrice.toAmount().toLong(),
            totalBuiltArea = totalBuiltArea,
            efficiencyPercent = efficiencyPercent.toAmount(),
            costPerMeter = costPerMeter.toAmount(),
            permitCost = permitCost.toAmount(),
            engineeringFeePercent = engineeringFeePercent.toAmount(),
            municipalityCharges = municipalityCharges.toAmount(),
            insurancePercent = insurancePercent.toAmount(),
            unexpectedPercent = unexpectedPercent.toAmount(),
            financingCost = financingCost.toAmount(),
            sellingPricePerMeter = sellingPricePerMeter.toAmount()
        )
    )

    Spacer(modifier = Modifier.height(Spacing.lg))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                constructionFeasibilityVerdict(result),
                style = MaterialTheme.typography.titleSmall,
                color = if (result.profitMarginPercent >= 10) {
                    MaterialTheme.extendedColors.success
                } else if (result.profitMarginPercent >= 0) {
                    MaterialTheme.extendedColors.warning
                } else {
                    MaterialTheme.extendedColors.danger
                }
            )
        }
    }
    ResultsCard(
        listOf(
            "متراژ خالص قابل فروش" to "%.0f متر".format(result.netSaleableArea),
            "متراژ مشاعات" to "%.0f متر".format(result.commonArea),
            "هزینه ساخت (Hard Cost)" to "${money(result.hardCost)} تومان",
            "هزینه‌های جانبی (Soft Cost)" to "${money(result.softCosts)} تومان",
            "سرمایه‌گذاری کل" to "${money(result.totalInvestment)} تومان",
            "میانگین قیمت تمام‌شده هر متر" to "${money(result.averageCostPerSaleableMeter)} تومان",
            "درآمد فروش تخمینی" to "${money(result.expectedRevenue)} تومان",
            "سود ناخالص" to "${money(result.grossProfit)} تومان",
            "سود خالص" to "${money(result.netProfit)} تومان",
            "حاشیه سود" to percent(result.profitMarginPercent),
            "بازده سرمایه (ROI)" to percent(result.roiPercent)
        )
    )
}
