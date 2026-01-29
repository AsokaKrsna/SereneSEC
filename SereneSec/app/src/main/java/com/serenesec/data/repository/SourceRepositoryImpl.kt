package com.serenesec.data.repository

import android.content.Context
import com.serenesec.data.local.dao.SourceDao
import com.serenesec.data.local.entity.SourceEntity
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import com.serenesec.domain.model.SourceType
import com.serenesec.domain.repository.SourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sourceDao: SourceDao
) : SourceRepository {
    
    override fun getAllSources(): Flow<List<Source>> {
        return sourceDao.getAllSources().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEnabledSources(): Flow<List<Source>> {
        return sourceDao.getEnabledSources().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getEnabledSourcesList(): List<Source> {
        return sourceDao.getEnabledSourcesList().map { it.toDomain() }
    }
    
    override suspend fun getSourceById(id: String): Source? {
        return sourceDao.getSourceById(id)?.toDomain()
    }
    
    override suspend fun addCustomSource(source: Source) {
        sourceDao.insertSource(SourceEntity.fromDomain(source.copy(isBuiltIn = false)))
    }
    
    override suspend fun setSourceEnabled(id: String, enabled: Boolean) {
        sourceDao.setSourceEnabled(id, enabled)
    }
    
    override suspend fun updateLastFetchTime(id: String) {
        sourceDao.updateLastFetchTime(id, System.currentTimeMillis())
    }
    
    override suspend fun incrementErrorCount(id: String) {
        sourceDao.incrementErrorCount(id)
    }
    
    override suspend fun recordSuccessfulFetch(id: String, articleCount: Int) {
        sourceDao.recordSuccessfulFetch(id, System.currentTimeMillis(), articleCount)
    }
    
    override suspend fun recordFailedFetch(id: String, errorMessage: String?) {
        sourceDao.recordFailedFetch(id, System.currentTimeMillis(), errorMessage)
    }
    
    override suspend fun deleteCustomSource(id: String) {
        sourceDao.deleteCustomSource(id)
    }
    
    override suspend fun initializeBuiltInSources() {
        // Check if already initialized
        if (sourceDao.getSourceCount() > 0) return
        
        try {
            val json = context.assets.open("sources_default.json")
                .bufferedReader()
                .use { it.readText() }
            
            val jsonObject = JSONObject(json)
            val sourcesArray = jsonObject.getJSONArray("sources")
            val sources = mutableListOf<SourceEntity>()
            
            for (i in 0 until sourcesArray.length()) {
                val sourceObj = sourcesArray.getJSONObject(i)
                sources.add(
                    SourceEntity(
                        id = sourceObj.getString("id"),
                        name = sourceObj.getString("name"),
                        url = sourceObj.getString("url"),
                        type = sourceObj.getString("type"),
                        category = sourceObj.getString("category"),
                        description = sourceObj.optString("description", null),
                        iconUrl = sourceObj.optString("iconUrl", null),
                        isBuiltIn = true,
                        isEnabled = true
                    )
                )
            }
            
            sourceDao.insertSources(sources)
        } catch (e: Exception) {
            // Log error but don't crash - user can add sources manually
            e.printStackTrace()
        }
    }
}
