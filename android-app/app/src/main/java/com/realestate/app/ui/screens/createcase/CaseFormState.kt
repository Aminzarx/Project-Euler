package com.realestate.app.ui.screens.createcase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.realestate.app.data.CaseFlag
import com.realestate.app.data.CasePriority
import com.realestate.app.data.FloorPreferenceOption
import com.realestate.app.data.LeadSource
import com.realestate.app.data.LegalDocumentType
import com.realestate.app.data.MortgageStatus
import com.realestate.app.data.OwnershipType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.RequestValidityType
import com.realestate.app.data.ViewPreferenceOption
import com.realestate.app.data.VisitStatus
import com.realestate.app.data.WaterSource

/**
 * Every field Step 4's dynamic form can collect, held in one place instead of as ~40 separate
 * composable parameters. Which subset is actually shown/used is decided by [CreateCaseWizardScreen]
 * based on the case type, transaction type, and property-type shape chosen in Steps 1-3 — this
 * class itself doesn't know or care which case type it belongs to.
 *
 * Numeric fields are kept as raw [String] input (mirroring the rest of the app's forms, e.g.
 * AddEditPropertyScreen) and only parsed into their real types in [toProperty].
 */
internal class CaseFormState {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var area by mutableStateOf("")
    var rooms by mutableStateOf("")
    var city by mutableStateOf("")
    var address by mutableStateOf("")
    var contactName by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    /** Set by the contact picker (see CaseDetailsForm.kt's ContactPickerField) the moment an
     *  existing contact is selected or a new one is created — resolved eagerly, not at save time,
     *  so [CreateCaseWizardScreen.buildProperty] can read it synchronously like every other field.
     *  Stays null for a case whose contact was never picked (typed name/phone only), exactly
     *  preserving pre-Contact-entity behavior for anyone who ignores the picker. */
    var contactId by mutableStateOf<Long?>(null)
    var status by mutableStateOf(PropertyStatus.NEW)
    var priority by mutableStateOf(CasePriority.NORMAL)
    var tags by mutableStateOf<List<String>>(emptyList())
    var imageUri by mutableStateOf<String?>(null)

