package com.serenesec.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serenesec.domain.usecase.FetchFeedsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker for periodic feed synchronization
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fetchFeedsUseCase: FetchFeedsUseCase
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val result = fetchFeedsUseCase()
            
            if (result.isSuccess) {
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
