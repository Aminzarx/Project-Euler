package com.realestate.app.data.dealassistant

import com.realestate.app.data.Property
import java.util.Calendar
import kotlin.math.ln
import kotlin.math.pow

// ---------- Financial ----------

data class CommissionResult(val perSideShare: Double, val total: Double)

/** Commission is charged to both sides of the deal at the same rate. */
fun calculateCommission(price: Long, ratePercent: Double): CommissionResult {
    val share = price * (ratePercent / 100.0)
    return CommissionResult(perSideShare = share, total = share * 2)
}

fun calculatePricePerMeter(price: Long, area: Double): Double = if (area > 0) price / area else 0.0

fun calculateTotalFromPricePerMeter(pricePerMeter: Double, area: Double): Double = pricePerMeter * area

data class PurchaseCostResult(
    val commission: Double,
    val tax: Double,
    val registration: Double,
    val transfer: Double,
    val total: Double
)

fun calculatePurchaseCost(
    price: Long,
    commissionPercent: Double,
    taxPercent: Double,
    registrationPercent: Double,
    transferPercent: Double
): PurchaseCostResult {
    val commission = price * commissionPercent / 100.0
    val tax = price * taxPercent / 100.0
    val registration = price * registrationPercent / 100.0
    val transfer = price * transferPercent / 100.0
    return PurchaseCostResult(
        commission = commission,
        tax = tax,
        registration = registration,
        transfer = transfer,
        total = price + commission + tax + registration + transfer
    )
}

/** Everything a construction-feasibility study needs, gathered in one place. */
data class ConstructionFeasibilityInputs(
    val landArea: Double,
    val landPrice: Long,
    val totalBuiltArea: Double,
    val efficiencyPercent: Double,
    val costPerMeter: Double,
    val permitCost: Double,
    val engineeringFeePercent: Double,
    val municipalityCharges: Double,
    val insurancePercent: Double,
    val unexpectedPercent: Double,
    val financingCost: Double,
    val sellingPricePerMeter: Double
)

data class ConstructionFeasibilityResult(
    val netSaleableArea: Double,
    val commonArea: Double,
    val hardCost: Double,
    val engineeringFees: Double,
    val insurance: Double,
    val unexpectedCosts: Double,
    val softCosts: Double,
    val totalInvestment: Double,
    val averageCostPerSaleableMeter: Double,
    val expectedRevenue: Double,
    val grossProfit: Double,
    val netProfit: Double,
    val profitMarginPercent: Double,
    val roiPercent: Double
)

fun calculateConstructionFeasibility(inputs: ConstructionFeasibilityInputs): ConstructionFeasibilityResult {
    val netSaleable = inputs.totalBuiltArea * (inputs.efficiencyPercent / 100.0)
    val common = inputs.totalBuiltArea - netSaleable
    val hardCost = inputs.totalBuiltArea * inputs.costPerMeter
    val engineeringFees = hardCost * (inputs.engineeringFeePercent / 100.0)
    val insurance = hardCost * (inputs.insurancePercent / 100.0)
    val preUnexpected = inputs.permitCost + engineeringFees + inputs.municipalityCharges + insurance + inputs.financingCost
    val unexpected = (hardCost + preUnexpected) * (inputs.unexpectedPercent / 100.0)
    val softCosts = preUnexpected + unexpected
    val totalInvestment = inputs.landPrice + hardCost + softCosts
    val avgCostPerSaleable = if (netSaleable > 0) totalInvestment / netSaleable else 0.0
    val revenue = netSaleable * inputs.sellingPricePerMeter
    val grossProfit = revenue - (inputs.landPrice + hardCost)
    val netProfit = revenue - totalInvestment
    val margin = if (revenue > 0) netProfit / revenue * 100.0 else 0.0
    val roi = if (totalInvestment > 0) netProfit / totalInvestment * 100.0 else 0.0
    return ConstructionFeasibilityResult(
        netSaleableArea = netSaleable,
        commonArea = common,
        hardCost = hardCost,
        engineeringFees = engineeringFees,
        insurance = insurance,
        unexpectedCosts = unexpected,
        softCosts = softCosts,
        totalInvestment = totalInvestment,
        averageCostPerSaleableMeter = avgCostPerSaleable,
        expectedRevenue = revenue,
        grossProfit = grossProfit,
        netProfit = netProfit,
        profitMarginPercent = margin,
        roiPercent = roi
    )
}

