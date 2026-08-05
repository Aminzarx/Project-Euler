package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.realestate.app.data.dealassistant.calculateConstructionCost
import com.realestate.app.data.dealassistant.calculateDepositFromRent
import com.realestate.app.data.dealassistant.calculateInstallment
import com.realestate.app.data.dealassistant.calculateLoan
import com.realestate.app.data.dealassistant.calculatePricePerMeter
import com.realestate.app.data.dealassistant.calculatePurchaseCost
import com.realestate.app.data.dealassistant.calculateRentalConversion
import com.realestate.app.data.dealassistant.calculateRoi
import com.realestate.app.data.dealassistant.calculateTotalFromPricePerMeter
import com.realestate.app.data.dealassistant.convertArea
import com.realestate.app.data.dealassistant.dateMillisOf
import com.realestate.app.data.dealassistant.daysBetween
import com.realestate.app.data.dealassistant.defaultCommissionRate
import com.realestate.app.data.dealassistant.percentOf
import com.realestate.app.data.dealassistant.whatPercent
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun money(value: Double): String = NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
private fun percent(value: Double): String = "%.1f٪".format(value)

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
                DealToolId.PRICE_PER_METER -> PricePerMeterCalculator(property?.price, property?.area)
                DealToolId.PURCHASE_COST -> PurchaseCostCalculator(property?.price)
                DealToolId.CONSTRUCTION_COST -> ConstructionCostCalculator(property?.price, property?.area)
                DealToolId.RENTAL_CONVERSION -> RentalConversionCalculator()
                DealToolId.ROI -> RoiCalculator()
                DealToolId.LOAN -> LoanCalculator()
                DealToolId.INSTALLMENT -> InstallmentCalculator(property?.price)
                DealToolId.AREA_CONVERTER -> AreaConverter(property?.area)
                DealToolId.PERCENTAGE -> PercentageCalculator()
                DealToolId.DATE_CALCULATOR -> DateCalculatorTool()
                else -> Text("این ابزار در دسترس نیست.")
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, suffix: String? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' }) },
        label = { Text(if (suffix != null) "$label ($suffix)" else label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
}

@Composable
private fun ResultsCard(rows: List<Pair<String, String>>) {
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

private fun String.toAmount(): Double = toDoubleOrNull() ?: 0.0

/** The numeric keyboard has no minus key, so "negative" is a toggle rather than something typed. */
@Composable
private fun IncreaseDecreaseToggle(isIncrease: Boolean, onChange: (Boolean) -> Unit) {
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

    NumberField("قیمت ملک", price, { price = it }, "تومان")
    NumberField("درصد کارمزد هر طرف", rate, { rate = it }, "٪")
    ResultsCard(
        listOf(
            "کارمزد هر طرف" to "${money(result.perSideShare)} تومان",
            "مجموع کارمزد" to "${money(result.total)} تومان"
        )
    )
}

@Composable
private fun PricePerMeterCalculator(prefillPrice: Long?, prefillArea: Double?) {
    var price by remember { mutableStateOf(prefillPrice?.toString() ?: "") }
    var area by remember { mutableStateOf(prefillArea?.toString() ?: "") }
    val priceValue = price.toAmount().toLong()
    val areaValue = area.toAmount()
    val perMeter = calculatePricePerMeter(priceValue, areaValue)

    NumberField("قیمت کل", price, { price = it }, "تومان")
    NumberField("متراژ", area, { area = it }, "متر")
    ResultsCard(listOf("قیمت هر متر" to "${money(perMeter)} تومان"))

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("محاسبه برعکس: از قیمت هر متر به قیمت کل", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    var reversePerMeter by remember { mutableStateOf("") }
    var reverseArea by remember { mutableStateOf(prefillArea?.toString() ?: "") }
    NumberField("قیمت هر متر", reversePerMeter, { reversePerMeter = it }, "تومان")
    NumberField("متراژ", reverseArea, { reverseArea = it }, "متر")
    ResultsCard(
        listOf(
            "قیمت کل" to "${money(calculateTotalFromPricePerMeter(reversePerMeter.toAmount(), reverseArea.toAmount()))} تومان"
        )
    )
}

@Composable
private fun PurchaseCostCalculator(prefillPrice: Long?) {
    var price by remember { mutableStateOf(prefillPrice?.toString() ?: "") }
    var commission by remember { mutableStateOf("0.5") }
    var tax by remember { mutableStateOf("5") }
    var registration by remember { mutableStateOf("1") }
    var transfer by remember { mutableStateOf("1") }

    NumberField("قیمت ملک", price, { price = it }, "تومان")
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
}

@Composable
private fun ConstructionCostCalculator(prefillLandPrice: Long?, prefillArea: Double?) {
    var landPrice by remember { mutableStateOf(prefillLandPrice?.toString() ?: "") }
    var builtArea by remember { mutableStateOf(prefillArea?.toString() ?: "") }
    var costPerMeter by remember { mutableStateOf("") }
    var sellingPricePerMeter by remember { mutableStateOf("") }

    NumberField("قیمت زمین", landPrice, { landPrice = it }, "تومان")
    NumberField("متراژ بنا", builtArea, { builtArea = it }, "متر")
    NumberField("هزینه ساخت هر متر", costPerMeter, { costPerMeter = it }, "تومان")
    NumberField("قیمت فروش تخمینی هر متر پس از ساخت", sellingPricePerMeter, { sellingPricePerMeter = it }, "تومان")

    val result = calculateConstructionCost(
        landPrice.toAmount().toLong(),
        builtArea.toAmount(),
        costPerMeter.toAmount(),
        sellingPricePerMeter.toAmount()
    )
    ResultsCard(
        listOf(
            "هزینه ساخت" to "${money(result.constructionCost)} تومان",
            "سرمایه‌گذاری کل" to "${money(result.totalInvestment)} تومان",
            "ارزش فروش تخمینی" to "${money(result.estimatedSellingValue)} تومان",
            "سود تخمینی" to "${money(result.profit)} تومان",
            "درصد سود" to percent(result.profitPercent)
        )
    )
}

@Composable
private fun RentalConversionCalculator() {
    var fullDeposit by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("3") }
    var convertAmount by remember { mutableStateOf("") }

    NumberField("رهن کامل", fullDeposit, { fullDeposit = it }, "تومان")
    NumberField("نرخ تبدیل ماهانه", rate, { rate = it }, "٪")
    NumberField("مبلغ تبدیل‌شونده به اجاره", convertAmount, { convertAmount = it }, "تومان")

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
    NumberField("اجاره ماهانه", monthlyRent, { monthlyRent = it }, "تومان")
    ResultsCard(listOf("رهن معادل" to "${money(calculateDepositFromRent(monthlyRent.toAmount(), rate.toAmount()))} تومان"))
}

@Composable
private fun RoiCalculator() {
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("1") }

    NumberField("قیمت خرید", purchasePrice, { purchasePrice = it }, "تومان")
    NumberField("قیمت فروش", sellingPrice, { sellingPrice = it }, "تومان")
    NumberField("مدت نگهداری", years, { years = it }, "سال")

    val result = calculateRoi(purchasePrice.toAmount(), sellingPrice.toAmount(), years.toAmount())
    ResultsCard(
        listOf(
            "سود" to "${money(result.profit)} تومان",
            "بازده سرمایه (ROI)" to percent(result.roiPercent),
            "بازده سالانه" to percent(result.annualReturnPercent),
            "رشد سرمایه" to "×${"%.2f".format(result.growthMultiplier)}"
        )
    )
}

@Composable
private fun LoanCalculator() {
    var principal by remember { mutableStateOf("") }
    var annualRate by remember { mutableStateOf("18") }
    var months by remember { mutableStateOf("12") }

    NumberField("مبلغ وام", principal, { principal = it }, "تومان")
    NumberField("نرخ سود سالانه", annualRate, { annualRate = it }, "٪")
    NumberField("مدت بازپرداخت", months, { months = it }, "ماه")

    val result = calculateLoan(principal.toAmount(), annualRate.toAmount(), months.toAmount().toInt())
    ResultsCard(
        listOf(
            "قسط ماهانه" to "${money(result.monthlyPayment)} تومان",
            "کل بازپرداخت" to "${money(result.totalPayment)} تومان",
            "کل سود" to "${money(result.totalInterest)} تومان"
        )
    )
}

@Composable
private fun InstallmentCalculator(prefillPrice: Long?) {
    var totalPrice by remember { mutableStateOf(prefillPrice?.toString() ?: "") }
    var downPayment by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("12") }
    var rate by remember { mutableStateOf("0") }

    NumberField("قیمت کل", totalPrice, { totalPrice = it }, "تومان")
    NumberField("پیش‌پرداخت", downPayment, { downPayment = it }, "تومان")
    NumberField("تعداد اقساط", count, { count = it })
    NumberField("نرخ سود سالانه (در صورت وجود)", rate, { rate = it }, "٪")

    val result = calculateInstallment(totalPrice.toAmount().toLong(), downPayment.toAmount(), count.toAmount().toInt(), rate.toAmount())
    ResultsCard(
        listOf(
            "مبلغ هر قسط" to "${money(result.installmentAmount)} تومان",
            "مجموع قابل پرداخت" to "${money(result.totalPayable)} تومان"
        )
    )
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
private fun NumberFieldCompact(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.width(90.dp)
    )
}
