package com.serenesec.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.preferences.AccentColor
import com.serenesec.data.preferences.SyncFrequency
import com.serenesec.data.preferences.ThemeMode
import com.serenesec.data.preferences.UserPreferences
import com.serenesec.domain.usecase.CleanupOldArticlesUseCase
import com.serenesec.domain.usecase.CleanupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val cleanupOldArticlesUseCase: CleanupOldArticlesUseCase
) : ViewModel() {
    
    val syncFrequency: StateFlow<SyncFrequency> = userPreferences.syncFrequency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncFrequency.NORMAL
        )
    
    val themeMode: StateFlow<ThemeMode> = userPreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )
    
    val accentColor: StateFlow<AccentColor> = userPreferences.accentColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccentColor.CYBER_BLUE
        )
    
    suspend fun setSyncFrequency(frequency: SyncFrequency) {
        userPreferences.setSyncFrequency(frequency)
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        userPreferences.setThemeMode(mode)
    }
    
    suspend fun setAccentColor(color: AccentColor) {
        userPreferences.setAccentColor(color)
    }
    
    suspend fun cleanupOldArticles(): String {
        return when (val result = cleanupOldArticlesUseCase()) {
            is CleanupResult.Success -> result.message
            is CleanupResult.Error -> "Cleanup failed: ${result.message}"
        }
    }
}
