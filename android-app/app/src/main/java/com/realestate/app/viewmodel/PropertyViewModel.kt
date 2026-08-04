package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyRepository
import com.realestate.app.data.PropertyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PropertyFilter(
    val query: String = "",
    val city: String? = null,
    val dealType: DealType? = null,
    val propertyType: PropertyType? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null
)

class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter

    val filteredProperties: StateFlow<List<Property>> = combine(
        repository.allProperties, _filter
    ) { properties, filter ->
        properties.filter { property ->
            val matchesQuery = filter.query.isBlank() ||
                property.title.contains(filter.query, ignoreCase = true) ||
                property.address.contains(filter.query, ignoreCase = true) ||
                property.city.contains(filter.query, ignoreCase = true)
            val matchesCity = filter.city == null || property.city == filter.city
            val matchesDealType = filter.dealType == null || property.dealType == filter.dealType
            val matchesPropertyType = filter.propertyType == null || property.propertyType == filter.propertyType
            val matchesMinPrice = filter.minPrice == null || property.price >= filter.minPrice
            val matchesMaxPrice = filter.maxPrice == null || property.price <= filter.maxPrice
            matchesQuery && matchesCity && matchesDealType && matchesPropertyType && matchesMinPrice && matchesMaxPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProperties: StateFlow<List<Property>> = repository.favoriteProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCities: StateFlow<List<String>> = repository.allProperties
        .map { properties -> properties.map { it.city }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(newFilter: PropertyFilter) {
        _filter.value = newFilter
    }

    fun getPropertyById(id: Long) = repository.getPropertyById(id)

    fun addProperty(property: Property) = viewModelScope.launch {
        repository.insert(property)
    }

    fun updateProperty(property: Property) = viewModelScope.launch {
        repository.update(property)
    }

    fun deleteProperty(property: Property) = viewModelScope.launch {
        repository.delete(property)
    }

    fun toggleFavorite(property: Property) = viewModelScope.launch {
        repository.update(property.copy(isFavorite = !property.isFavorite))
    }
}
