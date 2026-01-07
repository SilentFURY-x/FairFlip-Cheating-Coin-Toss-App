package com.fury.fairflip.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// Define our Custom "Mystic" Color Scheme
// We only define one scheme because the app is always Dark/Gold.
private val MysticColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = MysticBlack, // Text color on top of the Gold button
    secondary = RoyalGoldDark,
    tertiary = CheatRed, // Used for debug/cheat indicators
    background = MysticBlack,
    onBackground = TextWhite,
    surface = SurfaceGrey,
    onSurface = TextWhite
)

@Composable
fun FairFlipTheme(
    // We default to darkTheme = true to force the look
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We ignore dynamicColor to protect your Gold branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 1. Force our custom scheme (Ignore Light/Dynamic modes)
    val colorScheme = MysticColorScheme

    // 2. Fix the Status Bar (Battery/Time icons)
    // This makes the top bar blend with your black background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // CHANGED: Set to Transparent so the gradient shows through
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb() // Fix bottom bar too

            // Keep icons white (false = light icons for dark background)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    // 3. Apply the Theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}