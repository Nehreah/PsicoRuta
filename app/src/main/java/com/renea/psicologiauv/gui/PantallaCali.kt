package com.renea.psicologiauv.gui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.R
import com.renea.psicologiauv.ui.common.Hito
import com.renea.psicologiauv.ui.common.TarjetaHito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCali(
    onVolver: () -> Unit
) {
    var ampliarImagen by remember { mutableStateOf(false) }

    BackHandler {
        onVolver()
    }

    val hitos = remember {
        listOf(
            Hito(
                periodo = "1929",
                titulo = "Asesinato de Gonzalo Bravo Pérez",
                descripcion =
                    "Los estudiantes protestaban en Bogotá contra el " +
                            "gobierno conservador. El 8 y 9 de junio se convierten " +
                            "en la conmemoración histórica del estudiante colombiano."
            ),
            Hito(
                periodo = "1954",
                titulo = "Día del Estudiante Caído",
                descripcion =
                    "El 8 de junio muere Uriel Gutiérrez. El 9 de junio " +
                            "una marcha estudiantil es reprimida violentamente por el " +
                            "Ejército en la carrera 7.ª de Bogotá."
            ),
            Hito(
                periodo = "1964",
                titulo = "Huelga Universitaria de 1964",
                descripcion =
                    "Movilizaciones estudiantiles en la Universidad de Antioquia " +
                            "y otras regiones en reclamo de autonomía universitaria " +
                            "y cogobierno."
            ),
            Hito(
                periodo = "26 feb. 1971",
                titulo = "Masacre en la Universidad del Valle (Cali)",
                descripcion =
                    "La intervención militar en la sede San Fernando de Univalle " +
                            "en Cali deja varios estudiantes fallecidos y desencadena la " +
                            "huelga estudiantil nacional más grande del siglo XX."
            ),
            Hito(
                periodo = "1971",
                titulo = "Programa Mínimo Estudiantil",
                descripcion =
                    "El movimiento estudiantil unificado de Colombia formula el " +
                            "Programa Mínimo, exigiendo autonomía, financiación estatal " +
                            "y eliminación de la injerencia extranjera."
            ),
            Hito(
                periodo = "1989–1991",
                titulo = "Séptima Papeleta y Constituyente",
                descripcion =
                    "El movimiento estudiantil impulse la Séptima Papeleta en " +
                            "las elecciones de 1990, impulsando la redacción de la " +
                            "Constitución Política de 1991."
            ),
            Hito(
                periodo = "2011",
                titulo = "Mesa Amplia Nacional Estudiantil (MANE)",
                descripcion =
                    "La MANE moviliza masivamente al país contra la reforma " +
                            "a la Ley 30 de educación superior, logrando su " +
                            "retiro del Congreso."
            ),
            Hito(
                periodo = "2018",
                titulo = "Plataforma ENEES y Paro Universitario",
                descripcion =
                    "La ENEES lidera marchas históricas por la recuperación del " +
                            "presupuesto público de las universidades e instituciones " +
                            "de educación superior."
            ),
            Hito(
                periodo = "2019–2020",
                titulo = "Paro Nacional 21N",
                descripcion =
                    "Estudiantes y jóvenes de Cali y Colombia se suman al Paro " +
                            "Nacional exigiendo garantías de vida, paz, trabajo y educación."
            ),
            Hito(
                periodo = "2021",
                titulo = "Estallido Social en Cali",
                descripcion =
                    "Cali se convierte en el epicentro de la movilización " +
                            "popular y juvenil. La juventud universitaria y barrial " +
                            "lidera los puntos de resistencia por transformaciones sociales."
            )
        )
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Column {

                        Text(
                            text = "Cali",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Hitos del movimiento estudiantil",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = onVolver
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(18.dp)
        ) {

            /* --------------------------------------------------------- */
            /* ENCABEZADO CON IMAGEN                                     */
            /* --------------------------------------------------------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { ampliarImagen = true }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.historia_1),
                        contentDescription = "Colombia y Cali",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = "Ampliar",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Ampliar 🔍",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "COLOMBIA / CALI",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Un recorrido por los acontecimientos que " +
                                    "marcaron la movilización estudiantil en Cali.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            /* --------------------------------------------------------- */
            /* LÍNEA DE TIEMPO                                           */
            /* --------------------------------------------------------- */

            hitos.forEachIndexed { indice, hito ->

                TarjetaHito(
                    hito = hito,
                    esUltimo = indice == hitos.lastIndex
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }

    if (ampliarImagen) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Dialog(
            onDismissRequest = { ampliarImagen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { ampliarImagen = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clickable(enabled = false) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLOMBIA Y CALI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { ampliarImagen = false },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        if (scale == 1f) {
                                            offset = Offset.Zero
                                        } else {
                                            offset += pan
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.historia_1),
                                contentDescription = "Colombia y Cali",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Text(
                        text = "🔍 Pellizca con los dedos para acercar y desplazar la imagen",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}