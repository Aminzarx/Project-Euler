package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.dealassistant.AreaUnit
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.data.dealassistant.addDays
import com.realestate.app.data.dealassistant.applyPercentChange
import com.realestate.app.data.dealassistant.calculateCommission
import com.realestate.app.data.dealassistant.calculateDepositFromRent
import com.realestate.app.data.dealassistant.calculatePurchaseCost
import com.realestate.app.data.dealassistant.calculateRentalConversion
import com.realestate.app.data.dealassistant.convertArea
import com.realestate.app.data.dealassistant.dateMillisOf
import com.realestate.app.data.dealassistant.daysBetween
import com.realestate.app.data.dealassistant.defaultCommissionRate
import com.realestate.app.data.dealassistant.percentOf
import com.realestate.app.data.dealassistant.whatPercent
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun money(value: Double): String = NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
internal fun percent(value: Double): String = "%.1f٪".format(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCalculatorScreen(
    toolId: DealToolId,
    propertyId: Long?,
    viewModel: DealAssistantViewModel,
    propertyViewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val hasProperty = propertyId != null && propertyId > 0
    val propertyState = if (hasProperty) {
        propertyViewModel.getPropertyById(propertyId!!).collectAsStateWithLifecycle(initialValue = null)
    } else {
        null
    }
    val property = propertyState?.value

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen)
        ) {
            when (toolId) {
                DealToolId.COMMISSION -> CommissionCalculator(property?.price)
                DealToolId.PURCHASE_COST -> PurchaseCostCalculator(property?.price)
                DealToolId.CONSTRUCTION_COST -> ConstructionFeasibilityCalculator(property?.price, property?.area)
                DealToolId.RENTAL_CONVERSION -> RentalConversionCalculator()
                DealToolId.ROI -> InvestmentAnalysisCalculator(property?.price)
                DealToolId.LOAN -> LoanCalculatorFull(property?.price)
                DealToolId.INSTALLMENT -> PaymentPlanBuilder(property?.price)
                DealToolId.AREA_CONVERTER -> AreaConverter(property?.area)
                DealToolId.PERCENTAGE -> PercentageCalculator()
                DealToolId.DATE_CALCULATOR -> DateCalculatorTool()
                else -> Text("این ابزار در دسترس نیست.")
            }
        }
    }
}

