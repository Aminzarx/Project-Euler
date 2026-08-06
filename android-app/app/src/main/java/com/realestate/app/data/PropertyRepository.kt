package com.realestate.app.data

import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val dao: PropertyDao) {
    val allProperties: Flow<List<Property>> = dao.getAllProperties()
    val favoriteProperties: Flow<List<Property>> = dao.getFavoriteProperties()
    val recentlyViewedProperties: Flow<List<Property>> = dao.getRecentlyViewedProperties()

    fun getPropertyById(id: Long): Flow<Property?> = dao.getPropertyById(id)

    suspend fun insert(property: Property): Long = dao.insert(property)

    suspend fun insertAll(properties: List<Property>) = dao.insertAll(properties)

    suspend fun update(property: Property) = dao.update(property)

    suspend fun updateAll(properties: List<Property>) = dao.updateAll(properties)

    suspend fun delete(property: Property) = dao.delete(property)

    suspend fun deleteProperties(properties: List<Property>) = dao.deleteProperties(properties)
}