fun constructionFeasibilityVerdict(result: ConstructionFeasibilityResult): String = when {
    result.profitMarginPercent >= 25 -> "این پروژه حاشیه سود بالایی دارد و از نظر مالی جذاب به‌نظر می‌رسد."
    result.profitMarginPercent >= 10 -> "حاشیه سود این پروژه در محدوده معقول قرار دارد."
    result.profitMarginPercent >= 0 -> "حاشیه سود این پروژه پایین است؛ فرضیات هزینه و قیمت فروش را دوباره بررسی کنید."
    else -> "بر اساس اعداد وارد‌شده، این پروژه با قیمت فروش فعلی زیان‌ده است."
}

data class RentalConversionResult(val monthlyRent: Double, val remainingDeposit: Double)

/** Converts part of a full mortgage deposit (رهن کامل) into monthly rent at a given monthly rate. */
fun calculateRentalConversion(fullDeposit: Double, monthlyRatePercent: Double, amountToConvert: Double): RentalConversionResult {
    val rent = amountToConvert * monthlyRatePercent / 100.0
    return RentalConversionResult(monthlyRent = rent, remainingDeposit = fullDeposit - amountToConvert)
}

/** Reverse: the deposit equivalent of a target monthly rent at the given rate. */
fun calculateDepositFromRent(monthlyRent: Double, monthlyRatePercent: Double): Double =
    if (monthlyRatePercent > 0) monthlyRent * 100.0 / monthlyRatePercent else 0.0

data class InvestmentAnalysisInputs(
    val purchasePrice: Double,
    /** Null means "not sold yet" — the effective selling price is projected from [annualAppreciationPercent]. */
    val sellingPrice: Double?,
    val holdingYears: Double,
    val annualInflationPercent: Double,
    val annualAppreciationPercent: Double,
    val maintenanceCosts: Double,
    val renovationCosts: Double,
    val taxes: Double,
    val transactionCosts: Double,
    /** Annual return of an alternative investment, for a side-by-side comparison. Null = skip. */
    val opportunityCostPercent: Double?
)

data class InvestmentAnalysisResult(
    val effectiveSellingPrice: Double,
    val totalCosts: Double,
    val nominalProfit: Double,
    val realProfit: Double,
    val roiPercent: Double,
    val annualizedRoiPercent: Double,
    val capitalGrowthMultiplier: Double,
    val purchasingPowerAdjustedValue: Double,
    val breakEvenYear: Double?
)

