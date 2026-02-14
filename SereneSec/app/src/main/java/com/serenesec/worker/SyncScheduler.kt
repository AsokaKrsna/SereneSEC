package com.serenesec.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.serenesec.data.preferences.SyncFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for background sync and cleanup work
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    init {
        // Always schedule daily cleanup
        scheduleCleanupWork()
    }
    
    /**
     * Schedule periodic sync based on user preference
     */
    fun scheduleSyncWork(frequency: SyncFrequency) {
        // Cancel existing work first
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        
        // Don't schedule if manual only
        if (frequency == SyncFrequency.MANUAL || frequency.hours == 0) {
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            frequency.hours.toLong(), TimeUnit.HOURS,
            // Flex interval: 30 min window to save battery
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }
    
    /**
     * Schedule daily cleanup of old articles
     * Runs once per day to remove articles older than 5 days
     * (except saved, favorites, archived, or in collections)
     */
    private fun scheduleCleanupWork() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            1, TimeUnit.DAYS,
            // Flex interval: 6 hour window
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            CleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule
            cleanupRequest
        )
    }
    
    /**
     * Cancel all scheduled sync work
     */
    fun cancelSyncWork() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
    }
    
    /**
     * Cancel cleanup work (not recommended)
     */
    fun cancelCleanupWork() {
        workManager.cancelUniqueWork(CleanupWorker.WORK_NAME)
    }
}
