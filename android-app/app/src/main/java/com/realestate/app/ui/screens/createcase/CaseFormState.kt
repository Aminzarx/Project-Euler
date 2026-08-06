package com.realestate.app.ui.screens.createcase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.realestate.app.data.CaseFlag
import com.realestate.app.data.CasePriority
import com.realestate.app.data.MortgageStatus
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.RequestValidityType

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

    // Shared
    var expiryType by mutableStateOf(RequestValidityType.NO_EXPIRATION)
    var customExpiryAt by mutableStateOf<Long?>(null)

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
        title, description, price, area, rooms, city, address, contactName, contactPhone,
        status, priority, tags, imageUri,
        mortgageStatus, titleDeedReady, reasonForSelling, viewingHours, keyHolder,
        paymentConditions, constructionAge, legalStatus, hiddenNotes, floor, totalFloors,
        landZoning, hasBusinessLicense, ownerFlags,
        budgetMin, budgetMax, desiredMinArea, desiredMaxArea, desiredBedrooms, preferredAreas,
        floorPreference, viewPreference, cashAvailable, maxDeposit, maxMonthlyRent,
        requirementFlags, rentalFlags, expiryType, customExpiryAt
    )

    companion object {
        val OWNER_FLAGS = setOf(CaseFlag.VACANT, CaseFlag.NEGOTIABLE, CaseFlag.IMMEDIATE_SALE, CaseFlag.EXCHANGE_ACCEPTED)
        val REQUIREMENT_FLAGS = setOf(
            CaseFlag.PARKING_REQUIRED, CaseFlag.ELEVATOR_REQUIRED, CaseFlag.STORAGE_REQUIRED,
            CaseFlag.BALCONY_REQUIRED, CaseFlag.GARDEN_REQUIRED, CaseFlag.LUXURY_REQUIRED,
            CaseFlag.FURNISHED_REQUIRED, CaseFlag.NEW_BUILDING_REQUIRED, CaseFlag.ACCESSIBILITY_REQUIRED,
            CaseFlag.LOAN_REQUIRED
        )
        val RENTAL_FLAGS = setOf(CaseFlag.FLEXIBLE_DEPOSIT, CaseFlag.FLEXIBLE_RENT, CaseFlag.CONVERSION_ALLOWED)
    }
}
