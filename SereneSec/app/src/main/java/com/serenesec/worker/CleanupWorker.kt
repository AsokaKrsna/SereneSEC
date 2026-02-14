package com.serenesec.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serenesec.domain.repository.ArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker for cleaning up old articles
 * Runs daily to remove articles older than 5 days that are not:
 * - Saved for later
 * - Favorites
 * - Archived
 * - In collections
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val articleRepository: ArticleRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting article cleanup...")
            
            // Delete old regular articles (older than 5 days)
            val deletedRegular = articleRepository.deleteOldRegularArticles(olderThanDays = 5)
            Log.d(TAG, "Deleted $deletedRegular old regular articles")
            
            // Delete old archived articles (older than 30 days)
            articleRepository.deleteOldArchivedArticles(olderThanDays = 30)
            Log.d(TAG, "Deleted old archived articles (30+ days)")
            
            Log.d(TAG, "Article cleanup completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Article cleanup failed", e)
            // Don't retry cleanup failures
            Result.failure()
        }
    }
    
    companion object {
        private const val TAG = "CleanupWorker"
        const val WORK_NAME = "serenesec_cleanup"
    }
}
