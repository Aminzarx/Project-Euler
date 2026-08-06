package com.realestate.app.data.exportimport

import com.realestate.app.data.Property

/** One record that would be overwritten, paired with what it looked like immediately before —
 *  [previous] is what makes undo possible: without it, once [updated] is written there is no way
 *  to know what was there a moment ago. */
data class ImportUpdate(val previous: Property, val updated: Property)

/**
 * The result of comparing an incoming bundle against what's already on this device — what the
 * import preview shows, and exactly what [toInsert]/[toUpdate] the confirm step applies.
 *
 * Conflict strategy: matched by [Property.uid] (the portable identity — see Property.kt for why
 * the local, per-device [Property.id] can't be used for this), then last-write-wins by
 * [Property.lastModifiedAt]. This is deliberately simple rather than a full 3-way/field-level
 * merge: the primary use case is one agent handing a set of cases to a colleague, or moving them
 * between their own two devices — scenarios where "whichever copy was edited more recently wins"
 * is the answer nearly every time, and it's a rule an agent can hold in their head. A case genuinely
 * edited differently on both sides at the same time is rare enough, and important enough when it
 * does happen, that silently interleaving individual fields would be more likely to produce a
 * record nobody actually intended than to help — the agent still has both full records to compare
 * by hand if that ever happens, since [unchanged] means "kept as-is", not "discarded".
 */
data class ImportPlan(
    val toInsert: List<Property>,
    val toUpdate: List<ImportUpdate>,
    val unchanged: List<Property>
) {
    val totalIncoming: Int get() = toInsert.size + toUpdate.size + unchanged.size
}

/** Pure and side-effect-free — decides what *would* happen without touching the database, so the
 *  import preview can show real counts before the agent commits to anything. */
fun planImport(existing: List<Property>, incoming: List<Property>): ImportPlan {
    val existingByUid = existing.associateBy { it.uid }
    val toInsert = mutableListOf<Property>()
    val toUpdate = mutableListOf<ImportUpdate>()
    val unchanged = mutableListOf<Property>()

    incoming.forEach { incomingProperty ->
        val match = existingByUid[incomingProperty.uid]
        when {
            match == null -> toInsert += incomingProperty.copy(id = 0)
            incomingProperty.lastModifiedAt > match.lastModifiedAt ->
                toUpdate += ImportUpdate(previous = match, updated = incomingProperty.copy(id = match.id))
            else -> unchanged += incomingProperty
        }
    }

    return ImportPlan(toInsert, toUpdate, unchanged)
}

/** Narrows a full plan down to only the records the agent actually checked in the selective-import
 *  UI, keyed by the incoming record's uid. [unchanged] is never actionable (there is nothing an
 *  incoming record with no newer data could apply), so it passes through unfiltered — selecting or
 *  clearing it has no effect on what gets written either way. */
fun ImportPlan.filterToSelected(selectedUids: Set<String>): ImportPlan = ImportPlan(
    toInsert = toInsert.filter { it.uid in selectedUids },
    toUpdate = toUpdate.filter { it.updated.uid in selectedUids },
    unchanged = unchanged
)

/** What confirming this plan would need to capture *before* writing, in order to be able to
 *  reverse itself afterward: the uids of records it's about to insert (so undo can delete them —
 *  see PropertyDao.deleteByUids for why uid, not the local id), and the pre-overwrite value of
 *  every record it's about to update. Pulled out as a pure function so the undo snapshot's shape
 *  is unit-testable without touching a database. */
fun ImportPlan.undoSnapshot(): Pair<List<String>, List<Property>> =
    toInsert.map { it.uid } to toUpdate.map { it.previous }
