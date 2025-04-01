package com.example.csks_creatives.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.csks_creatives.presentation.components.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = tealGreen, // Buttons, accents
    secondary = tealGreen,
    tertiary = tealGreen,
    background = steelBlue, // Background color
    surface = azureBlue, // Surfaces like cards
    onPrimary = white, // Text on primary color
    onBackground = white, // Text/icons on background
    onSurface = white // Text/icons on surface
)

private val LightColorScheme = lightColorScheme(
    primary = tealGreen,
    secondary = tealGreen,
    tertiary = tealGreen,
    background = steelBlue,
    surface = azureBlue,
    onPrimary = white,
    onBackground = white,
    onSurface = white
)

@Composable
fun CSKS_CREATIVESTheme(
    darkTheme: Boolean = true,
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

@Composable
fun SetStatusAndNavigationBarColor(color: Color) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setNavigationBarColor(color)
        systemUiController.setSystemBarsColor(color)
    }
}