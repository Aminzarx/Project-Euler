package com.realestate.app.data.exportimport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMergeTest {

    @Test
    fun `a uid not present locally is planned as a new insert`() {
        val incoming = testProperty(uid = "new-uid")

        val plan = planImport(existing = emptyList(), incoming = listOf(incoming))

        assertEquals(1, plan.toInsert.size)
        assertEquals(0, plan.toUpdate.size)
        assertEquals(0, plan.unchanged.size)
        // id must be reset to 0 (let Room autogenerate) — it can never be the sender's own
        // device-local id, which means nothing on this device.
        assertEquals(0L, plan.toInsert.first().id)
    }

    @Test
    fun `a newer incoming record updates the existing local one, keeping the local id and the previous value for undo`() {
        val sharedUid = "shared-uid"
        val local = testProperty(uid = sharedUid, lastModifiedAt = 1000L, title = "عنوان قدیمی").copy(id = 42L)
        val incoming = testProperty(uid = sharedUid, lastModifiedAt = 2000L, title = "عنوان جدید")

        val plan = planImport(existing = listOf(local), incoming = listOf(incoming))

        assertEquals(0, plan.toInsert.size)
        assertEquals(1, plan.toUpdate.size)
        assertEquals(0, plan.unchanged.size)
        val update = plan.toUpdate.first()
        assertEquals(42L, update.updated.id)
        assertEquals("عنوان جدید", update.updated.title)
        // The previous value is preserved exactly as it was locally — this is what undo restores.
        assertEquals(42L, update.previous.id)
        assertEquals("عنوان قدیمی", update.previous.title)
    }

    @Test
    fun `an older or equal-age incoming record is left unchanged, never overwriting newer local edits`() {
        val sharedUid = "shared-uid"
        val local = testProperty(uid = sharedUid, lastModifiedAt = 2000L).copy(id = 7L)
        val staleIncoming = testProperty(uid = sharedUid, lastModifiedAt = 1000L)
        val sameAgeIncoming = testProperty(uid = sharedUid, lastModifiedAt = 2000L)

        val stalePlan = planImport(existing = listOf(local), incoming = listOf(staleIncoming))
        val sameAgePlan = planImport(existing = listOf(local), incoming = listOf(sameAgeIncoming))

        assertEquals(1, stalePlan.unchanged.size)
        assertTrue(stalePlan.toInsert.isEmpty() && stalePlan.toUpdate.isEmpty())
        assertEquals(1, sameAgePlan.unchanged.size)
    }

    @Test
    fun `a mixed bundle sorts each record into the right bucket independently`() {
        val existing = listOf(
            testProperty(uid = "keep-newer-local", lastModifiedAt = 5000L).copy(id = 1L),
            testProperty(uid = "will-be-updated", lastModifiedAt = 1000L).copy(id = 2L)
        )
        val incoming = listOf(
            testProperty(uid = "keep-newer-local", lastModifiedAt = 1000L), // stale -> unchanged
            testProperty(uid = "will-be-updated", lastModifiedAt = 5000L), // newer -> update
            testProperty(uid = "brand-new") // no local match -> insert
        )

        val plan = planImport(existing, incoming)

        assertEquals(3, plan.totalIncoming)
        assertEquals(1, plan.toInsert.size)
        assertEquals(1, plan.toUpdate.size)
        assertEquals(1, plan.unchanged.size)
    }

    @Test
    fun `importing nothing produces an empty, harmless plan`() {
        val plan = planImport(existing = listOf(testProperty()), incoming = emptyList())

        assertEquals(0, plan.totalIncoming)
    }

    @Test
    fun `filterToSelected keeps only checked inserts and updates, and always keeps unchanged`() {
        val existing = listOf(testProperty(uid = "update-me", lastModifiedAt = 1000L).copy(id = 9L))
        val incoming = listOf(
            testProperty(uid = "insert-me-a"),
            testProperty(uid = "insert-me-b"),
            testProperty(uid = "update-me", lastModifiedAt = 5000L),
            testProperty(uid = "unchanged-one", lastModifiedAt = 1000L)
        )
        val existingUnchanged = listOf(testProperty(uid = "unchanged-one", lastModifiedAt = 5000L).copy(id = 3L))
        val plan = planImport(existing + existingUnchanged, incoming)

        val filtered = plan.filterToSelected(setOf("insert-me-a", "update-me"))

        assertEquals(1, filtered.toInsert.size)
        assertEquals("insert-me-a", filtered.toInsert.first().uid)
        assertEquals(1, filtered.toUpdate.size)
        assertEquals("update-me", filtered.toUpdate.first().updated.uid)
        // unchanged passes through regardless of what was selected — there was nothing selectable there.
        assertEquals(plan.unchanged.size, filtered.unchanged.size)
    }

    @Test
    fun `filterToSelected with nothing selected produces an empty-but-valid apply plan`() {
        val plan = planImport(existing = emptyList(), incoming = listOf(testProperty(uid = "a"), testProperty(uid = "b")))

        val filtered = plan.filterToSelected(emptySet())

        assertTrue(filtered.toInsert.isEmpty())
        assertTrue(filtered.toUpdate.isEmpty())
    }

    @Test
    fun `undoSnapshot captures inserted uids and pre-overwrite values, ignoring unchanged records`() {
        val local = testProperty(uid = "update-me", lastModifiedAt = 1000L, title = "قدیمی").copy(id = 9L)
        val incoming = listOf(
            testProperty(uid = "insert-me"),
            testProperty(uid = "update-me", lastModifiedAt = 5000L, title = "جدید"),
            testProperty(uid = "stays-unchanged", lastModifiedAt = 1000L)
        )
        val plan = planImport(existing = listOf(local, testProperty(uid = "stays-unchanged", lastModifiedAt = 5000L)), incoming = incoming)

        val (insertedUids, previousValues) = plan.undoSnapshot()

        assertEquals(listOf("insert-me"), insertedUids)
        assertEquals(1, previousValues.size)
        assertEquals("قدیمی", previousValues.first().title)
        assertEquals(9L, previousValues.first().id)
    }

    @Test
    fun `undoSnapshot on a selectively-filtered plan only captures what was actually selected`() {
        val incoming = listOf(testProperty(uid = "a"), testProperty(uid = "b"))
        val plan = planImport(existing = emptyList(), incoming = incoming).filterToSelected(setOf("a"))

        val (insertedUids, previousValues) = plan.undoSnapshot()

        assertEquals(listOf("a"), insertedUids)
        assertTrue(previousValues.isEmpty())
    }
}
