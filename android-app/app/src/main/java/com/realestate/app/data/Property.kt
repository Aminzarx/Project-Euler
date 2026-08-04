package com.realestate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DealType { SALE, RENT }

enum class PropertyType { APARTMENT, VILLA, LAND, OFFICE, SHOP }

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Long,
    val area: Double,
    val rooms: Int,
    val city: String,
    val address: String,
    val ownerPhone: String,
    val dealType: DealType,
    val propertyType: PropertyType,
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
