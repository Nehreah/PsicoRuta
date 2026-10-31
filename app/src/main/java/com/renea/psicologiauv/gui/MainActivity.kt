package com.renea.psicologiauv.gui

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.ui.theme.PsicologiaUVTheme


// ========================================================================
// MAIN ACTIVITY
// ========================================================================

class MainActivity : ComponentActivity() {

    private val viewModel: ControlV by viewModels {
        ControlV.Factory(
            Serializador(applicationContext)
        )
    }

    // ------------------------------------------------------------------
    // ACTUALIZACIONES DENTRO DE LA APP (Google Play, modo flexible)
    // ------------------------------------------------------------------
    private lateinit var appUpdateManager: AppUpdateManager

    /** Verdadero cuando Play terminó de descargar la actualización y falta reiniciar la app. */
    private var actualizacionDescargada by mutableStateOf(false)

    private val lanzadorActualizacion =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

    private val oyenteInstalacion = InstallStateUpdatedListener { estado ->
        if (estado.installStatus() == InstallStatus.DOWNLOADED) actualizacionDescargada = true
    }

    private fun buscarActualizacion() {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (info.installStatus() == InstallStatus.DOWNLOADED) {
                        // Se descargó en segundo plano mientras la app estaba cerrada o pausada.
                        actualizacionDescargada = true
                    } else if (!ofertaActualizacionMostrada &&
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        ofertaActualizacionMostrada = true
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            lanzadorActualizacion,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    }
                }
                .addOnFailureListener { error ->
                    // Normal si la app no se instaló desde Google Play (por ejemplo, desde Android Studio).
                    Log.w("PsicoRuta", "No se pudo consultar actualizaciones", error)
                }
        } catch (e: Exception) {
            Log.w("PsicoRuta", "Error al buscar actualizaciones", e)
        }
    }

    override fun onResume() {
        super.onResume()
        buscarActualizacion()
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(oyenteInstalacion)
        super.onDestroy()
    }

    companion object {
        // Para ofrecer la actualización una sola vez por sesión (no en cada rotación de pantalla).
        private var ofertaActualizacionMostrada = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        appUpdateManager.registerListener(oyenteInstalacion)

        val preferenciasAviso = getSharedPreferences("psicoruta_ajustes", MODE_PRIVATE)

        setContent {
            // Tema elegido por el usuario (null = seguir el del sistema). Se recuerda entre sesiones.
            var temaManual by remember {
                mutableStateOf<Boolean?>(
                    if (preferenciasAviso.contains("tema_oscuro"))
                        preferenciasAviso.getBoolean("tema_oscuro", false)
                    else null
                )
            }
            val sistemaOscuro = androidx.compose.foundation.isSystemInDarkTheme()
            val oscuro = temaManual ?: sistemaOscuro

            // Todas las pantallas leen el modo desde la configuración: se la reescribe con el modo elegido.
            val configuracionBase = androidx.compose.ui.platform.LocalConfiguration.current
            val configuracionTema = remember(configuracionBase, oscuro) {
                android.content.res.Configuration(configuracionBase).apply {
                    uiMode = (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or
                            (if (oscuro) android.content.res.Configuration.UI_MODE_NIGHT_YES
                            else android.content.res.Configuration.UI_MODE_NIGHT_NO)
                }
            }

            // Íconos de las barras del sistema acordes al tema elegido.
            androidx.compose.runtime.DisposableEffect(oscuro) {
                enableEdgeToEdge(
                    statusBarStyle = androidx.activity.SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { oscuro },
                    navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { oscuro }
                )
                onDispose { }
            }

            val controlTema = remember(oscuro) {
                com.renea.psicologiauv.ui.theme.ControlTema(oscuro) {
                    val nuevo = !oscuro
                    temaManual = nuevo
                    preferenciasAviso.edit().putBoolean("tema_oscuro", nuevo).apply()
                }
            }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides configuracionTema,
                com.renea.psicologiauv.ui.theme.LocalControlTema provides controlTema
            ) {
            PsicologiaUVTheme(linea = viewModel.programa.estudiante?.linea, darkTheme = oscuro) {
                var avisoAceptado by remember {
                    mutableStateOf(preferenciasAviso.getBoolean("aviso_uso_aceptado", false))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        if (avisoAceptado) {
                            PsicologiaUVApp(
                                viewModel = viewModel,
                                mostrarBienvenidaInicial = preferenciasAviso
                                    .getBoolean("bienvenida_pendiente", false),
                                onBienvenidaTerminada = {
                                    preferenciasAviso.edit()
                                        .putBoolean("bienvenida_pendiente", false)
                                        .apply()
                                }
                            )
                        } else {
                            AvisoInicialPsicoRuta(
                                onContinuar = {
                                    preferenciasAviso.edit()
                                        .putBoolean("aviso_uso_aceptado", true)
                                        .putBoolean("bienvenida_pendiente", true)
                                        .apply()
                                    avisoAceptado = true
                                }
                            )
                        }
                    }
                    if (actualizacionDescargada) {
                        AvisoActualizacionLista(
                            onReiniciar = { appUpdateManager.completeUpdate() },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
            }
        }
    }
}




// ========================================================================
// AVISO DE ACTUALIZACIÓN LISTA: Play ya la descargó; falta reiniciar la app.
// ========================================================================

@Composable
private fun AvisoActualizacionLista(
    onReiniciar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(colores.inverseSurface)
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Actualización lista para instalar",
            style = MaterialTheme.typography.bodyMedium,
            color = colores.inverseOnSurface,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.TextButton(onClick = onReiniciar) {
            Text(
                text = "Reiniciar",
                color = colores.inversePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ========================================================================
// AVISO DE USO: se presenta en la primera apertura y queda disponible
// como confirmación inicial, sin sustituir una política de privacidad web.
// ========================================================================

@Composable
private fun AvisoInicialPsicoRuta(
    onContinuar: () -> Unit
) {
    var confirmacionLectura by rememberSaveable { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colores.surface)
                .border(1.dp, colores.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
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
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = colores.onPrimaryContainer,
                    modifier = Modifier.size(39.dp)
                )
            }

            Text(
                text = "PsicoRuta UV",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Antes de comenzar",
                style = MaterialTheme.typography.titleMedium,
                color = colores.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Una herramienta educativa para consultar y organizar información académica.",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            AvisoItem(
                icono = Icons.Filled.School,
                titulo = "Uso informativo",
                descripcion = "Contrasta la información académica importante con las fuentes y los canales oficiales de la Universidad. La aplicación no los sustituye."
            )
            AvisoItem(
                icono = Icons.Filled.PrivacyTip,
                titulo = "Privacidad",
                descripcion = "Evita introducir datos personales sensibles o información que no sea necesaria. La política de privacidad completa está disponible en la ficha de la aplicación en Google Play."
            )
            AvisoItem(
                icono = Icons.Filled.Save,
                titulo = "Copias de seguridad",
                descripcion = "Tus datos se guardan estrictamente de forma local por privacidad y NO se suben a la nube. Desinstalar la app o perder el dispositivo borrará tus datos locales de forma definitiva. Exporta periódicamente una copia de seguridad y guárdala en un lugar seguro."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colores.surfaceVariant.copy(alpha = 0.55f))
                    .clickable { confirmacionLectura = !confirmacionLectura }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmacionLectura,
                    onCheckedChange = { confirmacionLectura = it }
                )
                Text(
                    text = "He leído y comprendido este aviso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onContinuar,
                enabled = confirmacionLectura,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colores.primary,
                    contentColor = colores.onPrimary
                )
            ) {
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Este aviso no sustituye la política de privacidad completa.",
                style = MaterialTheme.typography.labelSmall,
                color = colores.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AvisoItem(
    icono: ImageVector,
    titulo: String,
    descripcion: String
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colores.primaryContainer.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colores.onPrimaryContainer,
                modifier = Modifier.size(21.dp)
            )
        }
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant
            )
        }
    }
}


