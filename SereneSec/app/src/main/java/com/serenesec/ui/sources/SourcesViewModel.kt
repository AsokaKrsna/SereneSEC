package com.serenesec.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.opml.OpmlManager
import com.serenesec.data.remote.DiscoveredFeed
import com.serenesec.data.remote.FeedDiscovery
import com.serenesec.domain.model.Source
import com.serenesec.domain.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val opmlManager: OpmlManager,
    private val feedDiscovery: FeedDiscovery
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
    
    fun addSource(source: Source) {
        viewModelScope.launch {
            sourceRepository.addCustomSource(source)
        }
    }

    /**
     * Auto-discovers feed URL and website title from a given URL
     */
    fun discoverFeed(url: String, onDiscovered: (DiscoveredFeed) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                feedDiscovery.discoverFeed(url)
            }
            onDiscovered(result)
        }
    }

    /**
     * Generates OPML 2.0 XML from current sources
     */
    fun exportOpml(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentSources = sources.value
            val opmlXml = withContext(Dispatchers.Default) {
                opmlManager.exportToOpml(currentSources)
            }
            onComplete(opmlXml)
        }
    }

    /**
     * Imports sources from an OPML input stream
     */
    fun importOpml(inputStream: InputStream, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                opmlManager.importFromOpml(inputStream)
            }
            for (source in result.sources) {
                sourceRepository.addCustomSource(source)
            }
            onComplete(result.sources.size)
        }
    }
}