fun calculateInvestmentAnalysis(inputs: InvestmentAnalysisInputs): InvestmentAnalysisResult {
    val years = inputs.holdingYears.coerceAtLeast(0.0)
    val effectiveSellingPrice = inputs.sellingPrice
        ?: inputs.purchasePrice * (1 + inputs.annualAppreciationPercent / 100.0).pow(years)
    val totalCosts = inputs.maintenanceCosts + inputs.renovationCosts + inputs.taxes + inputs.transactionCosts
    val nominalProfit = effectiveSellingPrice - inputs.purchasePrice - totalCosts
    val investedCapital = inputs.purchasePrice + totalCosts
    val roi = if (investedCapital > 0) nominalProfit / investedCapital * 100.0 else 0.0
    val annualizedRoi = if (years > 0 && inputs.purchasePrice > 0) {
        (((effectiveSellingPrice - totalCosts) / inputs.purchasePrice).pow(1.0 / years) - 1) * 100.0
    } else {
        roi
    }
    val inflationFactor = (1 + inputs.annualInflationPercent / 100.0).pow(years)
    val realProfit = if (inflationFactor > 0) nominalProfit / inflationFactor else nominalProfit
    val growth = if (inputs.purchasePrice > 0) effectiveSellingPrice / inputs.purchasePrice else 0.0
    val purchasingPowerValue = if (inflationFactor > 0) effectiveSellingPrice / inflationFactor else effectiveSellingPrice
    val breakEven = if (inputs.annualAppreciationPercent > 0 && inputs.purchasePrice > 0 && investedCapital > 0) {
        val ratio = investedCapital / inputs.purchasePrice
        if (ratio > 0) ln(ratio) / ln(1 + inputs.annualAppreciationPercent / 100.0) else null
    } else {
        null
    }
    return InvestmentAnalysisResult(
        effectiveSellingPrice = effectiveSellingPrice,
        totalCosts = totalCosts,
        nominalProfit = nominalProfit,
        realProfit = realProfit,
        roiPercent = roi,
        annualizedRoiPercent = annualizedRoi,
        capitalGrowthMultiplier = growth,
        purchasingPowerAdjustedValue = purchasingPowerValue,
        breakEvenYear = breakEven
    )
}

fun investmentVerdicts(result: InvestmentAnalysisResult, inputs: InvestmentAnalysisInputs): List<String> {
    val messages = mutableListOf<String>()
    messages += when {
        result.annualizedRoiPercent >= 20 -> "بازده سالانه این سرمایه‌گذاری بسیار مطلوب است."
        result.annualizedRoiPercent >= 10 -> "بازده سالانه این سرمایه‌گذاری معقول است."
        result.annualizedRoiPercent >= 0 -> "بازده سالانه این سرمایه‌گذاری پایین است."
        else -> "این سرمایه‌گذاری بر اساس فرضیات فعلی زیان‌ده است."
    }
    if (result.annualizedRoiPercent < inputs.annualInflationPercent) {
        messages += "بازده سالانه کمتر از نرخ تورم است؛ قدرت خرید سرمایه در عمل کاهش می‌یابد."
    }
    val opportunityCost = inputs.opportunityCostPercent
    if (opportunityCost != null) {
        messages += if (result.annualizedRoiPercent >= opportunityCost) {
            "بازده این ملک از گزینه جایگزین (%.1f٪) بهتر است.".format(opportunityCost)
        } else {
            "بازده این ملک از گزینه جایگزین (%.1f٪) کمتر است.".format(opportunityCost)
        }
    }
    return messages
}

enum class RepaymentMethod(val label: String) {
    EQUAL_INSTALLMENT("اقساط مساوی"),
    EQUAL_PRINCIPAL("اصل ثابت")
}

data class AmortizationRow(val period: Int, val payment: Double, val principal: Double, val interest: Double, val balance: Double)

data class LoanScheduleResult(
    val firstPayment: Double,
    val totalPayment: Double,
    val totalInterest: Double,
    val schedule: List<AmortizationRow>
)

