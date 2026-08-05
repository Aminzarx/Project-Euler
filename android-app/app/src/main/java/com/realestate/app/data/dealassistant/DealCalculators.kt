package com.realestate.app.data.dealassistant

import com.realestate.app.data.Property
import java.util.Calendar
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

data class ConstructionCostResult(
    val constructionCost: Double,
    val totalInvestment: Double,
    val estimatedSellingValue: Double,
    val profit: Double,
    val profitPercent: Double
)

fun calculateConstructionCost(
    landPrice: Long,
    builtArea: Double,
    costPerMeter: Double,
    sellingPricePerMeter: Double
): ConstructionCostResult {
    val constructionCost = builtArea * costPerMeter
    val totalInvestment = landPrice + constructionCost
    val estimatedSellingValue = builtArea * sellingPricePerMeter
    val profit = estimatedSellingValue - totalInvestment
    val profitPercent = if (totalInvestment > 0) profit / totalInvestment * 100.0 else 0.0
    return ConstructionCostResult(constructionCost, totalInvestment, estimatedSellingValue, profit, profitPercent)
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

data class RoiResult(val profit: Double, val roiPercent: Double, val annualReturnPercent: Double, val growthMultiplier: Double)

fun calculateRoi(purchasePrice: Double, sellingPrice: Double, years: Double): RoiResult {
    val profit = sellingPrice - purchasePrice
    val roi = if (purchasePrice > 0) profit / purchasePrice * 100.0 else 0.0
    val annual = if (years > 0) roi / years else roi
    val growth = if (purchasePrice > 0) sellingPrice / purchasePrice else 0.0
    return RoiResult(profit, roi, annual, growth)
}

data class LoanResult(val monthlyPayment: Double, val totalPayment: Double, val totalInterest: Double)

fun calculateLoan(principal: Double, annualRatePercent: Double, months: Int): LoanResult {
    if (months <= 0 || principal <= 0) return LoanResult(0.0, 0.0, 0.0)
    val monthlyRate = annualRatePercent / 100.0 / 12.0
    val payment = if (monthlyRate == 0.0) {
        principal / months
    } else {
        val factor = (1 + monthlyRate).pow(months.toDouble())
        principal * monthlyRate * factor / (factor - 1)
    }
    val total = payment * months
    return LoanResult(monthlyPayment = payment, totalPayment = total, totalInterest = total - principal)
}

data class InstallmentResult(val installmentAmount: Double, val totalPayable: Double)

fun calculateInstallment(totalPrice: Long, downPayment: Double, installmentCount: Int, annualRatePercent: Double): InstallmentResult {
    val remaining = (totalPrice - downPayment).coerceAtLeast(0.0)
    if (installmentCount <= 0) return InstallmentResult(0.0, remaining)
    return if (annualRatePercent <= 0.0) {
        InstallmentResult(installmentAmount = remaining / installmentCount, totalPayable = remaining)
    } else {
        val loan = calculateLoan(remaining, annualRatePercent, installmentCount)
        InstallmentResult(installmentAmount = loan.monthlyPayment, totalPayable = loan.totalPayment)
    }
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
