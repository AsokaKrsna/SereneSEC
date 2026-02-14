package com.serenesec.domain.usecase

import com.serenesec.domain.repository.ArticleRepository
import javax.inject.Inject

/**
 * Use case for cleaning up old articles
 */
class CleanupOldArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    /**
     * Clean up old articles
     * @param regularArticleDays Delete regular articles older than this many days (default: 5)
     * @param archivedArticleDays Delete archived articles older than this many days (default: 30)
     * @return Number of articles deleted
     */
    suspend operator fun invoke(
        regularArticleDays: Int = 5,
        archivedArticleDays: Int = 30
    ): CleanupResult {
        return try {
            // Delete old regular articles (not saved, not favorite, not in collections)
            val regularDeleted = articleRepository.deleteOldRegularArticles(regularArticleDays)
            
            // Delete old archived articles
            articleRepository.deleteOldArchivedArticles(archivedArticleDays)
            
            CleanupResult.Success(
                regularArticlesDeleted = regularDeleted,
                message = "Cleaned up $regularDeleted old articles"
            )
        } catch (e: Exception) {
            CleanupResult.Error(e.message ?: "Cleanup failed")
        }
    }
}

sealed class CleanupResult {
    data class Success(
        val regularArticlesDeleted: Int,
        val message: String
    ) : CleanupResult()
    
    data class Error(val message: String) : CleanupResult()
}
