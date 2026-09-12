package com.renea.psicologiauv.gui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.ui.theme.AprobadoColor

/* ====================================================================== */
/* PENSUM                                                                  */
/* ====================================================================== */

@Composable
fun PantallaPensum(
    modifier: Modifier = Modifier,
    estudiante: Estudiante,
    viewModel: ControlV
) {

    val programa = viewModel.programa

    var mostrarFiltros by remember {
        mutableStateOf(false)
    }

    var filtroArea by remember {
        mutableStateOf<String?>(null)
    }

    var filtroAprobado by remember {
        mutableStateOf<Boolean?>(null)
    }

    var materiaSeleccionada by remember {
        mutableStateOf<Asignatura?>(null)
    }

    var mostrarElectivas by remember {
        mutableStateOf(false)
    }

    var mostrarMenuAgregarComplementaria by remember {
        mutableStateOf(false)
    }

    var mostrarDialogoAgregarAsignatura by remember {
        mutableStateOf(false)
    }

    var mostrarDialogoAgregarActividad by remember {
        mutableStateOf(false)
    }

    /* ================================================================== */
    /* FILTROS                                                             */
    /* ================================================================== */

    val areasDisponibles = remember(estudiante.asignaturas) {
        estudiante.asignaturas
            .map { it.area }
            .distinct()
            .sorted()
    }

    val asignaturasFiltradas =
        estudiante.asignaturas.filter { materia ->

            val cumpleArea =
                filtroArea == null ||
                        materia.area == filtroArea

            val cumpleEstado =
                filtroAprobado == null ||
                        materia.aprobo() == filtroAprobado

            cumpleArea && cumpleEstado
        }

    val porSemestre =
        asignaturasFiltradas
            .groupBy { it.semestre }
            .toSortedMap()

    val materiasElectivas =
        porSemestre[0].orEmpty()

    /* ================================================================== */
    /* PROGRESO                                                            */
    /* ================================================================== */

    val datosProgreso = remember(
        programa,
        estudiante.asignaturas
    ) {
        calcularProgresoPensum(
            programa = programa
        )
    }

    /* ================================================================== */
    /* INTERFAZ                                                            */
    /* ================================================================== */

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 10.dp,
                    bottom = 82.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            /* ========================================================== */
            /* HEADER                                                      */
            /* ========================================================== */

            PensumHeaderModerno(
                progreso = datosProgreso.progreso,
                creditosHechos = datosProgreso.creditosHechos,
                creditosFaltantes = datosProgreso.creditosFaltantes,
                creditosTotales = datosProgreso.creditosCarrera,
                linea = estudiante.linea
            )

            /* ========================================================== */
            /* FILTROS                                                     */
            /* ========================================================== */

            if (mostrarFiltros) {

                PanelFiltrosCompacto(
                    areasDisponibles = areasDisponibles,
                    filtroArea = filtroArea,
                    filtroAprobado = filtroAprobado,

                    onAreaChange = {
                        filtroArea = it
                    },

                    onEstadoChange = {
                        filtroAprobado = it
                    },

                    onLimpiar = {
                        filtroArea = null
                        filtroAprobado = null
                    }
                )
            }

            /* ========================================================== */
            /* SEMESTRES                                                    */
            /* ========================================================== */

            CuboSemestres(
                porSemestre = porSemestre,

                materiaSeleccionada = { materia ->
                    materiaSeleccionada = materia
                }
            )

            /* ========================================================== */
            /* ESTADO VACÍO                                                */
            /* ========================================================== */

            if (
                porSemestre
                    .filterKeys { it in 1..11 }
                    .isEmpty()
            ) {

                EstadoVacioPensum()
            }
        }

        /* ================================================================== */
        /* CINTA DE OPCIONES INFERIOR                                         */
        /* ================================================================== */

        CintaOpcionesPensum(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 10.dp
                    ),

            filtrosActivos =
                filtroArea != null ||
                        filtroAprobado != null,

            tieneElectivas =
                materiasElectivas.isNotEmpty(),

            onFiltros = {
                mostrarFiltros = !mostrarFiltros
            },

            onElectivas = {
                mostrarElectivas = true
            },

            onAgregar = {
                mostrarMenuAgregarComplementaria = true
            }
        )
    }

    /* ====================================================================== */
    /* MENÚ AGREGAR                                                           */
    /* ====================================================================== */

    if (mostrarMenuAgregarComplementaria) {

        AlertDialog(
            onDismissRequest = {
                mostrarMenuAgregarComplementaria = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            },

            title = {
                Text(
                    text = "Agregar créditos de actividades complementarias",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "¿Qué deseas registrar?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        mostrarMenuAgregarComplementaria = false
                        mostrarDialogoAgregarAsignatura = true
                    }
                ) {

                    Text("Asignatura")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {

                        mostrarMenuAgregarComplementaria = false
                        mostrarDialogoAgregarActividad = true
                    }
                ) {

                    Text("Actividad")
                }
            }
        )
    }

    /* ====================================================================== */
    /* AGREGAR ASIGNATURA                                                      */
    /* ====================================================================== */

    if (mostrarDialogoAgregarAsignatura) {

        DialogoAgregarAsignaturaComplementaria(
            viewModel = viewModel,

            onDismiss = {
                mostrarDialogoAgregarAsignatura = false
            }
        )
    }

    /* ====================================================================== */
    /* AGREGAR ACTIVIDAD                                                       */
    /* ====================================================================== */

    if (mostrarDialogoAgregarActividad) {

        DialogoAgregarActividadComplementaria(
            viewModel = viewModel,

            onDismiss = {
                mostrarDialogoAgregarActividad = false
            }
        )
    }

    /* ====================================================================== */
    /* ELECTIVAS                                                               */
    /* ====================================================================== */

    if (mostrarElectivas) {

        val promedio =
            programa.promedioSemestre(0)

        DialogoElectivasCompacto(
            materias = materiasElectivas,
            promedio = promedio,

            onMateriaClick = { materia ->

                materiaSeleccionada = materia
                mostrarElectivas = false
            },

            onDismiss = {
                mostrarElectivas = false
            }
        )
    }

    /* ====================================================================== */
    /* EDITAR                                                                  */
    /* ====================================================================== */

    materiaSeleccionada?.let { materia ->

        DialogoEditarAsignatura(
            materia = materia,
            viewModel = viewModel,

            onDismiss = {
                materiaSeleccionada = null
            }
        )
    }
}

