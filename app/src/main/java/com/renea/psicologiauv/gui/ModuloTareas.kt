package com.renea.psicologiauv.gui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.data.RepositorioTareas
import com.renea.psicologiauv.model.AsignaturaHorario
import com.renea.psicologiauv.model.Tarea
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Fecha ISO local compatible con minSdk 24, sin depender de java.time. */
private fun fechaActualIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/** Valida estrictamente el formato AAAA-MM-DD. */
private fun fechaValida(valor: String): Boolean = runCatching {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    val fecha = formato.parse(valor) ?: return@runCatching false
    formato.format(fecha) == valor
}.getOrDefault(false)

@Composable
internal fun ModuloTareas(contexto: Context, horario: List<AsignaturaHorario>, claseInicial: AsignaturaHorario? = null, onClaseInicialConsumida: () -> Unit = {}) {
    // El módulo inicia recogido y solo se despliega cuando el usuario lo solicita.
    var expandido by remember { mutableStateOf(false) } // Cerrado por defecto al abrir la pantalla.
    var tareas by remember { mutableStateOf(RepositorioTareas.obtener(contexto)) }
    var asignaturaActiva by remember { mutableStateOf<AsignaturaHorario?>(claseInicial) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var tareaPresentando by remember { mutableStateOf<Tarea?>(null) }
    var creando by remember { mutableStateOf(false) }
    var gestionandoAsignatura by remember { mutableStateOf<AsignaturaHorario?>(null) }
    LaunchedEffect(claseInicial) {
        if (claseInicial != null) {
            // Desde el horario se abre el gestor con todas las tareas de la asignatura,
            // incluidas las vencidas; no se fuerza directamente la creación.
            gestionandoAsignatura = claseInicial
            asignaturaActiva = null
            creando = false
            tareaEditando = null
            onClaseInicialConsumida()
        }
    }
    val asignaturas = remember(horario) { horario.distinctBy { it.codigo.ifBlank { it.nombreAsignatura } }.sortedBy { it.nombreAsignatura } }
    fun actualizar(nuevas: List<Tarea>) { tareas = nuevas; RepositorioTareas.guardar(contexto, nuevas) }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { expandido = !expandido }.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) { Text("Tareas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text("${tareas.size} tareas guardadas · ordenadas por fecha", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Icon(if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
            if (expandido) {
                if (asignaturas.isEmpty()) Text("Importa tu horario para organizar las tareas por asignatura.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                else Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.Top) {
                    asignaturas.forEach { materia ->
                        // La matriz muestra únicamente tareas vigentes. Las vencidas siguen
                        // disponibles desde la gestión abierta en la matriz del horario.
                        val pendientes = tareas.filter {
                            ((materia.codigo.isNotBlank() && it.codigoAsignatura == materia.codigo) || it.nombreAsignatura == materia.nombreAsignatura) &&
                                    fechaValida(it.fecha).let { valida -> !valida || it.fecha >= fechaActualIso() }
                        }.sortedBy { it.fecha }
                        Column(Modifier.width(175.dp).padding(3.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(Modifier.fillMaxWidth().clickable { asignaturaActiva = materia; creando = true; tareaEditando = null }, shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Column(Modifier.padding(9.dp)) {
                                    Text(materia.nombreAsignatura, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Text("+ Agregar tarea", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            if (pendientes.isEmpty()) Text("Sin tareas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(6.dp))
                            pendientes.forEach { tarea ->
                                Surface(Modifier.fillMaxWidth().clickable { asignaturaActiva = materia; tareaPresentando = tarea; tareaEditando = null; creando = false }, shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(tarea.fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(tarea.nombre, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        if (tarea.companero.isNotBlank()) Text("Con ${tarea.companero}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (tarea.pdfUri.isNotBlank()) Text("PDF adjunto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    gestionandoAsignatura?.let { materia ->
        val tareasMateria = tareas.filter {
            (materia.codigo.isNotBlank() && it.codigoAsignatura == materia.codigo) ||
                    it.nombreAsignatura == materia.nombreAsignatura
        }.sortedBy { it.fecha }
        AlertDialog(
            onDismissRequest = { gestionandoAsignatura = null },
            title = { Text("Tareas · ${materia.nombreAsignatura}") },
            text = {
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Aquí aparecen todas las tareas, incluidas las vencidas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        asignaturaActiva = materia
                        creando = true
                        tareaEditando = null
                        gestionandoAsignatura = null
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar tarea")
                    }
                    if (tareasMateria.isEmpty()) {
                        Text("Todavía no hay tareas para esta asignatura.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else tareasMateria.forEach { tarea ->
                        val vencida = fechaValida(tarea.fecha) && tarea.fecha < fechaActualIso()
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                asignaturaActiva = materia
                                tareaPresentando = tarea
                                tareaEditando = null
                                creando = false
                                gestionandoAsignatura = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(tarea.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(tarea.fecha, style = MaterialTheme.typography.labelMedium, color = if (vencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                    if (vencida) Text("Fecha vencida", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    if (tarea.descripcion.isNotBlank()) Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Ver o editar tarea")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { gestionandoAsignatura = null }) { Text("Cerrar") } }
        )
    }

    tareaPresentando?.let { tarea ->
        val vencida = fechaValida(tarea.fecha) && tarea.fecha < fechaActualIso()
        AlertDialog(
            onDismissRequest = { tareaPresentando = null },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("DETALLE DE LA TAREA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(tarea.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 500.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("FECHA DE ENTREGA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            Text(tarea.fecha, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                            if (vencida) Text("Esta fecha ya venció", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Asignatura", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(tarea.nombreAsignatura, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                tarea.descripcion.ifBlank { "No se ha añadido una descripción para esta tarea." },
                                modifier = Modifier.fillMaxWidth().padding(13.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (tarea.companero.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Trabajo en compañía", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(tarea.companero, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (tarea.pdfUri.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Documento adjunto", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text("Hay un PDF asociado a esta tarea.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    tareaPresentando = null
                    tareaEditando = tarea
                    creando = false
                }) {
                    Text("Editar tarea")
                }
            },
            dismissButton = {
                TextButton(onClick = { tareaPresentando = null }) { Text("Cerrar") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (asignaturaActiva != null && (creando || tareaEditando != null)) {
        DialogoTarea(contexto, asignaturaActiva!!, tareaEditando, creando, onDismiss = { creando = false; tareaEditando = null; asignaturaActiva = null }, onGuardar = { tarea ->
            val nuevas = if (tareaEditando == null) tareas + tarea else tareas.map { if (it.id == tarea.id) tarea else it }
            actualizar(nuevas); creando = false; tareaEditando = null; asignaturaActiva = null
        }, onEliminar = { tarea -> actualizar(tareas.filterNot { it.id == tarea.id }); creando = false; tareaEditando = null; asignaturaActiva = null })
    }
}

@Composable
private fun DialogoTarea(contexto: Context, asignatura: AsignaturaHorario, original: Tarea?, nueva: Boolean, onDismiss: () -> Unit, onGuardar: (Tarea) -> Unit, onEliminar: (Tarea) -> Unit) {
    var nombre by remember(original?.id) { mutableStateOf(original?.nombre ?: "") }
    var descripcion by remember(original?.id) { mutableStateOf(original?.descripcion ?: "") }
    var fecha by remember(original?.id) { mutableStateOf(original?.fecha ?: fechaActualIso()) }
    var companero by remember(original?.id) { mutableStateOf(original?.companero ?: "") }
    var pdfUri by remember(original?.id) { mutableStateOf(original?.pdfUri ?: "") }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) { runCatching { contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; pdfUri = uri.toString() }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (nueva) "Agregar tarea" else "Editar tarea") }, text = {
        Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(asignatura.nombreAsignatura, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre de la tarea") }, singleLine = true)
            OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, minLines = 2)
            OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, supportingText = { Text("Usa el formato 2026-10-22") })
            OutlinedTextField(companero, { companero = it }, label = { Text("Compañero (opcional)") }, singleLine = true)
            OutlinedButton(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Description, null); Spacer(Modifier.width(6.dp)); Text(if (pdfUri.isBlank()) "Adjuntar PDF" else "Cambiar PDF adjunto") }
            if (pdfUri.isNotBlank()) Text("PDF adjunto", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        }
    }, confirmButton = { TextButton(enabled = nombre.isNotBlank() && fechaValida(fecha), onClick = {
        onGuardar(Tarea(original?.id ?: UUID.randomUUID().toString(), asignatura.codigo, asignatura.nombreAsignatura, nombre.trim(), descripcion.trim(), fecha.trim(), pdfUri, companero.trim()))
    }) { Text("Guardar") } }, dismissButton = {
        Row { if (original != null) TextButton(onClick = { onEliminar(original) }) { Icon(Icons.Default.Delete, null); Text("Eliminar") }; TextButton(onClick = onDismiss) { Text("Cancelar") } }
    })
}
