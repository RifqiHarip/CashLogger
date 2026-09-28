package com.rifqi.uangkas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Skema Warna Terang (Light Theme) berbasis Hijau & Emas KSTC
private val LightColorScheme = lightColorScheme(
    primary = KstcDarkGreen,
    onPrimary = Color.White,
    secondary = KstcGold,
    onSecondary = KstcDarkGreen,
    background = KstcBackground,
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
)

// Skema Warna Gelap (Dark Theme) opsional
private val DarkColorScheme = darkColorScheme(
    primary = KstcGold,
    onPrimary = KstcDarkGreen,
    secondary = KstcDarkGreen,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun UangKasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color tersedia di Android 12+ (bisa dimatikan agar tema KSTC konsisten)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalView.current.context
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Menyesuaikan warna status bar dengan warna primer KSTC
            window.statusBarColor = KstcDarkGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Bawaan template
        content = content
    )
}