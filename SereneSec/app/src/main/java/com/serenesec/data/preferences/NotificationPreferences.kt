package com.serenesec.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.serenesec.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notifications")

/**
 * Notification preferences with granular control
 */
@Singleton
class NotificationPreferences @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.notificationDataStore
    
    // Master notification toggle
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    // Per-category notifications
    val categoryNotifications: Flow<Set<Category>> = dataStore.data.map { preferences ->
        val categoryNames = preferences[CATEGORY_NOTIFICATIONS] ?: Category.entries.map { it.name }.toSet()
        categoryNames.mapNotNull { name ->
            try { Category.valueOf(name) } catch (e: Exception) { null }
        }.toSet()
    }
    
    suspend fun setCategoryNotification(category: Category, enabled: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[CATEGORY_NOTIFICATIONS]?.toMutableSet() 
                ?: Category.entries.map { it.name }.toMutableSet()
            if (enabled) {
                current.add(category.name)
            } else {
                current.remove(category.name)
            }
            preferences[CATEGORY_NOTIFICATIONS] = current
        }
    }
    
    // Per-source notifications (source IDs that have notifications enabled)
    val enabledSourceNotifications: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SOURCE_NOTIFICATIONS] ?: emptySet()
    }
    
    suspend fun setSourceNotification(sourceId: String, enabled: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[SOURCE_NOTIFICATIONS]?.toMutableSet() ?: mutableSetOf()
            if (enabled) {
                current.add(sourceId)
            } else {
                current.remove(sourceId)
            }
            preferences[SOURCE_NOTIFICATIONS] = current
        }
    }
    
    // Keyword alerts
    val keywordAlerts: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEYWORD_ALERTS] ?: setOf("CVE", "Zero-Day", "Critical", "Ransomware")
    }
    
    suspend fun setKeywordAlerts(keywords: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEYWORD_ALERTS] = keywords
        }
    }
    
    suspend fun addKeywordAlert(keyword: String) {
        dataStore.edit { preferences ->
            val current = preferences[KEYWORD_ALERTS]?.toMutableSet() ?: mutableSetOf()
            current.add(keyword)
            preferences[KEYWORD_ALERTS] = current
        }
    }
    
    suspend fun removeKeywordAlert(keyword: String) {
        dataStore.edit { preferences ->
            val current = preferences[KEYWORD_ALERTS]?.toMutableSet() ?: mutableSetOf()
            current.remove(keyword)
            preferences[KEYWORD_ALERTS] = current
        }
    }
    
    // Quiet hours
    val quietHoursEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[QUIET_HOURS_ENABLED] ?: false
    }
    
    val quietHoursStart: Flow<Int> = dataStore.data.map { preferences ->
        preferences[QUIET_HOURS_START] ?: 22 // 10 PM
    }
    
    val quietHoursEnd: Flow<Int> = dataStore.data.map { preferences ->
        preferences[QUIET_HOURS_END] ?: 7 // 7 AM
    }
    
    suspend fun setQuietHours(enabled: Boolean, startHour: Int? = null, endHour: Int? = null) {
        dataStore.edit { preferences ->
            preferences[QUIET_HOURS_ENABLED] = enabled
            startHour?.let { preferences[QUIET_HOURS_START] = it }
            endHour?.let { preferences[QUIET_HOURS_END] = it }
        }
    }
    
    // Notification style
    val notificationStyle: Flow<NotificationStyle> = dataStore.data.map { preferences ->
        val styleName = preferences[NOTIFICATION_STYLE] ?: NotificationStyle.HEADS_UP.name
        try {
            NotificationStyle.valueOf(styleName)
        } catch (e: Exception) {
            NotificationStyle.HEADS_UP
        }
    }
    
    suspend fun setNotificationStyle(style: NotificationStyle) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_STYLE] = style.name
        }
    }
    
    // Priority notifications only (critical keywords only)
    val priorityOnly: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PRIORITY_ONLY] ?: false
    }
    
    suspend fun setPriorityOnly(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PRIORITY_ONLY] = enabled
        }
    }
    
    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val CATEGORY_NOTIFICATIONS = stringSetPreferencesKey("category_notifications")
        private val SOURCE_NOTIFICATIONS = stringSetPreferencesKey("source_notifications")
        private val KEYWORD_ALERTS = stringSetPreferencesKey("keyword_alerts")
        private val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        private val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        private val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        private val NOTIFICATION_STYLE = stringPreferencesKey("notification_style")
        private val PRIORITY_ONLY = booleanPreferencesKey("priority_only")
    }
}

enum class NotificationStyle(val displayName: String) {
    HEADS_UP("Heads-up"),
    SILENT("Silent"),
    OFF("Notification only")
}
