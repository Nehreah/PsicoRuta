package com.renea.psicologiauv.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.ui.common.colorEstado
import com.renea.psicologiauv.ui.theme.AprobadoColor
import com.renea.psicologiauv.ui.theme.DarkGray

// ========================================================================
// FuncionesGui
// ========================================================================
// Este archivo es el punto único donde se declaran los componentes
// visuales de Jetpack Compose que se repiten en más de una pantalla:
// cómo SE VEN y cómo ACTÚAN. Las pantallas (PantallaAvance,
// PantallaPracticas, etc.) solo los importan en vez de reimplementarlos
// cada una por su lado.
//
// Antes de esto:
//   - PantallaAvance tenía "CajonIngles" (para Idiomas I / Idiomas II)
//   - PantallaPracticas tenía "CajonRequisitoMateria" (para Ética,
//     Evaluación y Diagnóstico, Práctica de fundamentación I/II)
// Ambos son, en el fondo, el mismo concepto: un cajón que dice si se
// cumplió o no un requisito. Se ven distinto a propósito (uno es una
// tarjeta clara y clicable con 3 estados, el otro una caja sólida de
// color con 2 estados), así que en vez de forzarlos a verse igual, ahora
// es UNA sola función (CajonRequisito) con un parámetro de "estilo" que
// elige cuál de las dos apariencias usar. El código de cada apariencia
// solo existe una vez, y ambas pantallas la importan.
//
// También había dos componentes de "arco de progreso" (el círculo de
// progreso de la carrera en Avance, y los medidores de créditos en
// Practicas) que dibujaban, cada uno por su lado, el mismo par de arcos
// (fondo + progreso) con Canvas. Ese dibujo repetido ahora vive en
// dibujarArcoProgreso().

// ------------------------------------------------------------------
// CAJÓN DE REQUISITO (usado por PantallaAvance y PantallaPracticas)
// ------------------------------------------------------------------

enum class EstiloCajonRequisito {
    /** Tarjeta clara, clicable, con 3 estados: deshabilitado / pendiente / cumple.
     * Estilo usado originalmente por los cajones de Idiomas I y II en PantallaAvance. */
    TARJETA,

    /** Caja sólida de color (verde/rojo), texto blanco, 2 estados: cumple o no.
     * Estilo usado originalmente por los cajones de requisitos de materias en PantallaPracticas. */
    SOLIDO
}

/**
 * Cajón que indica si se cumplió o no un requisito (una materia cursada,
 * un idioma aprobado, etc.). [estilo] decide la apariencia; el resto del
 * comportamiento (qué mostrar según [cumple] y [habilitado]) es el mismo
 * sin importar qué pantalla lo use.
 */
@Composable
fun CajonRequisito(
    nombre: String,
    cumple: Boolean,
    modifier: Modifier = Modifier,
    estilo: EstiloCajonRequisito = EstiloCajonRequisito.SOLIDO,
    habilitado: Boolean = true,
    onClick: () -> Unit = {}
) {
    when (estilo) {
        EstiloCajonRequisito.TARJETA ->
            CajonRequisitoTarjeta(
                nombre = nombre,
                cumple = cumple,
                habilitado = habilitado,
                modifier = modifier,
                onClick = onClick
            )

        EstiloCajonRequisito.SOLIDO ->
            CajonRequisitoSolido(
                nombre = nombre,
                cumple = cumple,
                modifier = modifier
            )
    }
}

@Composable
private fun CajonRequisitoTarjeta(
    nombre: String,
    cumple: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(enabled = habilitado) { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cumple) {
                Color(0xFFE8F5E9)  // Verde claro para aprobado
            } else if (habilitado) {
                MaterialTheme.colorScheme.surface
            } else {
                DarkGray.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (!habilitado) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!habilitado) {
                Text(
                    text = "No cursada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            } else if (cumple) {
                Text(
                    text = "✓ Aprobada",
                    style = MaterialTheme.typography.labelSmall,
                    color = AprobadoColor,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Pendiente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CajonRequisitoSolido(
    nombre: String,
    cumple: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(58.dp),
        shape = MaterialTheme.shapes.medium,
        color = colorEstado(cumple)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (cumple) "Cursó\n$nombre" else "No cursó\n$nombre",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 3
            )
        }
    }
}

// ------------------------------------------------------------------
// ARCO DE PROGRESO (usado por PantallaAvance y PantallaPracticas)
// ------------------------------------------------------------------

/**
 * Dibuja un arco de progreso compuesto por dos arcos superpuestos: uno de
 * fondo (la trayectoria completa) y uno que representa el avance real,
 * con extremos redondeados. Antes esto estaba duplicado en
 * CirculoProgresoCarrera (PantallaAvance, círculo completo) y en
 * IndicadorCreditos (PantallaPracticas, medio círculo): cada uno llamaba
 * a drawArc() dos veces con los mismos parámetros de estilo.
 *
 * El tamaño/posición del arco (dónde y de qué diámetro) sigue siendo
 * responsabilidad de quien llama, porque eso depende del layout de cada
 * pantalla; esta función solo centraliza CÓMO se dibuja el arco en sí.
 */
fun DrawScope.dibujarArcoProgreso(
    topLeft: Offset,
    size: Size,
    grosor: Float,
    anguloInicio: Float,
    anguloBarridoTotal: Float,
    progreso: Float,
    colorFondo: Color,
    colorProgreso: Color
) {
    drawArc(
        color = colorFondo,
        startAngle = anguloInicio,
        sweepAngle = anguloBarridoTotal,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = grosor, cap = StrokeCap.Round)
    )

    drawArc(
        color = colorProgreso,
        startAngle = anguloInicio,
        sweepAngle = anguloBarridoTotal * progreso,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = grosor, cap = StrokeCap.Round)
    )
}
