package com.renea.psicologiauv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ============================================================
// TEMA DE LA LÍNEA ACTUAL
// ============================================================
// Cualquier pantalla puede leer LocalTemaLinea.current para obtener la
// configuración cruda de la línea (por ejemplo colorSecundario, que es un
// tono profundo que siempre admite texto blanco encima).

val LocalTemaLinea = staticCompositionLocalOf { obtenerTemaLinea(null) }

/**
 * Control del tema claro/oscuro elegido por el usuario. [oscuro] indica el
 * tema activo y [alternar] lo cambia (y lo recuerda entre sesiones).
 */
class ControlTema(
    val oscuro: Boolean,
    val alternar: () -> Unit,
    val neutro: Boolean = false,
    val alternarNeutro: () -> Unit = {}
)

val LocalControlTema = staticCompositionLocalOf { ControlTema(false, {}) }

// ============================================================
// ESQUEMAS DERIVADOS DE LA LÍNEA
// ============================================================
// Todo el esquema de Material se calcula a partir de TemaLineaConfig, de
// modo que primary, containers, superficies, bordes y fondos siguen la
// línea elegida en claro y en oscuro. Los textos mantienen su contraste;
// solo toman un matiz suave de la línea.

// Reglas de diseño:
//  - Los MÓDULOS (tarjetas, chips, diálogos, barras) son neutros: blancos
//    en claro y casi negros en oscuro. El texto siempre contrasta: negro
//    sobre claro y blanco sobre oscuro.
//  - La línea solo tiñe lo gráfico: primary (íconos, barras, ilustraciones)
//    y el fondo general de la app (background).

private val TintaClara = Color(0xFF111114)
private val TextoSecundarioClaro = Color(0xFF45454D)
private val TextoSecundarioOscuro = Color(0xFFD0D0D6)

private fun esquemaClaro(c: TemaLineaConfig): ColorScheme {
    val primario = if (c.tipo == TipoLinea.NINGUNA) {
        Color(0xFF111114)
    } else {
        val oscurecer = when (c.tipo) {
            TipoLinea.EDUCATIVA -> 0.22f   // el ámbar necesita más peso para leerse
            else -> 0.12f
        }
        lerp(c.colorPrimario, Color.Black, oscurecer)
    }

    return lightColorScheme(
        primary = primario,
        onPrimary = White,
        primaryContainer = if (c.tipo == TipoLinea.NINGUNA) Color(0xFFF2F2F5) else Color(0xFFEDEDF1),
        onPrimaryContainer = TintaClara,

        secondary = if (c.tipo == TipoLinea.NINGUNA) Color(0xFF26262B) else c.colorSecundario,
        onSecondary = White,
        secondaryContainer = Color(0xFFF1F1F4),
        onSecondaryContainer = TintaClara,

        tertiary = SuccessLight,
        onTertiary = White,
        tertiaryContainer = Color(0xFFDDF3E4),
        onTertiaryContainer = Color(0xFF0F4A26),

        background = c.fondoClaro,
        onBackground = TintaClara,

        surface = White,
        onSurface = TintaClara,
        surfaceVariant = Color(0xFFF1F1F4),
        onSurfaceVariant = TextoSecundarioClaro,

        surfaceContainerLowest = White,
        surfaceContainerLow = Color(0xFFF8F8FA),
        surfaceContainer = Color(0xFFF3F3F6),
        surfaceContainerHigh = Color(0xFFEEEEF2),
        surfaceContainerHighest = Color(0xFFE9E9EE),

        outline = Color(0xFF8A8A94),
        outlineVariant = Color(0xFFDADADF),

        error = ErrorLight,
        onError = White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

private fun esquemaOscuro(c: TemaLineaConfig): ColorScheme {
    val fondo = c.fondoOscuro
    val esNeutro = c.tipo == TipoLinea.NINGUNA

    val primario = if (esNeutro) White else lerp(c.colorPrimario, White, 0.30f)
    val onPrimario = if (esNeutro) Color(0xFF0F0F12) else lerp(fondo, Color.Black, 0.40f)
    val secundario = if (esNeutro) Color(0xFFE2E2E8) else lerp(c.colorSecundario, White, 0.45f)

    return darkColorScheme(
        primary = primario,
        onPrimary = onPrimario,
        primaryContainer = if (esNeutro) Color(0xFF242429) else Color(0xFF2A2A31),
        onPrimaryContainer = White,

        secondary = secundario,
        onSecondary = onPrimario,
        secondaryContainer = Color(0xFF232329),
        onSecondaryContainer = White,

        tertiary = SuccessDark,
        onTertiary = Black,
        tertiaryContainer = Color(0xFF173A28),
        onTertiaryContainer = Color(0xFFB7E8C3),

        background = fondo,
        onBackground = White,

        surface = if (esNeutro) Color(0xFF16161A) else Charcoal,
        onSurface = White,
        surfaceVariant = if (esNeutro) Color(0xFF202026) else DarkGray,
        onSurfaceVariant = TextoSecundarioOscuro,

        surfaceContainerLowest = Color(0xFF0E0E12),
        surfaceContainerLow = Color(0xFF1A1A20),
        surfaceContainer = Color(0xFF1F1F25),
        surfaceContainerHigh = Color(0xFF26262D),
        surfaceContainerHighest = Color(0xFF2D2D34),

        outline = Color(0xFF8E8E98),
        outlineVariant = Color(0xFF3A3A42),

        error = ErrorDark,
        onError = Black,
        errorContainer = DarkRed,
        onErrorContainer = White
    )
}

@Composable
fun PsicologiaUVTheme(
    linea: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val tema = obtenerTemaLinea(linea)
    val colorScheme = if (darkTheme) esquemaOscuro(tema) else esquemaClaro(tema)

    CompositionLocalProvider(LocalTemaLinea provides tema) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
