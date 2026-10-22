package com.renea.psicologiauv.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.common.Hito
import com.renea.psicologiauv.ui.common.colorEstado

// ========================================================================
// TRABAJO DE GRADO
// ========================================================================
// Contenido basado en la Resolución 60-2019 del Consejo del Instituto de
// Psicología (Reglamentación de Trabajo de Grado del Programa de Psicología,
// Universidad del Valle). Es un resumen informativo: el texto oficial es el
// que prevalece.
//
// Esta pantalla muestra solo lo esencial (requisito inicial y ruta por
// semestres). El detalle vive en PantallaReglamentoTrabajoGrado.

// ------------------------------------------------------------------------
// Estilo compartido por esta pantalla y la del reglamento
// ------------------------------------------------------------------------

/** Texto en negro para tema claro; en tema oscuro conserva el color del tema. */
@Composable
internal fun colorTinta(): Color =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else Color.Black

/** Tamaños compactos de texto. */
internal val TextoCuerpo = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
internal val TextoChico = TextStyle(fontSize = 11.sp, lineHeight = 14.sp)
internal val TextoEtiqueta = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)

@Composable
fun PantallaTrabajoGrado(
    modifier: Modifier = Modifier,
    programa: ProgramaM,
    onVerReglamento: () -> Unit = {}
) {
    val tinta = colorTinta()
    var mostrarDirector by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ================================================================
        // ENCABEZADO: título + botón de director y línea
        // ================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = tinta,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Trabajo de grado",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = tinta
                )
                OutlinedButton(
                    onClick = { mostrarDirector = true },
                    modifier = Modifier.widthIn(max = 150.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = tinta
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Elegir director de TG y línea de investigación",
                        style = TextoChico,
                        fontWeight = FontWeight.Bold,
                        color = tinta
                    )
                }
            }
        }

        // ================================================================
        // REQUISITO INICIAL
        // ================================================================
        RequisitoInicial(
            pendientes = programa.requisitosInicioTrabajoGrado()
                .filter { !it.second }
                .map { it.first }
        )

        // ================================================================
        // RUTA POR SEMESTRES
        // ================================================================
        Row(
            modifier = Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Ruta por semestres",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tinta
            )
        }

        val hitos = listOf(
            Hito(
                periodo = "Semestre VII",
                titulo = "Seminario de Trabajo de Grado I",
                descripcion = "Exploras temas y enfoques según las líneas del Instituto. Cierras con el Anteproyecto, avalado por tu Director."
            ),
            Hito(
                periodo = "Semestre VIII",
                titulo = "Seminario de Trabajo de Grado II",
                descripcion = "Elaboras el Proyecto con tu Director, lo entregas a la Dirección del Programa y lo sustentas ante dos jurados."
            ),
            Hito(
                periodo = "Semestre IX",
                titulo = "Trabajo de Grado I",
                descripcion = "Ejecutas el proyecto y entregas “Avances del Trabajo de Grado”; el Director conceptúa Aprobado o Desaprobado."
            ),
            Hito(
                periodo = "Semestre X",
                titulo = "Trabajo de Grado II",
                descripcion = "Entregas el “Informe final” a los dos jurados para su sustentación."
            )
        )
        Column {
            hitos.forEachIndexed { indice, hito ->
                HitoCompacto(hito = hito, esUltimo = indice == hitos.lastIndex)
            }
        }

        // ================================================================
        // ACCESO AL REGLAMENTO COMPLETO
        // ================================================================
        Button(
            onClick = onVerReglamento,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Ver reglamento completo",
                style = TextoCuerpo,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    // ====================================================================
    // DIÁLOGO: cómo elegir director y línea (resumen del reglamento)
    // ====================================================================
    if (mostrarDirector) {
        AlertDialog(
            onDismissRequest = { mostrarDirector = false },
            confirmButton = {
                TextButton(onClick = { mostrarDirector = false }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "Director y línea de investigación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = tinta
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Punto("1. Línea", "Elige un tema afín a las líneas de investigación y prácticas del Instituto.")
                    Punto("2. Director", "Acuerda con un docente del Instituto o de la Universidad que acepte dirigirte.")
                    Punto("3. Oficialización", "Las áreas relacionadas avalan y la Dirección del Programa lo oficializa. Si es externo, lo autoriza el Comité de Programa.")
                }
            }
        )
    }
}

