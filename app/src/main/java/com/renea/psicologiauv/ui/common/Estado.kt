package com.renea.psicologiauv.ui.common

import androidx.compose.ui.graphics.Color
import com.renea.psicologiauv.ui.theme.AprobadoColor
import com.renea.psicologiauv.ui.theme.NoAprobadoColor

// ========================================================================
// ESTADO DE APROBACIÓN (UI)
// ========================================================================
// Punto único para toda la lógica de PRESENTACIÓN relacionada con si algo
// (una materia, un requisito, una línea profesional, un idioma...) está
// aprobado/cumplido o no. La lógica de NEGOCIO de si una materia está
// aprobada sigue viviendo en Asignatura.aprobo() / ProgramaM; esto solo
// decide cómo se ve en pantalla ese resultado.

/**
 * Color estándar para indicar si algo fue aprobado/cumplido (verde) o no
 * (rojo). Úsese en cualquier pantalla que necesite pintar un estado de
 * aprobación: materias, requisitos de línea, créditos completos, etc.
 */
fun colorEstado(aprobado: Boolean): Color =
    if (aprobado) AprobadoColor else NoAprobadoColor
