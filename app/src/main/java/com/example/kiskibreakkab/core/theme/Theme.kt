package com.example.kiskibreakkab.core.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

private val DarkColorScheme = darkColorScheme(
    primary = KiskiRed,
    secondary = KiskiBlack,
    tertiary = KiskiPurple,
    background = KiskiBlack,
    surface = Color(0xFF1E1E1E), // Slightly lighter than background
    onPrimary = KiskiWhite,
    onSecondary = KiskiWhite,
    onBackground = KiskiWhite,
    onSurface = KiskiWhite
)

private val LightColorScheme = lightColorScheme(
    primary = KiskiRed,
    secondary = KiskiLightGray,
    tertiary = KiskiPurple,
    background = KiskiWhite,
    surface = KiskiLightGray,
    onPrimary = KiskiWhite,
    onSecondary = KiskiBlack,
    onBackground = KiskiBlack,
    onSurface = KiskiBlack
)

@Composable
fun KiskiBreakKabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
