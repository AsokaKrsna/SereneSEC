package com.serenesec.data.preferences

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    
    private val dataStore = context.dataStore
    
    // Sync frequency in hours
    val syncFrequency: Flow<SyncFrequency> = dataStore.data.map { preferences ->
        val hours = preferences[SYNC_FREQUENCY_KEY] ?: SyncFrequency.NORMAL.hours
        SyncFrequency.fromHours(hours)
    }
    
    suspend fun setSyncFrequency(frequency: SyncFrequency) {
        dataStore.edit { preferences ->
            preferences[SYNC_FREQUENCY_KEY] = frequency.hours
        }
    }
    
    // Theme preference
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val mode = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(mode)
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    // Accent color preference
    val accentColor: Flow<AccentColor> = dataStore.data.map { preferences ->
        val colorName = preferences[ACCENT_COLOR_KEY] ?: AccentColor.CYBER_BLUE.name
        try {
            AccentColor.valueOf(colorName)
        } catch (e: Exception) {
            AccentColor.CYBER_BLUE
        }
    }
    
    suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color.name
        }
    }
    
    // First launch flag
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH_KEY] ?: true
    }
    
    suspend fun setFirstLaunchComplete() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }
    
    // Current filter
    val selectedFilter: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_FILTER_KEY]
    }
    
    suspend fun setSelectedFilter(filter: String?) {
        dataStore.edit { preferences ->
            if (filter == null) {
                preferences.remove(SELECTED_FILTER_KEY)
            } else {
                preferences[SELECTED_FILTER_KEY] = filter
            }
        }
    }
    
    companion object {
        private val SYNC_FREQUENCY_KEY = intPreferencesKey("sync_frequency_hours")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        private val SELECTED_FILTER_KEY = stringPreferencesKey("selected_filter")
    }
}

enum class SyncFrequency(val hours: Int, val displayName: String) {
    FREQUENT(2, "Every 2 hours"),
    NORMAL(6, "Every 6 hours"),
    BATTERY_SAVER(12, "Every 12 hours"),
    MANUAL(0, "Manual only");
    
    companion object {
        fun fromHours(hours: Int): SyncFrequency {
            return entries.find { it.hours == hours } ?: NORMAL
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/**
 * Accent color options for the app
 * Each color has light and dark variants
 */
enum class AccentColor(
    val displayName: String,
    val lightColor: Long,
    val darkColor: Long,
    val emoji: String
) {
    CYBER_BLUE(
        displayName = "Cyber Blue",
        lightColor = 0xFF0066CC,
        darkColor = 0xFF4FC3F7,
        emoji = "💙"
    ),
    HACKER_GREEN(
        displayName = "Hacker Green",
        lightColor = 0xFF00875A,
        darkColor = 0xFF4CAF50,
        emoji = "💚"
    ),
    PURPLE_HAZE(
        displayName = "Purple Haze",
        lightColor = 0xFF6200EE,
        darkColor = 0xFFBB86FC,
        emoji = "💜"
    ),
    CRIMSON_ALERT(
        displayName = "Crimson Alert",
        lightColor = 0xFFC62828,
        darkColor = 0xFFEF5350,
        emoji = "❤️"
    ),
    SUNSET_ORANGE(
        displayName = "Sunset Orange",
        lightColor = 0xFFE65100,
        darkColor = 0xFFFF9800,
        emoji = "🧡"
    ),
    TEAL_SECURE(
        displayName = "Teal Secure",
        lightColor = 0xFF00796B,
        darkColor = 0xFF26A69A,
        emoji = "💎"
    ),
    PINK_PUNCH(
        displayName = "Pink Punch",
        lightColor = 0xFFAD1457,
        darkColor = 0xFFEC407A,
        emoji = "💗"
    ),
    AMBER_ALERT(
        displayName = "Amber Alert",
        lightColor = 0xFFF57F17,
        darkColor = 0xFFFFCA28,
        emoji = "💛"
    );
    
    fun getColor(isDark: Boolean): Color {
        return Color(if (isDark) darkColor else lightColor)
    }
}