fun calculateLoanSchedule(
    principal: Double,
    annualRatePercent: Double,
    months: Int,
    gracePeriodMonths: Int = 0,
    method: RepaymentMethod = RepaymentMethod.EQUAL_INSTALLMENT
): LoanScheduleResult {
    if (principal <= 0 || months <= 0) return LoanScheduleResult(0.0, 0.0, 0.0, emptyList())
    val monthlyRate = annualRatePercent / 100.0 / 12.0
    val schedule = mutableListOf<AmortizationRow>()
    var balance = principal
    var totalInterest = 0.0
    var totalPayment = 0.0

    for (m in 1..gracePeriodMonths) {
        val interest = balance * monthlyRate
        schedule += AmortizationRow(m, interest, 0.0, interest, balance)
        totalInterest += interest
        totalPayment += interest
    }

    val remainingMonths = (months - gracePeriodMonths).coerceAtLeast(1)
    when (method) {
        RepaymentMethod.EQUAL_INSTALLMENT -> {
            val payment = if (monthlyRate == 0.0) {
                principal / remainingMonths
            } else {
                val factor = (1 + monthlyRate).pow(remainingMonths.toDouble())
                principal * monthlyRate * factor / (factor - 1)
            }
            repeat(remainingMonths) { index ->
                val interest = balance * monthlyRate
                val principalPaid = (payment - interest).coerceAtMost(balance)
                balance = (balance - principalPaid).coerceAtLeast(0.0)
                schedule += AmortizationRow(gracePeriodMonths + index + 1, payment, principalPaid, interest, balance)
                totalInterest += interest
                totalPayment += payment
            }
        }
        RepaymentMethod.EQUAL_PRINCIPAL -> {
            val principalPerMonth = principal / remainingMonths
            repeat(remainingMonths) { index ->
                val interest = balance * monthlyRate
                val payment = principalPerMonth + interest
                balance = (balance - principalPerMonth).coerceAtLeast(0.0)
                schedule += AmortizationRow(gracePeriodMonths + index + 1, payment, principalPerMonth, interest, balance)
                totalInterest += interest
                totalPayment += payment
            }
        }
    }

    val firstRealPayment = schedule.getOrNull(gracePeriodMonths)?.payment ?: schedule.firstOrNull()?.payment ?: 0.0
    return LoanScheduleResult(
        firstPayment = firstRealPayment,
        totalPayment = totalPayment,
        totalInterest = totalInterest,
        schedule = schedule
    )
}

data class EarlyRepaymentComparison(
    val originalTotalInterest: Double,
    val newTotalInterest: Double,
    val interestSaved: Double,
    val monthsSaved: Int
)

/** Simulates paying one extra lump sum at a given month, holding the loan's actual repayment method and grace period fixed, and letting the loan finish early instead. */
fun simulateEarlyRepayment(
    principal: Double,
    annualRatePercent: Double,
    months: Int,
    extraPaymentMonth: Int,
    extraPaymentAmount: Double,
    gracePeriodMonths: Int = 0,
    method: RepaymentMethod = RepaymentMethod.EQUAL_INSTALLMENT
): EarlyRepaymentComparison {
    val original = calculateLoanSchedule(principal, annualRatePercent, months, gracePeriodMonths, method)
    if (extraPaymentAmount <= 0 || extraPaymentMonth <= 0 || extraPaymentMonth > months) {
        return EarlyRepaymentComparison(original.totalInterest, original.totalInterest, 0.0, 0)
    }
    val monthlyRate = annualRatePercent / 100.0 / 12.0
    val remainingMonths = (months - gracePeriodMonths).coerceAtLeast(1)
    val equalInstallmentPayment = if (monthlyRate == 0.0) {
        principal / remainingMonths
    } else {
        val factor = (1 + monthlyRate).pow(remainingMonths.toDouble())
        principal * monthlyRate * factor / (factor - 1)
    }
    val principalPerMonth = principal / remainingMonths

    var balance = principal
    var totalInterest = 0.0
    var monthCount = 0
    while (balance > 0.01 && monthCount < months * 2) {
        monthCount++
        val interest = balance * monthlyRate
        if (monthCount > gracePeriodMonths) {
            val principalPaid = when (method) {
                RepaymentMethod.EQUAL_INSTALLMENT -> (equalInstallmentPayment - interest).coerceAtMost(balance)
                RepaymentMethod.EQUAL_PRINCIPAL -> principalPerMonth.coerceAtMost(balance)
            }
            balance -= principalPaid
        }
        totalInterest += interest
        if (monthCount == extraPaymentMonth) {
            balance -= extraPaymentAmount.coerceAtMost(balance)
        }
        if (balance < 0) balance = 0.0
    }
    return EarlyRepaymentComparison(
        originalTotalInterest = original.totalInterest,
        newTotalInterest = totalInterest,
        interestSaved = original.totalInterest - totalInterest,
        monthsSaved = months - monthCount
    )
}

// ---------- Utilities ----------

