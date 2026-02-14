package com.serenesec.domain.repository

import com.serenesec.domain.model.Article
import com.serenesec.domain.model.ArticleState
import com.serenesec.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for articles
 */
interface ArticleRepository {
    
    /**
     * Get inbox articles (unread + read, excludes archived)
     */
    fun getInboxArticles(): Flow<List<Article>>
    
    /**
     * Get inbox articles filtered by category
     */
    fun getInboxArticlesByCategory(category: Category): Flow<List<Article>>
    
    /**
     * Get only unread articles
     */
    fun getUnreadArticles(): Flow<List<Article>>
    
    /**
     * Get archived articles
     */
    fun getArchivedArticles(): Flow<List<Article>>
    
    /**
     * Get article by ID
     */
    suspend fun getArticleById(id: String): Article?
    
    /**
     * Get article by URL
     */
    suspend fun getArticleByUrl(url: String): Article?
    
    /**
     * Check if article exists by URL
     */
    suspend fun articleExists(url: String): Boolean
    
    /**
     * Insert new articles (ignores duplicates)
     */
    suspend fun insertArticles(articles: List<Article>): Int
    
    /**
     * Insert single article (from share intent)
     */
    suspend fun insertArticle(article: Article): Boolean
    
    /**
     * Update article state
     */
    suspend fun updateArticleState(id: String, state: ArticleState)
    
    /**
     * Mark article as read
     */
    suspend fun markAsRead(id: String)
    
    /**
     * Archive article
     */
    suspend fun archiveArticle(id: String)
    
    /**
     * Restore from archive
     */
    suspend fun restoreArticle(id: String)
    
    /**
     * Cache article HTML for offline reading
     */
    suspend fun cacheArticleHtml(id: String, html: String)
    
    /**
     * Get unread count as Flow
     */
    fun getUnreadCount(): Flow<Int>
    
    /**
     * Delete old archived articles (cleanup)
     */
    suspend fun deleteOldArchivedArticles(olderThanDays: Int = 30)
    
    /**
     * Delete old regular articles (not saved, not favorite, not in collections)
     * Keeps articles older than specified days that are:
     * - Saved for later
     * - Favorites
     * - Archived
     * - In collections
     */
    suspend fun deleteOldRegularArticles(olderThanDays: Int = 5): Int
    
    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    
    /**
     * Get favorite articles
     */
    fun getFavoriteArticles(): Flow<List<Article>>
    
    /**
     * Toggle saved for later
     */
    suspend fun toggleSavedForLater(id: String, saved: Boolean)
    
    /**
     * Get saved for later articles
     */
    fun getSavedForLaterArticles(): Flow<List<Article>>
}
