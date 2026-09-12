package com.renea.psicologiauv.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.renea.psicologiauv.R
import kotlin.math.cos
import kotlin.math.sin

// ========================================================================
// ENUM DE LÍNEAS PROFESIONALES
// ========================================================================

enum class TipoLinea(val id: String, val nombreVisible: String) {
    CLINICA("Clínica", "Clínica"),
    ORGANIZACIONAL("Organizacional", "Organizacional"),
    SOCIAL("Social", "Social"),
    NEUROCLINICA("NeuroClínica", "Neuroclínica"),
    EDUCATIVA("Educativa", "Educativa"),
    NINGUNA("Ninguna", "Sin línea")
}

// ========================================================================
// CONFIGURACIÓN VISUAL POR LÍNEA
// ========================================================================

data class TemaLineaConfig(
    val tipo: TipoLinea,
    val nombreVisible: String,
    val iconoRes: Int,
    val colorPrimario: Color,
    val colorSecundario: Color,
    val fondoClaro: Color,
    val fondoOscuro: Color,
    val chipFondoClaro: Color,
    val chipFondoOscuro: Color,
    val colorTextoChip: Color
)

// ========================================================================
// RESOLUTOR DE TEMA SEGÚN NOMBRE DE LÍNEA
// ========================================================================

fun obtenerTemaLinea(nombre: String?): TemaLineaConfig {
    val normalizado = nombre?.trim()?.lowercase().orEmpty()

    return when {
        normalizado.contains("clínica") && !normalizado.contains("neuro") ||
        normalizado.contains("clinica") && !normalizado.contains("neuro") -> {
            TemaLineaConfig(
                tipo = TipoLinea.CLINICA,
                nombreVisible = "Clínica",
                iconoRes = R.drawable.ic_linea_clinica,
                colorPrimario = Color(0xFFD4465B),       // Coral / Rosa clínico
                colorSecundario = Color(0xFF8C1D30),
                fondoClaro = Color(0xFFFDF1F3),          // Rosa pastel suave
                fondoOscuro = Color(0xFF221217),         // Tono rubí oscuro
                chipFondoClaro = Color(0xFFF6D6DD),
                chipFondoOscuro = Color(0xFF421E27),
                colorTextoChip = Color(0xFF7A1426)
            )
        }
        normalizado.contains("organizacional") -> {
            TemaLineaConfig(
                tipo = TipoLinea.ORGANIZACIONAL,
                nombreVisible = "Organizacional",
                iconoRes = R.drawable.ic_linea_organizacional,
                colorPrimario = Color(0xFF7A4FA3),       // Púrpura / Lavanda corporativo
                colorSecundario = Color(0xFF4E2C70),
                fondoClaro = Color(0xFFF6F0FA),          // Lavanda pastel suave
                fondoOscuro = Color(0xFF1B1226),         // Violeta pizarra oscuro
                chipFondoClaro = Color(0xFFEADBFA),
                chipFondoOscuro = Color(0xFF38234D),
                colorTextoChip = Color(0xFF47216D)
            )
        }
        normalizado.contains("social") -> {
            TemaLineaConfig(
                tipo = TipoLinea.SOCIAL,
                nombreVisible = "Social",
                iconoRes = R.drawable.ic_linea_social,
                colorPrimario = Color(0xFF41835A),       // Verde salvia / comunitario
                colorSecundario = Color(0xFF205133),
                fondoClaro = Color(0xFFEFF7F1),          // Menta pastel suave
                fondoOscuro = Color(0xFF111E15),         // Bosque oscuro
                chipFondoClaro = Color(0xFFD3EBD8),
                chipFondoOscuro = Color(0xFF1F3827),
                colorTextoChip = Color(0xFF1B532F)
            )
        }
        normalizado.contains("neuro") -> {
            TemaLineaConfig(
                tipo = TipoLinea.NEUROCLINICA,
                nombreVisible = "Neuroclínica",
                iconoRes = R.drawable.ic_linea_neuroclinica,
                colorPrimario = Color(0xFF2F76B9),       // Azul acero / neurociencia
                colorSecundario = Color(0xFF184978),
                fondoClaro = Color(0xFFEEF5FC),          // Azul cielo pastel suave
                fondoOscuro = Color(0xFF0F1B2C),         // Medianoche profundo
                chipFondoClaro = Color(0xFFD4E6F8),
                chipFondoOscuro = Color(0xFF1B324D),
                colorTextoChip = Color(0xFF134575)
            )
        }
        normalizado.contains("educativa") -> {
            TemaLineaConfig(
                tipo = TipoLinea.EDUCATIVA,
                nombreVisible = "Educativa",
                iconoRes = R.drawable.ic_linea_educativa,
                colorPrimario = Color(0xFFD88523),       // Ámbar dorado / conocimiento
                colorSecundario = Color(0xFF8A4E08),
                fondoClaro = Color(0xFFFDF8EE),          // Crema ámbar pastel suave
                fondoOscuro = Color(0xFF261C0F),         // Bronce tostado oscuro
                chipFondoClaro = Color(0xFFF7E6C8),
                chipFondoOscuro = Color(0xFF483318),
                colorTextoChip = Color(0xFF7E4603)
            )
        }
        else -> {
            TemaLineaConfig(
                tipo = TipoLinea.NINGUNA,
                nombreVisible = "Selecciona tu línea",
                iconoRes = R.drawable.ic_avatar_default,
                colorPrimario = Red,                     // Rojo institucional UV
                colorSecundario = Burgundy,
                fondoClaro = Color(0xFFFAF7F8),
                fondoOscuro = Color(0xFF18171C),
                chipFondoClaro = Color(0xFFF5D6DC),
                chipFondoOscuro = Color(0xFF38151D),
                colorTextoChip = Color(0xFF6E0D22)
            )
        }
    }
}

