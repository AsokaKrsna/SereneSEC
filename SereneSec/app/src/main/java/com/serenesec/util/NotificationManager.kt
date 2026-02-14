package com.serenesec.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.serenesec.MainActivity
import com.serenesec.R
import com.serenesec.data.preferences.NotificationPreferences
import com.serenesec.data.preferences.NotificationStyle
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SereneSecNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences
) {
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_NEW_ARTICLES,
                    "New Articles",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for new security articles"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_KEYWORD_ALERTS,
                    "Keyword Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority alerts for critical security keywords"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_SYNC,
                    "Background Sync",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Feed synchronization status"
                    setShowBadge(false)
                }
            )
            
            val manager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    suspend fun notifyNewArticles(articles: List<Article>) {
        // Check if notifications are enabled
        val enabled = notificationPreferences.notificationsEnabled.first()
        if (!enabled || articles.isEmpty()) return
        
        // Check runtime permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Check quiet hours
        if (isQuietHours()) return
        
        // Get notification preferences
        val categoryNotifications = notificationPreferences.categoryNotifications.first()
        val enabledSources = notificationPreferences.enabledSourceNotifications.first()
        val keywordAlerts = notificationPreferences.keywordAlerts.first()
        val priorityOnly = notificationPreferences.priorityOnly.first()
        val style = notificationPreferences.notificationStyle.first()
        
        // Filter articles based on preferences
        val filteredArticles = articles.filter { article ->
            // Check category filter
            val categoryMatch = categoryNotifications.contains(article.category)
            
            // Check source filter (if any sources are enabled, only notify for those)
            val sourceMatch = enabledSources.isEmpty() || enabledSources.contains(article.sourceId)
            
            // Check keyword alerts
            val keywordMatch = if (keywordAlerts.isNotEmpty()) {
                keywordAlerts.any { keyword ->
                    article.title.contains(keyword, ignoreCase = true) ||
                    article.summary?.contains(keyword, ignoreCase = true) == true
                }
            } else {
                !priorityOnly // If no keywords and not priority only, show all
            }
            
            categoryMatch && sourceMatch && (keywordMatch || !priorityOnly)
        }
        
        if (filteredArticles.isEmpty()) return
        
        // Determine if any are high-priority (keyword matches)
        val hasKeywordMatches = filteredArticles.any { article ->
            keywordAlerts.any { keyword ->
                article.title.contains(keyword, ignoreCase = true) ||
                article.summary?.contains(keyword, ignoreCase = true) == true
            }
        }
        
        // Send notification
        if (filteredArticles.size == 1) {
            sendSingleArticleNotification(filteredArticles.first(), hasKeywordMatches, style)
        } else {
            sendMultipleArticlesNotification(filteredArticles, hasKeywordMatches, style)
        }
    }
    
    private fun sendSingleArticleNotification(
        article: Article,
        isHighPriority: Boolean,
        style: NotificationStyle
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("article_id", article.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            article.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val channelId = if (isHighPriority) CHANNEL_KEYWORD_ALERTS else CHANNEL_NEW_ARTICLES
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(article.title)
            .setContentText(article.summary ?: "New ${article.category.displayName} article")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(article.summary ?: "New ${article.category.displayName} article"))
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                when (style) {
                    NotificationStyle.HEADS_UP -> {
                        setDefaults(NotificationCompat.DEFAULT_ALL)
                    }
                    NotificationStyle.SILENT -> {
                        setDefaults(0)
                        setSilent(true)
                    }
                    NotificationStyle.OFF -> {
                        setDefaults(0)
                        setSilent(true)
                        setOnlyAlertOnce(true)
                    }
                }
            }
            .build()
        
        NotificationManagerCompat.from(context).notify(article.id.hashCode(), notification)
    }
    
    private fun sendMultipleArticlesNotification(
        articles: List<Article>,
        hasHighPriority: Boolean,
        style: NotificationStyle
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val channelId = if (hasHighPriority) CHANNEL_KEYWORD_ALERTS else CHANNEL_NEW_ARTICLES
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("${articles.size} new security articles")
        
        articles.take(5).forEach { article ->
            inboxStyle.addLine(article.title)
        }
        
        if (articles.size > 5) {
            inboxStyle.addLine("And ${articles.size - 5} more...")
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${articles.size} new articles")
            .setContentText("New security articles available")
            .setStyle(inboxStyle)
            .setPriority(if (hasHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setNumber(articles.size)
            .apply {
                when (style) {
                    NotificationStyle.HEADS_UP -> {
                        setDefaults(NotificationCompat.DEFAULT_ALL)
                    }
                    NotificationStyle.SILENT -> {
                        setDefaults(0)
                        setSilent(true)
                    }
                    NotificationStyle.OFF -> {
                        setDefaults(0)
                        setSilent(true)
                        setOnlyAlertOnce(true)
                    }
                }
            }
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MULTIPLE, notification)
    }
    
    private suspend fun isQuietHours(): Boolean {
        val enabled = notificationPreferences.quietHoursEnabled.first()
        if (!enabled) return false
        
        val startHour = notificationPreferences.quietHoursStart.first()
        val endHour = notificationPreferences.quietHoursEnd.first()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return if (startHour < endHour) {
            currentHour in startHour until endHour
        } else {
            // Quiet hours span midnight
            currentHour >= startHour || currentHour < endHour
        }
    }
    
    companion object {
        private const val CHANNEL_NEW_ARTICLES = "new_articles"
        private const val CHANNEL_KEYWORD_ALERTS = "keyword_alerts"
        private const val CHANNEL_SYNC = "sync"
        private const val NOTIFICATION_ID_MULTIPLE = 1000
    }
}
