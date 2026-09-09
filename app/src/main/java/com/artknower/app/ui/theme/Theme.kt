package com.artknower.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = RustTerracotta,
    onPrimary = LightText,
    primaryContainer = RustTerracottaDark,
    onPrimaryContainer = LightText,
    secondary = RustTerracotta,
    onSecondary = LightText,
    secondaryContainer = RustTerracottaLight,
    onSecondaryContainer = DarkText,
    tertiary = RustTerracotta,
    onTertiary = LightText,
    background = CleanBackground,
    onBackground = DarkText,
    surface = CardSurface,
    onSurface = DarkText,
    surfaceVariant = ThinBorderColor,
    onSurfaceVariant = DarkText
)

@Composable
fun ArtKnowerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