// ========================================================================
// ILUSTRACIÓN VECTORIAL DE FONDO POR LÍNEA (CANVAS WATERMARK)
// ========================================================================

@Composable
fun IlustracionFondoLinea(
    tipo: TipoLinea,
    colorAcento: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val alphaBase = if (isDark) 0.16f else 0.11f
    val strokeColor = colorAcento.copy(alpha = alphaBase)
    val fillColor = colorAcento.copy(alpha = alphaBase * 0.65f)
    val glowColor = colorAcento.copy(alpha = if (isDark) 0.08f else 0.05f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Círculos suaves de fondo de luz
        drawCircle(
            color = glowColor,
            radius = w * 0.45f,
            center = Offset(w * 0.90f, h * 0.15f)
        )
        drawCircle(
            color = glowColor,
            radius = w * 0.35f,
            center = Offset(w * 0.10f, h * 0.85f)
        )

        when (tipo) {
            TipoLinea.CLINICA -> dibujarArteClinica(w, h, strokeColor, fillColor)
            TipoLinea.ORGANIZACIONAL -> dibujarArteOrganizacional(w, h, strokeColor, fillColor)
            TipoLinea.SOCIAL -> dibujarArteSocial(w, h, strokeColor, fillColor)
            TipoLinea.NEUROCLINICA -> dibujarArteNeuroclinica(w, h, strokeColor, fillColor)
            TipoLinea.EDUCATIVA -> dibujarArteEducativa(w, h, strokeColor, fillColor)
            TipoLinea.NINGUNA -> dibujarArteInstitucional(w, h, strokeColor, fillColor)
        }
    }
}

