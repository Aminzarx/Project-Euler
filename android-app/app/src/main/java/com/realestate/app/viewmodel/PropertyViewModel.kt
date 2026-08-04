package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyRepository
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.code
import com.realestate.app.data.datastore.SearchHistoryRepository
import com.realestate.app.data.property.PropertyExtrasRepository
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.ui.components.label
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class PropertyFilter(
    val query: String = "",
    val city: String? = null,
    val dealType: DealType? = null,
    val propertyType: PropertyType? = null,
    val status: PropertyStatus? = null,
    val tag: String? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null
)

class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )
    private val extrasRepository = PropertyExtrasRepository(
        AppDatabase.getInstance(application).noteDao(),
        AppDatabase.getInstance(application).timelineDao()
    )
    private val searchHistoryRepository = SearchHistoryRepository(application)

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter

    val allProperties: StateFlow<List<Property>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProperties: StateFlow<List<Property>> = combine(
        repository.allProperties, _filter
    ) { properties, filter ->
        properties.filter { property ->
            val matchesQuery = filter.query.isBlank() ||
                property.title.contains(filter.query, ignoreCase = true) ||
                property.address.contains(filter.query, ignoreCase = true) ||
                property.city.contains(filter.query, ignoreCase = true) ||
                property.code.contains(filter.query, ignoreCase = true)
            val matchesCity = filter.city == null || property.city == filter.city
            val matchesDealType = filter.dealType == null || property.dealType == filter.dealType
            val matchesPropertyType = filter.propertyType == null || property.propertyType == filter.propertyType
            val matchesStatus = filter.status == null || property.status == filter.status
            val matchesTag = filter.tag == null || property.tags.contains(filter.tag)
            val matchesMinPrice = filter.minPrice == null || property.price >= filter.minPrice
            val matchesMaxPrice = filter.maxPrice == null || property.price <= filter.maxPrice
            matchesQuery && matchesCity && matchesDealType && matchesPropertyType &&
                matchesStatus && matchesTag && matchesMinPrice && matchesMaxPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProperties: StateFlow<List<Property>> = repository.favoriteProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyViewedProperties: StateFlow<List<Property>> = repository.recentlyViewedProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedProperties: StateFlow<List<Property>> = allProperties
        .map { list -> list.filter { it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCities: StateFlow<List<String>> = repository.allProperties
        .map { properties -> properties.map { it.city }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<String>> = repository.allProperties
        .map { properties -> properties.flatMap { it.tags }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFolders: StateFlow<List<String>> = favoriteProperties
        .map { list -> list.mapNotNull { it.favoriteFolder }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = searchHistoryRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(newFilter: PropertyFilter) {
        _filter.value = newFilter
    }

    fun recordSearch(query: String) = viewModelScope.launch {
        searchHistoryRepository.addSearch(query)
    }

    fun getPropertyById(id: Long) = repository.getPropertyById(id)

    fun getNotesForProperty(propertyId: Long) = extrasRepository.getNotesForProperty(propertyId)

    fun getTimelineForProperty(propertyId: Long) = extrasRepository.getTimelineForProperty(propertyId)

    fun addProperty(property: Property) = viewModelScope.launch {
        val id = repository.insert(property)
        extrasRepository.logEvent(id, TimelineEventType.CREATED, "ملک ثبت شد")
    }

    fun updateProperty(property: Property) = viewModelScope.launch {
        val previous = repository.getPropertyById(property.id).first()
        repository.update(property.copy(lastModifiedAt = System.currentTimeMillis()))
        extrasRepository.logEvent(property.id, TimelineEventType.EDITED, "اطلاعات ملک ویرایش شد")
        if (previous != null && previous.price != property.price) {
            val from = NumberFormat.getNumberInstance(Locale.US).format(previous.price)
            val to = NumberFormat.getNumberInstance(Locale.US).format(property.price)
            extrasRepository.logEvent(
                property.id,
                TimelineEventType.PRICE_CHANGED,
                "قیمت از $from به $to تومان تغییر کرد"
            )
        }
    }

    fun deleteProperty(property: Property) = viewModelScope.launch {
        repository.delete(property)
    }

    fun archiveProperty(property: Property) = viewModelScope.launch {
        repository.update(property.copy(status = PropertyStatus.ARCHIVED, lastModifiedAt = System.currentTimeMillis()))
        extrasRepository.logEvent(property.id, TimelineEventType.ARCHIVED, "ملک بایگانی شد")
    }

    fun restoreProperty(property: Property) = viewModelScope.launch {
        repository.update(property.copy(status = PropertyStatus.ACTIVE, lastModifiedAt = System.currentTimeMillis()))
        extrasRepository.logEvent(property.id, TimelineEventType.RESTORED, "ملک از بایگانی بازگردانده شد")
    }

    fun updateStatus(property: Property, status: PropertyStatus) = viewModelScope.launch {
        repository.update(property.copy(status = status, lastModifiedAt = System.currentTimeMillis()))
        extrasRepository.logEvent(property.id, TimelineEventType.EDITED, "وضعیت به «${status.label()}» تغییر کرد")
    }

    fun addTag(property: Property, tag: String) = viewModelScope.launch {
        val trimmed = tag.trim()
        if (trimmed.isEmpty() || property.tags.contains(trimmed)) return@launch
        repository.update(property.copy(tags = property.tags + trimmed, lastModifiedAt = System.currentTimeMillis()))
    }

    fun removeTag(property: Property, tag: String) = viewModelScope.launch {
        repository.update(property.copy(tags = property.tags - tag, lastModifiedAt = System.currentTimeMillis()))
    }

    fun toggleFavorite(property: Property) = viewModelScope.launch {
        repository.update(property.copy(isFavorite = !property.isFavorite))
    }

    fun togglePinned(property: Property) = viewModelScope.launch {
        repository.update(property.copy(isPinned = !property.isPinned))
    }

    fun setFavoriteFolder(property: Property, folder: String?) = viewModelScope.launch {
        repository.update(property.copy(favoriteFolder = folder?.trim()?.takeIf { it.isNotEmpty() }))
    }

    fun markViewed(property: Property) = viewModelScope.launch {
        repository.update(property.copy(lastViewedAt = System.currentTimeMillis()))
    }

    fun markShared(property: Property) = viewModelScope.launch {
        repository.update(property.copy(lastSharedAt = System.currentTimeMillis()))
        extrasRepository.logEvent(property.id, TimelineEventType.SHARED, "ملک به اشتراک گذاشته شد")
    }

    fun addNote(propertyId: Long, content: String) = viewModelScope.launch {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return@launch
        extrasRepository.addNote(propertyId, trimmed)
        extrasRepository.logEvent(propertyId, TimelineEventType.NOTE_ADDED, "یادداشت جدید افزوده شد")
    }
}
