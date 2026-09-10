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
import androidx.compose.material.icons.filled.Settings
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
    val icono: Int,
    val color: Color
)


private val LINEAS_DISPONIBLES = listOf(

    LineaInfo(
        nombre = "Educativa",
        icono = R.drawable.ic_linea_educativa,
        color = Color(0xFF4E7AC7)
    ),

    LineaInfo(
        nombre = "Social",
        icono = R.drawable.ic_linea_social,
        color = Color(0xFFE0793D)
    ),

    LineaInfo(
        nombre = "Organizacional",
        icono = R.drawable.ic_linea_organizacional,
        color = Color(0xFF5C8A5C)
    ),

    LineaInfo(
        nombre = "Clínica",
        icono = R.drawable.ic_linea_clinica,
        color = Color(0xFFC74E4E)
    ),

    LineaInfo(
        nombre = "NeuroClínica",
        icono = R.drawable.ic_linea_neuroclinica,
        color = Color(0xFF8B5CC7)
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
    icono: Int,
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
            painter =
                painterResource(icono),

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
                .background(
                    MaterialTheme.colorScheme.background
                )
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

                onAvatarClick = {

                    mostrandoSelectorLinea =
                        !mostrandoSelectorLinea
                },

                onLineaClick = {

                    mostrandoSelectorLinea =
                        !mostrandoSelectorLinea
                },

                onSettingsClick = {

                    mostrandoDatosPersonales =
                        true
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


            // Separación visual entre SIRA y Accesos rápidos
            Spacer(Modifier.height(12.dp))


            // ============================================================
            // ACCESOS RÁPIDOS
            // ============================================================

            SeccionTitulo(

                icono =
                    Icons.Default.GridView,

                titulo =
                    "Accesos rápidos",

                subtitulo =
                    "Documentos y contactos importantes."
            )

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                            .copy(alpha = 0.35f)
                    )
            ) {

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        AccesoCuadrado(

                            icono =
                                Icons.Default.Description,

                            titulo =
                                "Res. 091",

                            modifier =
                                Modifier.weight(1f),

                            onClick = {

                                abrirEnlace(
                                    contexto,
                                    ENLACE_RESOLUCION_091
                                )
                            }
                        )

                        AccesoCuadrado(

                            icono =
                                Icons.Default.Description,

                            titulo =
                                "Acuerdo 009",

                            modifier =
                                Modifier.weight(1f),

                            onClick = {

                                abrirEnlace(
                                    contexto,
                                    ENLACE_ACUERDO_009
                                )
                            }
                        )

                        AccesoCuadrado(

                            icono =
                                Icons.Default.GridView,

                            titulo =
                                "Malla",

                            modifier =
                                Modifier.weight(1f),

                            onClick = {

                                abrirEnlace(
                                    contexto,
                                    ENLACE_MALLA_CURRICULAR
                                )
                            }
                        )
                    }


                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        AccesoCuadrado(

                            icono =
                                Icons.Default.Email,

                            titulo =
                                "Rep. Facultad",

                            modifier =
                                Modifier.weight(1f),

                            onClick = {

                                abrirCorreo(
                                    contexto,
                                    CORREO_REPRESENTANTE_FACULTAD
                                )
                            }
                        )

                        AccesoCuadrado(

                            icono =
                                Icons.Default.Email,

                            titulo =
                                "Dir. Programa",

                            modifier =
                                Modifier.weight(1f),

                            onClick = {

                                abrirCorreo(
                                    contexto,
                                    CORREO_DIRECCION_PROGRAMA
                                )
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }


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
                                .labelLarge,

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(11.dp),
                    color = Red.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(7.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Importar desde SIRA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Sincroniza tu historial académico",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SuccessDark.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SuccessDark)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "SIRA",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // EXPLICACIÓN
            Text(
                text = "Descarga tu historial académico desde SIRA y trae tus notas directamente a la aplicación. El proceso es rápido y conserva tus asignaturas existentes.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            // PASOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PasoSira("1", "Descarga", Modifier.weight(1f))
                PasoSira("2", "Selecciona", Modifier.weight(1f))
                PasoSira("3", "Importa", Modifier.weight(1f))
            }

            // ABRIR SIRA
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp)
                    .clickable(onClick = onAbrirSira),
                shape = RoundedCornerShape(12.dp),
                color = Red.copy(alpha = 0.07f),
                border = BorderStroke(1.dp, Red.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Red,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Abrir SIRA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Ir al portal académico",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Red.copy(alpha = 0.70f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // IMPORTAR + PDF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(43.dp)
                        .clickable(onClick = onImportar),
                    shape = RoundedCornerShape(12.dp),
                    color = Red
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Importar notas",
                                style = MaterialTheme.typography.labelMedium,
                                color = White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Desde PDF",
                                style = MaterialTheme.typography.labelSmall,
                                color = White.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(43.dp)
                        .clickable(onClick = onGenerarInforme),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Generar informe",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "PDF académico",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // NOTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Usa el PDF oficial generado por SIRA.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
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
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(11.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.60f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .background(Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
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
    onAvatarClick: () -> Unit,
    onLineaClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    // Tokens de tema: se adaptan automáticamente a modo oscuro / claro,
    // igual que el hero de PantallaPensum.
    val primary       = MaterialTheme.colorScheme.primary
    val surface       = MaterialTheme.colorScheme.surface
    val onSurface     = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar  = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryCont   = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp)
                .background(surface)
        ) {

            // ================================================================
            // CÍRCULOS DECORATIVOS — mismo estilo que PantallaPensum hero.
            // ================================================================

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 92.dp, y = (-120).dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.08f))
            )

            Box(
                modifier = Modifier
                    .size(205.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-82).dp, y = 92.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.055f))
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-78).dp, y = (-58).dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.035f))
            )

            Box(
                modifier = Modifier
                    .size(115.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 34.dp, y = 38.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.045f))
            )

            // Círculo de acento muy sutil.
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-22).dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.045f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {

                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(primaryCont.copy(alpha = 0.96f))
                            .clickable(onClick = onSettingsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_avatar_default),
                            contentDescription = "Editar datos",
                            tint = primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clickable(onClick = onSettingsClick),
                        shape = CircleShape,
                        color = surface,
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Editar datos",
                                tint = primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(Modifier.height(1.dp))

                Text(
                    text = if (codigo.isNotBlank()) "Código $codigo" else "Código no definido",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceVar
                )

                Spacer(Modifier.height(5.dp))

                Surface(
                    modifier = Modifier
                        .height(36.dp)
                        .clickable(onClick = onLineaClick),
                    shape = RoundedCornerShape(24.dp),
                    color = (linea?.color ?: primary).copy(alpha = 0.14f),
                    border = BorderStroke(
                        1.dp,
                        (linea?.color ?: primary).copy(alpha = 0.32f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (linea != null) Icons.Default.Check else Icons.Default.Person,
                            contentDescription = null,
                            tint = linea?.color ?: primary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            text = linea?.nombre ?: "Selecciona tu línea profesional",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = linea?.color ?: primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    text = if (mostrandoSelector) "Selecciona una nueva línea" else "Toca tu foto o la tuerca para editar tus datos",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVar
                )
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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