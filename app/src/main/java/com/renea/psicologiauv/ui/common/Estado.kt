package com.renea.psicologiauv.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
//
// Antes cada pantalla (PantallaAvance, PantallaPensum, PantallaPracticas)
// repetía su propio "if (aprobado) Color(0xFF2E7D32) else Color(0xFFC62828)"
// e incluso, en PantallaPensum, el mismo bloque de texto estaba copiado y
// pegado dos veces dentro del mismo archivo.

/**
 * Color estándar para indicar si algo fue aprobado/cumplido (verde) o no
 * (rojo). Úsese en cualquier pantalla que necesite pintar un estado de
 * aprobación: materias, requisitos de línea, créditos completos, etc.
 */
fun colorEstado(aprobado: Boolean): Color =
    if (aprobado) AprobadoColor else NoAprobadoColor

/**
 * Texto "✅ Aprobada" / "❌ No aprobada" con el color correspondiente.
 * Reemplaza el bloque que estaba duplicado en PantallaPensum.
 */
@Composable
fun TextoEstadoAprobacion(
    aprobado: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    textAlign: TextAlign? = null
) {
    Text(
        text = if (aprobado) "✅ Aprobada" else "❌ No aprobada",
        style = style,
        color = colorEstado(aprobado),
        textAlign = textAlign,
        modifier = modifier
    )
}
