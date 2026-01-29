package com.serenesec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.serenesec.data.preferences.AccentColor
import com.serenesec.data.preferences.SyncFrequency
import com.serenesec.data.preferences.ThemeMode
import com.serenesec.data.preferences.UserPreferences
import com.serenesec.ui.navigation.SereneSecNavHost
import com.serenesec.ui.theme.SereneSecTheme
import com.serenesec.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var syncScheduler: SyncScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val accentColor by userPreferences.accentColor.collectAsState(initial = AccentColor.CYBER_BLUE)
            val syncFrequency by userPreferences.syncFrequency.collectAsState(initial = SyncFrequency.NORMAL)
            
            // Schedule sync based on preference
            syncScheduler.scheduleSyncWork(syncFrequency)
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            SereneSecTheme(
                darkTheme = darkTheme,
                accentColor = accentColor
            ) {
                val navController = rememberNavController()
                SereneSecNavHost(navController = navController)
            }
        }
    }
}
