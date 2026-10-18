package com.renea.psicologiauv.gui

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.ModuloConsejoPensum
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.theme.AprobadoColor

/* ====================================================================== */
/* PENSUM                                                                  */
/* ====================================================================== */

@Composable
fun PantallaPensum(
    modifier: Modifier = Modifier,
    estudiante: Estudiante,
    viewModel: ControlV,
    onNavigate: (PantallasApp) -> Unit
) {

    val programa = viewModel.programa

    var moduloInformacion by remember { mutableStateOf<String?>(null) }

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
        programa.areasDelEstudiante()
    }

    val asignaturasFiltradas = programa.asignaturasFiltradas(
        filtroArea = filtroArea,
        soloAprobadas = filtroAprobado
    )

    val porSemestre = asignaturasFiltradas
        .groupBy { it.semestre }
        .toSortedMap()

    val materiasElectivas = programa.materiasElectivasPensum()

    // El estado académico derivado vive en ProgramaM; aquí solo se consume
    // para decidir qué semestre abrir al entrar al Pensum.
    val semestreEnCurso = programa.semestreEnCurso()


    /* ================================================================== */
    /* INTERFAZ                                                            */
    /* ================================================================== */

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                    bottom = 20.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            /* ========================================================== */
            /* HEADER                                                      */
            /* ========================================================== */



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
            /* SEMESTRES                                                   */
            /* ========================================================== */

            CuboSemestres(
                porSemestre = porSemestre,
                semestreInicial = semestreEnCurso,
                programa = programa,

                materiaSeleccionada = { materia ->
                    materiaSeleccionada = materia
                },

                onFiltros = {
                    mostrarFiltros = !mostrarFiltros
                },

                onNavigate = onNavigate
            )

            /* ========================================================== */
            /* CRÉDITOS COMPLEMENTARIOS — 5 CRÉDITOS                     */
            /* ========================================================== */

            BarraProgresoComplementariosPensum(
                creditosAprobados = programa.creditosComplementariosAprobados(),
                creditosTotales = programa.creditosComplementarios,
                onInfoClick = { moduloInformacion = "Créditos complementarios" },
                onCreditoClick = { indice ->
                    val materia = programa.asignaturaComplementariaParaCredito(indice)
                    if (materia != null) {
                        materiaSeleccionada = materia
                    } else {
                        mostrarMenuAgregarComplementaria = true
                    }
                }
            )

            /* ========================================================== */
            /* ELECTIVAS — PROGRESO POR ASIGNATURA                        */
            /* ========================================================== */

            BarrasElectivasPensum(
                estadosComplementarias = programa.estadosElectivasComplementarias(),
                estadosProfesionales = programa.estadosElectivasPensum().drop(2),
                onInfoClick = { moduloInformacion = "Electivas" },
                onMateriaClick = { codigo ->
                    // "3", "4" y "5" son los códigos provisionales de Electiva
                    // Profesional II, III y IV; tras importar el PDF esos espacios
                    // pasan a tener el código real de la electiva, así que se
                    // buscan por posición y no por código.
                    val posicionSlot = listOf("3", "4", "5").indexOf(codigo)
                    val materia = if (posicionSlot >= 0) {
                        programa.electivasProfesionalesIIaIV().getOrNull(posicionSlot)
                    } else {
                        programa.estudiante?.asignaturas
                            ?.find { it.codigo.trim() == codigo }
                    }
                    materia?.let { materiaSeleccionada = it }
                }
            )

            /* ========================================================== */
            /* IDIOMAS — PROGRESO POR ASIGNATURA                         */
            /* ========================================================== */

            val datosIdiomas = programa.datosIdiomas()
            BarrasIdiomasPensum(
                materiaIdiomaI = programa.materiaIdiomaActiva(datosIdiomas.idiomasICandidatas),
                materiaIdiomaII = programa.materiaIdiomaActiva(datosIdiomas.idiomasIICandidatas),
                onInfoClick = { moduloInformacion = "Idiomas" },
                onMateriaClick = { materia -> materiaSeleccionada = materia }
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

    }

    moduloInformacion?.let { modulo ->
        val descripcion = when (modulo) {
            "Créditos complementarios" -> "Estos créditos popularmente llamados créditos fantasma, son 5 créditos que debes cumplir como parte de tus actividades extraordinarias. Asistencia a eventos, congresos, simposios, presentación de ponencias, o en dado caso, cursar asignaturas equivalentes."
            "Electivas" -> "Aquí se muestra el avance de electivas complementarias y profesionales. Para la candidatura se consideran las electivas aprobadas según los requisitos del programa; entre las profesionales es obligatoria Problemas actuales del individuo en la sociedad."
            "Idiomas" -> "Uno de los requisitos obligatorios para ser candidato a grado de psicología es haber cursado dos niveles de idioma extranjeros. Lo común es cursar 2 niveles de inglés, no obstante cualquier otro idioma extranjero es válido para aprobar el requisito."
            else -> "Información del módulo $modulo."
        }
        AlertDialog(
            onDismissRequest = { moduloInformacion = null },
            title = { Text(modulo, fontWeight = FontWeight.Bold) },
            text = { Text(descripcion) },
            confirmButton = {
                TextButton(onClick = { moduloInformacion = null }) { Text("Entendido") }
            },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) }
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
            text = "${"%.0f".format(progreso * 100)}%",
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
    semestreInicial: Int,
    programa: ProgramaM,
    materiaSeleccionada: (Asignatura) -> Unit,
    onFiltros: () -> Unit,
    onNavigate: (PantallasApp) -> Unit
) {
    // Al entrar al Pensum, abrir el semestre que todavía está en curso.
    // Si el progreso cambia y el semestre inicial calculado cambia, actualizarlo.
    var semestreActual by remember(semestreInicial) {
        mutableIntStateOf(semestreInicial.coerceIn(1, 11))
    }

    fun seleccionarSemestre(semestre: Int) {
        semestreActual = semestre.coerceIn(1, 11)
    }

    val materiasActuales = porSemestre[semestreActual].orEmpty()
    val semestresDisponibles = (1..11).filter { semestre ->
        semestre != 11 || porSemestre[11].orEmpty().isNotEmpty()
    }
    val semestresNoActivos = semestresDisponibles.filter { it != semestreActual }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(semestreActual, semestresDisponibles) {
                var desplazamientoHorizontal = 0f
                var yaCambioSemestre = false
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, delta ->
                        if (!yaCambioSemestre) {
                            desplazamientoHorizontal += delta
                            if (desplazamientoHorizontal <= -72f) {
                                val indice = semestresDisponibles.indexOf(semestreActual)
                                if (indice in 0 until semestresDisponibles.lastIndex) {
                                    seleccionarSemestre(semestresDisponibles[indice + 1])
                                }
                                yaCambioSemestre = true
                            } else if (desplazamientoHorizontal >= 72f) {
                                val indice = semestresDisponibles.indexOf(semestreActual)
                                if (indice > 0) {
                                    seleccionarSemestre(semestresDisponibles[indice - 1])
                                }
                                yaCambioSemestre = true
                            }
                        }
                    },
                    onDragEnd = {
                        desplazamientoHorizontal = 0f
                        yaCambioSemestre = false
                    },
                    onDragCancel = {
                        desplazamientoHorizontal = 0f
                        yaCambioSemestre = false
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /* ================================================================ */
        /* SEMESTRE ACTIVO: es el único que despliega sus asignaturas.      */
        /* ================================================================ */
        var selectorAbierto by remember { mutableStateOf(false) }

        CaraCuboSemestre(
            semestre = semestreActual,
            materias = materiasActuales,
            programa = programa,
            onMateriaClick = materiaSeleccionada,
            onFiltros = onFiltros,
            semestresDisponibles = semestresDisponibles,
            porSemestre = porSemestre,
            selectorAbierto = selectorAbierto,
            onAbrirSelector = { selectorAbierto = true },
            onCerrarSelector = { selectorAbierto = false },
            onSeleccionarSemestre = { seleccionarSemestre(it) },
            onNavigate = onNavigate
        )
    }
}

