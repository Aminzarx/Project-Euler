package com.realestate.app.data.compatibility

import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.PropertyCategory
import com.realestate.app.data.PropertyType
import com.realestate.app.data.category

/**
 * How well a [CaseTransactionType] fits a given property — the four-grade scale from the Phase 1
 * matrix review, in ascending order of "let the agent pick this without a second thought."
 * [FULLY_SUPPORTED] is the assumed default for any pair with no explicit rule below, so adding a
 * brand-new [PropertyType] or [CaseTransactionType] never accidentally locks everything else out.
 */
enum class CompatibilityGrade { FULLY_SUPPORTED, CONDITIONAL, USUALLY_NOT_APPLICABLE, NOT_APPLICABLE }

/** [note] is the one-line business reason shown to the agent — always present except for
 *  [CompatibilityGrade.FULLY_SUPPORTED], where there is nothing worth saying. */
data class CompatibilityResult(val grade: CompatibilityGrade, val note: String? = null)

/**
 * The central, data-driven answer to "does this transaction type make sense for this property" —
 * replaces scattering that judgment as ad hoc `if`/`else` across the Create Case wizard, filters,
 * import, and a future matching engine (see the Phase 1/2 review this models). Every call site in
 * the app should go through [evaluate] (or the convenience wrappers below it) rather than
 * re-deriving this logic locally.
 *
 * Rules are defined at two levels, checked in order:
 * 1. [typeOverrides] — a specific [PropertyType] that genuinely behaves differently from the rest
 *    of its [PropertyCategory] (e.g. [PropertyType.OLD_HOUSE] vs. the rest of RESIDENTIAL for
 *    CONSTRUCTION_PARTNERSHIP).
 * 2. [categoryDefaults] — what's true for the category as a whole.
 *
 * A pair with neither is [CompatibilityGrade.FULLY_SUPPORTED] — the two tables only need to record
 * *exceptions* to "this transaction type works fine here," not the whole 15×18 grid.
 *
 * [PURCHASE]/[SALE] and [PRE_PURCHASE]/[PRE_SALE] are two sides of the same market event (owner
 * lists it as one, a client requests the other), so compatibility rules are written once against
 * the OWNER-side name and looked up through [normalize] — see [CaseTransactionType.appliesTo] for
 * the (separate, already-existing) rule about which [CaseType] each transaction type belongs to.
 */
object TransactionPropertyCompatibility {

    private fun normalize(type: CaseTransactionType): CaseTransactionType = when (type) {
        CaseTransactionType.PURCHASE -> CaseTransactionType.SALE
        CaseTransactionType.PRE_PURCHASE -> CaseTransactionType.PRE_SALE
        else -> type
    }

    private val G = CompatibilityGrade.FULLY_SUPPORTED
    private val C = CompatibilityGrade.CONDITIONAL
    private val U = CompatibilityGrade.USUALLY_NOT_APPLICABLE
    private val N = CompatibilityGrade.NOT_APPLICABLE

    private fun r(grade: CompatibilityGrade, note: String? = null) = CompatibilityResult(grade, note)

    private val categoryDefaults: Map<Pair<CaseTransactionType, PropertyCategory>, CompatibilityResult> = mapOf(
        // ---- RESIDENTIAL (ready units: apartment/villa/building/tower default) ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.RENT to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.RESIDENTIAL) to
            r(N, "مشارکت در ساخت نیازمند زمین یا ملک کلنگی است، نه واحد یا ساختمان آماده."),
        (CaseTransactionType.PRE_SALE to PropertyCategory.RESIDENTIAL) to
            r(U, "پیش‌فروش معمولاً برای ملک در حال ساخت است، نه واحد آماده."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.RESIDENTIAL) to r(G),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.RESIDENTIAL) to r(G),

