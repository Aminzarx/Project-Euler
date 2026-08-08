package com.realestate.app.data.dealassistant

/**
 * Every tool the Smart Deal Assistant knows about, grouped by category. Adding a future tool
 * (AI valuation, CRM, market trends, ...) means adding one entry here plus one branch in the
 * relevant screen — the hub, navigation and "recent tools" tracking all work automatically.
 */
enum class DealCategory(val label: String) {
    FINANCIAL("مالی"),
    ANALYSIS("تحلیل ملک"),
    MARKETING("بازاریابی"),
    UTILITIES("ابزارهای کاربردی")
}

enum class DealToolId(
    val category: DealCategory,
    val label: String,
    val description: String,
    /** Fully working tools vs. entries kept visible as an honest "coming soon" placeholder. */
    val available: Boolean = true
) {
    COMMISSION(DealCategory.FINANCIAL, "محاسبه کارمزد", "کارمزد مشاور را از قیمت ملک حساب کن"),
    PURCHASE_COST(DealCategory.FINANCIAL, "هزینه تخمینی خرید", "کارمزد، مالیات، ثبت و هزینه انتقال"),
    CONSTRUCTION_COST(DealCategory.FINANCIAL, "امکان‌سنجی ساخت‌وساز", "سرمایه‌گذاری کل، سود و بازده یک پروژه ساخت"),
    RENTAL_CONVERSION(DealCategory.FINANCIAL, "تبدیل رهن و اجاره", "تبدیل بین رهن کامل، اجاره و ترکیبی"),
    ROI(DealCategory.FINANCIAL, "تحلیل سرمایه‌گذاری", "سود واقعی، بازده سالانه و مقایسه با تورم"),
    LOAN(DealCategory.FINANCIAL, "محاسبه وام", "قسط، جدول اقساط و شبیه‌سازی پیش‌پرداخت"),
    INSTALLMENT(DealCategory.FINANCIAL, "برنامه پرداخت اقساطی", "طراحی برنامه پرداخت دلخواه بین خریدار و فروشنده"),

    AVERAGE_PRICE(DealCategory.ANALYSIS, "میانگین قیمت", "میانگین قیمت و قیمت هر متر املاک شما"),
    MARKET_VALUE(DealCategory.ANALYSIS, "برآورد ارزش بازار", "مقایسه با املاک مشابه در پایگاه داده شما"),
    NEIGHBORHOOD(DealCategory.ANALYSIS, "تحلیل محله", "آمار قیمت املاک به تفکیک شهر"),
    RANKING(DealCategory.ANALYSIS, "رتبه‌بندی املاک", "برترین املاک شما بر اساس معیارهای مختلف"),
    COMPARISON(DealCategory.ANALYSIS, "مقایسه املاک", "مقایسه دو یا چند ملک در کنار هم"),

    STORY_GENERATOR(DealCategory.MARKETING, "کارت استوری", "طراحی کارت استوری برای اشتراک‌گذاری"),
    AD_TEXT(DealCategory.MARKETING, "متن آگهی", "متن آگهی حرفه‌ای از روی اطلاعات ملک"),
    QR_CODE(DealCategory.MARKETING, "کد QR", "تولید کد QR برای اطلاعات تماس", available = false),
    POSTER_GENERATOR(DealCategory.MARKETING, "پوستر تبلیغاتی", "طراحی پوستر آماده برای چاپ و اشتراک", available = false),
    PDF_PRESENTATION(DealCategory.MARKETING, "فایل معرفی PDF", "تولید فایل معرفی ملک", available = false),
    BRANDING_TEMPLATES(DealCategory.MARKETING, "قالب برندینگ آژانس", "قالب‌های اختصاصی با برند آژانس", available = false),
    SHARE_TEMPLATES(DealCategory.MARKETING, "قالب‌های اشتراک‌گذاری", "متن‌های آماده برای شبکه‌های اجتماعی", available = false),

    AREA_CONVERTER(DealCategory.UTILITIES, "تبدیل واحد مساحت", "متر مربع، هکتار، جریب و فوت مربع"),
    PERCENTAGE(DealCategory.UTILITIES, "محاسبه درصد", "درصد از عدد، افزایش و کاهش درصدی"),
    DATE_CALCULATOR(DealCategory.UTILITIES, "محاسبه تاریخ", "فاصله بین دو تاریخ یا افزودن روز"),
    QUICK_NOTES(DealCategory.UTILITIES, "یادداشت سریع", "یادداشت‌های کوتاه و سریع"),
    CURRENCY_CONVERTER(DealCategory.UTILITIES, "تبدیل ارز", "به‌زودی", available = false);

    companion object {
        fun fromKey(key: String): DealToolId? = entries.find { it.name == key }
    }
}
