package com.realestate.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * The single source of truth for Property <-> JSON, shared by the full-database backup
 * (data/backup/BackupManager.kt) and the per-case encrypted export/import
 * (data/exportimport/). Both need the exact same field mapping; keeping one copy means a new
 * Property field only ever needs to be wired in here once, instead of the two call sites quietly
 * drifting apart.
 */
fun Property.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("uid", uid)
    put("title", title)
    put("description", description)
    put("price", price)
    put("area", area)
    put("rooms", rooms)
    put("city", city)
    put("address", address)
    put("ownerName", ownerName)
    put("ownerPhone", ownerPhone)
    // contactId deliberately NOT exported here: it's a local, per-device Contact.id (same
    // portability problem Property.id itself has — see Property.uid's doc comment), meaningless
    // on another device's Contact table. ownerName/ownerPhone (plain, portable strings) already
    // carry the contact info across a cross-device export/import; an imported case simply starts
    // with contactId = null and the agent can attach a local contact for it if they want to.
    put("dealType", dealType.name)
    put("propertyType", propertyType.name)
    put("status", status.name)
    put("tags", JSONArray(tags))
    put("imageUri", imageUri ?: JSONObject.NULL)
    put("isFavorite", isFavorite)
    put("isPinned", isPinned)
    put("favoriteFolder", favoriteFolder ?: JSONObject.NULL)
    put("dateAdded", dateAdded)
    put("lastModifiedAt", lastModifiedAt)
    put("lastViewedAt", lastViewedAt ?: JSONObject.NULL)
    put("lastSharedAt", lastSharedAt ?: JSONObject.NULL)
    put("viewCount", viewCount)
    put("followUpAt", followUpAt ?: JSONObject.NULL)

    // ---- Case redesign fields ----
    put("caseType", caseType.name)
    put("transactionType", transactionType?.name ?: JSONObject.NULL)
    put("caseFlags", JSONArray(caseFlags.map { it.name }))
    put("priority", priority.name)
    put("expiryType", expiryType.name)
    put("customExpiryAt", customExpiryAt ?: JSONObject.NULL)
    put("mortgageStatus", mortgageStatus.name)
    put("titleDeedReady", titleDeedReady ?: JSONObject.NULL)
    put("reasonForSelling", reasonForSelling ?: JSONObject.NULL)
    put("viewingHours", viewingHours ?: JSONObject.NULL)
    put("keyHolder", keyHolder ?: JSONObject.NULL)
    put("paymentConditions", paymentConditions ?: JSONObject.NULL)
    put("constructionAge", constructionAge ?: JSONObject.NULL)
    put("legalStatus", legalStatus ?: JSONObject.NULL)
    put("hiddenNotes", hiddenNotes ?: JSONObject.NULL)
    put("floor", floor ?: JSONObject.NULL)
    put("totalFloors", totalFloors ?: JSONObject.NULL)
    put("landZoning", landZoning ?: JSONObject.NULL)
    put("hasBusinessLicense", hasBusinessLicense ?: JSONObject.NULL)
    put("budgetMin", budgetMin ?: JSONObject.NULL)
    put("budgetMax", budgetMax ?: JSONObject.NULL)
    put("desiredMinArea", desiredMinArea ?: JSONObject.NULL)
    put("desiredMaxArea", desiredMaxArea ?: JSONObject.NULL)
    put("desiredBedrooms", desiredBedrooms ?: JSONObject.NULL)
    put("preferredAreas", JSONArray(preferredAreas))
    put("floorPreference", floorPreference ?: JSONObject.NULL)
    put("viewPreference", viewPreference ?: JSONObject.NULL)
    put("cashAvailable", cashAvailable ?: JSONObject.NULL)
    put("maxDeposit", maxDeposit ?: JSONObject.NULL)
    put("maxMonthlyRent", maxMonthlyRent ?: JSONObject.NULL)

    // ---- Field redesign, round 2 ----
    put("floorPreferenceOptions", JSONArray(floorPreferenceOptions.map { it.name }))
    put("viewPreferenceOptions", JSONArray(viewPreferenceOptions.map { it.name }))
    put("needsLoanFinancing", needsLoanFinancing)
    put("district", district ?: JSONObject.NULL)
    put("latitude", latitude ?: JSONObject.NULL)
    put("longitude", longitude ?: JSONObject.NULL)
    put("legalDocumentType", legalDocumentType?.name ?: JSONObject.NULL)
    put("ownershipType", ownershipType?.name ?: JSONObject.NULL)
    put("depositAmount", depositAmount ?: JSONObject.NULL)
    put("nextViewingAt", nextViewingAt ?: JSONObject.NULL)
    put("developerName", developerName ?: JSONObject.NULL)
    put("expectedDeliveryDate", expectedDeliveryDate ?: JSONObject.NULL)
    put("constructionProgressPercent", constructionProgressPercent ?: JSONObject.NULL)
    put("waterSource", waterSource?.name ?: JSONObject.NULL)
    put("hasWellPermit", hasWellPermit ?: JSONObject.NULL)
    put("frontageWidth", frontageWidth ?: JSONObject.NULL)
    put("leadSource", leadSource?.name ?: JSONObject.NULL)
    put("responsibleAgent", responsibleAgent ?: JSONObject.NULL)
    put("lastContactAt", lastContactAt ?: JSONObject.NULL)
    put("visitStatus", visitStatus?.name ?: JSONObject.NULL)
    put("isConfidential", isConfidential)

    // ---- CRM-practicality pass ----
    put("streetWidth", streetWidth ?: JSONObject.NULL)
    put("orientation", orientation?.name ?: JSONObject.NULL)
    put("hasNaturalLight", hasNaturalLight ?: JSONObject.NULL)
    put("unitsPerFloor", unitsPerFloor ?: JSONObject.NULL)
    put("totalUnits", totalUnits ?: JSONObject.NULL)
    put("structureType", structureType?.name ?: JSONObject.NULL)
    put("heatingSystem", heatingSystem?.name ?: JSONObject.NULL)
    put("coolingSystem", coolingSystem?.name ?: JSONObject.NULL)
    put("hasCompletionCertificate", hasCompletionCertificate ?: JSONObject.NULL)
    put("hasBankMortgage", hasBankMortgage ?: JSONObject.NULL)
    put("existingLoanAmount", existingLoanAmount ?: JSONObject.NULL)
    put("isOwnershipTransferable", isOwnershipTransferable ?: JSONObject.NULL)
    put("closingReason", closingReason?.name ?: JSONObject.NULL)
    put("excludedFloorPreferences", JSONArray(excludedFloorPreferences.map { it.name }))
    put("dealBreakerTags", JSONArray(dealBreakerTags))
    put("locationCapturedAt", locationCapturedAt ?: JSONObject.NULL)
}

