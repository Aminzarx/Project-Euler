package com.realestate.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Gregorian → Jalali (Persian solar/Hijri-Shamsi) calendar conversion. Self-contained — no
 * external library — using the standard, widely-used integer conversion algorithm (the same one
 * behind most PHP/JS "jdf"/"jalaali" implementations), so enabling the calendar toggle in
 * Settings doesn't pull in a new dependency for what is, at its core, one function.
 */
object JalaliDate {
    private val gregorianMonthOffsets = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    private val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /**
     * [Calendar.getInstance] is not cheap — it resolves the default locale and timezone and
     * allocates on every call — and this runs once per row in the activity, timeline and follow-up
     * lists. One scratch Calendar per thread is reused instead; it is only ever written and read
     * within a single synchronous call, and never escapes, so per-thread confinement is enough.
     */
    private val scratchCalendar = object : ThreadLocal<Calendar>() {
        override fun initialValue(): Calendar = Calendar.getInstance()
    }

    private fun calendarAt(timestamp: Long): Calendar =
        scratchCalendar.get()!!.apply { timeInMillis = timestamp }

    /** Returns (year, month 1-12, day) in the Jalali calendar for the given epoch millis. */
    fun toJalali(timestamp: Long): Triple<Int, Int, Int> {
        val cal = calendarAt(timestamp)
        return gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gregorianMonthOffsets[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }

    /** e.g. "۱۵ مهر ۱۴۰۳" or, with [includeTime], "۱۵ مهر ۱۴۰۳، ۱۴:۰۵". */
    fun format(timestamp: Long, includeTime: Boolean = false): String {
        val (jy, jm, jd) = toJalali(timestamp)
        val datePart = "$jd ${monthNames[jm - 1]} $jy"
        if (!includeTime) return datePart
        val cal = calendarAt(timestamp)
        val hh = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val mm = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$datePart، $hh:$mm"
    }
}

// Building a SimpleDateFormat parses the pattern and loads locale data, which is far too expensive
// to repeat once per list row. They are cached per thread rather than shared, because
// SimpleDateFormat is mutable and explicitly not thread-safe.
//
// Subclassed rather than ThreadLocal.withInitial: that overload only exists from API 26 and this
// app ships to minSdk 24, where calling it would be a NoSuchMethodError at runtime.
private val dateTimeFormat = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue() = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
}
private val dateOnlyFormat = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue() = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
}

/** Respects the user's Settings → "تقویم شمسی/میلادی" choice for any user-facing date/time text. */
fun formatAppDate(timestamp: Long, jalali: Boolean, includeTime: Boolean = true): String =
    if (jalali) {
        JalaliDate.format(timestamp, includeTime)
    } else {
        val formatter = if (includeTime) dateTimeFormat.get()!! else dateOnlyFormat.get()!!
        formatter.format(Date(timestamp))
    }
