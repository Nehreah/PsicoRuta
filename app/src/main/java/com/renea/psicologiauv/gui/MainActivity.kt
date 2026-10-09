package com.renea.psicologiauv.gui

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

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
import com.renea.psicologiauv.ui.common.ZoomableAnyScreen
import com.renea.psicologiauv.ui.theme.Burgundy
import com.renea.psicologiauv.ui.theme.Charcoal
import com.renea.psicologiauv.ui.theme.LightGray
import com.renea.psicologiauv.ui.theme.OffWhite
import com.renea.psicologiauv.ui.theme.Red
import com.renea.psicologiauv.ui.theme.Wine
import com.renea.psicologiauv.ui.theme.White
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
            PsicologiaUVTheme {
                var avisoAceptado by remember {
                    mutableStateOf(preferenciasAviso.getBoolean("aviso_uso_aceptado", false))
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (avisoAceptado) {
                        PsicologiaUVApp(viewModel = viewModel)
                    } else {
                        AvisoInicialPsicoRuta(
                            onContinuar = {
                                preferenciasAviso.edit()
                                    .putBoolean("aviso_uso_aceptado", true)
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
                color = colores.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Una herramienta educativa independiente para consultar y organizar información académica.",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            AvisoItem(
                icono = Icons.Filled.Info,
                titulo = "Proyecto independiente",
                descripcion = "PsicoRuta UV no es una aplicación oficial de la Universidad del Valle ni está patrocinada, administrada o respaldada por dicha institución."
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
// AVISO PERMANENTE DE INDEPENDENCIA (política de Play contra suplantación)
// Siempre visible sobre la barra de navegación; al tocarlo abre el detalle.
// ========================================================================

@Composable
private fun BannerIndependencia(onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colores.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "App independiente · No oficial de la Universidad del Valle",
            style = MaterialTheme.typography.labelSmall,
            color = colores.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DialogoAcercaDe(onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
        title = { Text("Acerca de PsicoRuta UV") },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "PsicoRuta UV es una aplicación independiente, creada por un estudiante, " +
                        "con fines educativos e informativos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "NO es una aplicación oficial de la Universidad del Valle, ni está afiliada, " +
                        "patrocinada, aprobada o avalada por dicha institución. No utiliza sus " +
                        "logotipos ni símbolos oficiales. El nombre de la Universidad y las " +
                        "referencias a su programa de Psicología se usan únicamente para " +
                        "identificar el plan de estudios al que se refiere la información.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "La aplicación no reemplaza los canales, documentos ni sistemas oficiales " +
                        "(como SIRA). Verifica allí cualquier información académica o normativa " +
                        "antes de tomar decisiones.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Entendido") }
        }
    )
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

    AVANCE(
        leyenda = "Avance",
        icono = Icons.Filled.TrendingUp
    ),

    PRACTICAS(
        leyenda = "Prácticas",
        icono = Icons.Filled.Work
    )
}


// ========================================================================
// APP PRINCIPAL
// ========================================================================

@Composable
fun PsicologiaUVApp(
    viewModel: ControlV
) {

    // ====================================================================
    // DESTINO ACTUAL
    // ====================================================================

    var destinoActual by rememberSaveable {

        mutableStateOf(
            if (viewModel.primeraVezQueAbreLaApp) {
                PantallasApp.USUARIO
            } else {
                PantallasApp.AVANCE
            }
        )
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
    // PANTALLAS VISIBLES
    // ====================================================================

    val pantallasVisibles =
        remember(cumpleRequisitosGrado) {

            PantallasApp.entries.filter { pantalla ->

                pantalla != PantallasApp.GRADO ||
                        cumpleRequisitosGrado
            }
        }


    // ====================================================================
    // PAGER
    // ====================================================================

    val paginaInicial =
        pantallasVisibles
            .indexOf(destinoActual)
            .coerceAtLeast(0)


    val pagerState =
        rememberPagerState(
            initialPage = paginaInicial
        ) {
            pantallasVisibles.size
        }


    // ====================================================================
    // CAMBIO DESDE LA BARRA
    // ====================================================================

    LaunchedEffect(
        destinoActual,
        pantallasVisibles
    ) {

        val indice =
            pantallasVisibles.indexOf(destinoActual)

        if (
            indice >= 0 &&
            indice != pagerState.currentPage
        ) {

            pagerState.animateScrollToPage(
                page = indice,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }


    // ====================================================================
    // CAMBIO DESDE EL SWIPE
    // ====================================================================

    LaunchedEffect(pagerState) {

        snapshotFlow {
            pagerState.settledPage
        }.collect { pagina ->

            pantallasVisibles
                .getOrNull(pagina)
                ?.let { pantalla ->

                    if (pantalla != destinoActual) {

                        destinoActual =
                            pantalla
                    }
                }
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

    var mostrarAcercaDe by rememberSaveable { mutableStateOf(false) }

    if (mostrarAcercaDe) {
        DialogoAcercaDe(onCerrar = { mostrarAcercaDe = false })
    }

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        bottomBar = {

            androidx.compose.foundation.layout.Column {

                // Aviso permanente: la app es independiente y no oficial.
                BannerIndependencia(onClick = { mostrarAcercaDe = true })

                BarraNavegacionModerna(

                    destinos = pantallasVisibles,

                    destinoActual = destinoActual,

                    onDestinoSeleccionado = { destino ->

                        destinoActual = destino
                    }
                )
            }
        }

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

        ZoomableAnyScreen(

            modifier =
                Modifier
                    .padding(paddingInterno)
                    .fillMaxSize()

        ) {

            HorizontalPager(

                state = pagerState,

                modifier =
                    Modifier.fillMaxSize()

            ) { pagina ->

                when (
                    pantallasVisibles.getOrNull(
                        pagina
                    )
                ) {

                    // ====================================================
                    // PERFIL
                    // ====================================================

                    PantallasApp.USUARIO -> {

                        PantallaEditar(
                            viewModel = viewModel
                        )
                    }


                    // ====================================================
                    // PENSUM
                    // ====================================================

                    PantallasApp.PENSUM -> {

                        PantallaPensum(
                            estudiante = estudiante,
                            viewModel = viewModel
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

                    PantallasApp.PRACTICAS -> {

                        PantallaPracticas(
                            programa =
                                viewModel.programa,
                            viewModel =
                                viewModel
                        )
                    }


                    null -> Unit
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
        MaterialTheme.colorScheme.background ==
                Color(0xFF0A0A0F)


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

                Red.copy(
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

                Red

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