// ========================================================================
// REQUISITO INICIAL
// ========================================================================

/**
 * Comprobador del requisito inicial. Si no falta ninguna asignatura básica
 * muestra el mensaje de cumplimiento; si falta alguna, la nombra.
 */
@Composable
private fun RequisitoInicial(pendientes: List<String>) {
    val scheme = MaterialTheme.colorScheme
    val tinta = colorTinta()
    val cumple = pendientes.isEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (cumple) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cumples",
                        tint = colorEstado(true),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cumples con todas las asignaturas básicas para iniciar el ciclo TG",
                        modifier = Modifier.weight(1f),
                        style = TextoCuerpo,
                        fontWeight = FontWeight.Bold,
                        color = tinta
                    )
                }
            } else {
                Text(
                    text = if (pendientes.size == 1) "Te falta aprobar:" else "Te faltan por aprobar:",
                    style = TextoCuerpo,
                    fontWeight = FontWeight.Bold,
                    color = tinta
                )
                pendientes.forEach { nombre ->
                    FilaRequisito(nombre = nombre, aprobada = false)
                }
            }
        }
    }
}

// ========================================================================
// HITO COMPACTO
// ========================================================================

/**
 * Versión compacta de TarjetaHito (misma línea de tiempo, menos relleno y
 * texto más pequeño) para que la ruta por semestres quepa en la pantalla.
 */
@Composable
private fun HitoCompacto(hito: Hito, esUltimo: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val tinta = colorTinta()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(scheme.primary)
            )
            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(scheme.outlineVariant)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (esUltimo) 0.dp else 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = scheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                    text = "${hito.periodo} · ${hito.titulo}",
                    style = TextoEtiqueta,
                    color = tinta
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = hito.descripcion,
                    style = TextoChico,
                    color = tinta
                )
            }
        }
    }
}

// ========================================================================
// COMPONENTES
// ========================================================================

/** Tarjeta desplegable: título con ícono y contenido que se abre al tocar. */
@Composable
internal fun SeccionTrabajoGrado(
    titulo: String,
    icono: ImageVector,
    inicialmenteAbierta: Boolean = false,
    contenido: @Composable () -> Unit
) {
    var abierta by remember { mutableStateOf(inicialmenteAbierta) }
    val scheme = MaterialTheme.colorScheme
    val tinta = colorTinta()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { abierta = !abierta }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = titulo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = tinta
                )
                Icon(
                    imageVector = if (abierta) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (abierta) "Contraer" else "Expandir",
                    tint = scheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = abierta) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    contenido()
                }
            }
        }
    }
}

@Composable
internal fun Texto(texto: String, negrita: Boolean = false) {
    Text(
        text = texto,
        style = TextoCuerpo,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        color = colorTinta()
    )
}

/** Párrafo con un título corto en negrilla al inicio. */
@Composable
internal fun Punto(titulo: String, texto: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(titulo)
                append(". ")
            }
            append(texto)
        },
        style = TextoCuerpo,
        color = colorTinta()
    )
}

/** Lista de opciones con nombre en negrilla y explicación debajo. */
@Composable
internal fun Opciones(vararg opciones: Pair<String, String>) {
    val scheme = MaterialTheme.colorScheme
    val tinta = colorTinta()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        opciones.forEach { (nombre, descripcion) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = scheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        text = nombre,
                        style = TextoEtiqueta,
                        color = tinta
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = descripcion,
                        style = TextoChico,
                        color = tinta
                    )
                }
            }
        }
    }
}

/** Fila de requisito: verde con check si está aprobada, rojo con X si no. */
@Composable
private fun FilaRequisito(nombre: String, aprobada: Boolean) {
    val color = colorEstado(aprobada)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (aprobada) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (aprobada) "Aprobada" else "Pendiente",
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = nombre,
            modifier = Modifier.weight(1f),
            style = TextoCuerpo,
            fontWeight = FontWeight.Medium,
            color = colorTinta()
        )
    }
}
