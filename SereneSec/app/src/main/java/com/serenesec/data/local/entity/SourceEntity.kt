package com.serenesec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import com.serenesec.domain.model.SourceType

/**
 * Room entity for content sources
 */
@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val type: String,          // SourceType as string
    val category: String,      // Category as string
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val lastFetchTime: Long? = null,
    val lastSuccessTime: Long? = null,   // Last successful fetch
    val lastErrorTime: Long? = null,     // Last error time
    val lastErrorMessage: String? = null, // Last error message
    val errorCount: Int = 0,
    val successCount: Int = 0,           // Total successful fetches
    val totalArticlesFetched: Int = 0,   // Lifetime article count
    val description: String? = null,
    val iconUrl: String? = null
) {
    fun toDomain(): Source = Source(
        id = id,
        name = name,
        url = url,
        type = SourceType.valueOf(type),
        category = Category.valueOf(category),
        isEnabled = isEnabled,
        isBuiltIn = isBuiltIn,
        lastFetchTime = lastFetchTime,
        lastSuccessTime = lastSuccessTime,
        lastErrorTime = lastErrorTime,
        lastErrorMessage = lastErrorMessage,
        errorCount = errorCount,
        successCount = successCount,
        totalArticlesFetched = totalArticlesFetched,
        description = description,
        iconUrl = iconUrl
    )

    companion object {
        fun fromDomain(source: Source): SourceEntity = SourceEntity(
            id = source.id,
            name = source.name,
            url = source.url,
            type = source.type.name,
            category = source.category.name,
            isEnabled = source.isEnabled,
            isBuiltIn = source.isBuiltIn,
            lastFetchTime = source.lastFetchTime,
            lastSuccessTime = source.lastSuccessTime,
            lastErrorTime = source.lastErrorTime,
            lastErrorMessage = source.lastErrorMessage,
            errorCount = source.errorCount,
            successCount = source.successCount,
            totalArticlesFetched = source.totalArticlesFetched,
            description = source.description,
            iconUrl = source.iconUrl
        )
    }
}
