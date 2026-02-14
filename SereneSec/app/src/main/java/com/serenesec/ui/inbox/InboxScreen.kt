package com.serenesec.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import com.serenesec.ui.theme.CategoryCVE
import com.serenesec.ui.theme.CategoryNews
import com.serenesec.ui.theme.CategoryResearch
import com.serenesec.ui.theme.CategoryTools
import com.serenesec.ui.theme.CategoryThreatIntel
import com.serenesec.ui.theme.CategoryGovAdvisory
import com.serenesec.ui.theme.CategoryMalware
import com.serenesec.ui.theme.CategoryExploits
import com.serenesec.ui.theme.CategoryPrograms
import com.serenesec.ui.theme.CategoryWriteups
import com.serenesec.ui.theme.CategoryPodcast
import com.serenesec.ui.theme.CategoryLearning
import com.serenesec.ui.theme.CategoryDefense
import com.serenesec.ui.theme.CategoryCloud
import com.serenesec.ui.theme.CategoryTradecraft
import com.serenesec.ui.theme.CategoryIncidents
import com.serenesec.ui.theme.CategoryConference

enum class BottomNavItem {
    INBOX, FAVORITES, SAVED, COLLECTIONS, ARCHIVE, WEBSITES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onArticleClick: (Article) -> Unit,
    onSettingsClick: () -> Unit,
    onAddSourceClick: () -> Unit = {},
    onCollectionsClick: () -> Unit = {},
    onWebsitesClick: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val readStatusFilter by viewModel.readStatusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedSources by viewModel.selectedSources.collectAsState()
    val selectedKeywords by viewModel.selectedKeywords.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val availableSources by viewModel.availableSources.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val refreshError by viewModel.refreshError.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var selectedNavItem by remember { mutableStateOf(BottomNavItem.INBOX) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedArticleForTags by remember { mutableStateOf<Article?>(null) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Sync nav item with filter
    LaunchedEffect(selectedFilter) {
        selectedNavItem = when (selectedFilter) {
            InboxFilter.ALL -> BottomNavItem.INBOX
            InboxFilter.FAVORITES -> BottomNavItem.FAVORITES
            InboxFilter.SAVED_FOR_LATER -> BottomNavItem.SAVED
        }
    }
    
    // Detect Archive selection and handle separately
    LaunchedEffect(selectedNavItem) {
        if (selectedNavItem == BottomNavItem.ARCHIVE) {
            viewModel.showArchivedArticles()
        }
    }
    
    // Show error snackbar
    LaunchedEffect(refreshError) {
        refreshError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // Initial refresh
    LaunchedEffect(Unit) {
        if (articles.isEmpty()) {
            viewModel.refresh()
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SereneSec",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitle = when (selectedNavItem) {
                            BottomNavItem.INBOX -> if (unreadCount > 0) "$unreadCount unread" else "All caught up"
                            BottomNavItem.FAVORITES -> "Favorites"
                            BottomNavItem.SAVED -> "Saved for later"
                            BottomNavItem.COLLECTIONS -> "Your collections"
                            BottomNavItem.ARCHIVE -> "Archived articles"
                            BottomNavItem.WEBSITES -> "Quick access sites"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Filter button with badge
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                    ) {
                                        Text(activeFilterCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters"
                            )
                        }
                    }
                    
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            ScrollableBottomNav(
                selectedItem = selectedNavItem,
                onItemSelected = { item ->
                    selectedNavItem = item
                    when (item) {
                        BottomNavItem.INBOX -> {
                            viewModel.hideArchive()
                            viewModel.selectFilter(InboxFilter.ALL)
                        }
                        BottomNavItem.FAVORITES -> {
                            viewModel.hideArchive()
                            viewModel.selectFilter(InboxFilter.FAVORITES)
                        }
                        BottomNavItem.SAVED -> {
                            viewModel.hideArchive()
                            viewModel.selectFilter(InboxFilter.SAVED_FOR_LATER)
                        }
                        BottomNavItem.COLLECTIONS -> {
                            viewModel.hideArchive()
                            onCollectionsClick()
                        }
                        BottomNavItem.ARCHIVE -> {
                            viewModel.showArchivedArticles()
                        }
                        BottomNavItem.WEBSITES -> {
                            viewModel.hideArchive()
                            onWebsitesClick()
                        }
                    }
                }
            )
        },

        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                if (selectedNavItem != BottomNavItem.ARCHIVE) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search articles...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Category filter chips
                if (selectedNavItem == BottomNavItem.INBOX) {
                    CategoryFilterRow(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                    
                    // Custom tags filter - compact row
                    CustomTagsFilterRow(
                        viewModel = hiltViewModel(),
                        selectedTagIds = selectedTags,
                        onTagToggle = { viewModel.toggleTagFilter(it) }
                    )
                }
                
                // Active filters summary
                if (activeFilterCount > 0 && selectedNavItem != BottomNavItem.ARCHIVE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${articles.size} results • $activeFilterCount filter${if (activeFilterCount > 1) "s" else ""} active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Text("Clear All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                // Articles list or empty state
                if (articles.isEmpty() && !isRefreshing) {
                    EmptyStateScreen(filter = selectedFilter, hasFilters = activeFilterCount > 0)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = articles,
                            key = { it.id }
                        ) { article ->
                            ArticleCard(
                                article = article,
                                onClick = {
                                    viewModel.markAsRead(article.id)
                                    onArticleClick(article)
                                },
                                onLongClick = {
                                    selectedArticleForTags = article
                                },
                                onArchive = { 
                                    if (selectedNavItem == BottomNavItem.ARCHIVE) {
                                        viewModel.restoreArticle(article.id)
                                    } else {
                                        viewModel.archiveArticle(article.id)
                                    }
                                },
                                onFavoriteToggle = { viewModel.toggleFavorite(article.id, it) },
                                onSaveForLaterToggle = { viewModel.toggleSavedForLater(article.id, it) },
                                isArchived = selectedNavItem == BottomNavItem.ARCHIVE
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Article actions dialog on long-press
    selectedArticleForTags?.let { article ->
        ArticleActionsBottomSheet(
            articleId = article.id,
            onDismiss = { selectedArticleForTags = null }
        )
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState
        ) {
            FilterBottomSheet(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { viewModel.setTimeRange(it) },
                readStatusFilter = readStatusFilter,
                onReadStatusSelected = { viewModel.setReadStatusFilter(it) },
                sortOption = sortOption,
                onSortOptionSelected = { viewModel.setSortOption(it) },
                selectedKeywords = selectedKeywords,
                onKeywordToggle = { viewModel.toggleKeywordTag(it) },
                onClearKeywords = { viewModel.clearKeywordFilters() },
                availableSources = availableSources,
                selectedSources = selectedSources,
                onSourceToggle = { viewModel.toggleSourceFilter(it) },
                onClearSources = { viewModel.clearSourceFilters() },
                onClearAll = { viewModel.clearAllFilters() },
                onApply = { showFilterSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    readStatusFilter: ReadStatusFilter,
    onReadStatusSelected: (ReadStatusFilter) -> Unit,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    selectedKeywords: Set<KeywordTag>,
    onKeywordToggle: (KeywordTag) -> Unit,
    onClearKeywords: () -> Unit,
    availableSources: List<Source>,
    selectedSources: Set<String>,
    onSourceToggle: (String) -> Unit,
    onClearSources: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters & Sort",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearAll) {
                Text("Reset All")
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Sort Options
        FilterSectionHeader(title = "Sort By", icon = Icons.Default.Sort)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.entries.forEach { option ->
                FilterChip(
                    selected = sortOption == option,
                    onClick = { onSortOptionSelected(option) },
                    label = { Text(option.displayName) },
                    leadingIcon = if (sortOption == option) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        
        // Time Range
        FilterSectionHeader(title = "Time Range")
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.entries.forEach { range ->
                FilterChip(
                    selected = selectedTimeRange == range,
                    onClick = { onTimeRangeSelected(range) },
                    label = { Text(range.displayName) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Read Status
        FilterSectionHeader(title = "Read Status")
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReadStatusFilter.entries.forEach { status ->
                FilterChip(
                    selected = readStatusFilter == status,
                    onClick = { onReadStatusSelected(status) },
                    label = { Text(status.displayName) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        
        // Keyword Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterSectionHeader(title = "Quick Keywords")
            if (selectedKeywords.isNotEmpty()) {
                TextButton(onClick = onClearKeywords) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeywordTag.entries.forEach { tag ->
                val isSelected = tag in selectedKeywords
                FilterChip(
                    selected = isSelected,
                    onClick = { onKeywordToggle(tag) },
                    label = { Text(tag.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getKeywordColor(tag).copy(alpha = 0.2f),
                        selectedLabelColor = getKeywordColor(tag)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        
        // Source Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterSectionHeader(
                title = if (selectedSources.isEmpty()) "Sources (All)" 
                       else "Sources (${selectedSources.size} selected)"
            )
            if (selectedSources.isNotEmpty()) {
                TextButton(onClick = onClearSources) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableSources.forEach { source ->
                val isSelected = source.id in selectedSources
                FilterChip(
                    selected = isSelected,
                    onClick = { onSourceToggle(source.id) },
                    label = { 
                        Text(
                            text = source.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = onApply,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Done", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FilterSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun getKeywordColor(tag: KeywordTag): Color {
    return when (tag) {
        KeywordTag.CVE -> Color(0xFFE53935)
        KeywordTag.RANSOMWARE -> Color(0xFFD81B60)
        KeywordTag.ZERO_DAY -> Color(0xFFFF5722)
        KeywordTag.MALWARE -> Color(0xFF9C27B0)
        KeywordTag.PHISHING -> Color(0xFF3F51B5)
        KeywordTag.APT -> Color(0xFF607D8B)
        KeywordTag.DATA_BREACH -> Color(0xFFF57C00)
        KeywordTag.PATCH -> Color(0xFF43A047)
        KeywordTag.CRITICAL -> Color(0xFFB71C1C)
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    val categories = listOf(
        null to "All",
        Category.NEWS to "News",
        Category.RESEARCH to "Research",
        Category.THREAT_INTEL to "Threat Intel",
        Category.TOOLS to "Tools",
        Category.CVE to "CVEs",
        Category.GOV_ADVISORY to "Gov",
        Category.MALWARE to "Malware",
        Category.EXPLOITS to "Exploits",
        Category.DEFENSE to "Defense",
        Category.CLOUD to "Cloud",
        Category.TRADECRAFT to "Tradecraft",
        Category.WRITEUPS to "Writeups",
        Category.LEARNING to "Learning",
        Category.PROGRAMS to "Programs",
        Category.PODCAST to "Podcast",
        Category.INCIDENTS to "Incidents",
        Category.CONFERENCE to "Conference"
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { (category, label) ->
            val isSelected = selectedCategory == category
            val chipColor = when (category) {
                Category.NEWS -> CategoryNews
                Category.TOOLS -> CategoryTools
                Category.CVE -> CategoryCVE
                Category.RESEARCH -> CategoryResearch
                Category.THREAT_INTEL -> CategoryThreatIntel
                Category.GOV_ADVISORY -> CategoryGovAdvisory
                Category.MALWARE -> CategoryMalware
                Category.EXPLOITS -> CategoryExploits
                Category.PROGRAMS -> CategoryPrograms
                Category.WRITEUPS -> CategoryWriteups
                Category.PODCAST -> CategoryPodcast
                Category.LEARNING -> CategoryLearning
                Category.DEFENSE -> CategoryDefense
                Category.CLOUD -> CategoryCloud
                Category.TRADECRAFT -> CategoryTradecraft
                Category.INCIDENTS -> CategoryIncidents
                Category.CONFERENCE -> CategoryConference
                null -> MaterialTheme.colorScheme.primary
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor
                )
            )
        }
    }
}

@Composable
private fun EmptyStateScreen(filter: InboxFilter, hasFilters: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val (icon, title, subtitle) = if (hasFilters) {
                Triple(
                    Icons.Default.Search,
                    "No Matches Found",
                    "Try adjusting your filters\nor search query."
                )
            } else {
                when (filter) {
                    InboxFilter.ALL -> Triple(
                        Icons.Default.CheckCircle,
                        "All Caught Up",
                        "You've read everything.\nPull down to refresh."
                    )
                    InboxFilter.FAVORITES -> Triple(
                        Icons.Default.Favorite,
                        "No Favorites Yet",
                        "Tap the heart icon on articles\nto add them here."
                    )
                    InboxFilter.SAVED_FOR_LATER -> Triple(
                        Icons.Default.Bookmark,
                        "Nothing Saved",
                        "Tap the bookmark icon on articles\nto save them for later."
                    )
                }
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
