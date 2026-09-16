package com.renea.psicologiauv.gui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.AvanceLineaProfesional
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.theme.AprobadoColor

// ========================================================================
// 7. PRÁCTICAS - requisitos y líneas de profundización profesional
// ========================================================================
//
// REDISEÑO VISUAL
// ----------------
// - Los indicadores de créditos (semicírculos) ahora animan su avance y
//   llevan un ícono, en vez de aparecer estáticos.
// - Los 4 requisitos de asignatura (Ética, Diagnóstico, Fundamentación
//   I/II) pasan de cajas sólidas de color a tarjetas con ícono, en la
//   misma línea visual que los cajones de Idiomas de PantallaAvance.
// - Los 4 cajones de línea profesional (Social, Organizacional,
//   Educativa, Clínica/NeuroClínica) llevan ahora un ícono por línea.
// - Las tarjetas de nivel (I/II/III) de la línea seleccionada se
//   redondearon y llevan un ícono de estado en vez de solo color de
//   texto para la nota.
//
// OPTIMIZACIÓN
// ------------
// Los créditos de la línea seleccionada, los créditos de
// profesionalización, las líneas faltantes, el avance de las 4 líneas
// y las materias de la línea seleccionada se recalculaban en CADA
// recomposición (incluyendo las producidas por las animaciones de los
// semicírculos). Ahora viven en un solo `remember(programa,
// otraLineaSeleccionada) { ... }` (ver [DatosPracticas]), así que solo
// se recalculan cuando el programa o la línea seleccionada realmente
// cambian.

