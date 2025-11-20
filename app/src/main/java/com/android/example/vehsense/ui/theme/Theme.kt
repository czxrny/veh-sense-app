package com.android.example.vehsense.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6c734f),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3a3d2b),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF929c6b),
    tertiary = Color(0xFFc7cfa7),

    background = Color(0xFF262622),
    surface = Color(0xFF21211e),
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5E6346),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7E855D),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF464a35),
    tertiary = Color(0xFF383b2b),

    background = Color(0xFFFBFFEB),
    surface = Color(0xFFeaeddd),
    onSurface = Color.Black,
)

    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

@Composable
fun VehSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val systemUiController = rememberSystemUiController()
    val color = if(darkTheme) DarkColorScheme.background else LightColorScheme.background

    SideEffect {
        systemUiController.setStatusBarColor(color = color, darkIcons = !darkTheme)
        systemUiController.setNavigationBarColor(color = color, darkIcons = !darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
