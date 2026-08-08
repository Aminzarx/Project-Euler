package com.realestate.app.data.compatibility

import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.PropertyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionPropertyCompatibilityTest {

    @Test
    fun `every transaction type x property type pair resolves without throwing`() {
        for (transactionType in CaseTransactionType.entries) {
            for (propertyType in PropertyType.entries) {
                TransactionPropertyCompatibility.evaluate(transactionType, propertyType)
            }
        }
    }

    @Test
    fun `FULLY_SUPPORTED results never carry a note - nothing worth explaining`() {
        for (transactionType in CaseTransactionType.entries) {
            for (propertyType in PropertyType.entries) {
                val result = TransactionPropertyCompatibility.evaluate(transactionType, propertyType)
                if (result.grade == CompatibilityGrade.FULLY_SUPPORTED) {
                    assertNull(
                        "FULLY_SUPPORTED $transactionType x $propertyType should not carry a note",
                        result.note
                    )
                }
            }
        }
    }

    @Test
    fun `every non-FULLY_SUPPORTED result explains itself`() {
        for (transactionType in CaseTransactionType.entries) {
            for (propertyType in PropertyType.entries) {
                val result = TransactionPropertyCompatibility.evaluate(transactionType, propertyType)
                if (result.grade != CompatibilityGrade.FULLY_SUPPORTED) {
                    assertNotEquals(
                        "$transactionType x $propertyType (${result.grade}) has a blank note",
                        "",
                        result.note.orEmpty().trim()
                    )
                }
            }
        }
    }

    // ---- The exact examples the original spec called out as "obviously meaningless" ----

    @Test
    fun `construction partnership on a farm is not applicable`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.CONSTRUCTION_PARTNERSHIP, PropertyType.FARM)
        assertEquals(CompatibilityGrade.NOT_APPLICABLE, result.grade)
    }

    @Test
    fun `construction partnership on undeveloped agricultural land is not applicable`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.CONSTRUCTION_PARTNERSHIP, PropertyType.LAND_AGRICULTURAL)
        assertEquals(CompatibilityGrade.NOT_APPLICABLE, result.grade)
    }

    @Test
    fun `pre-sale on a farm is not applicable`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.PRE_SALE, PropertyType.FARM)
        assertEquals(CompatibilityGrade.NOT_APPLICABLE, result.grade)
    }

    @Test
    fun `pre-purchase on undeveloped agricultural land is not applicable - normalizes the same as pre-sale`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.PRE_PURCHASE, PropertyType.LAND_AGRICULTURAL)
        assertEquals(CompatibilityGrade.NOT_APPLICABLE, result.grade)
    }

    // ---- The two classic, unambiguously valid uses construction partnership exists for ----

    @Test
    fun `construction partnership on buildable land is fully supported`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.CONSTRUCTION_PARTNERSHIP, PropertyType.LAND_BUILDABLE)
        assertEquals(CompatibilityGrade.FULLY_SUPPORTED, result.grade)
    }

    @Test
    fun `construction partnership on a demolishable old house is fully supported`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.CONSTRUCTION_PARTNERSHIP, PropertyType.OLD_HOUSE)
        assertEquals(CompatibilityGrade.FULLY_SUPPORTED, result.grade)
    }

    @Test
    fun `pre-sale on a project is fully supported`() {
        val result = TransactionPropertyCompatibility.evaluate(CaseTransactionType.PRE_SALE, PropertyType.PROJECT)
        assertEquals(CompatibilityGrade.FULLY_SUPPORTED, result.grade)
    }

    // ---- Normalization: owner-side and client-side names of the same market event must agree ----

    @Test
    fun `sale and purchase grade identically for the same property type`() {
        for (propertyType in PropertyType.entries) {
            assertEquals(
                TransactionPropertyCompatibility.evaluate(CaseTransactionType.SALE, propertyType).grade,
                TransactionPropertyCompatibility.evaluate(CaseTransactionType.PURCHASE, propertyType).grade
            )
        }
    }

    @Test
    fun `pre-sale and pre-purchase grade identically for the same property type`() {
        for (propertyType in PropertyType.entries) {
            assertEquals(
                TransactionPropertyCompatibility.evaluate(CaseTransactionType.PRE_SALE, propertyType).grade,
                TransactionPropertyCompatibility.evaluate(CaseTransactionType.PRE_PURCHASE, propertyType).grade
            )
        }
    }

    // ---- The "OTHER" escape hatch always stays fully open ----

    @Test
    fun `OTHER is fully supported for every property type`() {
        for (propertyType in PropertyType.entries) {
            assertEquals(
                CompatibilityGrade.FULLY_SUPPORTED,
                TransactionPropertyCompatibility.evaluate(CaseTransactionType.OTHER, propertyType).grade
            )
        }
    }

    // ---- selectablePropertyTypes: the Create Case picker's actual contract ----

    @Test
    fun `selectablePropertyTypes never includes a NOT_APPLICABLE type`() {
        for (transactionType in CaseTransactionType.entries) {
            val selectable = TransactionPropertyCompatibility.selectablePropertyTypes(transactionType)
            selectable.forEach { propertyType ->
                val grade = TransactionPropertyCompatibility.evaluate(transactionType, propertyType).grade
                assertNotEquals(
                    "$propertyType should not be selectable for $transactionType",
                    CompatibilityGrade.NOT_APPLICABLE,
                    grade
                )
            }
        }
    }

    @Test
    fun `selectablePropertyTypes for construction partnership excludes farm and includes buildable land`() {
        val selectable = TransactionPropertyCompatibility.selectablePropertyTypes(CaseTransactionType.CONSTRUCTION_PARTNERSHIP)
        assertTrue(PropertyType.LAND_BUILDABLE in selectable)
        assertTrue(PropertyType.OLD_HOUSE in selectable)
        assertTrue(PropertyType.FARM !in selectable)
        assertTrue(PropertyType.LAND_AGRICULTURAL !in selectable)
        assertTrue(PropertyType.PROJECT !in selectable)
    }

    @Test
    fun `selectablePropertyTypes for pre-sale excludes land and farm, includes project`() {
        val selectable = TransactionPropertyCompatibility.selectablePropertyTypes(CaseTransactionType.PRE_SALE)
        assertTrue(PropertyType.PROJECT in selectable)
        assertTrue(PropertyType.SEMI_FINISHED_BUILDING in selectable)
        assertTrue(PropertyType.LAND !in selectable)
        assertTrue(PropertyType.FARM !in selectable)
        assertTrue(PropertyType.GARDEN !in selectable)
        assertTrue(PropertyType.OLD_HOUSE !in selectable)
    }
}
