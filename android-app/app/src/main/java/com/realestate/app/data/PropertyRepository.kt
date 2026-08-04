package com.realestate.app.data

import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val dao: PropertyDao) {
    val allProperties: Flow<List<Property>> = dao.getAllProperties()
    val favoriteProperties: Flow<List<Property>> = dao.getFavoriteProperties()

    fun getPropertyById(id: Long): Flow<Property?> = dao.getPropertyById(id)

    suspend fun insert(property: Property): Long = dao.insert(property)

    suspend fun update(property: Property) = dao.update(property)

    suspend fun delete(property: Property) = dao.delete(property)
}
