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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.snapshotFlow

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.text.font.FontWeight
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

        setContent {
            PsicologiaUVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PsicologiaUVApp(
                        viewModel = viewModel
                    )
                }
            }
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

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        bottomBar = {

            BarraNavegacionModerna(

                destinos = pantallasVisibles,

                destinoActual = destinoActual,

                onDestinoSeleccionado = { destino ->

                    destinoActual = destino
                }
            )
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