/* ====================================================================== */
/* DATOS DE PROGRESO                                                       */
/* ====================================================================== */

private data class DatosProgresoPensum(
    val progreso: Float,
    val creditosHechos: Int,
    val creditosFaltantes: Int,
    val creditosCarrera: Int
)

/* ====================================================================== */
/* CÁLCULO DE PROGRESO                                                     */
/* ====================================================================== */

private fun calcularProgresoPensum(
    programa: com.renea.psicologiauv.model.ProgramaM
): DatosProgresoPensum {

    val creditosHechos = programa.creditosQueSirvenParaCarrera()

    val creditosFaltantes =
        (
                programa.creditosCarrera -
                        creditosHechos
                )
            .coerceAtLeast(0)

    val progreso =
        (
                programa.avanceCarrera() / 100f
                )
            .coerceIn(
                0f,
                1f
            )

    return DatosProgresoPensum(
        progreso = progreso,
        creditosHechos = creditosHechos,
        creditosFaltantes = creditosFaltantes,
        creditosCarrera = programa.creditosCarrera
    )
}

/* ====================================================================== */
/* HEADER MODERNO                                                          */
/* ====================================================================== */

@Composable
private fun PensumHeaderModerno(
    progreso: Float,
    creditosHechos: Int,
    creditosFaltantes: Int,
    creditosTotales: Int,
    linea: String? = null
) {
    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(durationMillis = 700),
        label = "pensumProgress"
    )

    val isDark = isSystemInDarkTheme()
    val temaLinea = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(linea)
    val colorAcento = temaLinea.colorPrimario
    val fondo = if (isDark) temaLinea.fondoOscuro else temaLinea.fondoClaro
    val colorTexto = if (isDark) Color.White else Color(0xFF17151A)
    val colorSubtexto = if (isDark) Color.White.copy(alpha = 0.70f) else temaLinea.colorSecundario
    val pistaProgreso = if (isDark) colorAcento.copy(alpha = 0.16f) else Color(0xFFE5E5EB)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(RoundedCornerShape(41.dp))
                            .background(colorAcento.copy(alpha = if (isDark) 0.18f else 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnilloPensum(
                            progreso = progresoAnimado,
                            colorProgreso = colorAcento,
                            colorFondo = pistaProgreso
                        )
                    }

                    Spacer(Modifier.width(13.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Créditos de carrera",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorSubtexto
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$creditosHechos",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorTexto
                            )
                            Text(
                                text = " / $creditosTotales cr.",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorSubtexto
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        LinearProgressIndicator(
                            progress = { progresoAnimado },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = colorAcento,
                            trackColor = pistaProgreso
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniStatPensum("Hechos", "$creditosHechos cr.", colorAcento, Modifier.weight(1f))
                    MiniStatPensum("Faltan", "$creditosFaltantes cr.", colorAcento, Modifier.weight(1f))
                    MiniStatPensum(
                        "Lo haces bien",
                        "No te rindas",
                        colorAcento,
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/* ====================================================================== */
/* MINI ESTADÍSTICA                                                        */
/* ====================================================================== */

@Composable
private fun MiniStatPensum(
    titulo: String,
    valor: String,
    colorAcento: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colorAcento.copy(alpha = if (isDark) 0.14f else 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(alpha = 0.70f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnilloPensum(
    progreso: Float,
    colorProgreso: Color = MaterialTheme.colorScheme.primary,
    colorFondo: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val diameter = size.minDimension - stroke
            val left = (size.width - diameter) / 2f
            val top = (size.height - diameter) / 2f
            drawArc(
                color = colorFondo,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(left, top),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = colorProgreso,
                startAngle = -90f,
                sweepAngle = 360f * progreso,
                useCenter = false,
                topLeft = Offset(left, top),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progreso * 100).toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colorProgreso
        )
    }
}

/* ====================================================================== */
/* CUBO DE SEMESTRES                                                       */
/* ====================================================================== */

@Composable
private fun CuboSemestres(
    porSemestre: Map<Int, List<Asignatura>>,
    materiaSeleccionada: (Asignatura) -> Unit
) {
    var semestreActual by remember { mutableIntStateOf(1) }

    fun seleccionarSemestre(semestre: Int) {
        semestreActual = semestre.coerceIn(1, 11)
    }

    val materiasActuales = porSemestre[semestreActual].orEmpty()
    val semestresDisponibles = (1..11).filter { semestre ->
        semestre != 11 || porSemestre[11].orEmpty().isNotEmpty()
    }
    val semestresNoActivos = semestresDisponibles.filter { it != semestreActual }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /* ================================================================ */
        /* SEMESTRE ACTIVO: es el único que despliega sus asignaturas.      */
        /* ================================================================ */
        CaraCuboSemestre(
            semestre = semestreActual,
            materias = materiasActuales,
            onMateriaClick = materiaSeleccionada
        )

        /* ================================================================ */
        /* SELECTOR DESLIZANTE: tres semestres visibles a la vez.          */
        /* ================================================================ */
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val anchoModulo = ((maxWidth - 12.dp) / 3f).coerceAtLeast(84.dp)
            val estadoDeslizamiento = rememberLazyListState()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                LazyRow(
                    state = estadoDeslizamiento,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 1.dp)
                ) {
                    items(
                        count = semestresNoActivos.size,
                        key = { indice -> semestresNoActivos[indice] }
                    ) { indice ->
                        val semestre = semestresNoActivos[indice]
                        ModuloSemestreDeslizante(
                            semestre = semestre,
                            materias = porSemestre[semestre].orEmpty(),
                            modifier = Modifier.width(anchoModulo),
                            onClick = { seleccionarSemestre(semestre) }
                        )
                    }
                }

                /* ======================================================== */
                /* INDICADOR: queda debajo del LazyRow, nunca superpuesto. */
                /* ======================================================== */
                if (semestresNoActivos.size > 3) {
                    val primeroVisible = estadoDeslizamiento.firstVisibleItemIndex
                    val progresoDeslizamiento =
                        (primeroVisible.toFloat() /
                                (semestresNoActivos.size - 3).coerceAtLeast(1))
                            .coerceIn(0f, 1f)

                    val colorBarra =
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    val colorIndicador =
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.width(5.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.42f)
                                .height(4.dp)
                        ) {
                            val radio = size.height / 2f
                            drawRoundRect(
                                color = colorBarra,
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    radio,
                                    radio
                                )
                            )

                            val anchoIndicador =
                                (size.width * 3f / semestresNoActivos.size)
                                    .coerceIn(24.dp.toPx(), size.width)
                            val desplazamientoMaximo =
                                (size.width - anchoIndicador).coerceAtLeast(0f)

                            drawRoundRect(
                                color = colorIndicador,
                                topLeft = Offset(
                                    desplazamientoMaximo * progresoDeslizamiento,
                                    0f
                                ),
                                size = Size(anchoIndicador, size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    radio,
                                    radio
                                )
                            )
                        }

                        Spacer(Modifier.width(5.dp))

                        Text(
                            text = "›",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

    }
}

/* ====================================================================== */
/* MÓDULO DE SEMESTRE DEL SELECTOR                                         */
/* ====================================================================== */

@Composable
private fun ModuloSemestreDeslizante(
    semestre: Int,
    materias: List<Asignatura>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val aprobadas = materias.count { it.aprobo() }
    val completo = materias.isNotEmpty() && aprobadas == materias.size
    val estado = if (completo) "Completado" else "Pendiente"
    val estadoColor = if (completo) AprobadoColor else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = semestre.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Semestre $semestre",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "${materias.size} Asig.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Text(
                text = "${materias.sumOf { it.creditos }} cr.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = estadoColor.copy(alpha = 0.07f)
            ) {
                Text(
                    text = estado,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = estadoColor.copy(alpha = 0.88f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CaraCuboSemestre(
    semestre: Int,
    materias: List<Asignatura>,
    onMateriaClick: (Asignatura) -> Unit,
    modifier: Modifier = Modifier
) {
    val todasAprobadas = materias.isNotEmpty() && materias.all { it.aprobo() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = semestre.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Semestre $semestre",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${materias.size} Asig. · ${materias.sumOf { it.creditos }} cr.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (todasAprobadas)
                        AprobadoColor.copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = if (todasAprobadas) "Completado" else "En curso",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (todasAprobadas)
                            AprobadoColor.copy(alpha = 0.90f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(7.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(Modifier.height(5.dp))

            if (materias.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Sin asignaturas registradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    materias.forEach { materia ->
                        MateriaCompacta(
                            materia = materia,
                            onClick = { onMateriaClick(materia) }
                        )
                    }
                }
            }
        }
    }
}

/* ====================================================================== */
/* MATERIA                                                                 */
/* ====================================================================== */

@Composable
private fun MateriaCompacta(
    materia: Asignatura,
    onClick: () -> Unit
) {

    val tieneNota =
        materia.nota > 0 ||
                materia.notaEspecial != null

    val aprobada =
        materia.aprobo()

    val fondoNota =
        when {

            aprobada ->
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.55f
                )

            tieneNota ->
                MaterialTheme.colorScheme
                    .errorContainer
                    .copy(
                        alpha = 0.72f
                    )

            else ->
                MaterialTheme.colorScheme
                    .surfaceVariant
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(11.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(29.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            fondoNota
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (aprobada) {

                    Icon(
                        imageVector =
                            Icons.Default.CheckCircle,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(16.dp),

                        tint =
                            AprobadoColor
                    )

                } else {

                    Text(
                        text =
                            materia.notaEspecial
                                ?: "?",

                        style =
                            MaterialTheme.typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                Modifier.width(7.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        materia.nombre,

                    style =
                        MaterialTheme.typography
                            .bodySmall,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        buildString {

                            append(
                                materia.codigo
                            )

                            append(" • ")

                            append(
                                materia.area
                            )
                        },

                    style =
                        MaterialTheme.typography
                            .labelSmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Spacer(
                Modifier.width(5.dp)
            )

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Surface(
                    shape =
                        RoundedCornerShape(6.dp),

                    color =
                        fondoNota
                ) {

                    Text(
                        text =
                            materia.textoNota(),

                        modifier =
                            Modifier.padding(
                                horizontal = 5.dp,
                                vertical = 2.dp
                            ),

                        style =
                            MaterialTheme.typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.ExtraBold,

                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )
                }

                Text(
                    text =
                        "${materia.creditos} cr.",

                    style =
                        MaterialTheme.typography
                            .labelSmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

/* ====================================================================== */
/* CINTA INFERIOR DE OPCIONES                                              */
/* ====================================================================== */

@Composable
private fun CintaOpcionesPensum(
    modifier: Modifier = Modifier,
    filtrosActivos: Boolean,
    tieneElectivas: Boolean,
    onFiltros: () -> Unit,
    onElectivas: () -> Unit,
    onAgregar: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpcionCintaPensum(Icons.Default.Tune, "Filtros", filtrosActivos, onClick = onFiltros)
            OpcionCintaPensum(
                icono = Icons.Default.School,
                texto = "Electivas",
                activa = false,
                habilitada = tieneElectivas,
                onClick = onElectivas
            )
            OpcionCintaPensum(
                icono = Icons.Default.Add,
                texto = "Cr/Compl",
                activa = false,
                habilitada = true,
                onClick = onAgregar
            )
        }
    }
}

@Composable
private fun OpcionCintaPensum(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    activa: Boolean,
    habilitada: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = habilitada, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = when {
            !habilitada -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
            activa -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                modifier = Modifier.size(16.dp),
                tint = when {
                    !habilitada -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    activa -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (activa) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = when {
                    !habilitada -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    activa -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/* ====================================================================== */
/* FILTROS                                                                 */
/* ====================================================================== */

@Composable
private fun PanelFiltrosCompacto(
    areasDisponibles: List<String>,
    filtroArea: String?,
    filtroAprobado: Boolean?,
    onAreaChange: (String?) -> Unit,
    onEstadoChange: (Boolean?) -> Unit,
    onLimpiar: () -> Unit
) {
    val hayFiltrosActivos = filtroArea != null || filtroAprobado != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Filtrar pensum",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (hayFiltrosActivos) {
                    TextButton(
                        onClick = onLimpiar,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Área",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(35.dp)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    FiltroChipCompacto(
                        texto = "Todas",
                        seleccionado = filtroArea == null,
                        onClick = { onAreaChange(null) }
                    )

                    areasDisponibles.forEach { area ->
                        FiltroChipCompacto(
                            texto = area,
                            seleccionado = filtroArea == area,
                            onClick = {
                                onAreaChange(if (filtroArea == area) null else area)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(35.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    FiltroChipCompacto(
                        texto = "Todos",
                        seleccionado = filtroAprobado == null,
                        onClick = { onEstadoChange(null) }
                    )
                    FiltroChipCompacto(
                        texto = "Aprobadas",
                        seleccionado = filtroAprobado == true,
                        onClick = {
                            onEstadoChange(if (filtroAprobado == true) null else true)
                        }
                    )
                    FiltroChipCompacto(
                        texto = "Pendientes",
                        seleccionado = filtroAprobado == false,
                        onClick = {
                            onEstadoChange(if (filtroAprobado == false) null else false)
                        }
                    )
                }
            }
        }
    }
}

/* ====================================================================== */
/* CHIP                                                                    */
/* ====================================================================== */

@Composable
private fun FiltroChipCompacto(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = {
            Text(
                text = texto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = Modifier.height(29.dp),
        shape = RoundedCornerShape(9.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = seleccionado,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        leadingIcon = if (seleccionado) {
            {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else null
    )
}

/* ====================================================================== */
/* ESTADO VACÍO                                                            */
/* ====================================================================== */

@Composable
private fun EstadoVacioPensum() {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),

        shape =
            RoundedCornerShape(17.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Default.School,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(36.dp),

                tint =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                text =
                    "No hay asignaturas",

                style =
                    MaterialTheme.typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(2.dp)
            )

            Text(
                text =
                    "No hay resultados con los filtros actuales.",

                style =
                    MaterialTheme.typography
                        .bodySmall,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

/* ====================================================================== */
/* ELECTIVAS                                                               */
/* ====================================================================== */

@Composable
private fun DialogoElectivasCompacto(
    materias: List<Asignatura>,
    promedio: Float,
    onMateriaClick: (Asignatura) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Asignaturas electivas",

                fontWeight =
                    FontWeight.ExtraBold
            )
        },

        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 480.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
            ) {

                if (promedio > 0f) {

                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(11.dp),

                        color =
                            MaterialTheme.colorScheme
                                .primaryContainer
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 11.dp,
                                    vertical = 7.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    "Promedio",

                                modifier =
                                    Modifier.weight(1f),

                                style =
                                    MaterialTheme.typography
                                        .labelMedium
                            )

                            Text(
                                text =
                                    "%.2f".format(
                                        promedio
                                    ),

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(6.dp)
                    )
                }

                materias.forEach { materia ->

                    ListItem(

                        modifier =
                            Modifier.clickable {

                                onMateriaClick(
                                    materia
                                )
                            },

                        headlineContent = {

                            Text(
                                text =
                                    materia.nombre,

                                maxLines = 1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        },

                        supportingContent = {

                            Text(
                                text =
                                    "${materia.codigo} • ${materia.area}"
                            )
                        },

                        trailingContent = {

                            Text(
                                text =
                                    "${materia.creditos} cr."
                            )
                        }
                    )

                    HorizontalDivider()
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cerrar"
                )
            }
        }
    )
}

/* ====================================================================== */
/* AGREGAR ASIGNATURA COMPLEMENTARIA                                       */
/* ====================================================================== */

@Composable
private fun DialogoAgregarAsignaturaComplementaria(
    viewModel: ControlV,
    onDismiss: () -> Unit
) {

    var nombreTexto by remember {
        mutableStateOf("")
    }

    var codigoTexto by remember {
        mutableStateOf("")
    }

    var semestreTexto by remember {
        mutableStateOf("")
    }

    var creditosTexto by remember {
        mutableStateOf("")
    }

    var notaTexto by remember {
        mutableStateOf("")
    }

    var notaEspecialSeleccionada by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "Agregar asignatura",
                fontWeight =
                    FontWeight.ExtraBold
            )
        },

        text = {

            Column(
                modifier =
                    Modifier.verticalScroll(
                        rememberScrollState()
                    )
            ) {

                CampoCompacto(
                    value =
                        nombreTexto,

                    onValueChange = {
                        nombreTexto = it
                    },

                    label =
                        "Nombre"
                )

                CampoCompacto(
                    value =
                        codigoTexto,

                    onValueChange = {
                        codigoTexto = it
                    },

                    label =
                        "Código"
                )

                CampoCompacto(
                    value =
                        semestreTexto,

                    onValueChange = {
                        semestreTexto = it
                    },

                    label =
                        "Semestre"
                )

                CampoCompacto(
                    value =
                        creditosTexto,

                    onValueChange = {
                        creditosTexto = it
                    },

                    label =
                        "Número de créditos"
                )

                CampoCompacto(
                    value =
                        notaTexto,

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

                    label =
                        "Nota"
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    text = "Nota especial:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("C.U", "E.X", "A.P").forEach { marca ->
                        val seleccionada = notaEspecialSeleccionada == marca
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
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
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
                        text = "Marcada como $notaEspecialSeleccionada ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancelar"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val creditos =
                        creditosTexto.toIntOrNull()

                    val semestre =
                        semestreTexto.toIntOrNull()

                    val nota =
                        notaTexto.toFloatOrNull()
                            ?: 0f

                    if (
                        nombreTexto.isNotBlank() &&
                        codigoTexto.isNotBlank() &&
                        semestre != null &&
                        creditos != null
                    ) {

                        viewModel
                            .agregarAsignaturaComplementaria(
                                nombre =
                                    nombreTexto,

                                codigo =
                                    codigoTexto,

                                semestre =
                                    semestre,

                                creditos =
                                    creditos,

                                nota =
                                    nota
                            )

                        if (notaEspecialSeleccionada != null) {
                            viewModel.cambiarNotaEspecialAsignatura(
                                codigoTexto,
                                notaEspecialSeleccionada!!
                            )
                        }

                        onDismiss()
                    }
                }
            ) {

                Text(
                    "Guardar"
                )
            }
        }
    )
}

/* ====================================================================== */
/* AGREGAR ACTIVIDAD                                                       */
/* ====================================================================== */

@Composable
private fun DialogoAgregarActividadComplementaria(
    viewModel: ControlV,
    onDismiss: () -> Unit
) {

    var nombreTexto by remember {
        mutableStateOf("")
    }

    var creditosTexto by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "Agregar actividad",
                fontWeight =
                    FontWeight.ExtraBold
            )
        },

        text = {

            Column {

                CampoCompacto(
                    value =
                        nombreTexto,

                    onValueChange = {
                        nombreTexto = it
                    },

                    label =
                        "Nombre"
                )

                CampoCompacto(
                    value =
                        creditosTexto,

                    onValueChange = {
                        creditosTexto = it
                    },

                    label =
                        "Número de créditos"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancelar"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val creditos =
                        creditosTexto.toIntOrNull()

                    if (
                        nombreTexto.isNotBlank() &&
                        creditos != null
                    ) {

                        viewModel
                            .agregarActividadComplementaria(
                                nombre =
                                    nombreTexto,

                                creditos =
                                    creditos
                            )

                        onDismiss()
                    }
                }
            ) {

                Text(
                    "Guardar"
                )
            }
        }
    )
}

/* ====================================================================== */
/* CAMPO                                                                   */
/* ====================================================================== */

@Composable
private fun CampoCompacto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(
        value =
            value,

        onValueChange =
            onValueChange,

        label = {
            Text(
                label
            )
        },

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 6.dp
                ),

        shape =
            RoundedCornerShape(11.dp),

        singleLine =
            true
    )
}

/* ====================================================================== */
/* EDITAR ASIGNATURA                                                       */
/* ====================================================================== */

@Composable
private fun DialogoEditarAsignatura(
    materia: Asignatura,
    viewModel: ControlV,
    onDismiss: () -> Unit
) {

    var semestreTexto by remember(
        materia.semestre
    ) {

        mutableStateOf(
            materia.semestre.toString()
        )
    }

    var notaTexto by remember(
        materia.nota
    ) {

        mutableStateOf(
            if (materia.nota > 0)
                materia.nota.toString()
            else
                ""
        )
    }

    var notaEspecialSeleccionada by remember(
        materia.notaEspecial
    ) {

        mutableStateOf(
            materia.notaEspecial
        )
    }

    var nombreTexto by remember(
        materia.nombre
    ) {

        mutableStateOf(
            materia.nombre
        )
    }

    var codigoTexto by remember(
        materia.codigo
    ) {

        mutableStateOf(
            materia.codigo
        )
    }

    val esElectiva =
        materia.componente.contains(
            "Electiva",
            ignoreCase = true
        ) ||
                materia.componente.contains(
                    "Idioma",
                    ignoreCase = true
                )

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Editar asignatura",

                fontWeight =
                    FontWeight.ExtraBold
            )
        },

        text = {

            Column(
                modifier =
                    Modifier.verticalScroll(
                        rememberScrollState()
                    )
            ) {

                if (esElectiva) {

                    CampoCompacto(
                        value =
                            nombreTexto,

                        onValueChange = {
                            nombreTexto = it
                        },

                        label =
                            "Nombre"
                    )

                    CampoCompacto(
                        value =
                            codigoTexto,

                        onValueChange = {
                            codigoTexto = it
                        },

                        label =
                            "Código"
                    )
                }

                CampoCompacto(
                    value =
                        semestreTexto,

                    onValueChange = {
                        semestreTexto = it
                    },

                    label =
                        "Semestre"
                )

                OutlinedTextField(
                    value =
                        notaTexto,

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

                    label = {
                        Text(
                            "Nota"
                        )
                    },

                    placeholder = {
                        Text(
                            notaEspecialSeleccionada
                                ?: materia.notaEspecial
                                ?: "-"
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(11.dp),

                    singleLine =
                        true
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    text = "Nota especial:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("C.U", "E.X", "A.P").forEach { marca ->
                        val seleccionada = notaEspecialSeleccionada == marca
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
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
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
                        text = "Marcada como $notaEspecialSeleccionada ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                OutlinedButton(
                    onClick = {
                        notaEspecialSeleccionada = null
                        notaTexto = ""
                        viewModel
                            .cambiarNotaAsignatura(
                                materia.codigo,
                                0f
                            )

                        onDismiss()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Reiniciar nota"
                    )
                }

                if (materia.complementaria) {

                    Spacer(
                        Modifier.height(4.dp)
                    )

                    TextButton(
                        onClick = {

                            viewModel
                                .eliminarAsignaturaComplementaria(
                                    materia.codigo
                                )

                            onDismiss()
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            ButtonDefaults
                                .textButtonColors(
                                    contentColor =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Warning,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(16.dp)
                        )

                        Spacer(
                            Modifier.width(5.dp)
                        )

                        Text(
                            "Eliminar asignatura"
                        )
                    }
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancelar"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    semestreTexto
                        .toIntOrNull()
                        ?.let {

                            viewModel
                                .cambiarSemestreAsignatura(
                                    materia.codigo,
                                    it
                                )
                        }

                    if (notaEspecialSeleccionada != null) {
                        viewModel
                            .cambiarNotaEspecialAsignatura(
                                materia.codigo,
                                notaEspecialSeleccionada!!
                            )
                    } else {
                        val num = notaTexto.toFloatOrNull()
                        if (num != null) {
                            viewModel
                                .cambiarNotaAsignatura(
                                    materia.codigo,
                                    num
                                )
                        } else if (notaTexto.isBlank() && materia.notaEspecial != null) {
                            viewModel
                                .cambiarNotaAsignatura(
                                    materia.codigo,
                                    0f
                                )
                        }
                    }

                    if (esElectiva) {

                        viewModel
                            .cambiarNombreAsignatura(
                                materia.codigo,
                                nombreTexto
                            )

                        viewModel
                            .cambiarCodigoAsignatura(
                                materia.codigo,
                                codigoTexto
                            )
                    }

                    onDismiss()
                }
            ) {

                Text(
                    "Guardar"
                )
            }
        }
    )
}