package com.realestate.app.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DealType { SALE, RENT }

enum class PropertyType { APARTMENT, VILLA, LAND, OFFICE, SHOP, WAREHOUSE, INDUSTRIAL, GARDEN, FARM, BUILDING, PROJECT }

enum class PropertyStatus {
    NEW, READY, ACTIVE, NEGOTIATING, RESERVED, SOLD, RENTED, ARCHIVED,
    SEARCHING, VISITED, WAITING, CONTRACT_SIGNED, CANCELLED, EXPIRED
}

/** Every record in the app is a Case: either someone's property (an [OWNER] listing) or what a
 *  client is looking for (a [CLIENT_REQUEST]). Everything else — transaction type, which fields
 *  matter, how it's displayed — is derived from this and [PropertyType], never hardcoded per
 *  screen. See ui/screens/createcase/ for the creation wizard built around this split. */
enum class CaseType { OWNER, CLIENT_REQUEST }

/** The three meaningfully different field-shapes a [PropertyType] can need. Rather than 11 fully
 *  bespoke forms (one per PropertyType), the creation wizard groups types by shape and shows the
 *  handful of fields that actually differ between them (floor/totalFloors for residential,
 *  zoning for land, business-license status for commercial) — see [PropertyType.formShape]. */
enum class PropertyFormShape { RESIDENTIAL, LAND, COMMERCIAL }

fun PropertyType.formShape(): PropertyFormShape = when (this) {
    PropertyType.APARTMENT, PropertyType.VILLA, PropertyType.BUILDING, PropertyType.PROJECT -> PropertyFormShape.RESIDENTIAL
    PropertyType.LAND, PropertyType.GARDEN, PropertyType.FARM -> PropertyFormShape.LAND
    PropertyType.OFFICE, PropertyType.SHOP, PropertyType.WAREHOUSE, PropertyType.INDUSTRIAL -> PropertyFormShape.COMMERCIAL
}

/** Replaces the old two-way [DealType] with the full set of transaction types a Case can be
 *  about. Not every value applies to every [CaseType] — see [CaseTransactionType.appliesTo]. Kept
 *  nullable on [Property] because every row created before this existed only has a [DealType];
 *  [Property.effectiveTransactionLabel] falls back to that for old data. */
enum class CaseTransactionType {
    SALE, PURCHASE, FULL_MORTGAGE, RENT, MORTGAGE_AND_RENT,
    CONSTRUCTION_PARTNERSHIP, PRE_SALE, PRE_PURCHASE, PROPERTY_EXCHANGE, INVESTMENT, OTHER
}

fun CaseTransactionType.appliesTo(caseType: CaseType): Boolean = when (caseType) {
    CaseType.OWNER -> this in ownerTransactionTypes
    CaseType.CLIENT_REQUEST -> this in clientRequestTransactionTypes
}

val ownerTransactionTypes = listOf(
    CaseTransactionType.SALE, CaseTransactionType.FULL_MORTGAGE, CaseTransactionType.RENT,
    CaseTransactionType.MORTGAGE_AND_RENT, CaseTransactionType.CONSTRUCTION_PARTNERSHIP,
    CaseTransactionType.PRE_SALE, CaseTransactionType.PROPERTY_EXCHANGE, CaseTransactionType.OTHER
)

val clientRequestTransactionTypes = listOf(
    CaseTransactionType.PURCHASE, CaseTransactionType.FULL_MORTGAGE, CaseTransactionType.RENT,
    CaseTransactionType.MORTGAGE_AND_RENT, CaseTransactionType.CONSTRUCTION_PARTNERSHIP,
    CaseTransactionType.PRE_PURCHASE, CaseTransactionType.INVESTMENT,
    CaseTransactionType.PROPERTY_EXCHANGE, CaseTransactionType.OTHER
)

/** Every boolean-ish status/requirement toggle a Case can carry, consolidated into one list
 *  column instead of ~17 separate boolean fields — a new flag later is an enum value, not a
 *  migration. The first four are Owner-only in practice, the rest Client-Request-only, but
 *  nothing enforces that split at the type level (a UI concern, not a data one). */
enum class CaseFlag {
    VACANT, NEGOTIABLE, IMMEDIATE_SALE, EXCHANGE_ACCEPTED,
    PARKING_REQUIRED, ELEVATOR_REQUIRED, STORAGE_REQUIRED, BALCONY_REQUIRED, GARDEN_REQUIRED,
    LUXURY_REQUIRED, FURNISHED_REQUIRED, NEW_BUILDING_REQUIRED, ACCESSIBILITY_REQUIRED,
    LOAN_REQUIRED, FLEXIBLE_DEPOSIT, FLEXIBLE_RENT, CONVERSION_ALLOWED
}

enum class MortgageStatus { NONE, PARTIAL, FULL }

enum class CasePriority { LOW, NORMAL, HIGH, URGENT }

/** How long a Case stays valid before the app should remind the consultant to follow up on it —
 *  see [Property.expiresAt]/[Property.isExpired]. */
enum class RequestValidityType { NO_EXPIRATION, DAYS_7, DAYS_15, DAYS_30, DAYS_60, DAYS_90, CUSTOM }

/**
 * [Immutable] is load-bearing for scroll performance, not decoration. Every field is a `val` and the
 * three [List] fields are only ever produced by the type converters and replaced wholesale via
 * `copy(...)` — never mutated in place — so the promise is accurate. Without it, Compose infers the
 * whole class as unstable (a `List` is an interface, so it can't prove otherwise), which means no
 * composable taking a Property can ever skip recomposition and no lambda capturing one can be
 * memoized. That turns every list row into a full re-layout whenever anything above it recomposes.
 */
