package com.serenesec.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.TagDao
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import com.serenesec.domain.repository.ArticleRepository
import com.serenesec.domain.repository.SourceRepository
import com.serenesec.domain.usecase.FetchFeedsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class InboxFilter {
    ALL, FAVORITES, SAVED_FOR_LATER
}

enum class TimeRange(val displayName: String, val daysBack: Int) {
    TODAY("Today", 1),
    THIS_WEEK("This Week", 7),
    THIS_MONTH("This Month", 30),
    ALL_TIME("All Time", 0)
}

enum class ReadStatusFilter(val displayName: String) {
    ALL("All"),
    UNREAD_ONLY("Unread"),
    READ_ONLY("Read")
}

enum class SortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    TITLE_AZ("Title A-Z"),
    TITLE_ZA("Title Z-A"),
    SOURCE_NAME("By Source")
}

// Popular security keywords for quick filtering
enum class KeywordTag(val displayName: String, val keywords: List<String>) {
    CVE("CVE", listOf("cve-", "cve ", "vulnerability", "vulnerabilities")),
    RANSOMWARE("Ransomware", listOf("ransomware", "ransom")),
    ZERO_DAY("Zero-Day", listOf("zero-day", "0-day", "zeroday", "0day")),
    MALWARE("Malware", listOf("malware", "trojan", "botnet", "worm")),
    PHISHING("Phishing", listOf("phishing", "credential", "social engineering")),
    APT("APT/Nation State", listOf("apt", "nation-state", "state-sponsored", "threat actor")),
    DATA_BREACH("Data Breach", listOf("breach", "leak", "exposed", "compromised")),
    PATCH("Patch/Update", listOf("patch", "update", "fix", "security update")),
    CRITICAL("Critical", listOf("critical", "urgent", "emergency", "severe"))
}

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val sourceRepository: SourceRepository,
    private val fetchFeedsUseCase: FetchFeedsUseCase,
    private val tagDao: TagDao
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(InboxFilter.ALL)
    val selectedFilter: StateFlow<InboxFilter> = _selectedFilter.asStateFlow()
    
    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()
    
    private val _readStatusFilter = MutableStateFlow(ReadStatusFilter.ALL)
    val readStatusFilter: StateFlow<ReadStatusFilter> = _readStatusFilter.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.NEWEST_FIRST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet()) // Empty = all sources
    val selectedSources: StateFlow<Set<String>> = _selectedSources.asStateFlow()
    
    private val _selectedKeywords = MutableStateFlow<Set<KeywordTag>>(emptySet())
    val selectedKeywords: StateFlow<Set<KeywordTag>> = _selectedKeywords.asStateFlow()
    
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet()) // Tag IDs
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()
    
    // All enabled sources for filter UI
    val availableSources: StateFlow<List<Source>> = sourceRepository.getEnabledSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Combine articles flow with all filters
    private val _showArchived = MutableStateFlow(false)
    
    val articles: StateFlow<List<Article>> = combine(
        articleRepository.getInboxArticles(),
        articleRepository.getFavoriteArticles(),
        articleRepository.getSavedForLaterArticles(),
        articleRepository.getArchivedArticles(),
        _selectedCategory,
        _selectedFilter,
        _selectedTimeRange,
        _readStatusFilter,
        _searchQuery,
        _sortOption,
        _selectedSources,
        _selectedKeywords,
        _selectedTags,
        _showArchived
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val inbox = values[0] as List<Article>
        @Suppress("UNCHECKED_CAST")
        val favorites = values[1] as List<Article>
        @Suppress("UNCHECKED_CAST")
        val saved = values[2] as List<Article>
        @Suppress("UNCHECKED_CAST")
        val archived = values[3] as List<Article>
        val category = values[4] as Category?
        val filter = values[5] as InboxFilter
        val timeRange = values[6] as TimeRange
        val readStatus = values[7] as ReadStatusFilter
        val search = values[8] as String
        val sort = values[9] as SortOption
        @Suppress("UNCHECKED_CAST")
        val sources = values[10] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val keywords = values[11] as Set<KeywordTag>
        @Suppress("UNCHECKED_CAST")
        val selectedTagIds = values[12] as Set<String>
        val showArchived = values[13] as Boolean
        
        // Select base list
        var baseList = if (showArchived) {
            archived
        } else {
            when (filter) {
                InboxFilter.ALL -> inbox
                InboxFilter.FAVORITES -> favorites
                InboxFilter.SAVED_FOR_LATER -> saved
            }
        }
        
        // Apply category filter
        if (category != null) {
            baseList = baseList.filter { it.category == category }
        }
        
        // Apply source filter
        if (sources.isNotEmpty()) {
            baseList = baseList.filter { it.sourceId in sources }
        }
        
        // Apply time range filter
        if (timeRange.daysBack > 0) {
            val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(timeRange.daysBack.toLong())
            baseList = baseList.filter { it.publishedDate >= cutoffTime }
        }
        
        // Apply read status filter
        baseList = when (readStatus) {
            ReadStatusFilter.ALL -> baseList
            ReadStatusFilter.UNREAD_ONLY -> baseList.filter { it.isUnread }
            ReadStatusFilter.READ_ONLY -> baseList.filter { it.isRead }
        }
        
        // Apply keyword filter (OR logic - match any selected keyword)
        if (keywords.isNotEmpty()) {
            baseList = baseList.filter { article ->
                val text = "${article.title} ${article.summary ?: ""}".lowercase()
                keywords.any { tag ->
                    tag.keywords.any { keyword -> text.contains(keyword) }
                }
            }
        }
        
        // Apply custom tag filter (AND logic - must have all selected tags)
        if (selectedTagIds.isNotEmpty()) {
            baseList = baseList.filter { article ->
                // Get all tag IDs for this article
                val articleTagIds = try {
                    tagDao.getTagsForArticleList(article.id).map { it.id }.toSet()
                } catch (e: Exception) {
                    emptySet()
                }
                // Article must have all selected tags
                selectedTagIds.all { it in articleTagIds }
            }
        }
        
        // Apply search filter
        if (search.isNotBlank()) {
            val query = search.lowercase()
            baseList = baseList.filter { article ->
                article.title.lowercase().contains(query) ||
                article.summary?.lowercase()?.contains(query) == true ||
                article.author?.lowercase()?.contains(query) == true
            }
        }
        
        // Apply sorting
        baseList = when (sort) {
            SortOption.NEWEST_FIRST -> baseList.sortedByDescending { it.publishedDate }
            SortOption.OLDEST_FIRST -> baseList.sortedBy { it.publishedDate }
            SortOption.TITLE_AZ -> baseList.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> baseList.sortedByDescending { it.title.lowercase() }
            SortOption.SOURCE_NAME -> baseList.sortedBy { it.sourceId }
        }
        
        baseList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val unreadCount: StateFlow<Int> = articleRepository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    // Count active filters for badge
    val activeFilterCount: StateFlow<Int> = combine(
        _selectedTimeRange,
        _readStatusFilter,
        _selectedSources,
        _selectedKeywords,
        _selectedTags,
        _searchQuery
    ) { flows ->
        val timeRange = flows[0] as TimeRange
        val readStatus = flows[1] as ReadStatusFilter
        @Suppress("UNCHECKED_CAST")
        val sources = flows[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val keywords = flows[3] as Set<KeywordTag>
        @Suppress("UNCHECKED_CAST")
        val tags = flows[4] as Set<String>
        val search = flows[5] as String
        
        var count = 0
        if (timeRange != TimeRange.ALL_TIME) count++
        if (readStatus != ReadStatusFilter.ALL) count++
        if (sources.isNotEmpty()) count++
        if (keywords.isNotEmpty()) count++
        if (tags.isNotEmpty()) count++
        if (search.isNotBlank()) count++
        count
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    init {
        viewModelScope.launch {
            sourceRepository.initializeBuiltInSources()
        }
    }
    
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }
    
    fun selectFilter(filter: InboxFilter) {
        _selectedFilter.value = filter
    }
    
    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }
    
    fun setReadStatusFilter(status: ReadStatusFilter) {
        _readStatusFilter.value = status
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    fun toggleSourceFilter(sourceId: String) {
        val current = _selectedSources.value.toMutableSet()
        if (sourceId in current) {
            current.remove(sourceId)
        } else {
            current.add(sourceId)
        }
        _selectedSources.value = current
    }
    
    fun clearSourceFilters() {
        _selectedSources.value = emptySet()
    }
    
    fun toggleKeywordTag(tag: KeywordTag) {
        val current = _selectedKeywords.value.toMutableSet()
        if (tag in current) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _selectedKeywords.value = current
    }
    
    fun clearKeywordFilters() {
        _selectedKeywords.value = emptySet()
    }
    
    fun toggleTagFilter(tagId: String) {
        val current = _selectedTags.value.toMutableSet()
        if (tagId in current) {
            current.remove(tagId)
        } else {
            current.add(tagId)
        }
        _selectedTags.value = current
    }
    
    fun clearTagFilters() {
        _selectedTags.value = emptySet()
    }
    
    fun clearAllFilters() {
        _selectedCategory.value = null
        _selectedTimeRange.value = TimeRange.ALL_TIME
        _readStatusFilter.value = ReadStatusFilter.ALL
        _searchQuery.value = ""
        _selectedSources.value = emptySet()
        _selectedKeywords.value = emptySet()
        _selectedTags.value = emptySet()
        _sortOption.value = SortOption.NEWEST_FIRST
    }
    
    fun refresh() {
        if (_isRefreshing.value) return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshError.value = null
            
            val result = fetchFeedsUseCase()
            
            result.onFailure { error ->
                _refreshError.value = error.message ?: "Failed to refresh feeds"
            }
            
            _isRefreshing.value = false
        }
    }
    
    fun markAsRead(articleId: String) {
        viewModelScope.launch {
            articleRepository.markAsRead(articleId)
        }
    }
    
    fun archiveArticle(articleId: String) {
        viewModelScope.launch {
            articleRepository.archiveArticle(articleId)
        }
    }
    
    fun restoreArticle(articleId: String) {
        viewModelScope.launch {
            articleRepository.restoreArticle(articleId)
        }
    }
    
    fun toggleFavorite(articleId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId, isFavorite)
        }
    }
    
    fun toggleSavedForLater(articleId: String, saved: Boolean) {
        viewModelScope.launch {
            articleRepository.toggleSavedForLater(articleId, saved)
        }
    }
    
    fun clearError() {
        _refreshError.value = null
    }
    
    fun showArchivedArticles() {
        _showArchived.value = true
        _selectedFilter.value = InboxFilter.ALL // Reset filter
    }
    
    fun hideArchive() {
        _showArchived.value = false
    }
}
