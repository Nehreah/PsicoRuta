package com.renea.psicologiauv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// DARK
// ============================================================

private val DarkColorScheme = darkColorScheme(

    // Acción principal
    primary = White,
    onPrimary = Black,

    // Rojo secundario
    secondary = Wine,
    onSecondary = White,

    // Estado/acento
    tertiary = SuccessDark,
    onTertiary = Black,

    // Fondo general
    background = Black,
    onBackground = White,

    // Cards
    surface = Charcoal,
    onSurface = White,

    // Superficie secundaria
    surfaceVariant = DarkGray,
    onSurfaceVariant = OffWhite,

    // Contenedores principales
    primaryContainer = Burgundy,
    onPrimaryContainer = White,

    secondaryContainer = DarkRed,
    onSecondaryContainer = White,

    tertiaryContainer = DarkGray,
    onTertiaryContainer = White,

    // Estados
    error = ErrorDark,
    onError = Black,

    errorContainer = DarkRed,
    onErrorContainer = White
)


// ============================================================
// LIGHT
// ============================================================

private val LightColorScheme = lightColorScheme(

    // Acción principal
    primary = Red,
    onPrimary = White,

    // Rojo secundario
    secondary = DarkRed,
    onSecondary = White,

    // Estado/acento
    tertiary = SuccessLight,
    onTertiary = White,

    // Fondo general
    background = White,
    onBackground = Black,

    // Cards
    surface = White,
    onSurface = Black,

    // Superficie secundaria
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkRed,

    // Contenedores principales
    primaryContainer = LightGray,
    onPrimaryContainer = Burgundy,

    secondaryContainer = LightGray,
    onSecondaryContainer = DarkRed,

    tertiaryContainer = LightGray,
    onTertiaryContainer = Burgundy,

    // Estados
    error = ErrorLight,
    onError = White,

    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)


@Composable
fun PsicologiaUVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}