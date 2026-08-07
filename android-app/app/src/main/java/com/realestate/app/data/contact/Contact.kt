package com.realestate.app.data.contact

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A person, independent of any one Case — the record that lets "this owner has 3 listings" or
 * "this client submitted 2 requests over 6 months" be an actual question the app can answer,
 * instead of three unrelated name/phone strings typed fresh each time. See
 * ui/screens/createcase/CaseDetailsForm.kt's contact picker for where this gets attached to a
 * Case, and [com.realestate.app.data.Property.contactId] for the FK.
 *
 * [Property.ownerName]/[Property.ownerPhone] are kept as a denormalized snapshot on the Case
 * itself (copied from the chosen Contact at save time) rather than removed in favor of this
 * table — every existing screen that reads a case's contact info (StoryCard, ad-text, Favorites,
 * PropertyList, PropertyDetail, ShareMessage) keeps working unchanged against that snapshot,
 * while this table is the write-side source of truth the picker searches/dedupes against.
 */
@Immutable
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["primaryPhone"]),
        Index(value = ["fullName"])
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = java.util.UUID.randomUUID().toString(),
    val fullName: String,
    val primaryPhone: String,
    val secondaryPhone: String? = null,
    val email: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Bumped whenever this contact is attached to a Case — a pure UI-ordering convenience
     *  (recently-active contacts surface first in the picker) with no filtering/reporting
     *  dependency on it, so keeping it in sync from one write path is an acceptable, deliberate
     *  denormalization rather than a data-integrity risk. */
    val lastCaseAt: Long? = null
)
