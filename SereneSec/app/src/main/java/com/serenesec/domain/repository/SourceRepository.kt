package com.serenesec.domain.repository

import com.serenesec.domain.model.Source
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for content sources
 */
interface SourceRepository {
    
    /**
     * Get all sources as a Flow
     */
    fun getAllSources(): Flow<List<Source>>
    
    /**
     * Get only enabled sources as a Flow
     */
    fun getEnabledSources(): Flow<List<Source>>
    
    /**
     * Get enabled sources as a one-time list
     */
    suspend fun getEnabledSourcesList(): List<Source>
    
    /**
     * Get a source by ID
     */
    suspend fun getSourceById(id: String): Source?
    
    /**
     * Add a custom source
     */
    suspend fun addCustomSource(source: Source)
    
    /**
     * Toggle source enabled state
     */
    suspend fun setSourceEnabled(id: String, enabled: Boolean)
    
    /**
     * Update source after successful fetch
     */
    suspend fun updateLastFetchTime(id: String)
    
    /**
     * Increment error count on fetch failure
     */
    suspend fun incrementErrorCount(id: String)
    
    /**
     * Record successful fetch with article count for analytics
     */
    suspend fun recordSuccessfulFetch(id: String, articleCount: Int)
    
    /**
     * Record failed fetch with error message for analytics
     */
    suspend fun recordFailedFetch(id: String, errorMessage: String?)
    
    /**
     * Delete a custom source (built-in sources cannot be deleted)
     */
    suspend fun deleteCustomSource(id: String)
    
    /**
     * Initialize built-in sources from bundled JSON
     */
    suspend fun initializeBuiltInSources()
}