@Composable
internal fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, suffix: String? = null, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' }) },
        label = { Text(if (suffix != null) "$label ($suffix)" else label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
}

@Composable
internal fun ResultsCard(rows: List<Pair<String, String>>) {
    Spacer(modifier = Modifier.height(Spacing.md))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            rows.forEachIndexed { index, (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.titleMedium)
                }
                if (index != rows.lastIndex) Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

internal fun String.toAmount(): Double = toDoubleOrNull() ?: 0.0

/** A collapsed-by-default explanation of the formula behind a tool's result — tap to reveal. */
@Composable
internal fun MethodologyNote(text: String) {
    Spacer(modifier = Modifier.height(Spacing.md))
    CollapsibleSection(title = "روش محاسبه", subtitle = "برای دیدن فرمول‌ها لمس کنید") {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The numeric keyboard has no minus key, so "negative" is a toggle rather than something typed. */
@Composable
internal fun IncreaseDecreaseToggle(isIncrease: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isIncrease, onClick = { onChange(true) }, label = { Text("افزایش") })
        FilterChip(selected = !isIncrease, onClick = { onChange(false) }, label = { Text("کاهش") })
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
}

@Composable
private fun CommissionCalculator(prefillPrice: Long?) {
    var price by remember { mutableStateOf(prefillPrice?.toString() ?: "") }
    var rate by remember { mutableStateOf(defaultCommissionRate().toString()) }
    val priceValue = price.toAmount().toLong()
    val rateValue = rate.toAmount()
    val result = calculateCommission(priceValue, rateValue)

    MoneyField("قیمت کل ملک (تومان)", price, { price = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    NumberField("درصد کارمزد هر طرف", rate, { rate = it }, "٪")
    ResultsCard(
        listOf(
            "کارمزد هر طرف" to "${money(result.perSideShare)} تومان",
            "مجموع کارمزد" to "${money(result.total)} تومان"
        )
    )
    MethodologyNote("کارمزد هر طرف = قیمت کل ملک × درصد کارمزد ÷ ۱۰۰. چون معمولاً هم خریدار و هم فروشنده کارمزد پرداخت می‌کنند، مجموع کارمزد دو برابر سهم هر طرف است.")
}

@Composable
private fun PurchaseCostCalculator(prefillPrice: Long?) {
    var price by remember { mutableStateOf(prefillPrice?.toString() ?: "") }
    var commission by remember { mutableStateOf("0.5") }
    var tax by remember { mutableStateOf("5") }
    var registration by remember { mutableStateOf("1") }
    var transfer by remember { mutableStateOf("1") }

    MoneyField("قیمت کل ملک (تومان)", price, { price = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    NumberField("کارمزد", commission, { commission = it }, "٪")
    NumberField("مالیات نقل و انتقال", tax, { tax = it }, "٪")
    NumberField("هزینه ثبت دفترخانه", registration, { registration = it }, "٪")
    NumberField("هزینه انتقال", transfer, { transfer = it }, "٪")

    val result = calculatePurchaseCost(
        price.toAmount().toLong(),
        commission.toAmount(),
        tax.toAmount(),
        registration.toAmount(),
        transfer.toAmount()
    )
    ResultsCard(
        listOf(
            "کارمزد" to "${money(result.commission)} تومان",
            "مالیات" to "${money(result.tax)} تومان",
            "هزینه ثبت" to "${money(result.registration)} تومان",
            "هزینه انتقال" to "${money(result.transfer)} تومان",
            "هزینه تخمینی کل خرید" to "${money(result.total)} تومان"
        )
    )
    MethodologyNote("هر یک از هزینه‌ها (کارمزد، مالیات، ثبت، انتقال) به‌صورت درصدی از قیمت ملک محاسبه می‌شود. هزینه تخمینی کل خرید = قیمت ملک + مجموع این هزینه‌ها.")
}

private val rentalConversionPresets = listOf(2.0, 2.5, 3.0, 3.5)

@Composable
private fun RentalConversionCalculator() {
    var fullDeposit by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("3") }
    var convertAmount by remember { mutableStateOf("") }

    Text(
        "این ابزار مبلغی از رهن کامل را با نرخ تبدیل ماهانه به اجاره تبدیل می‌کند — روش رایج «رهن به اجاره» در معاملات ملکی. نرخ رایج معمولاً بین ۲ تا ۳.۵ درصد در ماه است؛ می‌توانید عدد دلخواه خودتان را هم وارد کنید.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField("رهن کامل (تومان)", fullDeposit, { fullDeposit = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rentalConversionPresets.forEach { preset ->
            FilterChip(
                selected = rate.toAmount() == preset,
                onClick = { rate = preset.toString() },
                label = { Text("${"%.1f".format(preset)}٪") }
            )
        }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    NumberField("نرخ تبدیل ماهانه", rate, { rate = it }, "٪")
    MoneyField("مبلغ تبدیل‌شونده به اجاره (تومان)", convertAmount, { convertAmount = it }, modifier = Modifier.fillMaxWidth())

    val result = calculateRentalConversion(fullDeposit.toAmount(), rate.toAmount(), convertAmount.toAmount())
    ResultsCard(
        listOf(
            "اجاره ماهانه" to "${money(result.monthlyRent)} تومان",
            "رهن باقی‌مانده" to "${money(result.remainingDeposit)} تومان"
        )
    )

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("محاسبه برعکس: از اجاره ماهانه به رهن معادل", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    var monthlyRent by remember { mutableStateOf("") }
    MoneyField("اجاره ماهانه (تومان)", monthlyRent, { monthlyRent = it }, modifier = Modifier.fillMaxWidth())
    ResultsCard(listOf("رهن معادل" to "${money(calculateDepositFromRent(monthlyRent.toAmount(), rate.toAmount()))} تومان"))

    MethodologyNote("اجاره ماهانه = مبلغ تبدیل‌شونده × نرخ تبدیل ماهانه ÷ ۱۰۰. رهن باقی‌مانده = رهن کامل − مبلغ تبدیل‌شونده. در محاسبه برعکس نیز از همین رابطه استفاده می‌شود: رهن معادل = اجاره ماهانه × ۱۰۰ ÷ نرخ تبدیل.")
}

@Composable
private fun AreaConverter(prefillArea: Double?) {
    var value by remember { mutableStateOf(prefillArea?.toString() ?: "") }
    var fromUnit by remember { mutableStateOf(AreaUnit.SQUARE_METER) }

    NumberField("مقدار", value, { value = it })
    Text("واحد مبدأ", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AreaUnit.entries.forEach { unit ->
            FilterChip(selected = unit == fromUnit, onClick = { fromUnit = unit }, label = { Text(unit.label) })
        }
    }
    Spacer(modifier = Modifier.height(Spacing.md))

    val amount = value.toAmount()
    ResultsCard(
        AreaUnit.entries.filter { it != fromUnit }.map { target ->
            target.label to "%.2f".format(convertArea(amount, fromUnit, target))
        }
    )
}

@Composable
private fun PercentageCalculator() {
    var percentValue by remember { mutableStateOf("") }
    var ofValue by remember { mutableStateOf("") }
    NumberField("چند درصد", percentValue, { percentValue = it }, "٪")
    NumberField("از عدد", ofValue, { ofValue = it })
    ResultsCard(listOf("نتیجه" to money(percentOf(percentValue.toAmount(), ofValue.toAmount()))))

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("چند درصد از عدد دیگر است؟", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    var part by remember { mutableStateOf("") }
    var whole by remember { mutableStateOf("") }
    NumberField("عدد", part, { part = it })
    NumberField("از عدد", whole, { whole = it })
    ResultsCard(listOf("درصد" to percent(whatPercent(part.toAmount(), whole.toAmount()))))

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("افزایش یا کاهش درصدی", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    var base by remember { mutableStateOf("") }
    var change by remember { mutableStateOf("") }
    var isIncrease by remember { mutableStateOf(true) }
    NumberField("عدد پایه", base, { base = it })
    IncreaseDecreaseToggle(isIncrease, { isIncrease = it })
    NumberField("درصد تغییر", change, { change = it }, "٪")
    val signedChange = if (isIncrease) change.toAmount() else -change.toAmount()
    ResultsCard(listOf("نتیجه" to money(applyPercentChange(base.toAmount(), signedChange))))
}

@Composable
private fun DateCalculatorTool() {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    var startYear by remember { mutableStateOf("") }
    var startMonth by remember { mutableStateOf("") }
    var startDay by remember { mutableStateOf("") }
    var endYear by remember { mutableStateOf("") }
    var endMonth by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("") }

    Text("فاصله بین دو تاریخ", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberFieldCompact("سال", startYear) { startYear = it }
        NumberFieldCompact("ماه", startMonth) { startMonth = it }
        NumberFieldCompact("روز", startDay) { startDay = it }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberFieldCompact("سال", endYear) { endYear = it }
        NumberFieldCompact("ماه", endMonth) { endMonth = it }
        NumberFieldCompact("روز", endDay) { endDay = it }
    }

    val canCompute = listOf(startYear, startMonth, startDay, endYear, endMonth, endDay).all { it.isNotBlank() }
    if (canCompute) {
        val start = dateMillisOf(startYear.toAmount().toInt(), startMonth.toAmount().toInt(), startDay.toAmount().toInt())
        val end = dateMillisOf(endYear.toAmount().toInt(), endMonth.toAmount().toInt(), endDay.toAmount().toInt())
        ResultsCard(listOf("فاصله" to "${daysBetween(start, end)} روز"))
    }

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("افزودن یا کاستن روز از یک تاریخ", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    var baseYear by remember { mutableStateOf("") }
    var baseMonth by remember { mutableStateOf("") }
    var baseDay by remember { mutableStateOf("") }
    var daysToAdd by remember { mutableStateOf("") }
    var isForward by remember { mutableStateOf(true) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberFieldCompact("سال", baseYear) { baseYear = it }
        NumberFieldCompact("ماه", baseMonth) { baseMonth = it }
        NumberFieldCompact("روز", baseDay) { baseDay = it }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isForward, onClick = { isForward = true }, label = { Text("جلو رفتن") })
        FilterChip(selected = !isForward, onClick = { isForward = false }, label = { Text("عقب رفتن") })
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    NumberField("تعداد روز", daysToAdd, { daysToAdd = it })

    if (listOf(baseYear, baseMonth, baseDay).all { it.isNotBlank() } && daysToAdd.isNotBlank()) {
        val base = dateMillisOf(baseYear.toAmount().toInt(), baseMonth.toAmount().toInt(), baseDay.toAmount().toInt())
        val signedDays = if (isForward) daysToAdd.toAmount().toInt() else -daysToAdd.toAmount().toInt()
        val result = addDays(base, signedDays)
        ResultsCard(listOf("تاریخ نتیجه" to formatter.format(Date(result))))
    }
}

@Composable
internal fun NumberFieldCompact(label: String, value: String, modifier: Modifier = Modifier.width(90.dp), onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
