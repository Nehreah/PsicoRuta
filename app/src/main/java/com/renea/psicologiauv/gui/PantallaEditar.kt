package com.renea.psicologiauv.gui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.renea.psicologiauv.data.ImportadorNotas
import com.renea.psicologiauv.data.ReglaEspanol
import com.renea.psicologiauv.data.ReglaDeporteFormativo
import com.renea.psicologiauv.data.ImprimirPDF
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.data.RepositorioHorarios
import com.renea.psicologiauv.model.AsignaturaHorario
import com.renea.psicologiauv.model.AsignaturaNoSincronizada
import com.renea.psicologiauv.model.NotaImportada
import com.renea.psicologiauv.model.ModuloConsejoPrincipal
import com.renea.psicologiauv.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// ========================================================================
// SELECTOR DE DOCUMENTOS
// ========================================================================

private class AbrirDocumentoEnCarpeta(
    private val uriInicial: Uri?
) : ActivityResultContracts.OpenDocument() {

    override fun createIntent(
        context: Context,
        input: Array<String>
    ): Intent {

        return super.createIntent(
            context,
            input
        ).apply {

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                uriInicial != null
            ) {

                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    uriInicial
                )
            }
        }
    }
}


// ========================================================================
// ENLACES Y CONFIGURACIÓN
// ========================================================================

// Cambia a false para ocultar la opción «Acerca de» del menú flotante.
private const val MOSTRAR_OPCION_ACERCA_DE = false

private const val ENLACE_RESOLUCION_091 =
    "https://drive.google.com/file/d/1ke6EwQoExMTVH0PAq-CVV8dtOjzwkx_K/view?usp=drive_link"

private const val ENLACE_ACUERDO_009 =
    "https://registro.univalle.edu.co/wp-content/uploads/2024/05/acuerdo_009.pdf"

private const val ENLACE_MALLA_CURRICULAR =
    "https://drive.google.com/file/d/1oagbDquPWx4ggXu8yNtEyGnigYpgMr6r/view?usp=sharing"

private const val ENLACE_SIRA =
    "https://sira1.univalle.edu.co/sra/"

private const val CORREO_REPRESENTANTE_FACULTAD =
    "representante.facultad@correounivalle.edu.co"

private const val CORREO_DIRECCION_PROGRAMA =
    "pregrado.psicologia@correounivalle.edu.co"


// ========================================================================
// UTILIDADES
// ========================================================================

private fun abrirEnlace(
    contexto: Context,
    url: String
) {

    try {

        contexto.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                url.toUri()
            )
        )

    } catch (e: Exception) {

        Log.e(
            "PsicologiaUV",
            "Error abriendo enlace",
            e
        )

        Toast.makeText(
            contexto,
            "No se pudo abrir el enlace.",
            Toast.LENGTH_LONG
        ).show()
    }
}


private fun abrirCorreo(
    contexto: Context,
    correo: String
) {

    try {

        contexto.startActivity(
            Intent(
                Intent.ACTION_SENDTO,
                "mailto:$correo".toUri()
            )
        )

    } catch (e: Exception) {

        Log.e(
            "PsicologiaUV",
            "Error abriendo correo",
            e
        )

        Toast.makeText(
            contexto,
            "No hay ninguna aplicación de correo instalada.",
            Toast.LENGTH_LONG
        ).show()
    }
}


// ========================================================================
// INFORMACIÓN DE LÍNEAS PROFESIONALES
// ========================================================================

private data class LineaInfo(
    val nombre: String,
    val icono: ImageVector,
    val color: Color
)


private val LINEAS_DISPONIBLES = listOf(

    LineaInfo(
        nombre = "Educativa",
        icono = Icons.Filled.School,
        color = Color(0xFFD88523) // Ámbar / Dorado
    ),

    LineaInfo(
        nombre = "Social",
        icono = Icons.Filled.Groups,
        color = Color(0xFF41835A) // Verde salvia
    ),

    LineaInfo(
        nombre = "Organizacional",
        icono = Icons.Filled.CorporateFare,
        color = Color(0xFF7A4FA3) // Lavanda / Violeta
    ),

    LineaInfo(
        nombre = "Clínica",
        icono = Icons.Filled.Favorite,
        color = Color(0xFFD4465B) // Coral / Rosa clínico
    ),

    LineaInfo(
        nombre = "NeuroClínica",
        icono = Icons.Filled.Psychology,
        color = Color(0xFF2F76B9) // Azul acero
    )
)


private fun infoDeLinea(
    nombreLinea: String
): LineaInfo? {

    return LINEAS_DISPONIBLES.find {
        it.nombre == nombreLinea
    }
}


// ========================================================================
// CÍRCULO DE LÍNEA
// ========================================================================

@Composable
private fun CirculoLinea(
    icono: ImageVector,
    color: Color,
    tamano: Dp,
    seleccionado: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    Box(
        modifier =
            Modifier
                .size(tamano)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (seleccionado) {

                        Modifier.border(
                            width = 3.dp,
                            color = White,
                            shape = CircleShape
                        )

                    } else {

                        Modifier
                    }
                )
                .then(
                    if (onClick != null) {

                        Modifier.clickable(
                            onClick = onClick
                        )

                    } else {

                        Modifier
                    }
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector = icono,

            contentDescription = null,

            tint = White,

            modifier =
                Modifier.size(
                    tamano * 0.48f
                )
        )
    }
}


// ========================================================================
// PANTALLA EDITAR
// ========================================================================

