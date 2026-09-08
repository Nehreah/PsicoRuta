package com.renea.psicologiauv.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// ROJOS
// ============================================================
// Paleta institucional UV refinada: más saturación y contraste
// para una imagen más moderna, manteniendo el mismo rol de cada
// token (Red = acción/primario, Wine/Burgundy/DarkRed = superficies
// y contenedores oscuros) para no romper Theme.kt ni las pantallas
// que ya los usan directamente.

val Red = Color(0xFFD1123B)
val DeepRed = Color(0xFFA30E30)
val Wine = Color(0xFF7A1832)
val Burgundy = Color(0xFF551020)
val DarkRed = Color(0xFF3D0C18)

// ============================================================
// NEUTROS
// ============================================================

val Black = Color(0xFF0A0A0F)
val Charcoal = Color(0xFF17171D)
val DarkGray = Color(0xFF232329)

val White = Color(0xFFFAFAFA)
val LightGray = Color(0xFFEDEDF0)
val OffWhite = Color(0xFFE4E4E8)

val Gray = Color(0xFFB9B9C0)

// ============================================================
// ESTADOS
// ============================================================

// Verde oscuro para texto sobre fondos claros
val SuccessLight = Color(0xFF1B7A3D)

// Verde más claro para texto sobre fondos oscuros
val SuccessDark = Color(0xFF7FE3A0)

// Rojo oscuro para texto sobre fondos claros
val ErrorLight = Color(0xFFC4123B)

// Rojo claro para texto sobre fondos oscuros
val ErrorDark = Color(0xFFFF8FA3)

// ============================================================
// APROBACIÓN DE MATERIAS / REQUISITOS
// ============================================================
// Usar junto con colorEstado() en ui.common.Estado.kt

val AprobadoColor = Color(0xFF1FA35C)
val NoAprobadoColor = Color(0xFFE0294B)

// ============================================================
// ACENTOS DE MARCA (tarjetas destacadas / héroes)
// ============================================================
// Gradiente de marca fijo (no depende de tema claro/oscuro) para
// usar en tarjetas "héroe" que quieren llamar la atención, como el
// progreso de carrera en PantallaAvance. Se combina con texto en
// White/OffWhite por encima, que siempre tiene buen contraste.

val HeroGradientStart = Color(0xFFE0173F)
val HeroGradientEnd = Color(0xFF551020)

val AmberAccent = Color(0xFFE8A33D)