@Composable
fun PantallaPracticas(
    modifier: Modifier = Modifier,
    programa: ProgramaM,
    viewModel: ControlV? = null
) {
    // ================================================================
    // ESTADO ORIGINAL: se conserva
    // ================================================================
    var otraLineaSeleccionada by remember(programa.estudiante?.linea) {
        mutableStateOf(programa.estudiante?.linea?.takeIf { it != "No" })
    }

    // ================================================================
    // DATOS ORIGINALES: se conserva
    // ================================================================
    var expandidoRequisitos by remember { mutableStateOf(false) }
    var materiaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var indicadorExpandido by remember { mutableStateOf<String?>(null) }

    val datos = remember(programa, otraLineaSeleccionada) {
        calcularDatosPracticas(programa, otraLineaSeleccionada)
    }

    val scheme = MaterialTheme.colorScheme
    val primary = scheme.primary
    val text = scheme.onSurface
    val secondaryText = scheme.onSurfaceVariant
    val surface = scheme.surface
    val softSurface = scheme.surfaceVariant
    val outline = scheme.outlineVariant

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ================================================================
        // CABECERA — temática según línea profesional
        // ================================================================
        val isDark = isSystemInDarkTheme()
        val temaLinea = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(otraLineaSeleccionada)
        val colorAcento = temaLinea.colorPrimario
        val fondoCabecera = if (isDark) temaLinea.fondoOscuro else temaLinea.fondoClaro

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = fondoCabecera),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, colorAcento.copy(alpha = if (isDark) 0.25f else 0.15f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                com.renea.psicologiauv.ui.theme.IlustracionFondoLinea(
                    tipo = temaLinea.tipo,
                    colorAcento = colorAcento,
                    isDark = isDark,
                    modifier = Modifier.matchParentSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = colorAcento.copy(alpha = if (isDark) 0.18f else 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(temaLinea.iconoRes),
                                contentDescription = null,
                                tint = colorAcento,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (temaLinea.tipo != com.renea.psicologiauv.ui.theme.TipoLinea.NINGUNA)
                                "LÍNEA ${temaLinea.nombreVisible.uppercase()}"
                            else
                                "REQUISITOS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorAcento
                        )

                        Text(
                            text = "Prácticas profesionales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else text,
                            maxLines = 1
                        )
                    }

                    Surface(
                        modifier = Modifier.size(26.dp),
                        shape = RoundedCornerShape(13.dp),
                        color = colorAcento.copy(alpha = if (isDark) 0.15f else 0.08f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⋮",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento
                            )
                        }
                    }
                }
            }
        }

        // ================================================================
        // RESUMEN — cuatro semicírculos clickeables
        // ================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IndicadorCreditos(
                titulo = "Línea profesional ${otraLineaSeleccionada ?: "Ninguna"}",
                creditos = datos.creditosLineaSeleccionada,
                total = programa.creditosLinea,
                seleccionado = indicadorExpandido == "LINEA",
                onClick = {
                    indicadorExpandido = if (indicadorExpandido == "LINEA") null else "LINEA"
                },
                modifier = Modifier.weight(1f)
            )

            IndicadorCreditos(
                titulo = "Asignaturas profesionales",
                creditos = datos.creditosProfesionales,
                total = programa.creditosProfesionales,
                seleccionado = indicadorExpandido == "PROFESIONALES",
                onClick = {
                    indicadorExpandido = if (indicadorExpandido == "PROFESIONALES") null else "PROFESIONALES"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IndicadorCreditos(
                titulo = "Electivas complementarias",
                creditos = programa.creditoselectivasComplementarias(),
                total = 6,
                seleccionado = indicadorExpandido == "COMPLEMENTARIAS",
                onClick = {
                    indicadorExpandido = if (indicadorExpandido == "COMPLEMENTARIAS") null else "COMPLEMENTARIAS"
                },
                modifier = Modifier.weight(1f)
            )

            IndicadorCreditos(
                titulo = "Electivas profesionales",
                creditos = programa.creditoselectivasProfesionales(),
                total = 12,
                seleccionado = indicadorExpandido == "ELECTIVAS_PROF",
                onClick = {
                    indicadorExpandido = if (indicadorExpandido == "ELECTIVAS_PROF") null else "ELECTIVAS_PROF"
                },
                modifier = Modifier.weight(1f)
            )
        }

        BannerEstado(
            texto = if (programa.requisitoElectivas()) {
                "Cumple con los requisitos en electivas comp/prof"
            } else {
                "No cumple con los requisitos en electivas comp/prof"
            },
            cumple = programa.requisitoElectivas()
        )

        // ================================================================
        // ASIGNATURAS NECESARIAS (DESPLEGABLE)
        // ================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, outline.copy(alpha = 0.40f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val requisitosAprobados = programa.contarRequisitosAprobadosPracticas()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandidoRequisitos = !expandidoRequisitos },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Asignaturas necesarias",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = text,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primary.copy(alpha = 0.09f)
                    ) {
                        Text(
                            text = "$requisitosAprobados / 8",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = if (expandidoRequisitos) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expandidoRequisitos) "Colapsar" else "Expandir",
                        tint = secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (expandidoRequisitos) {
                    val lineas = listOf(
                        "Social" to Icons.Filled.Groups,
                        "Organizacional" to Icons.Filled.Business,
                        "Educativa" to Icons.Filled.School,
                        "Clínica/NeuroClínica" to Icons.Filled.LocalHospital
                    )

                    val asignaturasEstudiante = programa.estudiante?.asignaturas.orEmpty()
                    val matEtica = asignaturasEstudiante.find { it.codigo == "402203M" }
                    val matDiagnostico = asignaturasEstudiante.find { it.codigo == "402204M" }
                    val matFundI = asignaturasEstudiante.find { it.codigo == "402212M" }
                    val matFundII = asignaturasEstudiante.find { it.codigo == "402221M" }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TarjetaRequisito(
                                nombre = "Ética del ejercicio profesional",
                                cumple = programa.requisitosPracticasEtica(),
                                icono = Icons.Filled.Gavel,
                                modifier = Modifier.weight(1f),
                                onClick = { matEtica?.let { materiaSeleccionada = it } }
                            )
                            TarjetaRequisito(
                                nombre = "Eval. y Diagnóstico Psicológico",
                                cumple = programa.requisitosPracticasDiagnostico(),
                                icono = Icons.Filled.Description,
                                modifier = Modifier.weight(1f),
                                onClick = { matDiagnostico?.let { materiaSeleccionada = it } }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TarjetaRequisito(
                                nombre = "Práctica de fund. profesional I",
                                cumple = programa.requisitosPracticasFundamentacionI(),
                                icono = Icons.Filled.Filter1,
                                modifier = Modifier.weight(1f),
                                onClick = { matFundI?.let { materiaSeleccionada = it } }
                            )
                            TarjetaRequisito(
                                nombre = "Práctica de fund. profesional II",
                                cumple = programa.requisitosPracticasFundamentacionII(),
                                icono = Icons.Filled.Filter2,
                                modifier = Modifier.weight(1f),
                                onClick = { matFundII?.let { materiaSeleccionada = it } }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            lineas.forEach { (nombreLinea, icono) ->
                                CajonNivelLinea(
                                    linea = nombreLinea,
                                    icono = icono,
                                    avance = datos.avanceLineas[nombreLinea],
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }

                        BannerEstado(
                            texto = if (datos.todasLasLineasCursadas) {
                                "Cursó al menos un nivel de cada línea"
                            } else {
                                "No cursó al menos un nivel de cada línea"
                            },
                            cumple = datos.todasLasLineasCursadas
                        )
                    }
                }
            }
        }

        // ================================================================
        // LÍNEAS PROFESIONALES
        // ================================================================
        run {

            val lineaActual = otraLineaSeleccionada ?: "Ninguna"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, outline.copy(alpha = 0.40f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(11.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Líneas profesionales",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = text
                            )

                            Text(
                                text = lineaActual,
                                style = MaterialTheme.typography.labelMedium,
                                color = secondaryText
                            )
                        }

                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleLarge,
                            color = secondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ====================================================
                    // BOTONES DE LÍNEAS EN UNA SOLA FILA HORIZONTAL
                    // ====================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "Educativa",
                            "Social",
                            "Organizacional",
                            "Clínica",
                            "NeuroClínica"
                        ).forEach { lineaSeleccionable ->
                            FilterChip(
                                selected = otraLineaSeleccionada == lineaSeleccionable,
                                onClick = {
                                    otraLineaSeleccionada = lineaSeleccionable
                                },
                                label = {
                                    Text(
                                        text = lineaSeleccionable,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = softSurface.copy(alpha = 0.50f),
                                    labelColor = secondaryText,
                                    selectedContainerColor = primary.copy(alpha = 0.11f),
                                    selectedLabelColor = primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(13.dp))
                    HorizontalDivider(color = outline.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(13.dp))

                    // ====================================================
                    // MÓDULOS — TODOS EN UNA SOLA FILA HORIZONTAL
                    // ====================================================
                    if (datos.materiasLinea.isEmpty()) {
                        Text(
                            text = "No hay módulos registrados para esta línea todavía.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            datos.materiasLinea.take(3).forEachIndexed { indice, materia ->
                                if (indice > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                val nivel = when (indice) {
                                    0 -> "Nivel I"
                                    1 -> "Nivel II"
                                    else -> "Nivel III"
                                }

                                TarjetaNivelMateria(
                                    nivel = nivel,
                                    aprobada = materia.aprobo(),
                                    modifier = Modifier.width(108.dp),
                                    onClick = { materiaSeleccionada = materia }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    } // end Column
    } // end Box

    // ================================================================
    // DIÁLOGO CENTRADO: detalle de asignaturas al clickear semicírculo
    // ================================================================
    if (indicadorExpandido != null) {
        val asignaturasEstudiante = programa.estudiante?.asignaturas.orEmpty()

        val (tituloBanner, materiasBanner) = when (indicadorExpandido) {
            "LINEA" -> {
                val nombreLinea = otraLineaSeleccionada ?: "Ninguna"
                "Línea $nombreLinea" to programa.asignaturasDeLinea(otraLineaSeleccionada)
            }
            "PROFESIONALES" -> {
                "Asignaturas profesionales" to asignaturasEstudiante.filter { it.lineaProfesional != "No" }
            }
            "COMPLEMENTARIAS" -> {
                "Electivas complementarias" to asignaturasEstudiante.filter {
                    it.componente.contains("Electiva complementaria", ignoreCase = true)
                }
            }
            "ELECTIVAS_PROF" -> {
                "Electivas profesionales" to asignaturasEstudiante.filter {
                    it.componente.contains("Electiva profesional", ignoreCase = true)
                }
            }
            else -> "" to emptyList()
        }

        Dialog(
            onDismissRequest = { indicadorExpandido = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 24.dp
            ) {
                PanelAsignaturas(
                    titulo = tituloBanner,
                    materias = materiasBanner,
                    onMateriaClick = { materia -> materiaSeleccionada = materia },
                    onCerrar = { indicadorExpandido = null }
                )
            }
        }
    }

    if (materiaSeleccionada != null) {
        DialogoInformacionMateria(
            materia = materiaSeleccionada!!,
            viewModel = viewModel,
            onDismiss = { materiaSeleccionada = null }
        )
    }
}

@Composable
private fun IndicadorCreditos(
    titulo: String,
    creditos: Int,
    total: Int,
    modifier: Modifier = Modifier,
    seleccionado: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val creditosSeguros = creditos.coerceIn(0, total)
    val progresoObjetivo =
        if (total > 0) creditosSeguros.toFloat() / total.toFloat() else 0f

    val progresoAnimado by animateFloatAsState(
        targetValue = progresoObjetivo,
        animationSpec = tween(durationMillis = 700),
        label = "progresoCreditos"
    )

    val completo = creditos >= total
    val scheme = MaterialTheme.colorScheme
    val colorIndicador = if (completo) AprobadoColor else scheme.primary

    Card(
        modifier = if (onClick != null) modifier.height(104.dp).clickable { onClick() } else modifier.height(104.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (seleccionado) 4.dp else 2.dp),
        border = BorderStroke(
            width = if (seleccionado) 1.5.dp else 1.dp,
            color = if (seleccionado) colorIndicador else scheme.outlineVariant.copy(alpha = 0.40f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = MaterialTheme.typography.labelMedium.lineHeight
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Semicírculo: 180°, no círculo completo.
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    val grosor = 6.dp.toPx()
                    val diametro = size.width - grosor
                    val left = grosor / 2f
                    val top = size.height - diametro / 2f

                    drawArc(
                        color = scheme.surfaceVariant,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(left, top),
                        size = Size(diametro, diametro),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = grosor)
                    )

                    if (progresoAnimado > 0f) {
                        drawArc(
                            color = colorIndicador,
                            startAngle = 180f,
                            sweepAngle = 180f * progresoAnimado,
                            useCenter = false,
                            topLeft = Offset(left, top),
                            size = Size(diametro, diametro),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = grosor)
                        )
                    }
                }

                Text(
                    text = "$creditosSeguros/$total",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorIndicador,
                    modifier = Modifier.padding(top = 17.dp)
                )
            }
        }
    }
}

// ========================================================================
// DATOS DERIVADOS DE PRÁCTICAS (cálculo centralizado y memoizado)
// ========================================================================

/**
 * Snapshot inmutable de todos los datos que la pantalla de Prácticas
 * necesita renderizar. La lógica de negocio vive en [ProgramaM];
 * esta data class solo es un contenedor de UI.
 */
private data class DatosPracticas(
    val creditosLineaSeleccionada: Int,
    val creditosProfesionales: Int,
    val todasLasLineasCursadas: Boolean,
    val avanceLineas: Map<String, AvanceLineaProfesional>,
    val materiasLinea: List<com.renea.psicologiauv.model.Asignatura>
)

private fun calcularDatosPracticas(
    programa: ProgramaM,
    otraLineaSeleccionada: String?
): DatosPracticas = DatosPracticas(
    creditosLineaSeleccionada = if (otraLineaSeleccionada != null)
        programa.creditosLineaProfesional(otraLineaSeleccionada) else 0,
    creditosProfesionales = programa.creditosDeProfesionalizacion(),
    todasLasLineasCursadas = programa.todasLasLineasCursadas(),
    avanceLineas = programa.avanceLineasProfundizacion(),
    materiasLinea = programa.asignaturasDeLinea(otraLineaSeleccionada)
)


// ========================================================================
// BANNER DE ESTADO (cumple / no cumple)
// ========================================================================

@Composable
private fun BannerEstado(texto: String, cumple: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val color = if (cumple) AprobadoColor else scheme.error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (cumple) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Filled.HourglassEmpty
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 2
            )
        }
    }
}

