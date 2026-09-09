package com.renea.psicologiauv.gui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.theme.AmberAccent
import com.renea.psicologiauv.ui.theme.AprobadoColor

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
                creditosCarrera = datos.creditosCarreraTotal
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
                        cumple = datos.idiomasIAprobado,
                        habilitado = datos.existeIdiomasI,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            materiaIdiomaActiva(datos.idiomasICandidatas)?.let {
                                materiaIdiomaSeleccionada = it
                            }
                        }
                    )

                    CajonIdioma(
                        nombre = "Idiomas II",
                        cumple = datos.idiomasIIAprobado,
                        habilitado = datos.existeIdiomasII,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            materiaIdiomaActiva(datos.idiomasIICandidatas)?.let {
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

private data class DatosAvance(
    val avanceCarrera: Float,
    val avances: Map<String, Float>,
    val creditosCursados: Int,
    val creditosCarreraTotal: Int,
    val creditosComplementariosAprobados: Int,
    val existeIdiomasI: Boolean,
    val existeIdiomasII: Boolean,
    val idiomasIAprobado: Boolean,
    val idiomasIIAprobado: Boolean,
    val idiomasICandidatas: List<Asignatura>,
    val idiomasIICandidatas: List<Asignatura>
)

private fun calcularDatosAvance(programa: ProgramaM): DatosAvance {

    val asignaturas = programa.estudiante?.asignaturas.orEmpty()
    val creditosCursados = programa.creditosQueSirvenParaCarrera()

    val idiomasICandidatas = asignaturas.filter { it.nombre == "Idiomas I" }
    val idiomasIICandidatas = asignaturas.filter { it.nombre == "Idiomas II" }

    return DatosAvance(
        avanceCarrera = programa.avanceCarrera(),
        avances = programa.avanceAreas(),
        creditosCursados = creditosCursados,
        creditosCarreraTotal = programa.creditosCarrera,
        creditosComplementariosAprobados = programa.creditosComplementariosAprobados(),
        existeIdiomasI = idiomasICandidatas.isNotEmpty(),
        existeIdiomasII = idiomasIICandidatas.isNotEmpty(),
        idiomasIAprobado = idiomasICandidatas.any { it.aprobo() },
        idiomasIIAprobado = idiomasIICandidatas.any { it.aprobo() },
        idiomasICandidatas = idiomasICandidatas,
        idiomasIICandidatas = idiomasIICandidatas
    )
}

// ========================================================================
// MATERIA ACTIVA DE UN NOMBRE DE IDIOMA
// ========================================================================

private fun materiaIdiomaActiva(candidatas: List<Asignatura>): Asignatura? =
    candidatas.firstOrNull { it.aprobo() }
        ?: candidatas.firstOrNull { it.nota > 0f || it.notaEspecial != null }
        ?: candidatas.firstOrNull()

// ========================================================================
// TARJETA HÉROE: PROGRESO DE CARRERA
// ========================================================================

@Composable
private fun TarjetaHeroeProgreso(
    progreso: Float,
    creditosCursados: Int,
    creditosCarrera: Int
) {
    val progresoAnimado by animateFloatAsState(
        targetValue = (progreso / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "progresoCarrera"
    )

    // Color de acento rojo institucional (idéntico al hero de PantallaPensum).
    // El fondo, textos y barra de pista usan MaterialTheme para adaptarse
    // automáticamente a modo oscuro / claro.
    val rojo = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(233.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // =========================================================
            // CÍRCULOS DECORATIVOS (igual que PantallaPensum hero)
            // =========================================================

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .offset(x = (-82).dp, y = 110.dp)
                    .background(
                        color = rojo.copy(alpha = 0.055f),
                        shape = RoundedCornerShape(50)
                    )
            )

            Box(
                modifier = Modifier
                    .size(235.dp)
                    .offset(x = 245.dp, y = (-100).dp)
                    .background(
                        color = rojo.copy(alpha = 0.075f),
                        shape = RoundedCornerShape(50)
                    )
            )

            Box(
                modifier = Modifier
                    .size(175.dp)
                    .offset(x = 255.dp, y = 125.dp)
                    .background(
                        color = rojo.copy(alpha = 0.045f),
                        shape = RoundedCornerShape(50)
                    )
            )

            // =========================================================
            // CONTENIDO PRINCIPAL
            // =========================================================

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 22.dp,
                        vertical = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
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
                        colorFondo = surfaceVariant,
                        colorProgreso = rojo
                    ) {
                        Text(
                            text = "${"%.0f".format(progreso)}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = rojo,
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
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Créditos de carrera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$creditosCursados",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "/ $creditosCarrera cr.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceVariant,
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
                                color = surfaceVariant,
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progresoAnimado)
                                .fillMaxHeight()
                                .background(
                                    color = rojo,
                                    shape = RoundedCornerShape(50)
                                )
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
        0 -> Icons.Filled.MenuBook
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
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            iconContentColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(24.dp),

            icon = {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = null
                )
            },

            title = {
                Text(
                    text = materia.nombre,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    InformacionIdioma(
                        etiqueta = "Nombre",
                        valor = materia.nombre
                    )
                    InformacionIdioma(
                        etiqueta = "Código",
                        valor = materia.codigo
                    )
                    InformacionIdioma(
                        etiqueta = "Nota",
                        valor = if (materia.nota > 0) {
                            materia.nota.toString()
                        } else {
                            materia.notaEspecial ?: "-"
                        }
                    )
                    InformacionIdioma(
                        etiqueta = "Créditos",
                        valor = materia.creditos.toString()
                    )
                }
            },

            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            },

            confirmButton = {
                Button(
                    onClick = { modoEdicion = true },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Editar")
                }
            }
        )
    } else {
        DialogoEditarIdioma(
            materia = materia,
            viewModel = viewModel,
            onDismiss = onDismiss
        )
    }
}

// ========================================================================
// FILA DE INFORMACIÓN DE IDIOMA
// ========================================================================

@Composable
private fun InformacionIdioma(
    etiqueta: String,
    valor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
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
