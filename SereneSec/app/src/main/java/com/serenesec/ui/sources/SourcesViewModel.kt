package com.serenesec.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.domain.model.Source
import com.serenesec.domain.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val sourceRepository: SourceRepository
) : ViewModel() {
    
    val sources: StateFlow<List<Source>> = sourceRepository.getAllSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun toggleSource(id: String, enabled: Boolean) {
        viewModelScope.launch {
            sourceRepository.setSourceEnabled(id, enabled)
        }
    }
    
    fun deleteSource(id: String) {
        viewModelScope.launch {
            sourceRepository.deleteCustomSource(id)
        }
    }
    
    fun addSource(source: com.serenesec.domain.model.Source) {
        viewModelScope.launch {
            sourceRepository.addCustomSource(source)
        }
    }
}
