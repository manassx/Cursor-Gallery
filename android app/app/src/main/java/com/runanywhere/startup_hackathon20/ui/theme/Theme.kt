package com.runanywhere.startup_hackathon20.ui.theme

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
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkAccent,
    onPrimaryContainer = DarkBackground,
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkBackgroundAlt,
    onSecondaryContainer = DarkText,
    tertiary = DarkAccent,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkBackgroundAlt,
    onSurface = DarkText,
    surfaceVariant = DarkBackgroundAlt,
    onSurfaceVariant = DarkTextDim,
    error = ErrorRed,
    onError = DarkText,
    outline = DarkBorder,
    outlineVariant = DarkBorderAlt,
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightText,
    primaryContainer = LightAccent,
    onPrimaryContainer = LightText,
    secondary = LightAccent,
    onSecondary = LightText,
    background = LightBackground,
    onBackground = LightText,
    surface = Color(0xFFFFFBFF),
    onSurface = LightText,
    error = ErrorRed,
    outline = LightBorder,
)

@Composable
fun Startup_hackathon20Theme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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