enum class AreaUnit(val label: String, val toSquareMeters: Double) {
    SQUARE_METER("متر مربع", 1.0),
    HECTARE("هکتار", 10_000.0),
    JARIB("جریب", 1_000.0),
    SQUARE_FOOT("فوت مربع", 0.092903)
}

fun convertArea(value: Double, from: AreaUnit, to: AreaUnit): Double = value * from.toSquareMeters / to.toSquareMeters

fun percentOf(percent: Double, of: Double): Double = of * percent / 100.0

fun whatPercent(part: Double, of: Double): Double = if (of != 0.0) part / of * 100.0 else 0.0

fun applyPercentChange(base: Double, percent: Double): Double = base * (1 + percent / 100.0)

fun daysBetween(startMillis: Long, endMillis: Long): Long = (endMillis - startMillis) / (24 * 60 * 60 * 1000)

fun addDays(baseMillis: Long, days: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = baseMillis
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar.timeInMillis
}

fun dateMillisOf(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// ---------- Property analysis (derived straight from the agent's own listings) ----------

data class PriceStats(val averagePrice: Double, val averagePricePerMeter: Double, val count: Int)

fun averagePriceStats(properties: List<Property>): PriceStats {
    if (properties.isEmpty()) return PriceStats(0.0, 0.0, 0)
    val avgPrice = properties.map { it.price.toDouble() }.average()
    val perMeterValues = properties.filter { it.area > 0 }.map { it.price / it.area }
    val avgPerMeter = if (perMeterValues.isNotEmpty()) perMeterValues.average() else 0.0
    return PriceStats(averagePrice = avgPrice, averagePricePerMeter = avgPerMeter, count = properties.size)
}

data class MarketValueEstimate(val comparableCount: Int, val averagePricePerMeter: Double, val estimatedValue: Double, val differencePercent: Double)

/** Estimates a property's value from other listings of the same city + type, excluding itself. */
fun estimateMarketValue(target: Property, allProperties: List<Property>): MarketValueEstimate {
    val comparables = allProperties.filter {
        it.id != target.id && it.city == target.city && it.propertyType == target.propertyType && it.area > 0
    }
    if (comparables.isEmpty()) return MarketValueEstimate(0, 0.0, 0.0, 0.0)
    val avgPerMeter = comparables.map { it.price / it.area }.average()
    val estimatedValue = avgPerMeter * target.area
    val actualPerMeter = calculatePricePerMeter(target.price, target.area)
    val diff = if (avgPerMeter > 0) (actualPerMeter - avgPerMeter) / avgPerMeter * 100.0 else 0.0
    return MarketValueEstimate(comparables.size, avgPerMeter, estimatedValue, diff)
}

data class NeighborhoodStats(val city: String, val count: Int, val averagePrice: Double, val averagePricePerMeter: Double)

fun neighborhoodStats(properties: List<Property>): List<NeighborhoodStats> =
    properties.groupBy { it.city }
        .map { (city, list) ->
            val stats = averagePriceStats(list)
            NeighborhoodStats(city = city, count = stats.count, averagePrice = stats.averagePrice, averagePricePerMeter = stats.averagePricePerMeter)
        }
        .sortedByDescending { it.count }

enum class RankingMetric(val label: String) {
    MOST_VIEWED("پربازدیدترین"),
    HIGHEST_PRICE("گران‌ترین"),
    LOWEST_PRICE("ارزان‌ترین"),
    NEWEST("جدیدترین")
}

fun rankProperties(properties: List<Property>, metric: RankingMetric): List<Property> = when (metric) {
    RankingMetric.MOST_VIEWED -> properties.sortedByDescending { it.viewCount }
    RankingMetric.HIGHEST_PRICE -> properties.sortedByDescending { it.price }
    RankingMetric.LOWEST_PRICE -> properties.sortedBy { it.price }
    RankingMetric.NEWEST -> properties.sortedByDescending { it.dateAdded }
}

/** A starting point only — the rate is always freely editable in the calculator itself. */
fun defaultCommissionRate(): Double = 0.5