@Immutable
@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["dateAdded"]),
        Index(value = ["isFavorite"]),
        Index(value = ["lastViewedAt"])
    ]
)
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Long,
    val area: Double,
    val rooms: Int,
    val city: String,
    val address: String,
    /** The Case's primary contact — the property owner for an [CaseType.OWNER] case, the client
     *  themselves for a [CaseType.CLIENT_REQUEST] case. Deliberately one field either way rather
     *  than parallel owner/client name+phone pairs, since a Case only ever has one such contact. */
    val ownerName: String = "",
    val ownerPhone: String,
    val dealType: DealType,
    val propertyType: PropertyType,
    val status: PropertyStatus = PropertyStatus.NEW,
    val tags: List<String> = emptyList(),
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val favoriteFolder: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long? = null,
    val lastSharedAt: Long? = null,
    val viewCount: Int = 0,
    val followUpAt: Long? = null,

    // ---- Case redesign (see ui/screens/createcase/) ----
    val caseType: CaseType = CaseType.OWNER,
    val transactionType: CaseTransactionType? = null,
    val caseFlags: List<CaseFlag> = emptyList(),
    val priority: CasePriority = CasePriority.NORMAL,
    val expiryType: RequestValidityType = RequestValidityType.NO_EXPIRATION,
    val customExpiryAt: Long? = null,

    // ---- Owner-only fields ----
    val mortgageStatus: MortgageStatus = MortgageStatus.NONE,
    val titleDeedReady: Boolean? = null,
    val reasonForSelling: String? = null,
    val viewingHours: String? = null,
    val keyHolder: String? = null,
    val paymentConditions: String? = null,
    val constructionAge: Int? = null,
    val legalStatus: String? = null,
    /** Internal notes for the agent only — deliberately never read by StoryCard, the ad-text
     *  generator, or any other client-facing/shareable output. */
    val hiddenNotes: String? = null,
    val floor: Int? = null,
    val totalFloors: Int? = null,
    val landZoning: String? = null,
    val hasBusinessLicense: Boolean? = null,

    // ---- Client-request-only fields ----
    val budgetMin: Long? = null,
    val budgetMax: Long? = null,
    val desiredMinArea: Double? = null,
    val desiredMaxArea: Double? = null,
    val desiredBedrooms: Int? = null,
    val preferredAreas: List<String> = emptyList(),
    val floorPreference: String? = null,
    val viewPreference: String? = null,
    val cashAvailable: Long? = null,
    val maxDeposit: Long? = null,
    val maxMonthlyRent: Long? = null
)

/**
 * Derived, human-readable identifier - never stored, always computed from [Property.id].
 *
 * Deliberately not `"PR-%04d".format(id)`: [String.format] resolves against the default locale, so
 * on a fa-IR device — i.e. this app's entire audience — it emits Persian digits ("PR-۰۰۱۲"). That
 * silently breaks search-by-code, since the query the agent types is compared against a string that
 * no longer contains ASCII digits. The manual padding below is locale-independent, and also avoids
 * a Formatter allocation per property per keystroke in the search filter.
 */
val Property.code: String
    get() {
        val digits = id.toString()
        return when (digits.length) {
            1 -> "PR-000$digits"
            2 -> "PR-00$digits"
            3 -> "PR-0$digits"
            else -> "PR-$digits"
        }
    }

/** Absolute expiry timestamp, or null if the Case has no expiration. */
fun Property.expiresAt(): Long? = when (expiryType) {
    RequestValidityType.NO_EXPIRATION -> null
    RequestValidityType.CUSTOM -> customExpiryAt
    RequestValidityType.DAYS_7 -> dateAdded + 7L * DAY_MILLIS
    RequestValidityType.DAYS_15 -> dateAdded + 15L * DAY_MILLIS
    RequestValidityType.DAYS_30 -> dateAdded + 30L * DAY_MILLIS
    RequestValidityType.DAYS_60 -> dateAdded + 60L * DAY_MILLIS
    RequestValidityType.DAYS_90 -> dateAdded + 90L * DAY_MILLIS
}

fun Property.isExpired(now: Long = System.currentTimeMillis()): Boolean {
    val expiry = expiresAt() ?: return false
    return now >= expiry
}

/** A single comparable price for sorting mixed OWNER/CLIENT_REQUEST lists — the listing price for
 *  an OWNER case, the midpoint of the budget range for a CLIENT_REQUEST (falling back to whichever
 *  bound is set), or 0 when neither is known. */
fun Property.sortablePrice(): Long = when (caseType) {
    CaseType.OWNER -> price
    CaseType.CLIENT_REQUEST -> when {
        budgetMin != null && budgetMax != null -> (budgetMin + budgetMax) / 2
        budgetMax != null -> budgetMax
        budgetMin != null -> budgetMin
        else -> 0L
    }
}

/** Price-range filter match that works for both case types: an OWNER case matches on its actual
 *  price, a CLIENT_REQUEST matches when its budget range overlaps the filter range (so a request
 *  willing to pay up to 2B still shows up under a 1.5B-2.5B filter instead of being hidden behind
 *  its always-zero [Property.price]). */
fun Property.matchesPriceRange(min: Long?, max: Long?): Boolean = when (caseType) {
    CaseType.OWNER -> (min == null || price >= min) && (max == null || price <= max)
    CaseType.CLIENT_REQUEST -> {
        val lower = budgetMin ?: 0L
        val upper = budgetMax ?: Long.MAX_VALUE
        (min == null || upper >= min) && (max == null || lower <= max)
    }
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000
