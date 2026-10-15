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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.renea.psicologiauv.R
import com.renea.psicologiauv.data.ImportadorNotas
import com.renea.psicologiauv.data.ImprimirPDF
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.ui.theme.AmberAccent
import com.renea.psicologiauv.ui.theme.Burgundy
import com.renea.psicologiauv.ui.theme.DarkRed
import com.renea.psicologiauv.ui.theme.Red
import com.renea.psicologiauv.ui.theme.SuccessDark
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
// ENLACES
// ========================================================================

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
fun PantallaEditar(
    modifier: Modifier = Modifier,
    viewModel: ControlV
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

    var mostrandoMemoriaEstudiantil by remember {
        mutableStateOf(false)
    }

    var mostrandoReporteBugs by remember {
        mutableStateOf(false)
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
                    mostrandoMemoriaEstudiantil ||
                    mostrandoReporteBugs
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

            mostrandoMemoriaEstudiantil ->
                mostrandoMemoriaEstudiantil = false

            mostrandoReporteBugs ->
                mostrandoReporteBugs = false
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

                                val codigosConocidos =
                                    estudiante
                                        .asignaturas
                                        .map {
                                            it.codigo
                                        }

                                importadorNotas
                                    .extraerNotasPorCodigo(
                                        texto,
                                        codigosConocidos
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

                        resultado.isEmpty() -> {

                            Toast.makeText(
                                contexto,
                                "No se encontró ninguna materia del pensum en ese PDF.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {

                            val actualizadas =
                                viewModel
                                    .importarNotasDesdePdf(
                                        resultado
                                    )

                            Toast.makeText(
                                contexto,
                                "Se importaron $actualizadas nota(s).",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
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

                nombre =
                    nombre.ifBlank {
                        "Estudiante"
                    },

                codigo = codigoTexto,

                mostrandoSelector =
                    mostrandoSelectorLinea,

                onLineaClick = {

                    mostrandoSelectorLinea =
                        !mostrandoSelectorLinea
                },

                onSettingsClick = {

                    mostrandoDatosPersonales =
                        true
                },

                onMemoriaClick = {

                    mostrandoMemoriaEstudiantil =
                        !mostrandoMemoriaEstudiantil
                }
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


            // ============================================================
            // SIRA
            // ============================================================

            TarjetaSira(

                onAbrirSira = {

                    abrirEnlace(
                        contexto,
                        ENLACE_SIRA
                    )
                },

                onImportar = {

                    selectorPdfNotas.launch(
                        arrayOf("application/pdf")
                    )
                },

                onGenerarInforme = {

                    val necesitaPermiso =
                        Build.VERSION.SDK_INT <=
                                Build.VERSION_CODES.P &&
                                ContextCompat.checkSelfPermission(
                                    contexto,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED

                    if (necesitaPermiso) {

                        permisoAlmacenamiento.launch(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )

                    } else {

                        generarYAbrirInformePdf()
                    }
                }
            )


            // ============================================================
            // ACCESOS RÁPIDOS — MÓDULO DESPLEGABLE
            // ============================================================

            var accesosRapidosExpandidos by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color(0xFFFFF5F2)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(
                    1.dp,
                    Red.copy(alpha = 0.38f)
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
                            color = Red.copy(alpha = 0.14f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = Red,
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
                            tint = Red,
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
                                .background(
                                    if (isSystemInDarkTheme()) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        Color(0xFFFFFFFF)
                                    }
                                )
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
                                    onClick = {
                                        abrirEnlace(contexto, ENLACE_RESOLUCION_091)
                                    }
                                )

                                AccesoCuadrado(
                                    icono = Icons.Default.Description,
                                    titulo = "Acuerdo 009",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        abrirEnlace(contexto, ENLACE_ACUERDO_009)
                                    }
                                )

                                AccesoCuadrado(
                                    icono = Icons.Default.GridView,
                                    titulo = "Malla",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        abrirEnlace(contexto, ENLACE_MALLA_CURRICULAR)
                                    }
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
                                    onClick = {
                                        abrirCorreo(contexto, CORREO_REPRESENTANTE_FACULTAD)
                                    }
                                )

                                AccesoCuadrado(
                                    icono = Icons.Default.Email,
                                    titulo = "Dir. Programa",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        abrirCorreo(contexto, CORREO_DIRECCION_PROGRAMA)
                                    }
                                )

                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ============================================================
            // REPORTAR BUGS
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
                border = BorderStroke(1.dp, Red.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = Red.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Reportar bugs",
                                tint = Red,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Reportar bugs",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "¿Encontraste un fallo? Ayúdanos a mejorarlo.",
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
        // BOTÓN ELIMINAR
        // =================================================================

        FloatingActionButton(

            onClick = {

                mostrarConfirmacionEliminar =
                    true
            },

            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 18.dp,
                        bottom = 18.dp
                    ),

            containerColor =
                DarkRed.copy(
                    alpha = 0.90f
                ),

            contentColor =
                White,

            shape =
                CircleShape
        ) {

            Icon(

                imageVector =
                    Icons.Default.Delete,

                contentDescription =
                    "Eliminar estudiante",

                modifier =
                    Modifier.size(18.dp)
            )
        }


        // =================================================================
        // MEMORIA ESTUDIANTIL
        // =================================================================

        MemoriaEstudiantilFlotante(

            abierta =
                mostrandoMemoriaEstudiantil,

            onAbrir = {

                mostrandoMemoriaEstudiantil =
                    !mostrandoMemoriaEstudiantil
            },

            onAbrirHistoria = {

                mostrandoMemoriaEstudiantil =
                    false

                mostrandoHistoria =
                    true
            }
        )
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
                    tint = Red
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
                                containerColor =
                                    Red,

                                contentColor =
                                    White
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
    // DIÁLOGO REPORTAR BUGS
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
                    color = Red.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },

            title = {
                Text(
                    text = "Reportar bugs y fallos",
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
                        text = "Antes que nada, te pido disculpas a ti queridx usuarix si por alguna razón encuentras algún error, fallo o incoveniente. Este proyecto de software libre ha sido desarrollado por una sola persona(egresada), que con mucho amor, esfuerzo y cariño la ha desarrollado para mejorar la experiencia académica de lxs estudiantes de la Facultad de Psicología sin costo alguno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "¿Qué puedes reportar?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Errores, mecanicas que faltan, pantallas que no cargan, botones que no responden, datos incorrectos o cualquier comportamiento extraño. Si puedes, describe qué hiciste y qué esperabas que ocurriera.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Puedes escribir directamente a cualquiera de estos correos, con el asunto <Bug en PsicoRuta>",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            abrirCorreo(contexto, "contreras.javier@correounivalle.edu.co")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red.copy(alpha = 0.10f),
                            contentColor = Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text("contreras.javier@correounivalle.edu.co", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            abrirCorreo(contexto, "reneadev@gmail.com")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text("reneadev@gmail.com", fontWeight = FontWeight.SemiBold)
                    }
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
                    tint = Red
                )
            },

            title = {

                Text(
                    "Eliminar estudiante",
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
                            "El estudiante fue eliminado permanentemente.",
                            Toast.LENGTH_LONG
                        ).show()
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Red,

                            contentColor =
                                White
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
// MEMORIA ESTUDIANTIL — PANEL FLOTANTE
// ========================================================================

@Composable
private fun MemoriaEstudiantilFlotante(
    abierta: Boolean,
    onAbrir: () -> Unit,
    onAbrirHistoria: () -> Unit
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 18.dp,
                    end = 0.dp
                ),

        contentAlignment =
            Alignment.TopEnd
    ) {

        // ================================================================
        // PESTAÑA LATERAL CERRADA
        // ================================================================

        AnimatedVisibility(

            visible =
                !abierta,

            enter =
                fadeIn() +
                        slideInHorizontally(
                            initialOffsetX = {
                                it / 2
                            }
                        ),

            exit =
                fadeOut() +
                        slideOutHorizontally(
                            targetOffsetX = {
                                it / 2
                            }
                        )
        ) {

            Surface(

                modifier =
                    Modifier
                        .width(132.dp)
                        .height(44.dp)
                        .clickable(
                            onClick = onAbrir
                        ),

                shape =
                    RoundedCornerShape(
                        topStart = 20.dp,
                        bottomStart = 20.dp
                    ),

                color =
                    Burgundy.copy(
                        alpha = 0.84f
                    ),

                shadowElevation =
                    7.dp,

                border =
                    BorderStroke(
                        1.dp,
                        White.copy(
                            alpha = 0.12f
                        )
                    )
            ) {

                Row(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 9.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    // ----------------------------------------------------
                    // ICONO
                    // ----------------------------------------------------

                    Surface(

                        modifier =
                            Modifier.size(31.dp),

                        shape =
                            CircleShape,

                        color =
                            Red.copy(
                                alpha = 0.20f
                            )
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.History,

                                contentDescription =
                                    "Memoria estudiantil",

                                tint =
                                    White,

                                modifier =
                                    Modifier.size(17.dp)
                            )
                        }
                    }


                    Spacer(
                        Modifier.width(6.dp)
                    )


                    // ----------------------------------------------------
                    // TÍTULO
                    // ----------------------------------------------------

                    Text(

                        text =
                            "Memoria estudiantil",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            White,

                        maxLines = 1,

                        softWrap = false
                    )
                }
            }
        }


        // ================================================================
        // PANEL ABIERTO
        // ================================================================

        AnimatedVisibility(

            visible =
                abierta,

            enter =
                fadeIn() +
                        slideInHorizontally(
                            initialOffsetX = {
                                it
                            }
                        ),

            exit =
                fadeOut() +
                        slideOutHorizontally(
                            targetOffsetX = {
                                it
                            }
                        )
        ) {

            Card(

                modifier =
                    Modifier
                        .width(294.dp)
                        .padding(
                            end = 10.dp
                        ),

                shape =
                    RoundedCornerShape(
                        topStart = 24.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                        topEnd = 10.dp
                    ),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Burgundy.copy(
                                alpha = 0.96f
                            )
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            9.dp
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        White.copy(
                            alpha = 0.10f
                        )
                    )
            ) {

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 10.dp,
                                vertical = 13.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(0.dp)
                ) {

                    // ====================================================
                    // CABECERA
                    //
                    // IMPORTANTE:
                    // "Memoria estudiantil" NO es un botón.
                    // Solo el círculo de cerrar es interactivo.
                    // ====================================================

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(

                            modifier =
                                Modifier.size(29.dp),

                            shape =
                                RoundedCornerShape(
                                    13.dp
                                ),

                            color =
                                Red.copy(
                                    alpha = 0.18f
                                )
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.History,

                                    contentDescription =
                                        null,

                                    tint =
                                        White,

                                    modifier =
                                        Modifier.size(19.dp)
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
                                    "Memoria estudiantil",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    White,

                                maxLines = 1,

                                softWrap = false
                            )


                            Spacer(
                                Modifier.height(3.dp)
                            )


                            Text(

                                text =
                                    "Tu historia dentro del movimiento",

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,

                                color =
                                    White.copy(
                                        alpha = 0.62f
                                    ),

                                maxLines = 1,

                                softWrap = false
                            )
                        }


                        Spacer(
                            Modifier.width(6.dp)
                        )


                        // ------------------------------------------------
                        // SOLO ESTE ELEMENTO ES BOTÓN
                        // ------------------------------------------------

                        Surface(

                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clickable(
                                        onClick = onAbrir
                                    ),

                            shape =
                                CircleShape,

                            color =
                                White.copy(
                                    alpha = 0.09f
                                ),

                            border =
                                BorderStroke(
                                    1.dp,
                                    White.copy(
                                        alpha = 0.08f
                                    )
                                )
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Close,

                                    contentDescription =
                                        "Cerrar memoria estudiantil",

                                    tint =
                                        White.copy(
                                            alpha = 0.85f
                                        ),

                                    modifier =
                                        Modifier.size(18.dp)
                                )
                            }
                        }
                    }


                    Spacer(
                        Modifier.height(10.dp)
                    )


                    // ====================================================
                    // DIVISOR
                    // ====================================================

                    Box(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    White.copy(
                                        alpha = 0.09f
                                    )
                                )
                    )


                    Spacer(
                        Modifier.height(10.dp)
                    )


                    // ====================================================
                    // HISTORIA
                    // ====================================================

                    Surface(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clickable(
                                    onClick =
                                        onAbrirHistoria
                                ),

                        shape =
                            RoundedCornerShape(17.dp),

                        color =
                            White.copy(
                                alpha = 0.075f
                            ),

                        border =
                            BorderStroke(
                                1.dp,
                                White.copy(
                                    alpha = 0.09f
                                )
                            )
                    ) {

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 13.dp
                                    ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Surface(

                                modifier =
                                    Modifier.size(34.dp),

                                shape =
                                    CircleShape,

                                color =
                                    Red.copy(
                                        alpha = 0.18f
                                    )
                            ) {

                                Box(
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(

                                        imageVector =
                                            Icons.Default.History,

                                        contentDescription =
                                            null,

                                        tint =
                                            White,

                                        modifier =
                                            Modifier.size(18.dp)
                                    )
                                }
                            }


                            Spacer(
                                Modifier.width(8.dp)
                            )


                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(

                                    text =
                                        "Historia estudiantil",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelLarge,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        White
                                )


                                Spacer(
                                    Modifier.height(3.dp)
                                )


                                Text(

                                    text =
                                        "Cali y Latinoamérica",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,

                                    color =
                                        White.copy(
                                            alpha = 0.58f
                                        )
                                )
                            }


                            Spacer(
                                Modifier.width(8.dp)
                            )


                            Icon(

                                imageVector =
                                    Icons.AutoMirrored
                                        .Filled
                                        .ArrowForward,

                                contentDescription =
                                    "Abrir historia estudiantil",

                                tint =
                                    White.copy(
                                        alpha = 0.72f
                                    ),

                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }
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
    onAbrirSira: () -> Unit,
    onImportar: () -> Unit,
    onGenerarInforme: () -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Cabecera sencilla y desplegable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = colores.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Importar desde SIRA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Sincroniza tu historial académico",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = "SIRA",
                    style = MaterialTheme.typography.labelMedium,
                    color = colores.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(5.dp))
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Colapsar" else "Expandir",
                    tint = colores.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text(
                        text = "1. Ingresa al portal SIRA 2. Accede a tus notas 3. Descargalas como PDF 4. Importa tus nostas desde pdf",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )

                    // Pasos del proceso, con colores neutros

                    // Acceso al portal académico
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable(onClick = onAbrirSira),
                        shape = RoundedCornerShape(11.dp),
                        color = colores.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = colores.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Abrir SIRA",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colores.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Ir al portal académico",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colores.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colores.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Acciones: un único color destacado para la acción principal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(onClick = onImportar),
                            shape = RoundedCornerShape(11.dp),
                            color = Red
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Importar notas",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = White,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Desde PDF",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = White.copy(alpha = 0.85f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(onClick = onGenerarInforme),
                            shape = RoundedCornerShape(11.dp),
                            color = colores.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = colores.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Generar informe",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colores.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "PDF académico",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colores.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colores.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(15.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "Las electivas complementarias requieren actualización manual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }
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
    Surface(
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
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
    mostrandoSelector: Boolean,
    onLineaClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMemoriaClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val temaLinea = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(linea?.nombre)
    val colorAcento = temaLinea.colorPrimario
    val colorTexto = if (isDark) Color.White else Color(0xFF17151A)
    val colorTextoSecundario = if (isDark) Color.White.copy(alpha = 0.88f) else Color(0xFF39313A)
    val chipFondo = if (isDark) temaLinea.chipFondoOscuro else temaLinea.chipFondoClaro

    // Hero sin tarjeta ni marco: conserva la ilustración de fondo y toda la información.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
    ) {
        com.renea.psicologiauv.ui.theme.IlustracionFondoLinea(
            tipo = temaLinea.tipo,
            colorAcento = colorAcento,
            isDark = isDark,
            modifier = Modifier.matchParentSize()
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(5.dp)
                .size(36.dp)
                .clickable(onClick = onSettingsClick),
            shape = CircleShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.28f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Editar datos",
                    tint = colorAcento,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .height(34.dp)
                .clickable(onClick = onMemoriaClick),
            shape = RoundedCornerShape(18.dp),
            color = Burgundy,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Memoria es",
                    style = MaterialTheme.typography.labelMedium,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colorTexto,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(1.dp))

            Text(
                text = if (codigo.isNotBlank()) "Código $codigo" else "Código no definido",
                style = MaterialTheme.typography.labelMedium,
                color = colorTextoSecundario
            )

            Spacer(Modifier.height(7.dp))

            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable(onClick = onLineaClick),
                shape = RoundedCornerShape(20.dp),
                color = chipFondo,
                border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isDark) colorAcento else temaLinea.colorTextoChip,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = temaLinea.nombreVisible,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDark) Color.White else temaLinea.colorTextoChip,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) colorAcento else temaLinea.colorTextoChip,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(Modifier.height(5.dp))

            Text(
                text = if (mostrandoSelector) {
                    "Selecciona una nueva línea"
                } else {
                    "Toca la tuerca para editar tus datos"
                },
                style = MaterialTheme.typography.labelSmall,
                color = colorTextoSecundario,
                textAlign = TextAlign.Center
            )
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

@Composable
private fun SeccionTitulo(
    icono: ImageVector,
    titulo: String,
    subtitulo: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = Red.copy(alpha = 0.10f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, tint = Red, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
        color = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            Color(0xFFFFF0EC)
        },
        border = BorderStroke(1.dp, Red.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(25.dp),
                shape = CircleShape,
                color = Red.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = titulo, tint = Red, modifier = Modifier.size(14.dp))
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
