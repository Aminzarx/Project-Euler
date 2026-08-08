package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.data.dealassistant.QuickNote
import com.realestate.app.data.datastore.RecentToolsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DealAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val quickNoteDao = AppDatabase.getInstance(application).quickNoteDao()
    private val recentToolsRepository = RecentToolsRepository(application)

    val quickNotes: StateFlow<List<QuickNote>> = quickNoteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTools: StateFlow<List<DealToolId>> = recentToolsRepository.recentToolKeys
        .map { keys -> keys.mapNotNull { DealToolId.fromKey(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordToolUsage(tool: DealToolId) = viewModelScope.launch {
        recentToolsRepository.recordUsage(tool.name)
    }

    fun addQuickNote(content: String) = viewModelScope.launch {
        if (content.isNotBlank()) quickNoteDao.insert(QuickNote(content = content.trim()))
    }

    fun deleteQuickNote(note: QuickNote) = viewModelScope.launch {
        quickNoteDao.delete(note)
    }
}
