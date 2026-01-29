package com.serenesec.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.SourceDao
import com.serenesec.data.preferences.NotificationPreferences
import com.serenesec.data.preferences.NotificationStyle
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    private val sourceDao: SourceDao
) : ViewModel() {
    
    val notificationsEnabled: StateFlow<Boolean> = notificationPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val categoryNotifications: StateFlow<Set<Category>> = notificationPreferences.categoryNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Category.entries.toSet())
    
    val enabledSourceNotifications: StateFlow<Set<String>> = notificationPreferences.enabledSourceNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    
    val keywordAlerts: StateFlow<Set<String>> = notificationPreferences.keywordAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    
    val quietHoursEnabled: StateFlow<Boolean> = notificationPreferences.quietHoursEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val quietHoursStart: StateFlow<Int> = notificationPreferences.quietHoursStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)
    
    val quietHoursEnd: StateFlow<Int> = notificationPreferences.quietHoursEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    
    val notificationStyle: StateFlow<NotificationStyle> = notificationPreferences.notificationStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationStyle.HEADS_UP)
    
    val priorityOnly: StateFlow<Boolean> = notificationPreferences.priorityOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val sources: StateFlow<List<Source>> = sourceDao.getEnabledSources()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setNotificationsEnabled(enabled)
        }
    }
    
    fun setCategoryNotification(category: Category, enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setCategoryNotification(category, enabled)
        }
    }
    
    fun setSourceNotification(sourceId: String, enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setSourceNotification(sourceId, enabled)
        }
    }
    
    fun addKeywordAlert(keyword: String) {
        viewModelScope.launch {
            notificationPreferences.addKeywordAlert(keyword)
        }
    }
    
    fun removeKeywordAlert(keyword: String) {
        viewModelScope.launch {
            notificationPreferences.removeKeywordAlert(keyword)
        }
    }
    
    fun setQuietHours(enabled: Boolean, startHour: Int? = null, endHour: Int? = null) {
        viewModelScope.launch {
            notificationPreferences.setQuietHours(enabled, startHour, endHour)
        }
    }
    
    fun setNotificationStyle(style: NotificationStyle) {
        viewModelScope.launch {
            notificationPreferences.setNotificationStyle(style)
        }
    }
    
    fun setPriorityOnly(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setPriorityOnly(enabled)
        }
    }
}
