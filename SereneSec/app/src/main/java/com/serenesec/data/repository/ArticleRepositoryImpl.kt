package com.serenesec.data.repository

import com.serenesec.data.local.dao.ArticleDao
import com.serenesec.data.local.entity.ArticleEntity
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.ArticleState
import com.serenesec.domain.model.Category
import com.serenesec.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao
) : ArticleRepository {
    
    override fun getInboxArticles(): Flow<List<Article>> {
        return articleDao.getInboxArticles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getInboxArticlesByCategory(category: Category): Flow<List<Article>> {
        return articleDao.getInboxArticlesByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getUnreadArticles(): Flow<List<Article>> {
        return articleDao.getUnreadArticles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getArchivedArticles(): Flow<List<Article>> {
        return articleDao.getArchivedArticles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getArticleById(id: String): Article? {
        return articleDao.getArticleById(id)?.toDomain()
    }
    
    override suspend fun getArticleByUrl(url: String): Article? {
        return articleDao.getArticleByUrl(url)?.toDomain()
    }
    
    override suspend fun articleExists(url: String): Boolean {
        return articleDao.articleExists(url)
    }
    
    override suspend fun insertArticles(articles: List<Article>): Int {
        if (articles.isEmpty()) return 0
        val entities = articles.map { ArticleEntity.fromDomain(it) }
        val results = articleDao.insertArticles(entities)
        // Count successful insertions (non -1 values)
        return results.count { it != -1L }
    }
    
    override suspend fun insertArticle(article: Article): Boolean {
        val result = articleDao.insertArticle(ArticleEntity.fromDomain(article))
        return result != -1L
    }
    
    override suspend fun updateArticleState(id: String, state: ArticleState) {
        articleDao.updateArticleState(id, state.name)
    }
    
    override suspend fun markAsRead(id: String) {
        articleDao.markAsRead(id)
    }
    
    override suspend fun archiveArticle(id: String) {
        articleDao.archiveArticle(id)
    }
    
    override suspend fun restoreArticle(id: String) {
        articleDao.restoreArticle(id)
    }
    
    override suspend fun cacheArticleHtml(id: String, html: String) {
        articleDao.cacheArticleHtml(id, html)
    }
    
    override fun getUnreadCount(): Flow<Int> {
        return articleDao.getUnreadCount()
    }
    
    override suspend fun deleteOldArchivedArticles(olderThanDays: Int) {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        articleDao.deleteOldArchivedArticles(cutoffTime)
    }
    
    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        articleDao.setFavorite(id, isFavorite)
    }
    
    override fun getFavoriteArticles(): Flow<List<Article>> {
        return articleDao.getFavoriteArticles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun toggleSavedForLater(id: String, saved: Boolean) {
        articleDao.setSavedForLater(id, saved)
    }
    
    override fun getSavedForLaterArticles(): Flow<List<Article>> {
        return articleDao.getSavedForLaterArticles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