// ------------------------------------------------------------------------
// ARTE: CLÍNICA (Corazones, cuidado, siluetas de mente)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteClinica(w: Float, h: Float, stroke: Color, fill: Color) {
    // Corazón grande superior derecho
    dibujarCorazon(center = Offset(w * 0.84f, h * 0.28f), radio = h * 0.24f, color = stroke, anchoTrazo = 2.dp.toPx())
    dibujarCorazon(center = Offset(w * 0.84f, h * 0.28f), radio = h * 0.14f, color = fill)

    // Corazón mediano inferior izquierdo
    dibujarCorazon(center = Offset(w * 0.14f, h * 0.72f), radio = h * 0.20f, color = stroke, anchoTrazo = 2.dp.toPx())
    dibujarCorazon(center = Offset(w * 0.14f, h * 0.72f), radio = h * 0.10f, color = fill)

    // Pequeños corazones flotantes
    dibujarCorazon(center = Offset(w * 0.28f, h * 0.25f), radio = h * 0.11f, color = fill)
    dibujarCorazon(center = Offset(w * 0.72f, h * 0.78f), radio = h * 0.12f, color = stroke, anchoTrazo = 1.5.dp.toPx())
    dibujarCorazon(center = Offset(w * 0.48f, h * 0.12f), radio = h * 0.08f, color = fill)

    // Ondas suaves de bienestar
    val onda = Path().apply {
        moveTo(0f, h * 0.65f)
        cubicTo(w * 0.25f, h * 0.50f, w * 0.35f, h * 0.80f, w * 0.60f, h * 0.60f)
        cubicTo(w * 0.75f, h * 0.45f, w * 0.85f, h * 0.75f, w, h * 0.55f)
    }
    drawPath(onda, color = stroke.copy(alpha = stroke.alpha * 0.7f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.dibujarCorazon(center: Offset, radio: Float, color: Color, anchoTrazo: Float? = null) {
    val path = Path().apply {
        moveTo(center.x, center.y + radio * 0.65f)
        cubicTo(
            center.x - radio * 1.1f, center.y + radio * 0.10f,
            center.x - radio * 0.95f, center.y - radio * 0.80f,
            center.x, center.y - radio * 0.25f
        )
        cubicTo(
            center.x + radio * 0.95f, center.y - radio * 0.80f,
            center.x + radio * 1.1f, center.y + radio * 0.10f,
            center.x, center.y + radio * 0.65f
        )
        close()
    }
    if (anchoTrazo != null) {
        drawPath(path, color = color, style = Stroke(width = anchoTrazo, cap = StrokeCap.Round))
    } else {
        drawPath(path, color = color)
    }
}

// ------------------------------------------------------------------------
// ARTE: ORGANIZACIONAL (Red de nodos, engranajes, jerarquía, documentos)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteOrganizacional(w: Float, h: Float, stroke: Color, fill: Color) {
    // Red de nodos superior derecha
    val n1 = Offset(w * 0.75f, h * 0.20f)
    val n2 = Offset(w * 0.90f, h * 0.35f)
    val n3 = Offset(w * 0.80f, h * 0.55f)
    val n4 = Offset(w * 0.95f, h * 0.68f)
    val n5 = Offset(w * 0.65f, h * 0.40f)

    drawLine(stroke, n1, n2, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, n2, n3, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, n3, n4, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, n1, n5, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, n5, n3, strokeWidth = 1.5.dp.toPx())

    listOf(n1, n2, n3, n4, n5).forEach { pos ->
        drawCircle(color = fill, radius = 10.dp.toPx(), center = pos)
        drawCircle(color = stroke, radius = 10.dp.toPx(), center = pos, style = Stroke(1.5.dp.toPx()))
    }

    // Engranaje / silueta inferior izquierda
    val centroEngranaje = Offset(w * 0.16f, h * 0.75f)
    val radioE = h * 0.18f
    drawCircle(color = stroke, radius = radioE, center = centroEngranaje, style = Stroke(2.dp.toPx()))
    drawCircle(color = fill, radius = radioE * 0.45f, center = centroEngranaje)

    // Dientes de engranaje
    for (i in 0 until 8) {
        val angulo = (i * 45f) * (Math.PI / 180f).toFloat()
        val x1 = centroEngranaje.x + (radioE - 4.dp.toPx()) * cos(angulo)
        val y1 = centroEngranaje.y + (radioE - 4.dp.toPx()) * sin(angulo)
        val x2 = centroEngranaje.x + (radioE + 8.dp.toPx()) * cos(angulo)
        val y2 = centroEngranaje.y + (radioE + 8.dp.toPx()) * sin(angulo)
        drawLine(stroke, Offset(x1, y1), Offset(x2, y2), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    }

    // Pequeño ícono de documento
    val docTop = Offset(w * 0.12f, h * 0.20f)
    drawRoundRect(
        color = stroke,
        topLeft = docTop,
        size = Size(24.dp.toPx(), 30.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(1.5.dp.toPx())
    )
    drawLine(stroke, Offset(docTop.x + 4.dp.toPx(), docTop.y + 8.dp.toPx()), Offset(docTop.x + 20.dp.toPx(), docTop.y + 8.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, Offset(docTop.x + 4.dp.toPx(), docTop.y + 15.dp.toPx()), Offset(docTop.x + 16.dp.toPx(), docTop.y + 15.dp.toPx()), strokeWidth = 1.5.dp.toPx())
}

// ------------------------------------------------------------------------
// ARTE: SOCIAL (Redes comunitarias, perfiles, grupos interconectados)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteSocial(w: Float, h: Float, stroke: Color, fill: Color) {
    // Grupo social derecha: 3 personas interconectadas
    val cA = Offset(w * 0.85f, h * 0.25f)
    val cB = Offset(w * 0.74f, h * 0.45f)
    val cC = Offset(w * 0.88f, h * 0.62f)

    drawLine(stroke, cA, cB, strokeWidth = 2.dp.toPx())
    drawLine(stroke, cB, cC, strokeWidth = 2.dp.toPx())
    drawLine(stroke, cA, cC, strokeWidth = 1.5.dp.toPx())

    dibujarPersonaNodo(cA, 12.dp.toPx(), stroke, fill)
    dibujarPersonaNodo(cB, 14.dp.toPx(), stroke, fill)
    dibujarPersonaNodo(cC, 11.dp.toPx(), stroke, fill)

    // Grupo social izquierda
    val sA = Offset(w * 0.14f, h * 0.30f)
    val sB = Offset(w * 0.24f, h * 0.55f)
    val sC = Offset(w * 0.12f, h * 0.78f)

    drawLine(stroke, sA, sB, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, sB, sC, strokeWidth = 2.dp.toPx())

    dibujarPersonaNodo(sA, 11.dp.toPx(), stroke, fill)
    dibujarPersonaNodo(sB, 15.dp.toPx(), stroke, fill)
    dibujarPersonaNodo(sC, 13.dp.toPx(), stroke, fill)

    // Ondas expansivas de comunidad
    drawCircle(color = stroke.copy(alpha = stroke.alpha * 0.4f), radius = h * 0.35f, center = sB, style = Stroke(1.dp.toPx()))
    drawCircle(color = stroke.copy(alpha = stroke.alpha * 0.3f), radius = h * 0.42f, center = cB, style = Stroke(1.dp.toPx()))
}

private fun DrawScope.dibujarPersonaNodo(center: Offset, radio: Float, stroke: Color, fill: Color) {
    // Cabeza
    drawCircle(color = fill, radius = radio * 0.65f, center = Offset(center.x, center.y - radio * 0.35f))
    drawCircle(color = stroke, radius = radio * 0.65f, center = Offset(center.x, center.y - radio * 0.35f), style = Stroke(1.5.dp.toPx()))
    // Hombros
    val hombros = Path().apply {
        moveTo(center.x - radio, center.y + radio)
        cubicTo(center.x - radio, center.y + radio * 0.35f, center.x + radio, center.y + radio * 0.35f, center.x + radio, center.y + radio)
        close()
    }
    drawPath(hombros, color = fill)
    drawPath(hombros, color = stroke, style = Stroke(1.5.dp.toPx()))
}

// ------------------------------------------------------------------------
// ARTE: NEUROCLÍNICA (Sinapsis, neuronas, redes axonales)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteNeuroclinica(w: Float, h: Float, stroke: Color, fill: Color) {
    // Red sináptica derecha
    val b1 = Offset(w * 0.82f, h * 0.22f)
    val b2 = Offset(w * 0.92f, h * 0.40f)
    val b3 = Offset(w * 0.72f, h * 0.50f)
    val b4 = Offset(w * 0.88f, h * 0.68f)
    val b5 = Offset(w * 0.70f, h * 0.75f)

    drawLine(stroke, b1, b2, strokeWidth = 2.dp.toPx())
    drawLine(stroke, b1, b3, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, b2, b4, strokeWidth = 2.dp.toPx())
    drawLine(stroke, b3, b4, strokeWidth = 1.5.dp.toPx())
    drawLine(stroke, b3, b5, strokeWidth = 2.dp.toPx())

    listOf(b1, b2, b3, b4, b5).forEach { pos ->
        drawCircle(color = fill, radius = 9.dp.toPx(), center = pos)
        drawCircle(color = stroke, radius = 9.dp.toPx(), center = pos, style = Stroke(2.dp.toPx()))
        drawCircle(color = stroke, radius = 3.dp.toPx(), center = pos)
    }

    // Hemisferio cerebral izquierdo estilizado
    val centroCerebro = Offset(w * 0.16f, h * 0.45f)
    val radioC = h * 0.25f
    val cerebro = Path().apply {
        moveTo(centroCerebro.x, centroCerebro.y - radioC)
        cubicTo(centroCerebro.x + radioC * 0.8f, centroCerebro.y - radioC * 0.9f, centroCerebro.x + radioC * 0.9f, centroCerebro.y, centroCerebro.x + radioC * 0.7f, centroCerebro.y + radioC * 0.8f)
        cubicTo(centroCerebro.x + radioC * 0.3f, centroCerebro.y + radioC * 1.1f, centroCerebro.x - radioC * 0.5f, centroCerebro.y + radioC * 0.9f, centroCerebro.x - radioC * 0.6f, centroCerebro.y + radioC * 0.4f)
        cubicTo(centroCerebro.x - radioC * 0.8f, centroCerebro.y - radioC * 0.1f, centroCerebro.x - radioC * 0.4f, centroCerebro.y - radioC * 0.8f, centroCerebro.x, centroCerebro.y - radioC)
        close()
    }
    drawPath(cerebro, color = stroke, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // Giros internos del cerebro
    val giro = Path().apply {
        moveTo(centroCerebro.x - radioC * 0.2f, centroCerebro.y - radioC * 0.4f)
        cubicTo(centroCerebro.x + radioC * 0.3f, centroCerebro.y - radioC * 0.2f, centroCerebro.x - radioC * 0.1f, centroCerebro.y + radioC * 0.2f, centroCerebro.x + radioC * 0.3f, centroCerebro.y + radioC * 0.4f)
    }
    drawPath(giro, color = stroke, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))
}

// ------------------------------------------------------------------------
// ARTE: EDUCATIVA (Birrete, libros abiertos, estrellas de conocimiento)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteEducativa(w: Float, h: Float, stroke: Color, fill: Color) {
    // Birrete superior derecho
    val birreteCentro = Offset(w * 0.82f, h * 0.25f)
    val romboW = 32.dp.toPx()
    val romboH = 14.dp.toPx()
    val rombo = Path().apply {
        moveTo(birreteCentro.x, birreteCentro.y - romboH)
        lineTo(birreteCentro.x + romboW, birreteCentro.y)
        lineTo(birreteCentro.x, birreteCentro.y + romboH)
        lineTo(birreteCentro.x - romboW, birreteCentro.y)
        close()
    }
    drawPath(rombo, color = fill)
    drawPath(rombo, color = stroke, style = Stroke(2.dp.toPx()))
    // Borla
    drawLine(stroke, birreteCentro, Offset(birreteCentro.x + romboW * 0.8f, birreteCentro.y + romboH * 1.5f), strokeWidth = 1.5.dp.toPx())

    // Libro abierto inferior izquierdo
    val libroX = w * 0.16f
    val libroY = h * 0.70f
    val anchoPagina = 24.dp.toPx()
    val altoPagina = 18.dp.toPx()

    val libro = Path().apply {
        // Página izquierda
        moveTo(libroX, libroY)
        cubicTo(libroX - anchoPagina * 0.5f, libroY - 4.dp.toPx(), libroX - anchoPagina, libroY - 2.dp.toPx(), libroX - anchoPagina, libroY - altoPagina)
        lineTo(libroX, libroY - altoPagina + 3.dp.toPx())
        // Página derecha
        lineTo(libroX + anchoPagina, libroY - altoPagina)
        cubicTo(libroX + anchoPagina, libroY - 2.dp.toPx(), libroX + anchoPagina * 0.5f, libroY - 4.dp.toPx(), libroX, libroY)
        close()
    }
    drawPath(libro, color = fill)
    drawPath(libro, color = stroke, style = Stroke(2.dp.toPx()))

    // Estrellitas de destello (4 puntas)
    dibujarEstrellaDestello(Offset(w * 0.70f, h * 0.55f), 10.dp.toPx(), stroke)
    dibujarEstrellaDestello(Offset(w * 0.90f, h * 0.70f), 7.dp.toPx(), stroke)
    dibujarEstrellaDestello(Offset(w * 0.28f, h * 0.25f), 8.dp.toPx(), stroke)
    dibujarEstrellaDestello(Offset(w * 0.12f, h * 0.35f), 6.dp.toPx(), stroke)
}

private fun DrawScope.dibujarEstrellaDestello(center: Offset, radio: Float, color: Color) {
    val estrella = Path().apply {
        moveTo(center.x, center.y - radio)
        cubicTo(center.x, center.y, center.x, center.y, center.x + radio, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y + radio)
        cubicTo(center.x, center.y, center.x, center.y, center.x - radio, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - radio)
        close()
    }
    drawPath(estrella, color = color)
}

// ------------------------------------------------------------------------
// ARTE: DEFAULT / INSTITUCIONAL (Círculos y arcos académicos UV)
// ------------------------------------------------------------------------

private fun DrawScope.dibujarArteInstitucional(w: Float, h: Float, stroke: Color, fill: Color) {
    drawCircle(color = stroke, radius = h * 0.45f, center = Offset(w * 0.90f, h * 0.15f), style = Stroke(2.dp.toPx()))
    drawCircle(color = fill, radius = h * 0.25f, center = Offset(w * 0.90f, h * 0.15f))
    drawCircle(color = stroke, radius = h * 0.32f, center = Offset(w * 0.10f, h * 0.85f), style = Stroke(1.5.dp.toPx()))
    drawCircle(color = fill, radius = h * 0.16f, center = Offset(w * 0.10f, h * 0.85f))
}
