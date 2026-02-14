package com.serenesec.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serenesec.domain.repository.ArticleRepository
import com.serenesec.domain.usecase.FetchFeedsUseCase
import com.serenesec.util.SereneSecNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background worker for periodic feed synchronization
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fetchFeedsUseCase: FetchFeedsUseCase,
    private val articleRepository: ArticleRepository,
    private val notificationManager: SereneSecNotificationManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Get current unread count before sync
            val unreadCountBefore = articleRepository.getUnreadCount().first()
            
            // Fetch new articles
            val result = fetchFeedsUseCase()
            
            if (result.isSuccess) {
                // Get new unread count after sync
                val unreadCountAfter = articleRepository.getUnreadCount().first()
                
                // If there are new articles, get them and send notification
                if (unreadCountAfter > unreadCountBefore) {
                    val newArticles = articleRepository.getUnreadArticles().first()
                        .take((unreadCountAfter - unreadCountBefore).coerceAtMost(10))
                    
                    if (newArticles.isNotEmpty()) {
                        notificationManager.notifyNewArticles(newArticles)
                    }
                }
                
                Result.success()
            } else {
                // Retry on failure, but not too many times
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "serenesec_sync"
    }
}