@Composable
fun PantallaPerfil(
    modifier: Modifier = Modifier,
    viewModel: ControlV,
    onNavigate: (PantallasApp) -> Unit = {},
    mostrarBienvenidaInicial: Boolean = false,
    onBienvenidaTerminada: () -> Unit = {}
) {

    val estudiante =
        viewModel.programa.estudiante
            ?: return

    val contexto =
        LocalContext.current

    val scope =
        rememberCoroutineScope()


    // ====================================================================
    // ESTADOS
    // ====================================================================

    var mostrarConfirmacionEliminar by remember {
        mutableStateOf(false)
    }

    var mostrandoHistoria by remember {
        mutableStateOf(false)
    }

    var mostrandoHistoriaCali by remember {
        mutableStateOf(false)
    }

    var mostrandoHistoriaLatino by remember {
        mutableStateOf(false)
    }

    var mostrandoSelectorLinea by remember {
        mutableStateOf(false)
    }

    var mostrandoDatosPersonales by remember {
        mutableStateOf(false)
    }

    var mostrandoReporteBugs by remember {
        mutableStateOf(false)
    }

    var mostrandoAcercaDe by rememberSaveable {
        mutableStateOf(false)
    }

    // Módulos emergentes: importación, accesos rápidos y bienvenida inicial.
    var mostrandoCopiaSeguridad by rememberSaveable {
        mutableStateOf(false)
    }

    var mostrandoImportacion by rememberSaveable {
        mutableStateOf(false)
    }
    var asignaturaTareasSolicitada by remember { mutableStateOf<AsignaturaHorario?>(null) }

    // Códigos del catálogo/pensum que aparecieron en el último PDF (con o sin nota).
    var codigosPresentesPdf by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Asignaturas del PDF que no coincidieron con el pensum; se resuelven antes de pedir los semestres.
    var noSincronizadas by remember { mutableStateOf<List<AsignaturaNoSincronizada>>(emptyList()) }

    // Módulo que aparece tras importar notas para ubicar cada electiva profesional en su semestre.
    var mostrandoSemestresElectivas by rememberSaveable {
        mutableStateOf(false)
    }

    var menuAccionesAbierto by rememberSaveable {
        mutableStateOf(false)
    }

    var mostrandoBienvenida by rememberSaveable {
        mutableStateOf(mostrarBienvenidaInicial)
    }


    var nombre by remember(estudiante.nombre) {
        mutableStateOf(estudiante.nombre)
    }

    var codigoTexto by remember(estudiante.codigo) {
        mutableStateOf(
            estudiante.codigo.toString()
        )
    }


    val lineaActual =
        infoDeLinea(estudiante.linea)


    // ====================================================================
    // BACK
    // ====================================================================

    BackHandler(
        enabled =
            mostrandoSelectorLinea ||
                    mostrandoDatosPersonales ||
                    mostrandoHistoriaCali ||
                    mostrandoHistoriaLatino ||
                    mostrandoHistoria ||
                    mostrandoReporteBugs ||
                    mostrandoAcercaDe ||
                    mostrandoCopiaSeguridad
    ) {

        when {

            mostrandoHistoriaCali ->
                mostrandoHistoriaCali = false

            mostrandoHistoriaLatino ->
                mostrandoHistoriaLatino = false

            mostrandoHistoria ->
                mostrandoHistoria = false

            mostrandoSelectorLinea ->
                mostrandoSelectorLinea = false

            mostrandoDatosPersonales ->
                mostrandoDatosPersonales = false

            mostrandoReporteBugs ->
                mostrandoReporteBugs = false

            mostrandoCopiaSeguridad ->
                mostrandoCopiaSeguridad = false

            mostrandoAcercaDe ->
                mostrandoAcercaDe = false
        }
    }


    // ====================================================================
    // SERIALIZADOR / IMPORTADOR
    // ====================================================================

    val serializador =
        remember(contexto) {
            Serializador(contexto)
        }

    val importadorNotas =
        remember(contexto) {
            ImportadorNotas(contexto)
        }


    // ====================================================================
    // GENERAR PDF
    // ====================================================================

    fun generarYAbrirInformePdf() {

        scope.launch {

            try {

                Toast.makeText(
                    contexto,
                    "Generando informe...",
                    Toast.LENGTH_SHORT
                ).show()

                val uri =
                    withContext(Dispatchers.IO) {

                        val imprimirPDF =
                            ImprimirPDF(contexto)

                        imprimirPDF.generarYGuardar(
                            viewModel.programa
                        )
                    }

                if (uri != null) {

                    withContext(Dispatchers.Main) {

                        ImprimirPDF(contexto)
                            .abrirPDF(uri)
                    }

                } else {

                    Toast.makeText(
                        contexto,
                        "No se pudo generar el informe.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Log.e(
                    "PsicologiaUV",
                    "Error generando PDF",
                    e
                )

                Toast.makeText(
                    contexto,
                    "Error al generar el PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // ====================================================================
    // PERMISO DE ALMACENAMIENTO
    // ====================================================================

    val permisoAlmacenamiento =
        rememberLauncherForActivityResult(

            ActivityResultContracts.RequestPermission()

        ) { concedido ->

            if (concedido) {

                generarYAbrirInformePdf()

            } else {

                Toast.makeText(
                    contexto,
                    "No se puede generar el PDF sin permiso de almacenamiento.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    // ====================================================================
    // IMPORTAR PDF
    // ====================================================================

    val selectorPdfNotas =
        rememberLauncherForActivityResult(

            contract =
                remember(contexto) {

                    AbrirDocumentoEnCarpeta(
                        serializador.uriCarpetaDescargas()
                    )
                }

        ) { uri ->

            if (uri != null) {

                scope.launch {

                    val resultado =
                        try {

                            withContext(Dispatchers.IO) {

                                val texto =
                                    importadorNotas
                                        .extraerTexto(uri)

                                // Códigos del pensum + códigos del catálogo de
                                // electivas profesionales (JSON). Sin estos
                                // últimos, el PDF nunca se busca por las
                                // electivas reales y no se pueden asignar a los
                                // espacios de Electiva Profesional II, III y IV.
                                // 204001M (Español en el SIRA) se lee aparte: llena
                                // Cultura Escrita si fue aprobado o eximido (ReglaEspanol).
                                // Los alias de Deporte Formativo (leídos del JSON DeporteFormativo)
                                // también se incluyen para que el analizador los encuentre.
                                val codigosDeporte = serializador.leerDeporteFormativo()
                                val codigosConocidos =
                                    (listOf(ReglaEspanol.CODIGO_ESPANOL_SIRA) +
                                            codigosDeporte +
                                            estudiante
                                                .asignaturas
                                                .map {
                                                    it.codigo
                                                } +
                                            serializador
                                                .leerElectivasProfesionales()
                                                .map {
                                                    it.codigo
                                                })
                                        .distinct()

                                val notasCrudas = importadorNotas.extraerNotasPorCodigo(
                                    texto,
                                    codigosConocidos
                                )
                                val regla = ReglaEspanol.aplicar(notasCrudas)
                                val notas = ReglaDeporteFormativo.aplicar(regla.notas, codigosDeporte)
                                val grupos = importadorNotas.extraerGruposPorCodigo(texto)
                                val sinSincronizar = importadorNotas.extraerNoSincronizadas(
                                    texto,
                                    codigosConocidos
                                )
                                val presentes = importadorNotas.extraerCodigosPresentes(
                                    texto,
                                    codigosConocidos
                                )
                                // Cultura Escrita flotante: solo si no se asignó antes a una complementaria.
                                val flotante = regla.culturaFlotante?.takeIf { f ->
                                    estudiante.asignaturas.none { it.codigo == f.codigo }
                                }
                                ResultadoLecturaPdf(
                                    notas,
                                    grupos,
                                    listOfNotNull(flotante) + sinSincronizar,
                                    presentes
                                )
                            }

                        } catch (e: Exception) {

                            Log.e(
                                "PsicologiaUV",
                                "Error leyendo PDF",
                                e
                            )

                            null
                        }


                    when {

                        resultado == null -> {

                            Toast.makeText(
                                contexto,
                                "No se pudo leer el PDF seleccionado.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        resultado.notas.isEmpty() && resultado.grupos.isEmpty() && resultado.sinSincronizar.isEmpty() && resultado.presentes.isEmpty() -> {

                            Toast.makeText(
                                contexto,
                                "No se encontró ninguna materia del pensum en ese PDF.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {

                            val actualizadas =
                                viewModel.importarNotasDesdePdf(
                                    resultado.notas,
                                    resultado.grupos,
                                    resultado.presentes
                                )

                            Toast.makeText(
                                contexto,
                                "Se importaron $actualizadas nota(s).",
                                Toast.LENGTH_LONG
                            ).show()

                            // Primero se resuelven las asignaturas que no sincronizaron con el pensum.
                            noSincronizadas = resultado.sinSincronizar
                            codigosPresentesPdf = resultado.presentes.toSet()

                            // Si ya hay electivas profesionales cursadas, pide el semestre de cada una.
                            val hayElectivasCursadas = viewModel.programa.estudiante
                                ?.asignaturas.orEmpty()
                                .any { it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) }
                            if (hayElectivasCursadas) {
                                mostrandoSemestresElectivas = true
                            }
                        }
                    }
                }
            }
        }

    val selectorJsonRespaldo =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val nuevoEstudiante = withContext(Dispatchers.IO) {
                        serializador.importarEstudianteDesdeUri(uri)
                    }
                    if (nuevoEstudiante != null) {
                        viewModel.importarEstudianteDesdeJson(nuevoEstudiante)
                        Toast.makeText(
                            contexto,
                            "Perfil restaurado con éxito.",
                            Toast.LENGTH_LONG
                        ).show()
                        mostrandoCopiaSeguridad = false
                    } else {
                        Toast.makeText(
                            contexto,
                            "No se pudo leer el archivo de respaldo JSON.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


    // ====================================================================
    // MÓDULOS EMERGENTES
    // ====================================================================

    if (mostrandoBienvenida) {

        DialogoBienvenida(
            lineaActual = estudiante.linea,
            onLineaSeleccionada = { viewModel.cambiarLineaEstudiante(it) },
            onComenzar = {
                mostrandoBienvenida = false
                mostrandoImportacion = true
                onBienvenidaTerminada()
            },
            onOmitir = {
                mostrandoBienvenida = false
                onBienvenidaTerminada()
            }
        )
    }

    if (mostrandoCopiaSeguridad) {
        ModuloEmergente(
            onCerrar = { mostrandoCopiaSeguridad = false }
        ) {
            TarjetaCopiaSeguridad(
                onCerrar = { mostrandoCopiaSeguridad = false },
                onExportar = {
                    scope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            serializador.exportarEstudianteADescargas(estudiante)
                        }
                        if (uri != null) {
                            Toast.makeText(
                                contexto,
                                "Copia guardada en Descargas: estudiante_${estudiante.codigo}.json",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                contexto,
                                "No se pudo exportar la copia de seguridad.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                onImportar = {
                    selectorJsonRespaldo.launch(
                        arrayOf("application/json", "text/plain", "*/*")
                    )
                }
            )
        }
    }

    if (mostrandoImportacion) {

        ModuloEmergente(
            onCerrar = { mostrandoImportacion = false }
        ) {

            TarjetaSira(

                onCerrar = { mostrandoImportacion = false },

                onAbrirSira = {

                    abrirEnlace(
                        contexto,
                        ENLACE_SIRA
                    )
                },

                onImportar = {

                    mostrandoImportacion = false

                    selectorPdfNotas.launch(
                        arrayOf("application/pdf")
                    )
                },

            )
        }
    }


    if (noSincronizadas.isNotEmpty()) {
        ModuloEmergente(
            onCerrar = { noSincronizadas = emptyList() }
        ) {
            TarjetaNoSincronizadas(
                asignaturas = noSincronizadas,
                complementarias = viewModel.programa.electivasComplementarias(),
                onConfirmar = { asignaciones ->
                    viewModel.asignarNoSincronizadasAComplementarias(asignaciones)
                    noSincronizadas = emptyList()
                }
            )
        }
    }

    if (mostrandoSemestresElectivas && noSincronizadas.isEmpty()) {

        val electivasCursadas = estudiante.asignaturas
            .filter { it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) }

        if (electivasCursadas.isEmpty()) {
            mostrandoSemestresElectivas = false
        } else {
            ModuloEmergente(
                onCerrar = { mostrandoSemestresElectivas = false }
            ) {
                TarjetaSemestresElectivas(
                    electivas = electivasCursadas,
                    onSemestreSeleccionado = { codigo, semestre ->
                        viewModel.cambiarSemestreAsignatura(codigo, semestre)
                    },
                    onListo = { mostrandoSemestresElectivas = false }
                )
            }
        }
    }


    // ====================================================================
    // CONTENEDOR PRINCIPAL
    // ====================================================================

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Transparent)
    ) {

        // ================================================================
        // SUBPANTALLA CALI
        // ================================================================

        if (mostrandoHistoriaCali) {

            PantallaCali(
                onVolver = {
                    mostrandoHistoriaCali = false
                }
            )

            return@Box
        }


        // ================================================================
        // SUBPANTALLA LATINOAMÉRICA
        // ================================================================

        if (mostrandoHistoriaLatino) {

            PantallaLatino(
                onVolver = {
                    mostrandoHistoriaLatino = false
                }
            )

            return@Box
        }


        // ================================================================
        // SUBPANTALLA HISTORIA
        // ================================================================

        if (mostrandoHistoria) {

            PantallaHistoria(

                onVolver = {
                    mostrandoHistoria = false
                },

                onAbrirCali = {
                    mostrandoHistoriaCali = true
                },

                onAbrirLatino = {
                    mostrandoHistoriaLatino = true
                }
            )

            return@Box
        }


        // ================================================================
        // CONTENIDO PRINCIPAL
        // ================================================================

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 8.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {


            // ============================================================
            // HERO
            // ============================================================

            TarjetaPerfilHero(
                linea = lineaActual,
                nombre = nombre.ifBlank { "Estudiante" },
                codigo = codigoTexto,
                progreso = viewModel.programa.avanceCarrera(),
                creditosCursados = viewModel.programa.creditosQueSirvenParaCarrera(),
                creditosCarrera = viewModel.programa.creditosCarrera,
                mostrandoSelector = mostrandoSelectorLinea,
                onLineaClick = { mostrandoSelectorLinea = !mostrandoSelectorLinea },
                onSettingsClick = { mostrandoDatosPersonales = true },
                onNavigate = onNavigate
            )


            // ============================================================
            // SELECTOR DE LÍNEA
            // ============================================================

            AnimatedVisibility(
                visible = mostrandoSelectorLinea,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                TarjetaSelectorLinea(

                    lineaSeleccionada =
                        estudiante.linea,

                    onSeleccionar = { linea ->

                        viewModel
                            .cambiarLineaEstudiante(
                                linea.nombre
                            )

                        mostrandoSelectorLinea =
                            false
                    }
                )
            }


            // Consejo contextual del Pensum, compartido con su hero.
            ModuloConsejoPrincipal(
                consejo = viewModel.programa.consejoPensum(viewModel.programa.semestreEnCurso())
            )


            Spacer(Modifier.height(6.dp))

            // ============================================================
            // ATAJOS A PENSUM, PRÁCTICAS Y TRABAJO DE GRADO
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(PantallasApp.PENSUM) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Pensum",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(PantallasApp.PRACTICAS_INTRO) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Prácticas",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(PantallasApp.TRABAJO_GRADO) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Trabajo de grado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }

            ModuloHorarioEstudiante(
                estudiante = estudiante,
                semestreActual = viewModel.programa.semestreEnCurso(),
                candidatoAGrado = viewModel.programa.requisitoCreditosCarreraCumplido(),
                contexto = contexto,
                onImportarDatos = { mostrandoImportacion = true },
                onGestionarTareas = { asignaturaTareasSolicitada = it }
            )

            Spacer(Modifier.height(8.dp))
            ModuloTareas(
                contexto = contexto,
                horario = estudiante.horario,
                claseInicial = asignaturaTareasSolicitada,
                onClaseInicialConsumida = { asignaturaTareasSolicitada = null }
            )

            Spacer(Modifier.height(10.dp))

            // ============================================================
            // CONSEJERÍA ESTUDIANTIL
            // ============================================================

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            mostrandoReporteBugs = true
                        },
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Consejería e información",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Consejería e información",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Un espacio TUYO para acompañarte.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // ============================================================
            // AIRE FINAL
            // ============================================================

            Spacer(
                Modifier.height(30.dp)
            )
        }


        // =================================================================
        // MENÚ DE ACCIONES (mismo lugar que el antiguo botón de basura)
        // Al tocarlo se despliegan dos círculos: importar datos y eliminar.
        // =================================================================

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 18.dp,
                        bottom = 18.dp
                    ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AnimatedVisibility(
                visible = menuAccionesAbierto,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    AccionFlotante(
                        icono = Icons.Default.Delete,
                        etiqueta = "Borrar datos",
                        descripcion = "Borrar datos",
                        onClick = {
                            menuAccionesAbierto = false
                            mostrarConfirmacionEliminar = true
                        }
                    )

                    AccionFlotante(
                        icono = Icons.Default.Save,
                        etiqueta = "Copia de seguridad",
                        descripcion = "Exportar o importar copia de perfil en JSON",
                        onClick = {
                            menuAccionesAbierto = false
                            mostrandoCopiaSeguridad = true
                        }
                    )

                    AccionFlotante(
                        icono = Icons.Default.FileDownload,
                        etiqueta = "Importar datos",
                        descripcion = "Importar datos",
                        onClick = {
                            menuAccionesAbierto = false
                            mostrandoImportacion = true
                        }
                    )

                    AccionFlotante(
                        icono = Icons.Default.PictureAsPdf,
                        etiqueta = "Generar reporte",
                        descripcion = "Generar reporte PDF",
                        onClick = {
                            menuAccionesAbierto = false
                            val necesitaPermiso =
                                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                                    ContextCompat.checkSelfPermission(
                                        contexto,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) != PackageManager.PERMISSION_GRANTED

                            if (necesitaPermiso) {
                                permisoAlmacenamiento.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                generarYAbrirInformePdf()
                            }
                        }
                    )

                    if (MOSTRAR_OPCION_ACERCA_DE) {
                        AccionFlotante(
                            icono = Icons.Default.CorporateFare,
                            etiqueta = "Acerca de",
                            descripcion = "Información del proyecto y su creador",
                            onClick = {
                                menuAccionesAbierto = false
                                mostrandoAcercaDe = true
                            }
                        )
                    }
                }
            }

            FloatingActionButton(

                onClick = {
                    menuAccionesAbierto = !menuAccionesAbierto
                },

                containerColor =
                    MaterialTheme.colorScheme.primary,

                contentColor =
                    MaterialTheme.colorScheme.onPrimary,

                shape =
                    CircleShape
            ) {

                Icon(

                    imageVector =
                        if (menuAccionesAbierto) Icons.Default.Close
                        else Icons.Default.Add,

                    contentDescription =
                        if (menuAccionesAbierto) "Cerrar menú de acciones"
                        else "Abrir menú de acciones",

                    modifier =
                        Modifier.size(22.dp)
                )
            }
        }

    }


    // ====================================================================
    // DIÁLOGO DATOS PERSONALES
    // ====================================================================

    if (mostrandoDatosPersonales) {

        var lineaSeleccionadaDialog by remember(
            estudiante.linea
        ) {
            mutableStateOf(
                estudiante.linea
            )
        }


        AlertDialog(

            onDismissRequest = {

                mostrandoDatosPersonales =
                    false
            },

            containerColor =
                MaterialTheme.colorScheme.surface,

            icon = {

                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },

            title = {

                Text(
                    "Datos personales",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column(

                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    OutlinedTextField(

                        value = nombre,

                        onValueChange = {
                            nombre = it
                        },

                        label = {
                            Text("Nombre completo")
                        },

                        leadingIcon = {

                            Icon(
                                Icons.Default.Person,
                                contentDescription = null
                            )
                        },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(12.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    OutlinedTextField(

                        value = codigoTexto,

                        onValueChange = {
                            codigoTexto = it
                        },

                        label = {
                            Text("Código estudiantil")
                        },

                        leadingIcon = {

                            Icon(
                                Icons.Default.School,
                                contentDescription = null
                            )
                        },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(14.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    Spacer(
                        Modifier.height(2.dp)
                    )


                    // Tema claro / oscuro
                    val controlTema = com.renea.psicologiauv.ui.theme.LocalControlTema.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tema de la app",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = controlTema.alternar,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (controlTema.oscuro) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (controlTema.oscuro) "Claro" else "Oscuro")
                        }
                    }


                    Text(

                        text =
                            "Línea profesional",

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.Bold
                    )


                    FlowRow(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceEvenly,

                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        LINEAS_DISPONIBLES.forEach { linea ->

                            Column(

                                horizontalAlignment =
                                    Alignment.CenterHorizontally,

                                modifier =
                                    Modifier
                                        .width(54.dp)
                                        .clickable {

                                            lineaSeleccionadaDialog =
                                                linea.nombre
                                        }
                            ) {

                                CirculoLinea(

                                    icono =
                                        linea.icono,

                                    color =
                                        linea.color,

                                    tamano =
                                        40.dp,

                                    seleccionado =
                                        linea.nombre ==
                                                lineaSeleccionadaDialog
                                )


                                Spacer(
                                    Modifier.height(5.dp)
                                )


                                Text(

                                    text =
                                        linea.nombre,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,

                                    textAlign =
                                        TextAlign.Center,

                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {

                FilledTonalButton(

                    onClick = {

                        viewModel
                            .cambiarNombreEstudiante(
                                nombre
                            )

                        codigoTexto
                            .toIntOrNull()
                            ?.let {

                                viewModel
                                    .cambiarCodigoEstudiante(
                                        it
                                    )
                            }

                        viewModel
                            .cambiarLineaEstudiante(
                                lineaSeleccionadaDialog
                            )

                        mostrandoDatosPersonales =
                            false
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        ButtonDefaults
                            .filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                ) {

                    Icon(
                        Icons.Default.Save,
                        contentDescription = null
                    )

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Text(
                        "Guardar",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        mostrandoDatosPersonales =
                            false
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }


    // ====================================================================
    // DIÁLOGO CONSEJERÍA ESTUDIANTIL
    // ====================================================================

    if (mostrandoReporteBugs) {

        AlertDialog(

            onDismissRequest = {
                mostrandoReporteBugs = false
            },

            containerColor = MaterialTheme.colorScheme.surface,

            shape = RoundedCornerShape(26.dp),

            icon = {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },

            title = {
                Text(
                    text = "Consejería e información",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "La Consejería Estudiantil es un espacio de escucha que puedes aprovehar para resolver tus asuntos psicológicos, académicos y socioeconómicos, de forma privada y respetuosa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Puedes escribirles directamente al correo:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            abrirCorreo(contexto, "consejeriaestudiantil.psicologia@correounivalle.edu.co")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "consejeriaestudiantil.psicologia@correounivalle.edu.co",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    // Los accesos rápidos se conservan con su diseño actual,
                    // integrados al final del contenido de Consejería.
                    ModuloAccesosRapidos(contexto = contexto)
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        mostrandoReporteBugs = false
                    }
                ) {
                    Text("Cerrar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }


    if (MOSTRAR_OPCION_ACERCA_DE && mostrandoAcercaDe) {
        ModuloEmergente(onCerrar = { mostrandoAcercaDe = false }) {
            val colores = MaterialTheme.colorScheme
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colores.surface),
                border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CorporateFare,
                            contentDescription = null,
                            tint = colores.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Acerca de",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colores.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { mostrandoAcercaDe = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                    Text(
                        text = "Renea Dev",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colores.primaryContainer.copy(alpha = 0.42f),
                        border = BorderStroke(1.dp, colores.primary.copy(alpha = 0.14f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tecnología con propósito",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colores.onSurface
                            )
                            Text(
                                text = "PsicoRuta nace como una iniciativa académica y profesional orientada a resolver problemas reales mediante el diseño y la construcción de código traducido a herramientas útiles. Integra la psicología, la tecnología y la movilización social para facilitar el aprendizaje, fortalecer la autonomía y contribuir a la construcción de comunidad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colores.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Contacto y más información",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    contexto.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:nehreah@gmail.com".toUri()))
                                } catch (_: Exception) {
                                    Toast.makeText(contexto, "No se pudo abrir una aplicación de correo.", Toast.LENGTH_SHORT).show()
                                }
                            }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = colores.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Correo electrónico", style = MaterialTheme.typography.labelMedium, color = colores.onSurfaceVariant)
                            Text("nehreah@gmail.com", color = colores.primary)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable { abrirEnlace(contexto, "https://nehreah.github.io/WebPage-CV/") }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = colores.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Página web", style = MaterialTheme.typography.labelMedium, color = colores.onSurfaceVariant)
                            Text("https://nehreah.github.io/WebPage-CV/", color = colores.primary)
                        }
                    }
                    TextButton(
                        onClick = { mostrandoAcercaDe = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Cerrar") }
                }
            }
        }
    }

    // ====================================================================
    // DIÁLOGO ELIMINACIÓN
    // ====================================================================

    if (mostrarConfirmacionEliminar) {

        AlertDialog(

            onDismissRequest = {

                mostrarConfirmacionEliminar =
                    false
            },

            containerColor =
                MaterialTheme.colorScheme.surface,

            icon = {

                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },

            title = {

                Text(
                    "Borrar datos",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Text(
                    "Esta acción eliminará permanentemente los datos del estudiante. No se puede deshacer."
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        viewModel.eliminarEstudiante()

                        mostrarConfirmacionEliminar =
                            false

                        Toast.makeText(
                            contexto,
                            "Los datos del estudiante fueron eliminados permanentemente.",
                            Toast.LENGTH_LONG
                        ).show()
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                        ),

                    shape =
                        RoundedCornerShape(14.dp)
                ) {

                    Text(
                        "Eliminar definitivamente",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        mostrarConfirmacionEliminar =
                            false
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}


// ========================================================================
// TARJETA COPIA DE SEGURIDAD (JSON)
// ========================================================================

@Composable
private fun TarjetaCopiaSeguridad(
    onCerrar: () -> Unit,
    onExportar: () -> Unit,
    onImportar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colores.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = colores.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Copia de seguridad (JSON)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    Text(
                        text = "Exporta o restaura todo tu perfil",
                        style = MaterialTheme.typography.labelSmall,
                        color = colores.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar copia de seguridad",
                        tint = colores.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colores.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Cómo funciona",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    PasoSira("1", "Exportar: Guarda un archivo .json con tus notas, línea y horario en la carpeta Descargas.")
                    PasoSira("2", "Importar: Carga ese archivo .json para restaurar tu perfil en este o en otro dispositivo.")
                    PasoSira("3", "Tus datos viajan seguros y quedan respaldados localmente.")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    AccionSiraCompacta(
                        modifier = Modifier.weight(1f),
                        texto = "Exportar perfil",
                        icono = Icons.Default.Save,
                        onClick = onExportar
                    )
                    AccionSiraCompacta(
                        modifier = Modifier.weight(1f),
                        texto = "Importar perfil",
                        icono = Icons.Default.FileDownload,
                        onClick = onImportar
                    )
                }
            }
        }
    }
}

// ========================================================================
// TARJETA SIRA
// ========================================================================

@Composable
private fun TarjetaSira(
    onCerrar: () -> Unit,
    onAbrirSira: () -> Unit,
    onImportar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colores.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = colores.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Importar datos desde SIRA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    Text(
                        text = "Importa tus notas académicas",
                        style = MaterialTheme.typography.labelSmall,
                        color = colores.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar importar y exportar datos",
                        tint = colores.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            run {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colores.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Cómo usarlo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colores.onSurface
                        )
                        PasoSira("1", "Descarga tus notas en PDF desde el SIRA.")
                        PasoSira("2", "Da click en Importar y selecciona el archivo descargado.")
                        PasoSira("3", "¡Disfruta la app!")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        AccionSiraCompacta(
                            modifier = Modifier.weight(1f),
                            texto = "Abrir SIRA",
                            icono = Icons.AutoMirrored.Filled.OpenInNew,
                            onClick = onAbrirSira
                        )
                        AccionSiraCompacta(
                            modifier = Modifier.weight(1f),
                            texto = "Importar datos",
                            icono = Icons.Default.FileDownload,
                            onClick = onImportar
                        )
                    }
                }
            }
        }
    }
}

// ========================================================================
// MÓDULO EMERGENTE (contenedor común)
// ========================================================================

@Composable
private fun ModuloEmergente(
    onCerrar: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
        ) {
            contenido()
        }
    }
}


// ========================================================================
// ASIGNATURAS QUE NO SINCRONIZARON CON EL PENSUM (tras importar notas)
// ========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaNoSincronizadas(
    asignaturas: List<AsignaturaNoSincronizada>,
    complementarias: List<com.renea.psicologiauv.model.Asignatura>,
    onConfirmar: (List<Pair<AsignaturaNoSincronizada, Int>>) -> Unit
) {
    val colores = MaterialTheme.colorScheme

    // codigo -> posición de la complementaria elegida (0 = I, 1 = II); sin entrada = descartar.
    val elecciones = remember { androidx.compose.runtime.mutableStateMapOf<String, Int>() }

    // Nombre editable: por si el PDF no permitió leerlo bien, el usuario lo corrige.
    val nombres = remember {
        androidx.compose.runtime.mutableStateMapOf<String, String>().apply {
            asignaturas.forEach { put(it.codigo, it.nombre) }
        }
    }

    fun elegir(codigo: String, posicion: Int?) {
        if (posicion == null) {
            elecciones.remove(codigo)
        } else {
            // Un espacio admite una sola asignatura: quien lo tenía vuelve a "Descartar".
            elecciones.keys.toList()
                .filter { elecciones[it] == posicion }
                .forEach { elecciones.remove(it) }
            elecciones[codigo] = posicion
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Asignaturas sin sincronizar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface
            )
            Text(
                text = "Estas asignaturas no sincronizaron con el pensum, ¿deseas descartarlas o asignarlas a una electiva complementaria?",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant
            )

            asignaturas.forEach { item ->
                val eleccion = elecciones[item.codigo]
                val notaTexto = when (val n = item.nota) {
                    is NotaImportada.Numerica -> "%.1f".format(n.valor)
                    is NotaImportada.Especial -> n.marca
                    null -> "En curso"
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colores.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = nombres[item.codigo] ?: item.nombre,
                        onValueChange = { nombres[item.codigo] = it },
                        label = { Text("Nombre de la asignatura") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (item.nota == null) "${item.codigo} · En curso" else "${item.codigo} · Nota: $notaTexto",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val opciones = listOf<Pair<String, Int?>>(
                            "Descartar" to null,
                            "Complementaria I" to 0,
                            "Complementaria II" to 1
                        )
                        opciones.forEach { (etiqueta, posicion) ->
                            val seleccionada = eleccion == posicion
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (seleccionada) colores.primary else colores.surface)
                                    .border(
                                        1.dp,
                                        if (seleccionada) colores.primary else colores.outlineVariant,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { elegir(item.codigo, posicion) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = etiqueta,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                                    color = if (seleccionada) colores.onPrimary else colores.onSurface
                                )
                            }
                        }
                    }
                    // Aviso si el espacio elegido ya tenía una asignatura con nota.
                    val ocupante = eleccion?.let { complementarias.getOrNull(it) }
                    if (ocupante != null && (ocupante.nota > 0f || ocupante.notaEspecial != null)) {
                        Text(
                            text = "Reemplazará a «${ocupante.nombre}».",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.error
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onConfirmar(
                        asignaturas.mapNotNull { item ->
                            elecciones[item.codigo]?.let { posicion ->
                                val nombre = nombres[item.codigo]?.trim().orEmpty()
                                item.copy(nombre = nombre.ifBlank { item.nombre }) to posicion
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ========================================================================
// SEMESTRE DE CADA ELECTIVA PROFESIONAL (tras importar notas)
// ========================================================================

/** Resultado de leer el PDF de notas. */
private data class ResultadoLecturaPdf(
    val notas: Map<String, NotaImportada>,
    val grupos: Map<String, String>,
    val sinSincronizar: List<AsignaturaNoSincronizada>,
    val presentes: List<String>
)

/**
 * Electiva profesional que se debe ubicar en un semestre: ya tiene nota
 * (numérica o marca especial), o se está cursando (aparece en el PDF sin nota),
 * o es un espacio ya resuelto con su código real aunque aún no tenga nota.
 * Los espacios vacíos ("3", "4", "5") y la Electiva Profesional I sin
 * matricular no cuentan.
 */
private fun com.renea.psicologiauv.model.Asignatura.esElectivaProfesionalParaSemestre(
    codigosPresentesPdf: Set<String>
): Boolean {
    if (!componente.equals("Electiva profesional", ignoreCase = true)) return false
    val codigoLimpio = codigo.trim()
    val conNota = nota > 0f || notaEspecial != null
    val espacioResuelto = codigoLimpio !in listOf("3", "4", "5", "402197M")
    return conNota || espacioResuelto || codigoLimpio in codigosPresentesPdf
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaSemestresElectivas(
    electivas: List<com.renea.psicologiauv.model.Asignatura>,
    onSemestreSeleccionado: (codigo: String, semestre: Int) -> Unit,
    onListo: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Electivas profesionales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface
            )
            Text(
                text = "Selecciona el semestre en el que cursaste cada electiva.",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant
            )

            electivas.forEach { electiva ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colores.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = electiva.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface
                    )
                    if (electiva.nota <= 0f && electiva.notaEspecial == null) {
                        Text(
                            text = "En curso",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..11).forEach { semestre ->
                            val seleccionado = electiva.semestre == semestre
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (seleccionado) colores.primary
                                        else colores.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (seleccionado) colores.primary
                                        else colores.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable {
                                        onSemestreSeleccionado(electiva.codigo, semestre)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = semestre.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (seleccionado) colores.onPrimary else colores.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onListo,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Listo", fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ========================================================================
// DIÁLOGO DE BIENVENIDA (primera apertura)
// ========================================================================

@Composable
private fun DialogoBienvenida(
    lineaActual: String,
    onLineaSeleccionada: (String) -> Unit,
    onComenzar: () -> Unit,
    onOmitir: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    var lineaElegida by rememberSaveable(lineaActual) {
        mutableStateOf(lineaActual.takeIf { it in listOf("Clínica", "NeuroClínica", "Social", "Organizacional", "Educativa") } ?: "Social")
    }

    Dialog(
        onDismissRequest = onOmitir,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colores.surface)
                .border(
                    1.dp,
                    colores.outlineVariant.copy(alpha = 0.45f),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(colores.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = colores.onPrimaryContainer,
                    modifier = Modifier.size(39.dp)
                )
            }

            Text(
                text = "¡Bienvenido a PsicoRuta UV!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Aquí podrás seguir tu avance en el pensum, tus prácticas y tu trabajo de grado.",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colores.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Para empezar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colores.onSurface
                )
                PasoSira("1", "Descarga tus notas en PDF desde el SIRA.")
                PasoSira("2", "Impórtalas en la app con un solo toque.")
                PasoSira("3", "¡Disfruta la app!")
            }

            Text(
                text = "Elige tu línea profesional",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Puedes cambiarla más adelante en la configuración.",
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            val opcionesLinea = listOf("Educativa", "Social", "Organizacional", "Clínica", "NeuroClínica")
            opcionesLinea.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (lineaElegida == opcion) colores.primaryContainer else colores.surfaceVariant.copy(alpha = 0.35f))
                        .clickable {
                            lineaElegida = opcion
                            onLineaSeleccionada(opcion)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = lineaElegida == opcion,
                        onClick = {
                            lineaElegida = opcion
                            onLineaSeleccionada(opcion)
                        }
                    )
                    Text(
                        text = opcion,
                        color = if (lineaElegida == opcion) colores.onPrimaryContainer else colores.onSurface,
                        fontWeight = if (lineaElegida == opcion) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Button(
                onClick = onComenzar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colores.primary,
                    contentColor = colores.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Importar mis datos", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onOmitir) {
                Text("Más tarde")
            }
        }
    }
}


// ========================================================================
// ACCIÓN FLOTANTE (círculo del menú de acciones)
// ========================================================================

@Composable
private fun AccionFlotante(
    icono: ImageVector,
    etiqueta: String,
    descripcion: String,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(46.dp),
            containerColor = colores.secondaryContainer,
            contentColor = colores.onSecondaryContainer,
            shape = CircleShape
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                modifier = Modifier.size(20.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colores.surface,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.6f))
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colores.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}


// ========================================================================
// ACCESOS RÁPIDOS — MÓDULO DESPLEGABLE
// ========================================================================

@Composable
private fun ModuloAccesosRapidos(
    contexto: Context
) {
    var accesosRapidosExpandidos by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        accesosRapidosExpandidos = !accesosRapidosExpandidos
                    }
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(9.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accesos rápidos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Documentos y contactos importantes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (accesosRapidosExpandidos) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (accesosRapidosExpandidos) {
                        "Contraer accesos rápidos"
                    } else {
                        "Expandir accesos rápidos"
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp)
                )
            }

            AnimatedVisibility(
                visible = accesosRapidosExpandidos,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AccesoCuadrado(
                            icono = Icons.Default.Description,
                            titulo = "Res. 091",
                            modifier = Modifier.weight(1f),
                            onClick = { abrirEnlace(contexto, ENLACE_RESOLUCION_091) }
                        )

                        AccesoCuadrado(
                            icono = Icons.Default.Description,
                            titulo = "Acuerdo 009",
                            modifier = Modifier.weight(1f),
                            onClick = { abrirEnlace(contexto, ENLACE_ACUERDO_009) }
                        )

                        AccesoCuadrado(
                            icono = Icons.Default.GridView,
                            titulo = "Malla",
                            modifier = Modifier.weight(1f),
                            onClick = { abrirEnlace(contexto, ENLACE_MALLA_CURRICULAR) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AccesoCuadrado(
                            icono = Icons.Default.Email,
                            titulo = "Rep. Facultad",
                            modifier = Modifier.weight(1f),
                            onClick = { abrirCorreo(contexto, CORREO_REPRESENTANTE_FACULTAD) }
                        )

                        AccesoCuadrado(
                            icono = Icons.Default.Email,
                            titulo = "Dir. Programa",
                            modifier = Modifier.weight(1f),
                            onClick = { abrirCorreo(contexto, CORREO_DIRECCION_PROGRAMA) }
                        )

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
private fun AccionSiraCompacta(
    modifier: Modifier = Modifier,
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.height(60.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = colores.surfaceVariant,
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colores.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = colores.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========================================================================
// PASO SIRA
// ========================================================================

@Composable
private fun PasoSira(
    numero: String,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = RoundedCornerShape(7.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

// ========================================================================
// HERO DE PERFIL
// ========================================================================

@Composable
private fun TarjetaPerfilHero(
    linea: LineaInfo?,
    nombre: String,
    codigo: String,
    progreso: Float,
    creditosCursados: Int,
    creditosCarrera: Int,
    mostrandoSelector: Boolean,
    onLineaClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigate: (PantallasApp) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val temaLinea = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(linea?.nombre)
    val colorAcento = temaLinea.colorPrimario
    val progresoAnimado by animateFloatAsState(
        targetValue = (progreso / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 850),
        label = "avancePerfilHero"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surface)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // El gráfico queda a la izquierda y funciona como acceso a la pantalla de avance.
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .clickable { onNavigate(PantallasApp.AVANCE) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    drawArc(
                        color = scheme.outlineVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = colorAcento,
                        startAngle = -90f,
                        sweepAngle = 360f * progresoAnimado,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progreso.toInt().coerceIn(0, 100)}%",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Click acá",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // El bloque de datos queda centrado en el espacio disponible del hero.
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (codigo.isNotBlank()) "Código $codigo" else "Código no definido",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .heightIn(min = 32.dp)
                            .clickable(onClick = onLineaClick),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.38f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colorAcento,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = temaLinea.nombreVisible,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Cambiar línea",
                                tint = colorAcento,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = "$creditosCursados de $creditosCarrera créditos",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    if (mostrandoSelector) {
                        Text(
                            text = "Selecciona una nueva línea",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clickable(onClick = onSettingsClick),
                    shape = CircleShape,
                    color = scheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Editar datos personales",
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ========================================================================
// SELECTOR DE LÍNEA
// ========================================================================

@Composable
private fun TarjetaSelectorLinea(
    lineaSeleccionada: String,
    onSeleccionar: (LineaInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "Línea profesional",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige el área que representa tu perfil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LINEAS_DISPONIBLES.forEach { linea ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(58.dp).clickable { onSeleccionar(linea) }
                    ) {
                        CirculoLinea(
                            icono = linea.icono,
                            color = linea.color,
                            tamano = 40.dp,
                            seleccionado = linea.nombre == lineaSeleccionada
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = linea.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            fontWeight = if (linea.nombre == lineaSeleccionada) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

// ========================================================================
// TÍTULO DE SECCIÓN
// ========================================================================



// ========================================================================
// ACCESO CUADRADO
// ========================================================================

@Composable
private fun AccesoCuadrado(
    icono: ImageVector,
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(52.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(25.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = titulo, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}


// ========================================================================
// HORARIO DEL ESTUDIANTE — MATRIZ SEMANAL
// ========================================================================

@Composable
private fun ModuloHorarioEstudiante(
    estudiante: com.renea.psicologiauv.model.Estudiante,
    semestreActual: Int,
    candidatoAGrado: Boolean,
    contexto: Context,
    onImportarDatos: () -> Unit,
    onGestionarTareas: (AsignaturaHorario) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var claseSeleccionada by remember { mutableStateOf<AsignaturaHorario?>(null) }
    val catalogo = remember(contexto) { RepositorioHorarios.cargarCatalogo(contexto) }
    val horario = remember(estudiante.asignaturas, semestreActual, catalogo) {
        RepositorioHorarios.horarioDelSemestre(estudiante, semestreActual, catalogo)
    }
    // El horario forma parte del modelo del estudiante y se mantiene sincronizado
    // con las asignaturas pendientes del semestre que el programa identifica como actual.
    estudiante.horario = horario.toMutableList()

    // Con el 100 % de los créditos ya no hay horario que importar.
    if (candidatoAGrado) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Eres candidato a grado, felicitaciones",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        return
    }

    val hayGrupoMatriculado = estudiante.asignaturas.any {
        it.semestre == semestreActual && !it.aprobo() && it.grupo.isNotBlank()
    }

    if (!hayGrupoMatriculado) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Ingresa tu tabulado con tus asignaturas de este semestre",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(onClick = onImportarDatos) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Importar mis datos")
                    }
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { expandido = !expandido }
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.GridView, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("Horario de clases", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Semestre $semestreActual · ${horario.size} sesiones encontradas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Contraer horario" else "Expandir horario",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp)
                )
            }
            AnimatedVisibility(visible = expandido, enter = fadeIn(), exit = fadeOut()) {
                if (horario.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No se encontraron horarios para tus asignaturas del semestre actual. Importa tus datos para cargar tu tabulado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onImportarDatos) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Importar mis datos")
                        }
                    }
                } else {
                    MatrizHorarioSemanal(horario = horario, onClaseClick = { claseSeleccionada = it })
                }
            }
        }
    }

    claseSeleccionada?.let { clase ->
        AlertDialog(
            onDismissRequest = { claseSeleccionada = null },
            title = { Text(clase.nombreAsignatura) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatoHorario("Código", clase.codigo)
                    DatoHorario("Grupo", clase.grupo.ifBlank { "No especificado" })
                    DatoHorario("Día de clase", nombreDiaHorario(clase.diaClase))
                    DatoHorario("Horas de clase", clase.horasClase)
                    DatoHorario("Hora de inicio", horaLegible(clase.horaInicio))
                    DatoHorario("Espacio", clase.espacio)
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { onGestionarTareas(clase); claseSeleccionada = null }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar o eliminar tareas")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { claseSeleccionada = null }) { Text("Cerrar") } }
        )
    }

}

@Composable
private fun MatrizHorarioSemanal(
    horario: List<AsignaturaHorario>,
    onClaseClick: (AsignaturaHorario) -> Unit
) {
    val dias = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM")
    val diaHoy = when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "LUN"
        java.util.Calendar.TUESDAY -> "MAR"
        java.util.Calendar.WEDNESDAY -> "MIE"
        java.util.Calendar.THURSDAY -> "JUE"
        java.util.Calendar.FRIDAY -> "VIE"
        java.util.Calendar.SATURDAY -> "SAB"
        java.util.Calendar.SUNDAY -> "DOM"
        else -> "LUN"
    }
    val scroll = rememberScrollState()
    val densidad = androidx.compose.ui.platform.LocalDensity.current

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        androidx.compose.runtime.LaunchedEffect(diaHoy, maxWidth) {
            val indice = dias.indexOf(diaHoy)
            val anchoColumnaPx = with(densidad) { 112.dp.toPx() }
            val centroColumna = indice * anchoColumnaPx + anchoColumnaPx / 2f
            val centroVisible = with(densidad) { maxWidth.toPx() / 2f }
            scroll.scrollTo((centroColumna - centroVisible).toInt().coerceAtLeast(0))
        }
        Column(Modifier.fillMaxWidth().horizontalScroll(scroll)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                dias.forEach { dia ->
                    val esHoy = dia == diaHoy
                    Box(
                        Modifier.width(112.dp).padding(3.dp)
                            .background(
                                if (esHoy) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(9.dp)
                            ).padding(vertical = 9.dp, horizontal = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nombreDiaCortoHorario(dia) + if (esHoy) " · Hoy" else "",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (esHoy) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.Top) {
                dias.forEach { dia ->
                    val clases = horario.filter { it.diaClase == dia }
                        .sortedWith(compareBy<AsignaturaHorario> { it.horaInicio }.thenBy { it.horaFin }.thenBy { it.nombreAsignatura })
                    Column(
                        modifier = Modifier.width(112.dp).padding(horizontal = 2.dp, vertical = 3.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (clases.isEmpty()) {
                            Box(
                                Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin actividades", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    textAlign = TextAlign.Center, modifier = Modifier.padding(5.dp))
                            }
                        } else {
                            clases.forEach { clase ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { onClaseClick(clase) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (dia == diaHoy) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                                ) {
                                    Column(Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            clase.nombreAsignatura,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (dia == diaHoy) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 4,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${horaLegible(clase.horaInicio)}–${horaLegible(clase.horaFin)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Espacio: ${clase.espacio.ifBlank { "No especificado" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoHorario(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun horaLegible(hora: Int): String = "%02d:00".format(hora)
private fun nombreDiaHorario(dia: String): String = when (dia) {
    "LUN" -> "Lunes"; "MAR" -> "Martes"; "MIE" -> "Miércoles"; "JUE" -> "Jueves"
    "VIE" -> "Viernes"; "SAB" -> "Sábado"; "DOM" -> "Domingo"; else -> dia
}
private fun nombreDiaCortoHorario(dia: String): String = when (dia) {
    "LUN" -> "Lunes"; "MAR" -> "Martes"; "MIE" -> "Miérc."; "JUE" -> "Jueves"
    "VIE" -> "Viernes"; "SAB" -> "Sábado"; "DOM" -> "Domingo"; else -> dia
}
