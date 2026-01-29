package com.serenesec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serenesec.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    
    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun getAllSources(): Flow<List<SourceEntity>>
    
    @Query("SELECT * FROM sources WHERE isEnabled = 1 ORDER BY name ASC")
    fun getEnabledSources(): Flow<List<SourceEntity>>
    
    @Query("SELECT * FROM sources WHERE isEnabled = 1")
    suspend fun getEnabledSourcesList(): List<SourceEntity>
    
    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getSourceById(id: String): SourceEntity?
    
    @Query("SELECT * FROM sources WHERE isBuiltIn = 1")
    suspend fun getBuiltInSources(): List<SourceEntity>
    
    @Query("SELECT * FROM sources WHERE isBuiltIn = 0")
    fun getCustomSources(): Flow<List<SourceEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)
    
    @Update
    suspend fun updateSource(source: SourceEntity)
    
    @Query("UPDATE sources SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setSourceEnabled(id: String, isEnabled: Boolean)
    
    // Health tracking updates
    @Query("""
        UPDATE sources SET 
            lastFetchTime = :timestamp, 
            lastSuccessTime = :timestamp,
            successCount = successCount + 1,
            totalArticlesFetched = totalArticlesFetched + :articleCount,
            lastErrorMessage = NULL
        WHERE id = :id
    """)
    suspend fun recordSuccessfulFetch(id: String, timestamp: Long, articleCount: Int)
    
    @Query("""
        UPDATE sources SET 
            lastFetchTime = :timestamp,
            lastErrorTime = :timestamp,
            lastErrorMessage = :errorMessage,
            errorCount = errorCount + 1
        WHERE id = :id
    """)
    suspend fun recordFailedFetch(id: String, timestamp: Long, errorMessage: String?)
    
    // Legacy methods for backwards compatibility
    @Query("UPDATE sources SET lastFetchTime = :timestamp, errorCount = 0 WHERE id = :id")
    suspend fun updateLastFetchTime(id: String, timestamp: Long)
    
    @Query("UPDATE sources SET errorCount = errorCount + 1 WHERE id = :id")
    suspend fun incrementErrorCount(id: String)
    
    @Query("DELETE FROM sources WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomSource(id: String)
    
    @Query("SELECT COUNT(*) FROM sources")
    suspend fun getSourceCount(): Int
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM sources WHERE isEnabled = 1")
    fun getEnabledSourceCount(): Flow<Int>
    
    @Query("SELECT SUM(totalArticlesFetched) FROM sources")
    fun getTotalArticlesFetched(): Flow<Int?>
    
    @Query("SELECT SUM(successCount) FROM sources")
    fun getTotalSuccessCount(): Flow<Int?>
    
    @Query("SELECT SUM(errorCount) FROM sources")
    fun getTotalErrorCount(): Flow<Int?>
    
    // Get sources with health issues (more than 30% errors)
    @Query("""
        SELECT * FROM sources 
        WHERE isEnabled = 1 
        AND (successCount + errorCount) > 0
        AND (CAST(errorCount AS REAL) / (successCount + errorCount)) > 0.3
        ORDER BY errorCount DESC
    """)
    fun getUnhealthySources(): Flow<List<SourceEntity>>
}
