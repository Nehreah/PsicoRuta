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
    programa: ProgramaM
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

    Column(
        modifier = modifier
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
                        modifier = Modifier.size(30.dp),
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
        // RESUMEN — dos tarjetas compactas
        // ================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IndicadorCreditos(
                titulo = "Línea profesional ${otraLineaSeleccionada ?: "Ninguna"}",
                creditos = datos.creditosLineaSeleccionada,
                total = programa.creditosLinea,
                modifier = Modifier.weight(1f)
            )

            IndicadorCreditos(
                titulo = "Asignaturas profesionales",
                creditos = datos.creditosProfesionales,
                total = programa.creditosProfesionales,
                modifier = Modifier.weight(1f)
            )
        }

        // ================================================================
        // ESTADO DE REQUISITOS
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
                // Los 8 requisitos cuentan por igual: 4 asignaturas + 4 líneas.
                val requisitosAprobados = listOf(
                    programa.requisitosPracticasEtica(),
                    programa.requisitosPracticasDiagnostico(),
                    programa.requisitosPracticasFundamentacionI(),
                    programa.requisitosPracticasFundamentacionII(),
                    datos.avanceLineas.containsKey("Social"),
                    datos.avanceLineas.containsKey("Organizacional"),
                    datos.avanceLineas.containsKey("Educativa"),
                    datos.avanceLineas.containsKey("Clínica/NeuroClínica")
                ).count { it }

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        text = "Estado de requisitos",
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
                }

                // ------------------------------------------------------------
                // 8 requisitos compactos: 4 asignaturas + 4 líneas.
                // ------------------------------------------------------------
                val lineas = listOf(
                    "Social" to Icons.Filled.Groups,
                    "Organizacional" to Icons.Filled.Business,
                    "Educativa" to Icons.Filled.School,
                    "Clínica/NeuroClínica" to Icons.Filled.LocalHospital
                )

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
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaRequisito(
                            nombre = "Eval. y Diagnóstico Psicológico",
                            cumple = programa.requisitosPracticasDiagnostico(),
                            icono = Icons.Filled.Description,
                            modifier = Modifier.weight(1f)
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
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaRequisito(
                            nombre = "Práctica de fund. profesional II",
                            cumple = programa.requisitosPracticasFundamentacionII(),
                            icono = Icons.Filled.Filter2,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // El mensaje original se conserva y queda directamente
                    // antes de los cuatro cajones de línea.
                    BannerEstado(
                        texto = if (datos.todasLasLineasCursadas) {
                            "Cursó al menos un nivel de cada línea"
                        } else {
                            "No cursó al menos un nivel de cada línea"
                        },
                        cumple = datos.todasLasLineasCursadas
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        lineas.take(2).forEach { (nombreLinea, icono) ->
                            CajonNivelLinea(
                                linea = nombreLinea,
                                icono = icono,
                                avance = datos.avanceLineas[nombreLinea],
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        lineas.drop(2).forEach { (nombreLinea, icono) ->
                            CajonNivelLinea(
                                linea = nombreLinea,
                                icono = icono,
                                avance = datos.avanceLineas[nombreLinea],
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                    // TODOS LOS BOTONES ORIGINALES SE CONSERVAN
                    // ====================================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "Educativa",
                                "Social",
                                "Organizacional"
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
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 3.dp),
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

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
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
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 3.dp),
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
                                    modifier = Modifier.width(108.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun IndicadorCreditos(
    titulo: String,
    creditos: Int,
    total: Int,
    modifier: Modifier = Modifier
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
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.40f))
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
): DatosPracticas {

    val asignaturas = programa.estudiante?.asignaturas.orEmpty()

    val creditosLineaSeleccionada =
        asignaturas
            .filter { it.lineaProfesional == otraLineaSeleccionada && it.aprobo() }
            .sumOf { it.creditos }

    val lineasFaltantes = obtenerLineasFaltantes(programa)

    val materiasLinea =
        asignaturas.filter { it.lineaProfesional == otraLineaSeleccionada }

    return DatosPracticas(
        creditosLineaSeleccionada = creditosLineaSeleccionada,
        creditosProfesionales = programa.creditosDeProfesionalizacion(),
        todasLasLineasCursadas = lineasFaltantes.isEmpty(),
        avanceLineas = programa.avanceLineasProfundizacion(),
        materiasLinea = materiasLinea
    )
}

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
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val stateColor = if (cumple) AprobadoColor else scheme.primary
    val stateBackground = if (cumple) {
        AprobadoColor.copy(alpha = 0.09f)
    } else {
        scheme.primary.copy(alpha = 0.055f)
    }

    Surface(
        modifier = modifier.height(66.dp),
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

    val textoNivel = if (avance != null) {
        when (avance.nivel) {
            1 -> "Nivel I"
            2 -> "Nivel II"
            3 -> "Nivel III"
            else -> "Nivel ${avance.nivel}"
        }
    } else {
        "No cursó"
    }

    Surface(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(50),
                color = color.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = linea,
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Text(
                    text = textoNivel,
                    color = color,
                    style = MaterialTheme.typography.labelMedium
                )
            }
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
    modifier: Modifier = Modifier
) {
    val colorTarjeta = MaterialTheme.colorScheme.surfaceContainerLow
    val colorEncabezado =
        if (aprobada) AprobadoColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondaryContainer
    val colorTextoEncabezado =
        if (aprobada) AprobadoColor else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = modifier.height(54.dp),
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
// LÍNEAS FALTANTES
// ========================================================================

private fun obtenerLineasFaltantes(programa: ProgramaM): List<String> {

    val estudiante = programa.estudiante ?: return emptyList()

    val gruposLinea = listOf("Social", "Organizacional", "Educativa", "Clínica/NeuroClínica")

    val lineasCursadas =
        estudiante.asignaturas
            .filter { it.aprobo() }
            .map { it.lineaProfesional }
            .distinct()
            .toMutableSet()

    // ================================================================
    // CLÍNICA Y NEUROCLÍNICA CUENTAN COMO UNA MISMA LÍNEA
    // ================================================================

    if (lineasCursadas.contains("Clínica") || lineasCursadas.contains("NeuroClínica")) {
        lineasCursadas.remove("Clínica")
        lineasCursadas.remove("NeuroClínica")
        lineasCursadas.add("Clínica/NeuroClínica")
    }

    return gruposLinea.filter { it !in lineasCursadas }
}