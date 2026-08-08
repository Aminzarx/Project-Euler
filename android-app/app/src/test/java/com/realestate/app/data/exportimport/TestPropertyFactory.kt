package com.realestate.app.data.exportimport

import com.realestate.app.data.CaseType
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyType
import java.util.UUID

/** A minimally-filled, always-valid Property for tests that don't care about most of its ~50
 *  fields — only the ones relevant to export/import (uid, lastModifiedAt) usually need overriding. */
internal fun testProperty(
    uid: String = UUID.randomUUID().toString(),
    title: String = "خانه تست",
    lastModifiedAt: Long = 1_700_000_000_000L,
    caseType: CaseType = CaseType.OWNER
): Property = Property(
    id = 0,
    uid = uid,
    title = title,
    description = "",
    price = 1_000_000_000L,
    area = 80.0,
    rooms = 2,
    city = "تهران",
    address = "خیابان آزادی",
    ownerPhone = "09120000000",
    dealType = DealType.SALE,
    propertyType = PropertyType.APARTMENT,
    lastModifiedAt = lastModifiedAt,
    caseType = caseType
)
