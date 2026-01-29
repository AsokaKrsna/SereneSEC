package com.serenesec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serenesec.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    
    // Get articles by state
    @Query("SELECT * FROM articles WHERE state = 'UNREAD' ORDER BY publishedDate DESC")
    fun getUnreadArticles(): Flow<List<ArticleEntity>>
    
    @Query("SELECT * FROM articles WHERE state = 'READ' ORDER BY publishedDate DESC")
    fun getReadArticles(): Flow<List<ArticleEntity>>
    
    @Query("SELECT * FROM articles WHERE state = 'ARCHIVED' ORDER BY publishedDate DESC")
    fun getArchivedArticles(): Flow<List<ArticleEntity>>
    
    // Get articles excluding archived (main inbox)
    @Query("SELECT * FROM articles WHERE state != 'ARCHIVED' ORDER BY publishedDate DESC")
    fun getInboxArticles(): Flow<List<ArticleEntity>>
    
    // Filter by category
    @Query("SELECT * FROM articles WHERE state != 'ARCHIVED' AND category = :category ORDER BY publishedDate DESC")
    fun getInboxArticlesByCategory(category: String): Flow<List<ArticleEntity>>
    
    // Get single article
    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): ArticleEntity?
    
    @Query("SELECT * FROM articles WHERE contentUrl = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticle(article: ArticleEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<ArticleEntity>): List<Long>
    
    // Update operations
    @Update
    suspend fun updateArticle(article: ArticleEntity)
    
    @Query("UPDATE articles SET state = :state WHERE id = :id")
    suspend fun updateArticleState(id: String, state: String)
    
    @Query("UPDATE articles SET contentHtml = :html WHERE id = :id")
    suspend fun cacheArticleHtml(id: String, html: String)
    
    // Mark as read when opened
    @Query("UPDATE articles SET state = 'READ' WHERE id = :id AND state = 'UNREAD'")
    suspend fun markAsRead(id: String)
    
    // Archive article
    @Query("UPDATE articles SET state = 'ARCHIVED' WHERE id = :id")
    suspend fun archiveArticle(id: String)
    
    // Restore from archive
    @Query("UPDATE articles SET state = 'READ' WHERE id = :id AND state = 'ARCHIVED'")
    suspend fun restoreArticle(id: String)
    
    // Counts
    @Query("SELECT COUNT(*) FROM articles WHERE state = 'UNREAD'")
    fun getUnreadCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM articles WHERE state != 'ARCHIVED'")
    fun getInboxCount(): Flow<Int>
    
    // Cleanup old articles (keep last 30 days by default)
    @Query("DELETE FROM articles WHERE state = 'ARCHIVED' AND createdAt < :olderThan")
    suspend fun deleteOldArchivedArticles(olderThan: Long)
    
    // Delete all articles from a source
    @Query("DELETE FROM articles WHERE sourceId = :sourceId")
    suspend fun deleteArticlesBySource(sourceId: String)
    
    // Check if article exists
    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE contentUrl = :url)")
    suspend fun articleExists(url: String): Boolean
    
    // Favorites
    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)
    
    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY publishedDate DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>
    
    @Query("SELECT COUNT(*) FROM articles WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>
    
    // Save for later
    @Query("UPDATE articles SET isSavedForLater = :saved WHERE id = :id")
    suspend fun setSavedForLater(id: String, saved: Boolean)
    
    @Query("SELECT * FROM articles WHERE isSavedForLater = 1 ORDER BY publishedDate DESC")
    fun getSavedForLaterArticles(): Flow<List<ArticleEntity>>
    
    @Query("SELECT COUNT(*) FROM articles WHERE isSavedForLater = 1")
    fun getSavedForLaterCount(): Flow<Int>
    
    // Analytics
    @Query("SELECT COUNT(*) FROM articles")
    fun getTotalArticleCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM articles WHERE state = 'READ'")
    fun getReadCount(): Flow<Int>
    
    // Get articles without cached content for auto-caching
    @Query("SELECT * FROM articles WHERE isSavedForLater = 1 AND contentHtml IS NULL")
    suspend fun getUncachedSavedArticles(): List<ArticleEntity>
    
    // Get articles with cached content
    @Query("SELECT * FROM articles WHERE contentHtml IS NOT NULL ORDER BY publishedDate DESC")
    fun getCachedArticles(): Flow<List<ArticleEntity>>
}

