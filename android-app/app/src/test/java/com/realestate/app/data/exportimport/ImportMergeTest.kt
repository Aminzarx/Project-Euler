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
    fun `a newer incoming record updates the existing local one, keeping the local id`() {
        val sharedUid = "shared-uid"
        val local = testProperty(uid = sharedUid, lastModifiedAt = 1000L).copy(id = 42L)
        val incoming = testProperty(uid = sharedUid, lastModifiedAt = 2000L, title = "عنوان جدید")

        val plan = planImport(existing = listOf(local), incoming = listOf(incoming))

        assertEquals(0, plan.toInsert.size)
        assertEquals(1, plan.toUpdate.size)
        assertEquals(0, plan.unchanged.size)
        assertEquals(42L, plan.toUpdate.first().id)
        assertEquals("عنوان جدید", plan.toUpdate.first().title)
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
}
