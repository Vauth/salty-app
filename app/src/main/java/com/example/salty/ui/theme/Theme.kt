package com.example.salty.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryCyanDark,
    onPrimaryContainer = PrimaryCyanLight,
    secondary = SecondaryTeal,
    onSecondary = DarkBackground,
    secondaryContainer = SecondaryPurple,
    onSecondaryContainer = White,
    tertiary = SecondaryPink,
    onTertiary = White,
    tertiaryContainer = SecondaryPurple,
    onTertiaryContainer = White,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightGray,
    surfaceTint = PrimaryCyan,
    inverseSurface = White,
    inverseOnSurface = DarkBackground,
    error = Error,
    onError = White,
    errorContainer = Error,
    onErrorContainer = White,
    outline = DarkGray,
    outlineVariant = DarkSurfaceVariant,
    scrim = DarkBackground
)

@Composable
fun SaltyTheme(
    darkTheme: Boolean = true, // Always use dark theme by default
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Use dark scheme as fallback since app is dark-mode focused
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