/* ====================================================================== */
/* MÓDULO DE SEMESTRE DEL SELECTOR                                         */
/* ====================================================================== */

@Composable
private fun BarraProgresoComplementariosPensum(
    creditosAprobados: Int,
    creditosTotales: Int,
    onInfoClick: () -> Unit,
    onCreditoClick: (Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val progreso = creditosAprobados.coerceIn(0, creditosTotales)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Créditos complementarios",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Información sobre créditos complementarios", tint = scheme.primary)
                }

                Text(
                    text = "$progreso / $creditosTotales",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (progreso == creditosTotales && creditosTotales > 0)
                        AprobadoColor
                    else
                        scheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                repeat(creditosTotales) { indice ->
                    val completado = indice < progreso

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clickable { onCreditoClick(indice) },
                            shape = RoundedCornerShape(50),
                            color = if (completado) scheme.primary else scheme.surfaceVariant
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun BarrasElectivasPensum(
    estadosComplementarias: List<Boolean>,
    estadosProfesionales: List<Boolean>,
    onInfoClick: () -> Unit,
    onMateriaClick: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Electivas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Información sobre electivas", tint = scheme.primary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                val etiquetas = listOf(
                    "Comp. I", "Comp. II", "Prof. I", "Prof. II", "Prof. III", "Prof. IV"
                )
                val codigos = listOf("1", "2", "402197M", "3", "4", "5")
                val estados = estadosComplementarias + estadosProfesionales

                estados.forEachIndexed { index, cursada ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clickable { onMateriaClick(codigos[index]) },
                            shape = RoundedCornerShape(50),
                            color = if (cursada) scheme.primary else scheme.surfaceVariant
                        ) {}

                        Text(
                            text = etiquetas[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cursada) scheme.onSurface else scheme.onSurfaceVariant,
                            fontWeight = if (cursada) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarrasIdiomasPensum(
    materiaIdiomaI: Asignatura?,
    materiaIdiomaII: Asignatura?,
    onInfoClick: () -> Unit,
    onMateriaClick: (Asignatura) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val idiomas = listOf(
        "Idiomas I" to materiaIdiomaI,
        "Idiomas II" to materiaIdiomaII
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Idiomas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Información sobre idiomas", tint = scheme.primary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                idiomas.forEach { (nombre, materia) ->
                    val aprobado = materia?.aprobo() == true
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .then(
                                    if (materia != null) {
                                        Modifier.clickable { onMateriaClick(materia) }
                                    } else {
                                        Modifier
                                    }
                                ),
                            shape = RoundedCornerShape(50),
                            color = if (aprobado) scheme.primary else scheme.surfaceVariant
                        ) {}

                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (aprobado) scheme.onSurface else scheme.onSurfaceVariant,
                            fontWeight = if (aprobado) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaraCuboSemestre(
    semestre: Int,
    materias: List<Asignatura>,
    programa: ProgramaM,
    onMateriaClick: (Asignatura) -> Unit,
    onFiltros: () -> Unit,
    semestresDisponibles: List<Int>,
    porSemestre: Map<Int, List<Asignatura>>,
    selectorAbierto: Boolean,
    onAbrirSelector: () -> Unit,
    onCerrarSelector: () -> Unit,
    onSeleccionarSemestre: (Int) -> Unit,
    onNavigate: (PantallasApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val todasAprobadas = programa.semestreCompletado(semestre)
    val estadoColor = if (todasAprobadas) AprobadoColor else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // HERO DEL SEMESTRE: estado arriba a la derecha, título, asignaturas,
        // créditos y recomendación.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onFiltros),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtrar pensum",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = estadoColor.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = if (todasAprobadas) "Completado" else "En curso",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = (if (todasAprobadas) AprobadoColor else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.90f),
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Semestre $semestre",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = onAbrirSelector,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = "Elegir semestre",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = selectorAbierto,
                        onDismissRequest = onCerrarSelector
                    ) {
                        semestresDisponibles.forEach { semestreDisponible ->
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            "Semestre $semestreDisponible",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${porSemestre[semestreDisponible].orEmpty().size} asignaturas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    if (semestreDisponible == semestre) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    onSeleccionarSemestre(semestreDisponible)
                                    onCerrarSelector()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${materias.size} Asignaturas · ${materias.sumOf { it.creditos }} Créditos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(11.dp))

                ModuloConsejoPensum(
                    programa = programa,
                    semestre = semestre,
                    onIrPracticas = { onNavigate(PantallasApp.PRACTICAS_INTRO) },
                    onIrTrabajoGrado = { onNavigate(PantallasApp.TRABAJO_GRADO) }
                )

            }
        }

        // Las asignaturas quedan separadas visualmente del hero.
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

/* ====================================================================== */
/* MATERIA                                                                 */
/* ====================================================================== */

@Composable
private fun MateriaCompacta(
    materia: Asignatura,
    onClick: () -> Unit
) {
    val aprobada = materia.aprobo()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nombre de la asignatura con su código al lado, en letra más pequeña.
            val colorCodigo = MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = buildAnnotatedString {
                    append(materia.nombre)
                    if (materia.codigo.trim().length > 2) {
                        append("  ")
                        withStyle(
                            SpanStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorCodigo
                            )
                        ) {
                            append(materia.codigo.trim())
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = materia.textoNota(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            if (aprobada) {
                Spacer(Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Asignatura aprobada",
                    modifier = Modifier.size(13.dp),
                    tint = AprobadoColor
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

    val esElectiva = materia.esElectivaOIdioma()

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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