// ========================================================================
// TARJETA DE REQUISITO (Ética / Diagnóstico / Fundamentación I / II)
// ========================================================================

@Composable
private fun TarjetaRequisito(
    nombre: String,
    cumple: Boolean,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val stateColor = if (cumple) AprobadoColor else scheme.primary
    val stateBackground = if (cumple) {
        AprobadoColor.copy(alpha = 0.09f)
    } else {
        scheme.primary.copy(alpha = 0.055f)
    }

    Surface(
        modifier = if (onClick != null) modifier.height(66.dp).clickable { onClick() } else modifier.height(66.dp),
        shape = RoundedCornerShape(16.dp),
        color = stateBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(50),
                color = stateColor.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (cumple) {
                            Icons.Filled.CheckCircle
                        } else {
                            icono
                        },
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 3
                )

                Text(
                    text = if (cumple) "Cumple" else "Pendiente",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (cumple) AprobadoColor else scheme.primary
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

// ========================================================================
// CAJÓN DE NIVEL DE LÍNEA PROFESIONAL
// ========================================================================

@Composable
private fun CajonNivelLinea(
    linea: String,
    icono: ImageVector,
    avance: AvanceLineaProfesional?,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val aprobado = avance != null
    val color = if (aprobado) AprobadoColor else scheme.primary
    val textoNivel = avance?.textoNivel() ?: "No cursó"

    Surface(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = linea,
                color = scheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(50),
                color = color.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = textoNivel,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ========================================================================
// INDICADOR DE CRÉDITOS (semicírculo animado)
// ========================================================================

// ========================================================================
// TARJETA DE NIVEL DE MATERIA (I / II / III de la línea seleccionada)
// ========================================================================

@Composable
private fun TarjetaNivelMateria(
    nivel: String,
    aprobada: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colorTarjeta = MaterialTheme.colorScheme.surfaceContainerLow
    val colorEncabezado =
        if (aprobada) AprobadoColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondaryContainer
    val colorTextoEncabezado =
        if (aprobada) AprobadoColor else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = if (onClick != null) modifier.height(54.dp).clickable { onClick() } else modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 0.7.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = colorTarjeta)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(colorEncabezado),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (aprobada) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colorTextoEncabezado,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = nivel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colorTextoEncabezado,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ========================================================================
// MÓDULO DE INFORMACIÓN Y EDICIÓN DE ASIGNATURAS
// ========================================================================

@Composable
private fun DialogoInformacionMateria(
    materia: Asignatura,
    viewModel: ControlV?,
    onDismiss: () -> Unit
) {
    var modoEdicion by remember { mutableStateOf(false) }

    if (!modoEdicion) {
        val scheme = MaterialTheme.colorScheme
        val aprobada = materia.aprobo()
        val colorEstado = if (aprobada) AprobadoColor else scheme.primary

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = scheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.20f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HERO HEADER: Icon Badge
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = colorEstado.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (aprobada) Icons.Filled.CheckCircle else Icons.Filled.School,
                                contentDescription = null,
                                tint = colorEstado,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TITULO DE LA MATERIA (Nombre visible sólo aquí)
                    Text(
                        text = materia.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // CHIP DE ESTADO Y NOTA
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorEstado.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (aprobada) Icons.Filled.CheckCircle else Icons.Filled.Description,
                                contentDescription = null,
                                tint = colorEstado,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (aprobada) "Aprobada · Nota ${materia.textoNota()}" else "Pendiente",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorEstado
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.40f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // GRILLA DE DETALLES (Sin repetir el nombre)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FichaInformacionTile(
                            etiqueta = "Código",
                            valor = materia.codigo,
                            modifier = Modifier.weight(1f)
                        )
                        FichaInformacionTile(
                            etiqueta = "Semestre",
                            valor = if (materia.semestre > 0) "Semestre ${materia.semestre}" else "-",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FichaInformacionTile(
                            etiqueta = "Créditos",
                            valor = "${materia.creditos} CR",
                            modifier = Modifier.weight(1f)
                        )
                        FichaInformacionTile(
                            etiqueta = "Calificación",
                            valor = if (materia.nota > 0) materia.nota.toString() else (materia.notaEspecial ?: "Sin nota"),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BOTONES DE ACCIÓN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, scheme.outlineVariant)
                        ) {
                            Text(
                                text = "Cerrar",
                                color = scheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { modoEdicion = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colorEstado)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Editar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        DialogoEditarMateria(
            materia = materia,
            viewModel = viewModel,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun FichaInformacionTile(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = etiqueta.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DialogoEditarMateria(
    materia: Asignatura,
    viewModel: ControlV?,
    onDismiss: () -> Unit
) {
    var codigoTexto by remember(materia.codigo) {
        mutableStateOf(materia.codigo)
    }

    var semestreTexto by remember(materia.semestre) {
        mutableStateOf(materia.semestre.toString())
    }

    var notaTexto by remember(materia.nota) {
        mutableStateOf(
            if (materia.nota > 0) materia.nota.toString() else ""
        )
    }

    var notaEspecialSeleccionada by remember(materia.notaEspecial) {
        mutableStateOf(materia.notaEspecial)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),

        title = {
            Text(
                text = "Editar asignatura",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = materia.nombre,
                    onValueChange = {},
                    label = { Text("Nombre") },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = codigoTexto,
                    onValueChange = { codigoTexto = it },
                    label = { Text("Código") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = semestreTexto,
                    onValueChange = { semestreTexto = it },
                    label = { Text("Semestre") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = { input ->
                        notaTexto = input
                        val upper = input.trim().uppercase()

                        if (upper in listOf("C.U", "CU", "E.X", "EX", "A.P", "AP")) {
                            val mapeada = when (upper) {
                                "CU", "C.U" -> "C.U"
                                "EX", "E.X" -> "E.X"
                                "AP", "A.P" -> "A.P"
                                else -> upper
                            }

                            notaEspecialSeleccionada = mapeada
                            notaTexto = ""
                        } else if (input.isNotBlank()) {
                            notaEspecialSeleccionada = null
                        }
                    },
                    label = { Text("Nota") },
                    placeholder = {
                        Text(
                            notaEspecialSeleccionada
                                ?: materia.notaEspecial
                                ?: "-"
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Nota especial:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("C.U", "E.X", "A.P").forEach { marca ->
                        val seleccionada =
                            notaEspecialSeleccionada == marca

                        FilterChip(
                            selected = seleccionada,
                            onClick = {
                                if (seleccionada) {
                                    notaEspecialSeleccionada = null
                                } else {
                                    notaEspecialSeleccionada = marca
                                    notaTexto = ""
                                }
                            },
                            label = {
                                Text(
                                    text = marca,
                                    fontWeight =
                                        if (seleccionada)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                )
                            },
                            leadingIcon = if (seleccionada) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                if (notaEspecialSeleccionada != null) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Marcada como $notaEspecialSeleccionada (cuenta para créditos, no para promedio)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        notaEspecialSeleccionada = null
                        notaTexto = ""
                        viewModel?.cambiarNotaAsignatura(
                            materia.codigo,
                            0f
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reiniciar nota")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = materia.creditos.toString(),
                    onValueChange = {},
                    label = { Text("Créditos") },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },

        confirmButton = {
            Button(
                shape = RoundedCornerShape(50),
                onClick = {
                    val nuevoSemestre = semestreTexto.toIntOrNull()
                    val nuevaNota = notaTexto.toFloatOrNull()
                    val codigoValido = codigoTexto.isNotBlank()

                    if (nuevoSemestre != null && codigoValido && viewModel != null) {
                        viewModel.cambiarSemestreAsignatura(
                            materia.codigo,
                            nuevoSemestre
                        )

                        if (notaEspecialSeleccionada != null) {
                            viewModel.cambiarNotaEspecialAsignatura(
                                materia.codigo,
                                notaEspecialSeleccionada!!
                            )
                        } else if (nuevaNota != null) {
                            viewModel.cambiarNotaAsignatura(
                                materia.codigo,
                                nuevaNota
                            )
                        } else if (
                            notaTexto.isBlank() &&
                            materia.notaEspecial != null
                        ) {
                            viewModel.cambiarNotaAsignatura(
                                materia.codigo,
                                0f
                            )
                        }

                        if (codigoTexto != materia.codigo) {
                            viewModel.cambiarCodigoAsignatura(
                                materia.codigo,
                                codigoTexto
                            )
                        }

                        onDismiss()
                    }
                }
            ) {
                Text("Guardar")
            }
        }
    )
}


// ========================================================================
// PANEL DE ASIGNATURAS (diálogo centrado al hacer clic en semicírculo)
// ========================================================================

@Composable
private fun PanelAsignaturas(
    titulo: String,
    materias: List<Asignatura>,
    onMateriaClick: (Asignatura) -> Unit,
    onCerrar: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val primary = scheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // CABECERA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono decorativo
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = primary.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (materias.isEmpty()) "Sin asignaturas" else "${materias.size} asignaturas",
                    style = MaterialTheme.typography.labelSmall,
                    color = primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón X
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCerrar() },
                shape = RoundedCornerShape(50),
                color = scheme.onSurfaceVariant.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.40f))
        Spacer(modifier = Modifier.height(16.dp))

        // LISTA DE MATERIAS
        if (materias.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = scheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.50f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sin asignaturas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                materias.forEach { materia ->
                    TarjetaMateriaBanner(
                        materia = materia,
                        onClick = { onMateriaClick(materia) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaMateriaBanner(
    materia: Asignatura,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val aprobada = materia.aprobo()
    val acento = if (aprobada) AprobadoColor else scheme.primary
    val fondoCard = if (aprobada)
        AprobadoColor.copy(alpha = 0.06f)
    else
        scheme.surfaceContainerLow

    Card(
        modifier = Modifier
            .width(156.dp)
            .height(148.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondoCard),
        border = BorderStroke(1.dp, acento.copy(alpha = if (aprobada) 0.30f else 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ZONA SUPERIOR: indicador de estado + nombre
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 13.dp)
                    .padding(top = 13.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PASTILLA DE ESTADO
                Surface(
                    shape = RoundedCornerShape(50),
                    color = acento.copy(alpha = 0.13f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (aprobada) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = acento,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = if (aprobada) materia.textoNota() else "Pendiente",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = acento
                        )
                    }
                }

                // NOMBRE — máximo 2 líneas, corte con ellipsis
                Text(
                    text = materia.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }

            // ZONA INFERIOR: créditos + botón editar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp)
                    .padding(bottom = 11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // CRÉDITOS
                Text(
                    text = "${materia.creditos} créditos",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )

                // BOTÓN EDITAR
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(10.dp),
                    color = acento.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = acento,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = acento
                        )
                    }
                }
            }
        }
    }
}
