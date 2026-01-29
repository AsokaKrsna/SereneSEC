package com.serenesec.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.serenesec.data.preferences.AccentColor

/**
 * Creates a dynamic color scheme based on the selected accent color
 */
private fun createDarkColorScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = BackgroundDark,
    primaryContainer = accent.copy(alpha = 0.3f),
    onPrimaryContainer = OnSurfaceDark,
    
    secondary = Secondary,
    onSecondary = BackgroundDark,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnSurfaceDark,
    
    tertiary = Info,
    onTertiary = BackgroundDark,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = OnBackgroundDarkMuted,
    
    error = Error,
    onError = BackgroundDark,
    
    outline = OnBackgroundDarkMuted,
    outlineVariant = SurfaceDarkElevated
)

private fun createLightColorScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = BackgroundLight,
    primaryContainer = accent.copy(alpha = 0.15f),
    onPrimaryContainer = OnSurfaceLight,
    
    secondary = Secondary,
    onSecondary = BackgroundLight,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnSurfaceLight,
    
    tertiary = Info,
    onTertiary = BackgroundLight,
    
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = OnBackgroundLightMuted,
    
    error = Error,
    onError = BackgroundLight,
    
    outline = OnBackgroundLightMuted,
    outlineVariant = SurfaceLightElevated
)

@Composable
fun SereneSecTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = AccentColor.CYBER_BLUE,
    content: @Composable () -> Unit
) {
    val accent = accentColor.getColor(darkTheme)
    val colorScheme = if (darkTheme) {
        createDarkColorScheme(accent)
    } else {
        createLightColorScheme(accent)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