    // Owner-only
    var mortgageStatus by mutableStateOf(MortgageStatus.NONE)
    var titleDeedReady by mutableStateOf<Boolean?>(null)
    var reasonForSelling by mutableStateOf("")
    var viewingHours by mutableStateOf("")
    var keyHolder by mutableStateOf("")
    var paymentConditions by mutableStateOf("")
    var constructionAge by mutableStateOf("")
    var legalStatus by mutableStateOf("")
    var hiddenNotes by mutableStateOf("")
    var floor by mutableStateOf("")
    var totalFloors by mutableStateOf("")
    var landZoning by mutableStateOf("")
    var hasBusinessLicense by mutableStateOf<Boolean?>(null)
    var ownerFlags by mutableStateOf<Set<CaseFlag>>(emptySet())
    var district by mutableStateOf("")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)
    var legalDocumentType by mutableStateOf<LegalDocumentType?>(null)
    var ownershipType by mutableStateOf<OwnershipType?>(null)
    var depositAmount by mutableStateOf("")
    var nextViewingAt by mutableStateOf<Long?>(null)
    var developerName by mutableStateOf("")
    var expectedDeliveryDate by mutableStateOf<Long?>(null)
    var constructionProgressPercent by mutableStateOf("")
    var waterSource by mutableStateOf<WaterSource?>(null)
    var hasWellPermit by mutableStateOf<Boolean?>(null)
    var frontageWidth by mutableStateOf("")

    // Client-request-only
    var budgetMin by mutableStateOf("")
    var budgetMax by mutableStateOf("")
    var desiredMinArea by mutableStateOf("")
    var desiredMaxArea by mutableStateOf("")
    var desiredBedrooms by mutableStateOf("")
    var preferredAreas by mutableStateOf<List<String>>(emptyList())
    var floorPreference by mutableStateOf("")
    var viewPreference by mutableStateOf("")
    var cashAvailable by mutableStateOf("")
    var maxDeposit by mutableStateOf("")
    var maxMonthlyRent by mutableStateOf("")
    var requirementFlags by mutableStateOf<Set<CaseFlag>>(emptySet())
    var rentalFlags by mutableStateOf<Set<CaseFlag>>(emptySet())
    var floorPreferenceOptions by mutableStateOf<Set<FloorPreferenceOption>>(emptySet())
    var viewPreferenceOptions by mutableStateOf<Set<ViewPreferenceOption>>(emptySet())
    var needsLoanFinancing by mutableStateOf(false)

    // Shared (both case types)
    var expiryType by mutableStateOf(RequestValidityType.NO_EXPIRATION)
    var customExpiryAt by mutableStateOf<Long?>(null)
    var leadSource by mutableStateOf<LeadSource?>(null)
    /** Free text — see the doc comment on [Property.responsibleAgent] for why this isn't a FK. */
    var responsibleAgent by mutableStateOf("")
    var lastContactAt by mutableStateOf<Long?>(null)
    var visitStatus by mutableStateOf<VisitStatus?>(null)
    var isConfidential by mutableStateOf(false)

    fun loadFrom(p: Property) {
        title = p.title
        description = p.description
        price = if (p.price != 0L) p.price.toString() else ""
        area = if (p.area != 0.0) p.area.toString() else ""
        rooms = if (p.rooms != 0) p.rooms.toString() else ""
        city = p.city
        address = p.address
        contactName = p.ownerName
        contactPhone = p.ownerPhone
        contactId = p.contactId
        status = p.status
        priority = p.priority
        tags = p.tags
        imageUri = p.imageUri
        mortgageStatus = p.mortgageStatus
        titleDeedReady = p.titleDeedReady
        reasonForSelling = p.reasonForSelling.orEmpty()
        viewingHours = p.viewingHours.orEmpty()
        keyHolder = p.keyHolder.orEmpty()
        paymentConditions = p.paymentConditions.orEmpty()
        constructionAge = p.constructionAge?.toString().orEmpty()
        legalStatus = p.legalStatus.orEmpty()
        hiddenNotes = p.hiddenNotes.orEmpty()
        floor = p.floor?.toString().orEmpty()
        totalFloors = p.totalFloors?.toString().orEmpty()
        landZoning = p.landZoning.orEmpty()
        hasBusinessLicense = p.hasBusinessLicense
        ownerFlags = p.caseFlags.filter { it in OWNER_FLAGS }.toSet()
        district = p.district.orEmpty()
        latitude = p.latitude
        longitude = p.longitude
        legalDocumentType = p.legalDocumentType
        ownershipType = p.ownershipType
        depositAmount = p.depositAmount?.toString().orEmpty()
        nextViewingAt = p.nextViewingAt
        developerName = p.developerName.orEmpty()
        expectedDeliveryDate = p.expectedDeliveryDate
        constructionProgressPercent = p.constructionProgressPercent?.toString().orEmpty()
        waterSource = p.waterSource
        hasWellPermit = p.hasWellPermit
        frontageWidth = p.frontageWidth?.toString().orEmpty()
        leadSource = p.leadSource
        responsibleAgent = p.responsibleAgent.orEmpty()
        lastContactAt = p.lastContactAt
        visitStatus = p.visitStatus
        isConfidential = p.isConfidential
        floorPreferenceOptions = p.floorPreferenceOptions.toSet()
        viewPreferenceOptions = p.viewPreferenceOptions.toSet()
        needsLoanFinancing = p.needsLoanFinancing
        budgetMin = p.budgetMin?.toString().orEmpty()
        budgetMax = p.budgetMax?.toString().orEmpty()
        desiredMinArea = p.desiredMinArea?.toString().orEmpty()
        desiredMaxArea = p.desiredMaxArea?.toString().orEmpty()
        desiredBedrooms = p.desiredBedrooms?.toString().orEmpty()
        preferredAreas = p.preferredAreas
        floorPreference = p.floorPreference.orEmpty()
        viewPreference = p.viewPreference.orEmpty()
        cashAvailable = p.cashAvailable?.toString().orEmpty()
        maxDeposit = p.maxDeposit?.toString().orEmpty()
        maxMonthlyRent = p.maxMonthlyRent?.toString().orEmpty()
        requirementFlags = p.caseFlags.filter { it in REQUIREMENT_FLAGS }.toSet()
        rentalFlags = p.caseFlags.filter { it in RENTAL_FLAGS }.toSet()
        expiryType = p.expiryType
        customExpiryAt = p.customExpiryAt
    }

    /** Blanks every Owner-only field — called when the agent switches an in-progress case from
     *  Owner to Client Request, so the record that gets saved never carries stale, meaningless
     *  values (a mortgage status, a key holder) left over from the type they backed out of. */
    fun resetOwnerOnlyFields() {
        mortgageStatus = MortgageStatus.NONE
        titleDeedReady = null
        reasonForSelling = ""
        viewingHours = ""
        keyHolder = ""
        paymentConditions = ""
        constructionAge = ""
        legalStatus = ""
        hiddenNotes = ""
        floor = ""
        totalFloors = ""
        landZoning = ""
        hasBusinessLicense = null
        ownerFlags = emptySet()
        district = ""
        latitude = null
        longitude = null
        legalDocumentType = null
        ownershipType = null
        depositAmount = ""
        nextViewingAt = null
        developerName = ""
        expectedDeliveryDate = null
        constructionProgressPercent = ""
        waterSource = null
        hasWellPermit = null
        frontageWidth = ""
    }

    /** The Client-Request-only counterpart of [resetOwnerOnlyFields]. */
    fun resetClientRequestOnlyFields() {
        budgetMin = ""
        budgetMax = ""
        desiredMinArea = ""
        desiredMaxArea = ""
        desiredBedrooms = ""
        preferredAreas = emptyList()
        floorPreference = ""
        viewPreference = ""
        cashAvailable = ""
        maxDeposit = ""
        maxMonthlyRent = ""
        requirementFlags = emptySet()
        rentalFlags = emptySet()
        floorPreferenceOptions = emptySet()
        viewPreferenceOptions = emptySet()
        needsLoanFinancing = false
    }

    /**
     * Values as of the last [markClean] call — the reference point for [isDirty].
     *
     * Deliberately *not* initialised by reading the fields at construction time: the wizard builds
     * this inside a `remember { }`, whose calculation block runs during composition, so reading the
     * backing states there would subscribe the whole wizard to every field and make each keystroke
     * recompose all four steps. It is filled in from a side effect instead, which runs outside
     * composition and therefore records nothing.
     */
    private var baseline: List<Any?>? = null

    /** Marks the current values as the saved state — call after the form is first populated. */
    fun markClean() {
        baseline = snapshot()
    }

    /** Whether anything has been typed/selected since [markClean]. Answers "is there unsaved work
     *  worth warning about before leaving?" — false until a baseline exists, so a form that hasn't
     *  finished loading can never trigger a spurious warning. */
    fun isDirty(): Boolean {
        val base = baseline ?: return false
        return snapshot() != base
    }

    private fun snapshot(): List<Any?> = listOf(
        title, description, price, area, rooms, city, address, contactName, contactPhone, contactId,
        status, priority, tags, imageUri,
        mortgageStatus, titleDeedReady, reasonForSelling, viewingHours, keyHolder,
        paymentConditions, constructionAge, legalStatus, hiddenNotes, floor, totalFloors,
        landZoning, hasBusinessLicense, ownerFlags,
        district, latitude, longitude, legalDocumentType, ownershipType, depositAmount,
        nextViewingAt, developerName, expectedDeliveryDate, constructionProgressPercent,
        waterSource, hasWellPermit, frontageWidth,
        budgetMin, budgetMax, desiredMinArea, desiredMaxArea, desiredBedrooms, preferredAreas,
        floorPreference, viewPreference, cashAvailable, maxDeposit, maxMonthlyRent,
        requirementFlags, rentalFlags, floorPreferenceOptions, viewPreferenceOptions,
        needsLoanFinancing, expiryType, customExpiryAt,
        leadSource, responsibleAgent, lastContactAt, visitStatus, isConfidential
    )

    companion object {
        val OWNER_FLAGS = setOf(
            CaseFlag.VACANT, CaseFlag.NEGOTIABLE, CaseFlag.IMMEDIATE_SALE, CaseFlag.EXCHANGE_ACCEPTED,
            // Amenities the property actually has — the owner-side mirror of AMENITY_REQUIREMENT_FLAGS
            // below, see [com.realestate.app.data.amenityPairs] for how the two line up.
            CaseFlag.PARKING_AVAILABLE, CaseFlag.ELEVATOR_AVAILABLE, CaseFlag.STORAGE_AVAILABLE,
            CaseFlag.BALCONY_AVAILABLE, CaseFlag.GARDEN_YARD_AVAILABLE, CaseFlag.LUXURY_FINISH,
            CaseFlag.FURNISHED_UNIT, CaseFlag.NEWLY_BUILT, CaseFlag.ACCESSIBILITY_READY
        )
        val REQUIREMENT_FLAGS = setOf(
            CaseFlag.PARKING_REQUIRED, CaseFlag.ELEVATOR_REQUIRED, CaseFlag.STORAGE_REQUIRED,
            CaseFlag.BALCONY_REQUIRED, CaseFlag.GARDEN_REQUIRED, CaseFlag.LUXURY_REQUIRED,
            CaseFlag.FURNISHED_REQUIRED, CaseFlag.NEW_BUILDING_REQUIRED, CaseFlag.ACCESSIBILITY_REQUIRED,
            CaseFlag.LOAN_REQUIRED
        )
        /** The subset of [REQUIREMENT_FLAGS] rendered as amenity chips — excludes [CaseFlag.LOAN_REQUIRED],
         *  which now has its own [needsLoanFinancing] boolean instead of being a flag-in-a-list. */
        val AMENITY_REQUIREMENT_FLAGS = REQUIREMENT_FLAGS - CaseFlag.LOAN_REQUIRED
        /** The subset of [OWNER_FLAGS] that are amenities rather than listing-status flags —
         *  rendered as its own "امکانات ملک" chip group, separate from VACANT/NEGOTIABLE/etc. */
        val AMENITY_OWNER_FLAGS = OWNER_FLAGS - setOf(
            CaseFlag.VACANT, CaseFlag.NEGOTIABLE, CaseFlag.IMMEDIATE_SALE, CaseFlag.EXCHANGE_ACCEPTED
        )
        val OWNER_STATUS_FLAGS = OWNER_FLAGS - AMENITY_OWNER_FLAGS
        val RENTAL_FLAGS = setOf(CaseFlag.FLEXIBLE_DEPOSIT, CaseFlag.FLEXIBLE_RENT, CaseFlag.CONVERSION_ALLOWED)
    }
}
