package com.renea.psicologiauv.gui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import androidx.compose.runtime.*
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.DatosIdiomas
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.theme.AmberAccent
import com.renea.psicologiauv.ui.theme.AprobadoColor
import kotlinx.coroutines.delay

// ========================================================================
// 3. AVANCE - reporte de progreso
// ========================================================================

@Composable
fun PantallaAvance(
    modifier: Modifier = Modifier,
    programa: ProgramaM,
    viewModel: ControlV
) {
    val datos = remember(programa) { calcularDatosAvance(programa) }

    var materiaIdiomaSeleccionada by remember {
        mutableStateOf<Asignatura?>(null)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaHeroeProgreso(
                progreso = datos.avanceCarrera,
                creditosCursados = datos.creditosCursados,
                creditosCarrera = datos.creditosCarreraTotal,
                linea = programa.estudiante?.linea
            )

            TarjetaAvancePorArea(avances = datos.avances)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    BarraProgresoComplementarios(
                        creditosAprobados = datos.creditosComplementariosAprobados,
                        creditosTotales = programa.creditosComplementarios
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CajonIdioma(
                        nombre = "Idiomas I",
                        cumple = datos.idiomas.idiomasIAprobado,
                        habilitado = datos.idiomas.existeIdiomasI,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            programa.materiaIdiomaActiva(datos.idiomas.idiomasICandidatas)?.let {
                                materiaIdiomaSeleccionada = it
                            }
                        }
                    )

                    CajonIdioma(
                        nombre = "Idiomas II",
                        cumple = datos.idiomas.idiomasIIAprobado,
                        habilitado = datos.idiomas.existeIdiomasII,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            programa.materiaIdiomaActiva(datos.idiomas.idiomasIICandidatas)?.let {
                                materiaIdiomaSeleccionada = it
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    materiaIdiomaSeleccionada?.let { materia ->
        DialogoInformacionIdioma(
            materia = materia,
            viewModel = viewModel,
            onDismiss = { materiaIdiomaSeleccionada = null }
        )
    }
}

// ========================================================================
// DATOS DERIVADOS DE AVANCE
// ========================================================================

/**
 * Agrega los campos de progreso general a los datos de idioma que ya
 * provee el modelo, para usarlos como snapshot inmutable en la UI.
 */
private data class DatosAvance(
    val avanceCarrera: Float,
    val avances: Map<String, Float>,
    val creditosCursados: Int,
    val creditosCarreraTotal: Int,
    val creditosComplementariosAprobados: Int,
    val idiomas: DatosIdiomas
)

private fun calcularDatosAvance(programa: ProgramaM): DatosAvance = DatosAvance(
    avanceCarrera = programa.avanceCarrera(),
    avances = programa.avanceAreas(),
    creditosCursados = programa.creditosQueSirvenParaCarrera(),
    creditosCarreraTotal = programa.creditosCarrera,
    creditosComplementariosAprobados = programa.creditosComplementariosAprobados(),
    idiomas = programa.datosIdiomas()
)

// ========================================================================
// TARJETA HÉROE: PROGRESO DE CARRERA (incluye el consejo rotativo)
// ========================================================================

private val ConsejosCampus = listOf(
    "Habito el campus y limpio los espacios que uso.",
    "No soy cochinx, no dejo colillas, ni latas tiradas en ningun espacio de mi univerisdad.",
    "Respeto a mis compañerxs y ellos me respetan también.",
    "Cuido las zonas verdes, porque son zonas de vida.",
    "El piso NO es basurero. Uso los botes de basura porque amo mi universidad.",
    "Bajo el volumen en espacios de estudio compartidos.",
    "Respeto a mis compañerxs en su diversidad.",
    "No soy conchudx, si exijo, aplico.",
    "No soy pusilanime, participo activamente en mi facultad.",
    "Si lo ensucio, lo limpio.",
    "Si ofendo a un compañero, me disculpo.",
    "No dejo basura 'para después'. Después siempre se convierte en nunca.",
    "Mi libertad termina donde empieza la libertad de lxs demás.",
    "Cuido a los animales que habitan el campus y no los molesto ni alimento irresponsablemente.",
    "No normalizo el acoso, la discriminación, ni las burlas. Si veo algo, no me hago de la vista gorda. BRINDO AYUDA.",
    "No soy espectadxr: si el campus es de todxs, su cuidado también.",
    "Todo lo hago desde el amor y los buenos deseos",
    "La digna rabia y la protesta: son consignas justas",
    "Después de sexto semestre parece que la universdiad y la vida pesan más, apoyate en tus amigos"
)

@Composable
private fun TarjetaHeroeProgreso(
    progreso: Float,
    creditosCursados: Int,
    creditosCarrera: Int,
    linea: String? = null,
    consejos: List<String> = ConsejosCampus,
    intervaloMs: Long = 10_000L
) {
    val progresoAnimado by animateFloatAsState(
        targetValue = (progreso / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "progresoCarrera"
    )

    val isDark = isSystemInDarkTheme()
    val temaLinea = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(linea)
    val colorAcento = temaLinea.colorPrimario
    val fondo = if (isDark) temaLinea.fondoOscuro else temaLinea.fondoClaro
    val colorTexto = if (isDark) Color.White else Color(0xFF17151A)
    val colorSubtexto = if (isDark) Color.White.copy(alpha = 0.70f) else temaLinea.colorSecundario
    val pistaProgreso = if (isDark) colorAcento.copy(alpha = 0.16f) else Color(0xFFE5E5EB)

    // Orden aleatorio de consejos:
    // cada vez que se crea esta pantalla se baraja la lista, por lo que
    // el primer consejo ya no es siempre el mismo. Además, se recorren
    // todos los consejos antes de volver a repetirlos.
    var ordenConsejos by remember(consejos) {
        mutableStateOf(consejos.shuffled())
    }
    var indiceConsejo by remember(consejos) { mutableStateOf(0) }

    LaunchedEffect(consejos) {
        while (true) {
            delay(intervaloMs.milliseconds)

            if (ordenConsejos.isEmpty()) continue

            if (indiceConsejo < ordenConsejos.lastIndex) {
                indiceConsejo++
            } else {
                // Ya se mostraron todos. Se vuelve a barajar para comenzar
                // otro ciclo aleatorio, evitando repetir inmediatamente
                // el último consejo mostrado.
                val ultimoConsejo = ordenConsejos[indiceConsejo]
                var nuevoOrden = consejos.shuffled()

                if (nuevoOrden.size > 1 && nuevoOrden.first() == ultimoConsejo) {
                    nuevoOrden = nuevoOrden.drop(1) + nuevoOrden.first()
                }

                ordenConsejos = nuevoOrden
                indiceConsejo = 0
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(233.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        border = BorderStroke(1.dp, colorAcento.copy(alpha = if (isDark) 0.25f else 0.15f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // =========================================================
            // ILUSTRACIÓN VECTORIAL DE FONDO (MARCA DE AGUA POR LÍNEA)
            // =========================================================

            com.renea.psicologiauv.ui.theme.IlustracionFondoLinea(
                tipo = temaLinea.tipo,
                colorAcento = colorAcento,
                isDark = isDark,
                modifier = Modifier.matchParentSize()
            )

            // =========================================================
            // CONTENIDO PRINCIPAL (progreso arriba + consejo abajo)
            // =========================================================

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 22.dp,
                        vertical = 18.dp
                    )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    // -----------------------------------------------------
                    // ANILLO DE PROGRESO
                    // -----------------------------------------------------

                    Box(
                        modifier = Modifier
                            .size(128.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnilloProgreso(
                            progreso = progresoAnimado,
                            diametro = 128.dp,
                            grosor = 11.dp,
                            colorFondo = pistaProgreso,
                            colorProgreso = colorAcento
                        ) {
                            Text(
                                text = "${"%.0f".format(progreso)}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(22.dp))

                    // -----------------------------------------------------
                    // INFORMACIÓN
                    // -----------------------------------------------------

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "Créditos de carrera",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorSubtexto
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$creditosCursados",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorTexto
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "/ $creditosCarrera cr.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = colorSubtexto,
                                modifier = Modifier.padding(bottom = 7.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Barra horizontal de progreso
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .background(
                                    color = pistaProgreso,
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progresoAnimado)
                                    .fillMaxHeight()
                                    .background(
                                        color = colorAcento,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // -----------------------------------------------------
                // CONSEJO ROTATIVO (integrado al hero)
                // -----------------------------------------------------

                HorizontalDivider(
                    color = colorAcento.copy(alpha = if (isDark) 0.25f else 0.15f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Crossfade(
                        targetState = ordenConsejos.getOrElse(indiceConsejo) {
                            consejos.firstOrNull().orEmpty()
                        },
                        label = "consejoRotativoHero"
                    ) { consejo ->
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Consejo: ")
                                }
                                append(consejo)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorTexto,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ========================================================================
// ANILLO DE PROGRESO REUTILIZABLE
// ========================================================================

@Composable
private fun AnilloProgreso(
    progreso: Float,
    diametro: androidx.compose.ui.unit.Dp,
    grosor: androidx.compose.ui.unit.Dp,
    colorFondo: Color,
    colorProgreso: Color,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(diametro),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosorPx = grosor.toPx()
            val diametroDibujo = size.minDimension - grosorPx
            val izquierda = (size.width - diametroDibujo) / 2f
            val arriba = (size.height - diametroDibujo) / 2f

            dibujarArcoProgreso(
                topLeft = Offset(izquierda, arriba),
                size = Size(diametroDibujo, diametroDibujo),
                grosor = grosorPx,
                anguloInicio = -90f,
                anguloBarridoTotal = 360f,
                progreso = progreso,
                colorFondo = colorFondo,
                colorProgreso = colorProgreso
            )
        }

        contenido()
    }
}

// ========================================================================
// AVANCE POR ÁREA
// ========================================================================

private fun iconoParaArea(indice: Int): ImageVector =
    when (indice) {
        0 -> Icons.AutoMirrored.Filled.MenuBook
        1 -> Icons.Filled.WorkspacePremium
        else -> Icons.Filled.Science
    }

@Composable
private fun TarjetaAvancePorArea(avances: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Avance por área",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                avances.entries.forEachIndexed { indice, (area, porcentaje) ->
                    AnilloArea(
                        nombre = area,
                        porcentaje = porcentaje,
                        icono = iconoParaArea(indice),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnilloArea(
    nombre: String,
    porcentaje: Float,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    val progresoObjetivo = (porcentaje / 100f).coerceIn(0f, 1f)

    val progresoAnimado by animateFloatAsState(
        targetValue = progresoObjetivo,
        animationSpec = tween(durationMillis = 900),
        label = "progresoArea"
    )

    val colorProgreso =
        if (progresoAnimado >= 1f) {
            AprobadoColor
        } else {
            MaterialTheme.colorScheme.primary
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnilloProgreso(
            progreso = progresoAnimado,
            diametro = 74.dp,
            grosor = 8.dp,
            colorFondo = MaterialTheme.colorScheme.surfaceVariant,
            colorProgreso = colorProgreso
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorProgreso,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "${"%.0f".format(porcentaje)}%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colorProgreso,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = nombre,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ========================================================================
// BARRA DE CRÉDITOS COMPLEMENTARIOS
// ========================================================================

@Composable
private fun BarraProgresoComplementarios(
    creditosAprobados: Int,
    creditosTotales: Int
) {
    val progreso = creditosAprobados.coerceIn(0, creditosTotales)
    val completo = progreso == creditosTotales && creditosTotales > 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (completo) Icons.Filled.CheckCircle
                    else Icons.Filled.HourglassEmpty,
                    contentDescription = null,
                    tint = if (completo) AprobadoColor else AmberAccent,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Créditos complementarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$progreso / $creditosTotales",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (completo)
                    AprobadoColor
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(creditosTotales) { indice ->
                val completado = indice < progreso

                val colorSegmento =
                    when {
                        !completado -> MaterialTheme.colorScheme.surfaceVariant
                        completo -> AprobadoColor
                        else -> MaterialTheme.colorScheme.primary
                    }

                val alturaAnimada by animateFloatAsState(
                    targetValue = if (completado) 1f else 0.4f,
                    animationSpec = tween(durationMillis = 500),
                    label = "segmentoComplementario"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height((10 + 14 * alturaAnimada).dp),
                    shape = RoundedCornerShape(50),
                    color = colorSegmento
                ) {}
            }
        }
    }
}

// ========================================================================
// CAJÓN DE IDIOMA
// ========================================================================

@Composable
private fun CajonIdioma(
    nombre: String,
    cumple: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor =
        when {
            !habilitado -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            cumple -> AprobadoColor.copy(alpha = 0.14f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        }

    val colorTexto =
        if (!habilitado) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Surface(
        modifier = Modifier
            .then(modifier)
            .height(92.dp)
            .clickable(enabled = habilitado, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (cumple)
                    Icons.Filled.CheckCircle
                else
                    Icons.Filled.Translate,
                contentDescription = null,
                tint = when {
                    !habilitado -> colorTexto
                    cumple -> AprobadoColor
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorTexto
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = when {
                    !habilitado -> "No cursada"
                    cumple -> "Aprobada"
                    else -> "Pendiente"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    !habilitado -> colorTexto
                    cumple -> AprobadoColor
                    else -> MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========================================================================
// MÓDULO DE INFORMACIÓN DE IDIOMAS
// ========================================================================

@Composable
private fun DialogoInformacionIdioma(
    materia: Asignatura,
    viewModel: ControlV,
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
                                imageVector = Icons.Filled.Translate,
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
                            etiqueta = "Créditos",
                            valor = "${materia.creditos} CR",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FichaInformacionTile(
                            etiqueta = "Calificación",
                            valor = if (materia.nota > 0) materia.nota.toString() else (materia.notaEspecial ?: "Sin nota"),
                            modifier = Modifier.weight(1f)
                        )
                        FichaInformacionTile(
                            etiqueta = "Estado",
                            valor = if (aprobada) "Aprobado" else "En curso",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // NOTA INFORMATIVA DE IDIOMAS
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Esta sección es llamada idiomas, no inglés. Dado que la naturaleza del requisito para el grado es cursar dos niveles de idioma extranjero. Usualmente se cursa inglés, pero alemán, japonés, etc. también son asignaturas válidas.",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.labelSmall.lineHeight
                            )
                        }
                    }
                }
            }
        }
    } else {
        DialogoEditarIdioma(
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

// ========================================================================
// MÓDULO DE EDICIÓN DE IDIOMAS
// ========================================================================

@Composable
private fun DialogoEditarIdioma(
    materia: Asignatura,
    viewModel: ControlV,
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
                                        imageVector = Icons.Default.CheckCircle,
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
                        viewModel.cambiarNotaAsignatura(
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

                    if (nuevoSemestre != null && codigoValido) {
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
