package com.kottland.searchplant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen80,
    secondary = MutedGreen80,
    tertiary = WarmBrown80,
    background = DarkBackground,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    primaryContainer = LightGreen40,
    onPrimaryContainer = Color.White,
    secondaryContainer = MutedGreen40,
    onSecondaryContainer = Color.White,
    tertiaryContainer = EarthBrown40,
    onTertiaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGreen,
    secondary = LightGreen,
    tertiary = EarthBrown,
    background = SoftBeige,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = ForestGreen,
    onTertiary = Color.White,
    onBackground = ForestGreen,
    onSurface = ForestGreen,
    primaryContainer = LightGreen,
    onPrimaryContainer = ForestGreen,
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = ForestGreen,
    tertiaryContainer = Color(0xFFF3E5AB),
    onTertiaryContainer = DarkBrown
)

@Composable
fun SearchPlantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}