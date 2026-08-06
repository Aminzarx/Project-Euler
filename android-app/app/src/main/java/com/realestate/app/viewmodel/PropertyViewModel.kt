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
import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.CaseType
import com.realestate.app.data.code
import com.realestate.app.data.matchesPriceRange
import com.realestate.app.data.datastore.CaseWizardDefaults
import com.realestate.app.data.datastore.CaseWizardPreferencesRepository
import com.realestate.app.data.datastore.SearchHistoryRepository
import com.realestate.app.data.datastore.SecurityPreferencesRepository
import com.realestate.app.data.formatAppDate
import com.realestate.app.data.property.Note
import com.realestate.app.data.property.PropertyExtrasRepository
import com.realestate.app.data.property.TimelineEvent
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.ui.components.label
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    val maxPrice: Long? = null,
    // null = both owner listings and client requests together; set to scope the list to just one.
    val caseType: CaseType? = null
)

data class RecentActivity(
    val event: TimelineEvent,
    val propertyId: Long,
    val propertyTitle: String
)

/** Last-used values from the most recently added property — prefilled into the add-property form. */
data class SmartDefaults(
    val city: String? = null,
    val propertyType: PropertyType? = null,
    val dealType: DealType? = null
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
    private val securityPreferencesRepository = SecurityPreferencesRepository(application)
    private val caseWizardPreferencesRepository = CaseWizardPreferencesRepository(application)

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter

    /**
     * The single subscription to the properties table. Everything below derives from *this*
     * StateFlow rather than calling `repository.allProperties` again: that property is a cold Room
     * Flow, so each extra `stateIn` on it used to open its own query and re-map every row (type
     * converters included) on every single write. Toggling one favorite re-ran the same query four
     * times. Sharing one upstream keeps it at one.
     */
    val allProperties: StateFlow<List<Property>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProperties: StateFlow<List<Property>> = combine(
        allProperties, _filter
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
            val matchesPrice = property.matchesPriceRange(filter.minPrice, filter.maxPrice)
            val matchesCaseType = filter.caseType == null || property.caseType == filter.caseType
            matchesQuery && matchesCity && matchesDealType && matchesPropertyType &&
                matchesStatus && matchesTag && matchesPrice && matchesCaseType
        }.sortedByDescending { it.isPinned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProperties: StateFlow<List<Property>> = repository.favoriteProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyViewedProperties: StateFlow<List<Property>> = repository.recentlyViewedProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedProperties: StateFlow<List<Property>> = allProperties
        .map { list -> list.filter { it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Properties opened the most — a separate signal from "recently viewed" (most recent, not most frequent). */
    val frequentProperties: StateFlow<List<Property>> = allProperties
        .map { list -> list.filter { it.viewCount > 0 }.sortedByDescending { it.viewCount }.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Properties with a follow-up due today or overdue, soonest first. */
    val todayFollowUps: StateFlow<List<Property>> = allProperties
        .map { list ->
            val endOfToday = endOfTodayMillis()
            list.filter { it.followUpAt != null && it.followUpAt <= endOfToday }
                .sortedBy { it.followUpAt }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Collected straight into a sorted set instead of map/distinct/sorted, which built three
    // intermediate lists per emission.
    val availableCities: StateFlow<List<String>> = allProperties
        .map { properties -> properties.mapTo(sortedSetOf<String>()) { it.city }.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<String>> = allProperties
        .map { properties -> properties.flatMapTo(sortedSetOf<String>()) { it.tags }.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFolders: StateFlow<List<String>> = favoriteProperties
        .map { list -> list.mapNotNull { it.favoriteFolder }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = searchHistoryRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivities: StateFlow<List<RecentActivity>> = combine(
        extrasRepository.getRecentEvents(8), allProperties
    ) { events, properties -> events.toRecentActivities(properties) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Same feed as [recentActivities] but with a much larger window, for the "مشاهده همه" full-history screen. */
    val allRecentActivities: StateFlow<List<RecentActivity>> = combine(
        extrasRepository.getRecentEvents(200), allProperties
    ) { events, properties -> events.toRecentActivities(properties) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun List<TimelineEvent>.toRecentActivities(properties: List<Property>): List<RecentActivity> {
        val titleById = properties.associateBy({ it.id }, { it.title })
        return mapNotNull { event ->
            val title = titleById[event.propertyId] ?: return@mapNotNull null
            RecentActivity(event, event.propertyId, title)
        }
    }

    /** Prefills for the add-property form, based on the most recently added property. */
    val smartDefaults: StateFlow<SmartDefaults> = allProperties
        .map { list ->
            val last = list.maxByOrNull { it.dateAdded }
            if (last == null) {
                SmartDefaults()
            } else {
                SmartDefaults(city = last.city, propertyType = last.propertyType, dealType = last.dealType)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SmartDefaults())

    val caseWizardDefaults: StateFlow<CaseWizardDefaults> = caseWizardPreferencesRepository.defaults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CaseWizardDefaults())

    fun recordCaseTypeUsed(type: CaseType) = viewModelScope.launch {
        caseWizardPreferencesRepository.recordCaseType(type.name)
    }

    fun recordTransactionTypeUsed(caseType: CaseType, type: CaseTransactionType) = viewModelScope.launch {
        if (caseType == CaseType.OWNER) {
            caseWizardPreferencesRepository.recordOwnerTransactionType(type.name)
        } else {
            caseWizardPreferencesRepository.recordClientTransactionType(type.name)
        }
    }

    private var lastDeletedProperty: Property? = null
    private val _deletionEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deletionEvents: SharedFlow<String> = _deletionEvents

    // A save navigates straight back off this screen (see CreateCaseWizardScreen.save()), so a
    // Snackbar hosted on that screen would be torn down before anyone saw it — same reason
    // deletionEvents lives here instead of on the list screen. RealEstateApp's shell hosts the
    // Snackbar and outlives the navigation.
    private val _saveEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val saveEvents: SharedFlow<String> = _saveEvents

    fun undoLastDelete() = viewModelScope.launch {
        lastDeletedProperty?.let { property ->
            repository.insert(property)
            lastDeletedProperty = null
        }
    }

    /** Call once the delete snackbar is dismissed without the user tapping Undo, so the
     *  property's notes/timeline (kept around during the undo window) don't linger as
     *  orphaned rows forever. */
    fun finalizeDelete() = viewModelScope.launch {
        lastDeletedProperty?.let { property ->
            extrasRepository.deleteAllForProperty(property.id)
            lastDeletedProperty = null
        }
    }

    fun updateFilter(newFilter: PropertyFilter) {
        _filter.value = newFilter
    }

    fun recordSearch(query: String) = viewModelScope.launch {
        searchHistoryRepository.addSearch(query)
    }

    fun clearRecentSearches() = viewModelScope.launch {
        searchHistoryRepository.clear()
    }

    fun getPropertyById(id: Long) = repository.getPropertyById(id)

    fun getNotesForProperty(propertyId: Long) = extrasRepository.getNotesForProperty(propertyId)

    fun getTimelineForProperty(propertyId: Long) = extrasRepository.getTimelineForProperty(propertyId)

    fun updateNote(note: Note) = viewModelScope.launch {
        extrasRepository.updateNote(note)
    }

    private var lastDeletedNote: Note? = null
    private val _noteDeletionEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val noteDeletionEvents: SharedFlow<Unit> = _noteDeletionEvents

    fun deleteNote(note: Note) = viewModelScope.launch {
        lastDeletedNote = note
        extrasRepository.deleteNote(note.id)
        _noteDeletionEvents.emit(Unit)
    }

    fun undoLastNoteDelete() = viewModelScope.launch {
        lastDeletedNote?.let { note ->
            extrasRepository.restoreNote(note)
            lastDeletedNote = null
        }
    }

    fun addProperty(property: Property) = viewModelScope.launch {
        val id = repository.insert(property)
        extrasRepository.logEvent(id, TimelineEventType.CREATED, "ملک ثبت شد")
        _saveEvents.emit("پرونده ثبت شد")
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
        _saveEvents.emit("تغییرات ذخیره شد")
    }

    fun deleteProperty(property: Property) = viewModelScope.launch {
        lastDeletedProperty = property
        repository.delete(property)
        _deletionEvents.emit(property.title)
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

    // Assigning a work queue implies "keep this on my radar" — so it also marks the property as
    // favorite, otherwise it would silently never show up in the screen that lists work queues.
    fun setFavoriteFolder(property: Property, folder: String?) = viewModelScope.launch {
        val trimmedFolder = folder?.trim()?.takeIf { it.isNotEmpty() }
        repository.update(
            property.copy(
                favoriteFolder = trimmedFolder,
                isFavorite = property.isFavorite || trimmedFolder != null
            )
        )
    }

    fun markViewed(property: Property) = viewModelScope.launch {
        repository.update(property.copy(lastViewedAt = System.currentTimeMillis(), viewCount = property.viewCount + 1))
    }

    fun setFollowUp(property: Property, timestamp: Long?) = viewModelScope.launch {
        repository.update(property.copy(followUpAt = timestamp))
        if (timestamp != null) {
            val jalali = securityPreferencesRepository.settings.first().calendarJalali
            val formatted = formatAppDate(timestamp, jalali)
            extrasRepository.logEvent(property.id, TimelineEventType.FOLLOW_UP_SET, "پیگیری برای $formatted تنظیم شد")
        }
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

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun toggleSelection(propertyId: Long) {
        _selectedIds.value = _selectedIds.value.let { current ->
            if (current.contains(propertyId)) current - propertyId else current + propertyId
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun selectAll(ids: List<Long>) {
        _selectedIds.value = ids.toSet()
    }

    fun deleteSelected(properties: List<Property>) = viewModelScope.launch {
        repository.deleteProperties(properties)
        clearSelection()
    }
}

private fun endOfTodayMillis(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
    calendar.set(java.util.Calendar.MINUTE, 59)
    calendar.set(java.util.Calendar.SECOND, 59)
    calendar.set(java.util.Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}
