package com.renea.psicologiauv.gui

import androidx.compose.ui.graphics.luminance
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.ui.theme.Red
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val preferenciasAviso = getSharedPreferences("psicoruta_ajustes", MODE_PRIVATE)

        setContent {
            PsicologiaUVTheme(linea = viewModel.programa.estudiante?.linea) {
                var avisoAceptado by remember {
                    mutableStateOf(preferenciasAviso.getBoolean("aviso_uso_aceptado", false))
                }

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
            }
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
        icono = Icons.Filled.TrendingUp
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


// ========================================================================
// BARRA DE NAVEGACIÓN MODERNA
// ========================================================================

@Composable
private fun BarraNavegacionModerna(
    destinos: List<PantallasApp>,
    destinoActual: PantallasApp,
    onDestinoSeleccionado: (PantallasApp) -> Unit
) {

    val esTemaOscuro =
        MaterialTheme.colorScheme.background.luminance() < 0.5f


    val fondoBarra =
        MaterialTheme.colorScheme.surface


    val bordeBarra =
        MaterialTheme.colorScheme.outlineVariant
            .copy(alpha = 0.35f)


    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 6.dp,
                    bottom = 8.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Surface(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape =
                            RoundedCornerShape(26.dp)
                    ),

            shape =
                RoundedCornerShape(26.dp),

            color =
                fondoBarra,

            border =
                androidx.compose.foundation.BorderStroke(
                    width = 0.8.dp,
                    color = bordeBarra
                )
        ) {

            Row(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 6.dp,
                            vertical = 6.dp
                        ),

                horizontalArrangement =
                    Arrangement.SpaceEvenly,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                destinos.forEach { destino ->

                    ItemBarraNavegacion(

                        destino = destino,

                        seleccionado =
                            destino ==
                                    destinoActual,

                        esTemaOscuro =
                            esTemaOscuro,

                        onClick = {

                            onDestinoSeleccionado(
                                destino
                            )
                        }
                    )
                }
            }
        }
    }
}


// ========================================================================
// ITEM DE NAVEGACIÓN
// ========================================================================

@Composable
private fun ItemBarraNavegacion(
    destino: PantallasApp,
    seleccionado: Boolean,
    esTemaOscuro: Boolean,
    onClick: () -> Unit
) {

    // ====================================================================
    // ANIMACIONES
    // ====================================================================

    val anchoSeleccionado by
    animateDpAsState(

        targetValue =
            if (seleccionado) {
                112.dp
            } else {
                52.dp
            },

        animationSpec =
            tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing
            ),

        label = "anchoItem"
    )


    val escalaIcono by
    animateFloatAsState(

        targetValue =
            if (seleccionado) {
                1.08f
            } else {
                1f
            },

        animationSpec =
            tween(
                durationMillis = 220
            ),

        label = "escalaIcono"
    )


    val colorFondo by
    animateColorAsState(

        targetValue =
            if (seleccionado) {

                MaterialTheme.colorScheme.primary.copy(
                    alpha =
                        if (esTemaOscuro) {
                            0.18f
                        } else {
                            0.10f
                        }
                )

            } else {

                Color.Transparent
            },

        animationSpec =
            tween(
                durationMillis = 220
            ),

        label = "colorFondo"
    )


    val colorIcono by
    animateColorAsState(

        targetValue =
            if (seleccionado) {

                MaterialTheme.colorScheme.primary

            } else {

                MaterialTheme.colorScheme
                    .onSurfaceVariant
            },

        animationSpec =
            tween(
                durationMillis = 220
            ),

        label = "colorIcono"
    )


    val colorTexto by
    animateColorAsState(

        targetValue =
            if (seleccionado) {

                MaterialTheme.colorScheme
                    .onSurface

            } else {

                Color.Transparent
            },

        animationSpec =
            tween(
                durationMillis = 180
            ),

        label = "colorTexto"
    )


    // ====================================================================
    // ITEM
    // ====================================================================

    Box(

        modifier =
            Modifier
                .height(58.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(
                    colorFondo
                )
                .clickable(
                    onClick = onClick
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis = 280,
                            easing =
                                FastOutSlowInEasing
                        )
                )
                .padding(
                    horizontal =
                        if (seleccionado) {
                            8.dp
                        } else {
                            4.dp
                        }
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Row(

            modifier =
                Modifier
                    .height(58.dp)
                    .width(anchoSeleccionado),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ============================================================
            // ICONO
            // ============================================================

            Icon(

                imageVector =
                    destino.icono,

                contentDescription =
                    destino.leyenda,

                tint =
                    colorIcono,

                modifier =
                    Modifier
                        .size(
                            if (seleccionado) {
                                23.dp
                            } else {
                                21.dp
                            }
                        )
                        .graphicsLayer {
                            scaleX = escalaIcono
                            scaleY = escalaIcono
                        }
            )


            // ============================================================
            // TEXTO
            // ============================================================

            AnimatedVisibility(
                visible = seleccionado
            ) {

                Row {

                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )

                    Text(

                        text =
                            destino.leyenda,

                        color =
                            colorTexto,

                        style =
                            MaterialTheme.typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            12.sp,

                        maxLines = 1
                    )
                }
            }
        }
    }
}