// ========================================================================
// PANTALLAS DE LA APP
// ========================================================================

enum class PantallasApp(
    val leyenda: String,
    val icono: ImageVector
) {

    USUARIO(
        leyenda = "Perfil",
        icono = Icons.Filled.Person
    ),

    PENSUM(
        leyenda = "Pensum",
        icono = Icons.Filled.Home
    ),

    GRADO(
        leyenda = "Grado",
        icono = Icons.Filled.School
    ),

    TRABAJO_GRADO(
        leyenda = "Trabajo de grado",
        icono = Icons.Filled.Description
    ),

    AVANCE(
        leyenda = "Avance",
        icono = Icons.AutoMirrored.Filled.TrendingUp
    ),

    PRACTICAS(
        leyenda = "Prácticas",
        icono = Icons.Filled.Work
    ),

    PRACTICAS_INTRO(
        leyenda = "Prácticas profesionales",
        icono = Icons.Filled.Work
    ),

    REGLAMENTO_TRABAJO_GRADO(
        leyenda = "Reglamento de trabajo de grado",
        icono = Icons.Filled.Description
    )
}


// ========================================================================
// APP PRINCIPAL
// ========================================================================

@Composable
fun PsicologiaUVApp(
    viewModel: ControlV,
    mostrarBienvenidaInicial: Boolean = false,
    onBienvenidaTerminada: () -> Unit = {}
) {

    // ====================================================================
    // DESTINO ACTUAL
    // ====================================================================

    var destinoActual by rememberSaveable {

        mutableStateOf(PantallasApp.USUARIO)
    }


    // ====================================================================
    // GRADO
    // ====================================================================

    val cumpleRequisitosGrado =
        viewModel.programa.cumpleRequisitosGrado()


    LaunchedEffect(cumpleRequisitosGrado) {

        if (
            destinoActual == PantallasApp.GRADO &&
            !cumpleRequisitosGrado
        ) {

            destinoActual =
                PantallasApp.AVANCE
        }
    }


    // ====================================================================
    // NAVEGACIÓN DIRECTA
    // ====================================================================
    // No usamos HorizontalPager para cambiar de pantalla. El Pager obliga a
    // medir/componer una infraestructura de páginas aunque el usuario solo
    // quiera saltar de Perfil a Pensum o Prácticas. Aquí cada destino se
    // muestra directamente, evitando ese trabajo y haciendo el cambio
    // prácticamente inmediato.

    val pantallasVisibles =
        remember(cumpleRequisitosGrado) {
            PantallasApp.entries.filter { pantalla ->
                pantalla != PantallasApp.PRACTICAS_INTRO &&
                pantalla != PantallasApp.REGLAMENTO_TRABAJO_GRADO &&
                (pantalla != PantallasApp.GRADO || cumpleRequisitosGrado)
            }
        }

    LaunchedEffect(pantallasVisibles) {
        if (destinoActual !in pantallasVisibles) {
            destinoActual = PantallasApp.USUARIO
        }
    }

    // ====================================================================
    // HISTORIAL
    // ====================================================================

    val historialPestanas =
        remember {

            mutableStateListOf(
                destinoActual
            )
        }


    LaunchedEffect(destinoActual) {

        if (
            historialPestanas.lastOrNull() !=
            destinoActual
        ) {

            historialPestanas.add(
                destinoActual
            )
        }
    }


    // ====================================================================
    // BOTÓN ATRÁS
    // ====================================================================

    BackHandler(
        enabled =
            historialPestanas.size > 1
    ) {

        historialPestanas.removeAt(
            historialPestanas.lastIndex
        )

        destinoActual =
            historialPestanas.last()
    }


    // ====================================================================
    // SCAFFOLD
    // ====================================================================

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

    ) { paddingInterno ->

        val estudiante =
            viewModel.programa.estudiante


        // ================================================================
        // SIN ESTUDIANTE
        // ================================================================

        if (estudiante == null) {

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingInterno),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "No se pudo cargar la información del estudiante.",

                    color =
                        MaterialTheme.colorScheme.onBackground,

                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            return@Scaffold
        }


        // ================================================================
        // PÁGINAS
        // ================================================================

        // Fondo temático global: la ilustración depende de la línea profesional
        // del estudiante y queda detrás de todas las pantallas, no dentro del hero.
        val temaPantalla = com.renea.psicologiauv.ui.theme.obtenerTemaLinea(estudiante.linea)
        val temaOscuro = androidx.compose.foundation.isSystemInDarkTheme()
        val fondoTematico = if (temaOscuro) temaPantalla.fondoOscuro else temaPantalla.fondoClaro

        Box(
            modifier = Modifier
                .padding(paddingInterno)
                .fillMaxSize()
                // Con navegación por botones, reserva exactamente el espacio
                // de la barra del sistema. En navegación por gestos este inset
                // normalmente es 0, por lo que la app conserva toda la pantalla.
                .navigationBarsPadding()
                .background(fondoTematico)
        ) {
            com.renea.psicologiauv.ui.theme.IlustracionFondoLinea(
                tipo = temaPantalla.tipo,
                colorAcento = temaPantalla.colorPrimario,
                isDark = temaOscuro,
                modifier = Modifier.fillMaxSize()
            )

            // Navegación directa: cada pantalla ocupa el espacio disponible
            // sin pasar por un Pager intermedio. Las pantallas secundarias
            // tienen un pequeño encabezado propio para que el botón de regreso
            // quede cerca del contenido y no flotando a distancia.
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (destinoActual != PantallasApp.USUARIO) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .padding(start = 10.dp, top = 4.dp, end = 10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    destinoActual = PantallasApp.USUARIO
                                },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 0.7.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Volver al perfil principal",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (destinoActual) {

                        // ====================================================
                        // PERFIL
                        // ====================================================

                        PantallasApp.USUARIO -> {

                            PantallaPerfil(
                                viewModel = viewModel,
                                mostrarBienvenidaInicial = mostrarBienvenidaInicial,
                                onBienvenidaTerminada = onBienvenidaTerminada,
                                onNavigate = { destino ->
                                    destinoActual = if (destino == PantallasApp.PRACTICAS) {
                                        PantallasApp.PRACTICAS_INTRO
                                    } else {
                                        destino
                                    }
                                }
                            )
                        }


                        // ====================================================
                        // PENSUM
                        // ====================================================

                        PantallasApp.PENSUM -> {

                            PantallaPensum(
                                estudiante = estudiante,
                                viewModel = viewModel,
                                onNavigate = { destino ->
                                    destinoActual = destino
                                }
                            )
                        }


                        // ====================================================
                        // GRADO
                        // ====================================================

                        PantallasApp.GRADO -> {

                            PantallaGrado(
                                programa =
                                    viewModel.programa
                            )
                        }


                        // ====================================================
                        // TRABAJO DE GRADO
                        // ====================================================

                        PantallasApp.TRABAJO_GRADO -> {

                            PantallaTrabajoGrado(
                                programa = viewModel.programa,
                                onVerReglamento = {
                                    destinoActual =
                                        PantallasApp.REGLAMENTO_TRABAJO_GRADO
                                }
                            )
                        }


                        // ====================================================
                        // REGLAMENTO DEL TRABAJO DE GRADO
                        // ====================================================

                        PantallasApp.REGLAMENTO_TRABAJO_GRADO -> {

                            PantallaReglamentoTrabajoGrado()
                        }


                        // ====================================================
                        // AVANCE
                        // ====================================================

                        PantallasApp.AVANCE -> {

                            PantallaAvance(
                                programa =
                                    viewModel.programa,

                                viewModel =
                                    viewModel
                            )
                        }


                        // ====================================================
                        // PRÁCTICAS
                        // ====================================================

                        PantallasApp.PRACTICAS_INTRO -> {

                            PantallaPracticasIntro(
                                programa = viewModel.programa,
                                onEvaluar = {
                                    destinoActual = PantallasApp.PRACTICAS
                                }
                            )
                        }

                        PantallasApp.PRACTICAS -> {

                            PantallaPracticas(
                                programa =
                                    viewModel.programa,
                                viewModel =
                                    viewModel
                            )
                        }


                    }
                }
            }
        }
    }
}