fun JSONObject.toProperty(): Property = Property(
    id = getLong("id"),
    // A pre-uid backup (format version 1) has no such key — generate a fresh identity for it on
    // restore rather than fail, since restore is a same-device operation where identity stability
    // across the load doesn't matter the way it does for a real cross-device import.
    uid = optStringOrNull("uid") ?: java.util.UUID.randomUUID().toString(),
    title = getString("title"),
    description = optString("description", ""),
    price = getLong("price"),
    area = getDouble("area"),
    rooms = getInt("rooms"),
    city = getString("city"),
    address = getString("address"),
    ownerName = optString("ownerName", ""),
    ownerPhone = optString("ownerPhone", ""),
    dealType = DealType.valueOf(getString("dealType")),
    propertyType = PropertyType.valueOf(getString("propertyType")),
    status = PropertyStatus.valueOf(optString("status", PropertyStatus.NEW.name)),
    tags = getJSONArray("tags").let { arr -> (0 until arr.length()).map { i -> arr.getString(i) } },
    imageUri = if (isNull("imageUri")) null else getString("imageUri"),
    isFavorite = optBoolean("isFavorite", false),
    isPinned = optBoolean("isPinned", false),
    favoriteFolder = if (isNull("favoriteFolder")) null else optString("favoriteFolder"),
    dateAdded = getLong("dateAdded"),
    lastModifiedAt = optLong("lastModifiedAt", System.currentTimeMillis()),
    lastViewedAt = if (isNull("lastViewedAt")) null else getLong("lastViewedAt"),
    lastSharedAt = if (isNull("lastSharedAt")) null else getLong("lastSharedAt"),
    viewCount = optInt("viewCount", 0),
    followUpAt = if (has("followUpAt") && !isNull("followUpAt")) getLong("followUpAt") else null,

    // ---- Case redesign fields — every one defaults exactly like MIGRATION_6_7 does, so a
    // version-1 backup (no such keys at all) restores as a well-formed OWNER case. ----
    caseType = if (has("caseType")) CaseType.valueOf(getString("caseType")) else CaseType.OWNER,
    transactionType = optEnumOrNull("transactionType", CaseTransactionType::valueOf),
    caseFlags = optJSONArray("caseFlags")?.let { arr -> (0 until arr.length()).map { CaseFlag.valueOf(arr.getString(it)) } } ?: emptyList(),
    priority = if (has("priority")) CasePriority.valueOf(getString("priority")) else CasePriority.NORMAL,
    expiryType = if (has("expiryType")) RequestValidityType.valueOf(getString("expiryType")) else RequestValidityType.NO_EXPIRATION,
    customExpiryAt = optLongOrNull("customExpiryAt"),
    mortgageStatus = if (has("mortgageStatus")) MortgageStatus.valueOf(getString("mortgageStatus")) else MortgageStatus.NONE,
    titleDeedReady = optBooleanOrNull("titleDeedReady"),
    reasonForSelling = optStringOrNull("reasonForSelling"),
    viewingHours = optStringOrNull("viewingHours"),
    keyHolder = optStringOrNull("keyHolder"),
    paymentConditions = optStringOrNull("paymentConditions"),
    constructionAge = optIntOrNull("constructionAge"),
    legalStatus = optStringOrNull("legalStatus"),
    hiddenNotes = optStringOrNull("hiddenNotes"),
    floor = optIntOrNull("floor"),
    totalFloors = optIntOrNull("totalFloors"),
    landZoning = optStringOrNull("landZoning"),
    hasBusinessLicense = optBooleanOrNull("hasBusinessLicense"),
    budgetMin = optLongOrNull("budgetMin"),
    budgetMax = optLongOrNull("budgetMax"),
    desiredMinArea = optDoubleOrNull("desiredMinArea"),
    desiredMaxArea = optDoubleOrNull("desiredMaxArea"),
    desiredBedrooms = optIntOrNull("desiredBedrooms"),
    preferredAreas = optJSONArray("preferredAreas")?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList(),
    floorPreference = optStringOrNull("floorPreference"),
    viewPreference = optStringOrNull("viewPreference"),
    cashAvailable = optLongOrNull("cashAvailable"),
    maxDeposit = optLongOrNull("maxDeposit"),
    maxMonthlyRent = optLongOrNull("maxMonthlyRent"),

    // ---- Field redesign, round 2 — absent in any pre-existing backup/export, so every key
    // defaults to its "nothing set yet" value exactly like the Case-redesign block above. ----
    floorPreferenceOptions = optJSONArray("floorPreferenceOptions")
        ?.let { arr -> (0 until arr.length()).map { FloorPreferenceOption.valueOf(arr.getString(it)) } } ?: emptyList(),
    viewPreferenceOptions = optJSONArray("viewPreferenceOptions")
        ?.let { arr -> (0 until arr.length()).map { ViewPreferenceOption.valueOf(arr.getString(it)) } } ?: emptyList(),
    needsLoanFinancing = optBoolean("needsLoanFinancing", false),
    district = optStringOrNull("district"),
    latitude = optDoubleOrNull("latitude"),
    longitude = optDoubleOrNull("longitude"),
    legalDocumentType = optEnumOrNull("legalDocumentType", LegalDocumentType::valueOf),
    ownershipType = optEnumOrNull("ownershipType", OwnershipType::valueOf),
    depositAmount = optLongOrNull("depositAmount"),
    nextViewingAt = optLongOrNull("nextViewingAt"),
    developerName = optStringOrNull("developerName"),
    expectedDeliveryDate = optLongOrNull("expectedDeliveryDate"),
    constructionProgressPercent = optIntOrNull("constructionProgressPercent"),
    waterSource = optEnumOrNull("waterSource", WaterSource::valueOf),
    hasWellPermit = optBooleanOrNull("hasWellPermit"),
    frontageWidth = optDoubleOrNull("frontageWidth"),
    leadSource = optEnumOrNull("leadSource", LeadSource::valueOf),
    responsibleAgent = optStringOrNull("responsibleAgent"),
    lastContactAt = optLongOrNull("lastContactAt"),
    visitStatus = optEnumOrNull("visitStatus", VisitStatus::valueOf),
    isConfidential = optBoolean("isConfidential", false),

    // ---- CRM-practicality pass ----
    streetWidth = optDoubleOrNull("streetWidth"),
    orientation = optEnumOrNull("orientation", PropertyOrientation::valueOf),
    hasNaturalLight = optBooleanOrNull("hasNaturalLight"),
    unitsPerFloor = optIntOrNull("unitsPerFloor"),
    totalUnits = optIntOrNull("totalUnits"),
    structureType = optEnumOrNull("structureType", StructureType::valueOf),
    heatingSystem = optEnumOrNull("heatingSystem", HeatingSystem::valueOf),
    coolingSystem = optEnumOrNull("coolingSystem", CoolingSystem::valueOf),
    hasCompletionCertificate = optBooleanOrNull("hasCompletionCertificate"),
    hasBankMortgage = optBooleanOrNull("hasBankMortgage"),
    existingLoanAmount = optLongOrNull("existingLoanAmount"),
    isOwnershipTransferable = optBooleanOrNull("isOwnershipTransferable"),
    closingReason = optEnumOrNull("closingReason", ClosingReason::valueOf),
    excludedFloorPreferences = optJSONArray("excludedFloorPreferences")
        ?.let { arr -> (0 until arr.length()).map { FloorPreferenceOption.valueOf(arr.getString(it)) } } ?: emptyList(),
    dealBreakerTags = optJSONArray("dealBreakerTags")?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList(),
    locationCapturedAt = optLongOrNull("locationCapturedAt")
)

// ---- Small nullable-read helpers so every optional field above reads the same way whether the
// key is entirely absent (an old backup) or present-but-JSON-null (a new backup, field unset). ----
fun JSONObject.optStringOrNull(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null
fun JSONObject.optIntOrNull(key: String): Int? = if (has(key) && !isNull(key)) getInt(key) else null
fun JSONObject.optLongOrNull(key: String): Long? = if (has(key) && !isNull(key)) getLong(key) else null
fun JSONObject.optDoubleOrNull(key: String): Double? = if (has(key) && !isNull(key)) getDouble(key) else null
fun JSONObject.optBooleanOrNull(key: String): Boolean? = if (has(key) && !isNull(key)) getBoolean(key) else null
fun <T> JSONObject.optEnumOrNull(key: String, valueOf: (String) -> T): T? =
    if (has(key) && !isNull(key)) valueOf(getString(key)) else null
