package com.serenesec.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.ArticleDao
import com.serenesec.data.local.dao.SourceDao
import com.serenesec.domain.model.Source
import com.serenesec.domain.model.SourceHealth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsState(
    val totalSources: Int = 0,
    val enabledSources: Int = 0,
    val totalArticles: Int = 0,
    val totalFetches: Int = 0,
    val totalErrors: Int = 0,
    val overallSuccessRate: Int = 100,
    val healthySources: Int = 0,
    val unhealthySources: Int = 0,
    val sources: List<Source> = emptyList()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val sourceDao: SourceDao,
    private val articleDao: ArticleDao
) : ViewModel() {
    
    // Combine first 3 flows
    private val sourcesFlow = combine(
        sourceDao.getAllSources().map { list -> list.map { it.toDomain() } },
        sourceDao.getEnabledSourceCount(),
        sourceDao.getTotalArticlesFetched()
    ) { sources, enabled, fetched ->
        Triple(sources, enabled, fetched ?: 0)
    }
    
    // Combine next 3 flows
    private val statsFlow = combine(
        sourceDao.getTotalSuccessCount(),
        sourceDao.getTotalErrorCount(),
        articleDao.getTotalArticleCount()
    ) { successes, errors, articleCount ->
        Triple(successes ?: 0, errors ?: 0, articleCount)
    }
    
    val analyticsState: StateFlow<AnalyticsState> = combine(
        sourcesFlow,
        statsFlow
    ) { (sources, enabled, fetched), (successes, errors, articleCount) ->
        val totalAttempts = successes + errors
        
        val successRate = if (totalAttempts > 0) {
            ((successes.toDouble() / totalAttempts) * 100).toInt()
        } else 100
        
        val healthyCount = sources.count { it.healthStatus == SourceHealth.HEALTHY }
        val unhealthyCount = sources.count { 
            it.healthStatus == SourceHealth.FAILING || it.healthStatus == SourceHealth.UNSTABLE 
        }
        
        AnalyticsState(
            totalSources = sources.size,
            enabledSources = enabled,
            totalArticles = articleCount,
            totalFetches = fetched,
            totalErrors = errors,
            overallSuccessRate = successRate,
            healthySources = healthyCount,
            unhealthySources = unhealthyCount,
            sources = sources.sortedByDescending { it.errorCount }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsState()
    )
}
