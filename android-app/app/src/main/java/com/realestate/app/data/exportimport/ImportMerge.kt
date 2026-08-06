package com.realestate.app.data.exportimport

import com.realestate.app.data.Property

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
    val toUpdate: List<Property>,
    val unchanged: List<Property>
) {
    val totalIncoming: Int get() = toInsert.size + toUpdate.size + unchanged.size
}

/** Pure and side-effect-free — decides what *would* happen without touching the database, so the
 *  import preview can show real counts before the agent commits to anything. */
fun planImport(existing: List<Property>, incoming: List<Property>): ImportPlan {
    val existingByUid = existing.associateBy { it.uid }
    val toInsert = mutableListOf<Property>()
    val toUpdate = mutableListOf<Property>()
    val unchanged = mutableListOf<Property>()

    incoming.forEach { incomingProperty ->
        val match = existingByUid[incomingProperty.uid]
        when {
            match == null -> toInsert += incomingProperty.copy(id = 0)
            incomingProperty.lastModifiedAt > match.lastModifiedAt -> toUpdate += incomingProperty.copy(id = match.id)
            else -> unchanged += incomingProperty
        }
    }

    return ImportPlan(toInsert, toUpdate, unchanged)
}