        // ---- LAND (bare/buildable land default) ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.LAND) to
            r(N, "زمین خالی قابل سکونت یا بهره‌برداری آماده نیست."),
        (CaseTransactionType.RENT to PropertyCategory.LAND) to
            r(U, "اجارهٔ زمین خالی فقط برای کاربری‌های خاص (پارکینگ، انبارش) معنا دارد."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.LAND) to
            r(U, "همانند اجارهٔ ساده، کاربرد خاص و کم‌تکرار."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.LAND) to r(G),
        (CaseTransactionType.PRE_SALE to PropertyCategory.LAND) to
            r(N, "زمین خام موضوع پیش‌فروش نیست؛ آنچه رویش ساخته می‌شود موضوع است."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.LAND) to r(G),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.LAND) to
            r(C, "فروش اقساطی زمین (به‌ویژه زمین‌های شهرکی) رایج است."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.LAND) to
            r(U, "اجارهٔ کوتاه‌مدت زمین فقط برای کاربری خاص (کمپینگ، رویداد) معنا دارد."),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.LAND) to
            r(U, "کاربرد خاص و محدود."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.LAND) to r(G),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.LAND) to
            r(C, "معاوضهٔ زمین با زمین (تجمیع پلاک) ممکن و گاهی رایج است."),

        // ---- AGRICULTURAL (garden default — friendlier of garden/farm; FARM overridden below) ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.AGRICULTURAL) to
            r(U, "باغ/مزرعه معمولاً به‌صورت اجارهٔ ساده یا رویدادی واگذار می‌شود، نه رهن کامل."),
        (CaseTransactionType.RENT to PropertyCategory.AGRICULTURAL) to
            r(C, "اجارهٔ باغ برای مراسم/رویداد رایج است؛ اجارهٔ سکونتی بلندمدت کمتر متداول."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.AGRICULTURAL) to
            r(C, "برای اجارهٔ بلندمدت ممکن؛ کاربرد رویدادی معمولاً اجارهٔ سادهٔ روزانه است."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.AGRICULTURAL) to
            r(U, "فقط پس از تغییر کاربری رسمی از باغی/کشاورزی به مسکونی یا تجاری معنا دارد."),
        (CaseTransactionType.PRE_SALE to PropertyCategory.AGRICULTURAL) to
            r(N, "باغ/مزرعه محصول ساخت‌وساز نیست."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.AGRICULTURAL) to r(G),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.AGRICULTURAL) to
            r(U, "کم‌تکرار."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.AGRICULTURAL) to r(G),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.AGRICULTURAL) to r(G),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.AGRICULTURAL) to
            r(C, "ممکن."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.AGRICULTURAL) to r(G),

        // ---- COMMERCIAL (office/shop) ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.COMMERCIAL) to r(G),
        (CaseTransactionType.RENT to PropertyCategory.COMMERCIAL) to r(G),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.COMMERCIAL) to r(G),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.COMMERCIAL) to
            r(U, "بازسازی بنای تجاری/اداری فرسوده ممکن ولی غیرمتداول."),
        (CaseTransactionType.PRE_SALE to PropertyCategory.COMMERCIAL) to
            r(C, "پیش‌فروش واحد تجاری/اداری در پروژه‌های ترکیبی رایج ولی محدودتر از مسکونی."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.COMMERCIAL) to r(G),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.COMMERCIAL) to r(G),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.COMMERCIAL) to
            r(N, "اجارهٔ کوتاه‌مدت دفتر/مغازه مدل کسب‌وکار متفاوتی (فضای اشتراکی) است."),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.COMMERCIAL) to
            r(N, "خارج از جریان اصلی معاملات ملکیِ مشاور."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.COMMERCIAL) to
            r(C, "ممکن."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.COMMERCIAL) to r(G),

        // ---- INDUSTRIAL (warehouse/industrial) ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.INDUSTRIAL) to r(G),
        (CaseTransactionType.RENT to PropertyCategory.INDUSTRIAL) to r(G),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.INDUSTRIAL) to r(G),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.INDUSTRIAL) to
            r(U, "نوسازی زمین/بنای صنعتی معمولاً نیازمند تغییر کاربری است."),
        (CaseTransactionType.PRE_SALE to PropertyCategory.INDUSTRIAL) to
            r(U, "پیش‌فروش واحد انبار/صنعتی در شهرک‌های خاص وجود دارد اما کم‌تکرار."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.INDUSTRIAL) to
            r(C, "بازار سرمایه‌گذاری تخصصی‌تر (لجستیک/تولید)."),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.INDUSTRIAL) to
            r(U, "معاملات صنعتی معمولاً نقدی یا با تسهیلات بانکی جداگانه انجام می‌شود."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.INDUSTRIAL) to
            r(N, "کاربردی ندارد."),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.INDUSTRIAL) to
            r(N, "کاربردی ندارد."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.INDUSTRIAL) to
            r(U, "کم‌تکرار به دلیل اختلاف ارزش."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.INDUSTRIAL) to
            r(C, "ممکن."),

        // ---- PROJECT (under-construction development) ----
        (CaseTransactionType.SALE to PropertyCategory.PROJECT) to
            r(C, "معمولاً واحد به واحد پیش‌فروش می‌شود؛ فروش یکجای کل پروژه به سازندهٔ دیگر ممکن ولی نادر است."),
        (CaseTransactionType.FULL_MORTGAGE to PropertyCategory.PROJECT) to
            r(N, "پروژهٔ در حال ساخت هنوز قابل سکونت نیست."),
        (CaseTransactionType.RENT to PropertyCategory.PROJECT) to
            r(N, "هنوز چیزی برای اجاره وجود ندارد."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyCategory.PROJECT) to
            r(N, "هنوز چیزی برای اجاره وجود ندارد."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyCategory.PROJECT) to
            r(N, "پروژه خودِ خروجیِ یک مشارکت است، نه ورودی آن."),
        (CaseTransactionType.PRE_SALE to PropertyCategory.PROJECT) to r(G),
        (CaseTransactionType.PROPERTY_EXCHANGE to PropertyCategory.PROJECT) to
            r(C, "معاوضهٔ یک پروژهٔ در حال ساخت معامله‌ای در سطح سازنده/سرمایه‌گذار است."),
        (CaseTransactionType.INVESTMENT to PropertyCategory.PROJECT) to r(G),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyCategory.PROJECT) to
            r(C, "با پیش‌فروش هم‌پوشانی دارد."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyCategory.PROJECT) to
            r(N, "چیزی برای اجاره وجود ندارد."),
        (CaseTransactionType.DAILY_RENT to PropertyCategory.PROJECT) to
            r(N, "چیزی برای اجاره وجود ندارد."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyCategory.PROJECT) to
            r(N, "اختلاف ارزش در سطح معاملهٔ سازنده معنا ندارد."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyCategory.PROJECT) to
            r(U, "این حالت عملاً یک قرارداد مشارکت در ساخت است، نه معاوضهٔ ساده.")
    )

    private val typeOverrides: Map<Pair<CaseTransactionType, PropertyType>, CompatibilityResult> = mapOf(
        // ---- OLD_HOUSE deviates sharply from the rest of RESIDENTIAL: its whole purpose is being
        // torn down and rebuilt, not lived in or pre-sold. ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyType.OLD_HOUSE) to
            r(C, "فقط در صورت قابل‌سکونت بودن ممکن است."),
        (CaseTransactionType.RENT to PropertyType.OLD_HOUSE) to
            r(C, "فقط در صورت قابل‌سکونت بودن ممکن است."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyType.OLD_HOUSE) to
            r(C, "فقط در صورت قابل‌سکونت بودن ممکن است."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.OLD_HOUSE) to
            r(G),
        (CaseTransactionType.PRE_SALE to PropertyType.OLD_HOUSE) to
            r(N, "کلنگی ورودی بازسازی است، نه خروجی ساخت."),
        (CaseTransactionType.INVESTMENT to PropertyType.OLD_HOUSE) to
            r(C, "معمولاً غیرمستقیم، با هدف مشارکت در ساخت یا بازسازی."),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyType.OLD_HOUSE) to
            r(C, "کمتر رایج ولی رخ می‌دهد."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyType.OLD_HOUSE) to
            r(C, "خانه‌های کلنگیِ بازسازی‌شده به اقامتگاه بوم‌گردی؛ بازار خاص ولی واقعی."),
        (CaseTransactionType.DAILY_RENT to PropertyType.OLD_HOUSE) to
            r(C, "بوم‌گردی روزانه در خانهٔ بازسازی‌شده، بازار خاص."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.OLD_HOUSE) to
            r(C, "ممکن."),

        // ---- SEMI_FINISHED_BUILDING: a shell mid-construction, not livable, not a partnership
        // input in the demolish-and-rebuild sense. ----
        (CaseTransactionType.FULL_MORTGAGE to PropertyType.SEMI_FINISHED_BUILDING) to
            r(N, "نیمه‌ساز قابل سکونت نیست."),
        (CaseTransactionType.RENT to PropertyType.SEMI_FINISHED_BUILDING) to
            r(N, "قابل سکونت یا بهره‌برداری نیست."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyType.SEMI_FINISHED_BUILDING) to
            r(N, "قابل بهره‌برداری نیست."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "به‌جای تخریب و بازسازی، مشارکت با هدف «تکمیل ساخت» معنا پیدا می‌کند."),
        (CaseTransactionType.PRE_SALE to PropertyType.SEMI_FINISHED_BUILDING) to
            r(G),
        (CaseTransactionType.PROPERTY_EXCHANGE to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "ممکن ولی کمتر رایج، به دلیل دشواری ارزش‌گذاری ملک نیمه‌تمام."),
        (CaseTransactionType.INVESTMENT to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "سرمایه‌گذاری با هدف تکمیل و فروش مجدد."),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "ممکن."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyType.SEMI_FINISHED_BUILDING) to
            r(N, "قابل بهره‌برداری نیست."),
        (CaseTransactionType.DAILY_RENT to PropertyType.SEMI_FINISHED_BUILDING) to
            r(N, "قابل بهره‌برداری نیست."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "ممکن."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyType.SEMI_FINISHED_BUILDING) to
            r(C, "ممکن."),

        // ---- VILLA/BUILDING/TOWER: milder deviations within RESIDENTIAL (large-plot villas and
        // whole buildings occasionally redevelop; whole-tower deals are rarer than unit-level ones) ----
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.VILLA) to
            r(U, "فقط در صورت وجود قطعهٔ بزرگ قابل تفکیک یا نوسازی شهری معنا دارد."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.BUILDING) to
            r(U, "فقط اگر ساختمان فرسوده و در پایان عمر مفید باشد معنا دارد."),
        (CaseTransactionType.INVESTMENT to PropertyType.VILLA) to
            r(C, "بیشتر برای اجارهٔ کوتاه‌مدت/گردشگری معنا دارد تا صرفاً افزایش سرمایه."),
        (CaseTransactionType.PRE_SALE to PropertyType.APARTMENT) to
            r(C, "فقط اگر واحد واقعاً در حال ساخت باشد — نیازمند مفهوم «مرحلهٔ ساخت» مستقل از نوع ملک."),
        (CaseTransactionType.PRE_SALE to PropertyType.TOWER) to
            r(C, "پیش‌فروش واحدهای برجِ در حال ساخت رایج است؛ برج آماده نباید پیش‌فروش ثبت شود."),
        (CaseTransactionType.PRE_SALE to PropertyType.BUILDING) to
            r(U, "پیش‌فروش کل ساختمان پیش از تکمیل عملاً با «پروژه» هم‌پوشانی دارد."),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyType.BUILDING) to r(C, "ممکن."),
        (CaseTransactionType.INSTALLMENT_SALE to PropertyType.TOWER) to r(C, "در سطح واحد ممکن."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyType.BUILDING) to
            r(U, "اجارهٔ کوتاه‌مدت کل ساختمان (مدل هتل‌آپارتمان) کاربرد خاص دارد."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyType.TOWER) to
            r(U, "در سطح واحد شبیه آپارتمان است؛ در سطح کل برج کاربرد ندارد."),
        (CaseTransactionType.DAILY_RENT to PropertyType.BUILDING) to r(U, "کاربرد خاص."),
        (CaseTransactionType.DAILY_RENT to PropertyType.TOWER) to r(U, "در سطح کل برج کاربرد ندارد."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.VILLA) to r(C, "ممکن."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.BUILDING) to
            r(U, "معمولاً اختلاف ارزش زیاد است."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.TOWER) to
            r(N, "اختلاف ارزش بسیار زیاد است."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyType.BUILDING) to r(C, "ممکن."),
        (CaseTransactionType.EXCHANGE_WITH_LAND to PropertyType.TOWER) to r(U, "کم‌تکرار."),

        // ---- FARM deviates from the (GARDEN-shaped) AGRICULTURAL default: no standing event-venue
        // business the way a garden has, and rezoning is a harder blocker for active farmland. ----
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.FARM) to
            r(N, "زمین کشاورزی فعال بدون تغییر کاربری رسمی هرگز موضوع مشارکت در ساخت نیست."),
        (CaseTransactionType.SHORT_TERM_RENT to PropertyType.FARM) to
            r(C, "گردشگری کشاورزی/اقامت مزرعه‌ای، بازار نوظهور اما واقعی."),
        (CaseTransactionType.DAILY_RENT to PropertyType.FARM) to
            r(C, "اقامت مزرعه‌ای روزانه، بازار نوظهور."),
        (CaseTransactionType.EXCHANGE_WITH_VEHICLE to PropertyType.FARM) to
            r(U, "کم‌تکرار."),

        // ---- LAND subtypes: the split this whole model exists for. LAND_BUILDABLE inherits the
        // LAND category default as-is (it *is* that default); the other three override it. ----
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.LAND_AGRICULTURAL) to
            r(N, "زمین کشاورزی بدون تغییر کاربری رسمی هرگز موضوع مشارکت در ساخت نیست."),
        (CaseTransactionType.RENT to PropertyType.LAND_AGRICULTURAL) to
            r(U, "اجارهٔ زمین کشاورزی برای بهره‌برداری زراعی، قراردادی تخصصی و متفاوت از اجارهٔ معمول است."),
        (CaseTransactionType.MORTGAGE_AND_RENT to PropertyType.LAND_AGRICULTURAL) to
            r(U, "مشابه اجاره، بازار تخصصی کشاورزی."),
        (CaseTransactionType.INVESTMENT to PropertyType.LAND_AGRICULTURAL) to
            r(C, "سرمایه‌گذاری کشاورزی/آگری‌بیزنس، بازار تخصصی."),

        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.LAND_COMMERCIAL) to
            r(C, "بازسازی/توسعهٔ تجاری روی زمین تجاری رایج‌تر از دستهٔ عمومیِ مشارکت در ساخت است."),
        (CaseTransactionType.CONSTRUCTION_PARTNERSHIP to PropertyType.LAND_INDUSTRIAL) to
            r(C, "نوسازی زمین صنعتی رخ می‌دهد اما معمولاً نیازمند تأیید کاربری است.")
    )

    /** The single source of truth every call site should use. */
    fun evaluate(transactionType: CaseTransactionType, propertyType: PropertyType): CompatibilityResult {
        val normalized = normalize(transactionType)
        typeOverrides[normalized to propertyType]?.let { return it }
        return categoryDefaults[normalized to propertyType.category()] ?: CompatibilityResult(CompatibilityGrade.FULLY_SUPPORTED)
    }

    /** Convenience for the Create Case wizard's Property Type step: every [PropertyType] whose
     *  grade is strictly better than [CompatibilityGrade.NOT_APPLICABLE] for [transactionType] —
     *  see Phase 1/2 Step 4: invalid combinations are prevented at the source, not flagged after
     *  the fact, while [CompatibilityGrade.CONDITIONAL]/[CompatibilityGrade.USUALLY_NOT_APPLICABLE]
     *  options stay selectable (with a warning) so real but uncommon scenarios are never blocked. */
    fun selectablePropertyTypes(transactionType: CaseTransactionType): List<PropertyType> =
        PropertyType.entries.filter { evaluate(transactionType, it).grade != CompatibilityGrade.NOT_APPLICABLE }
}
