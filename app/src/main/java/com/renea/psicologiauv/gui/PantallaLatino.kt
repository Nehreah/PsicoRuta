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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.R
import com.renea.psicologiauv.ui.common.Hito
import com.renea.psicologiauv.ui.common.TarjetaHito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLatino(
    onVolver: () -> Unit
) {
    var ampliarImagen by remember { mutableStateOf(false) }

    BackHandler {
        onVolver()
    }

    val hitos = remember {
        listOf(
            Hito(
                periodo = "1918",
                titulo = "Reforma Universitaria de Córdoba",
                descripcion =
                    "El Manifiesto Liminar del 21 de junio marca un hito " +
                            "fundamental del movimiento estudiantil " +
                            "latinoamericano."
            ),
            Hito(
                periodo = "1919–1920",
                titulo = "Expansión de la Reforma Universitaria",
                descripcion =
                    "Las ideas reformistas se expanden hacia Perú y " +
                            "otros países de América Latina."
            ),
            Hito(
                periodo = "Década de 1920",
                titulo = "Reforma en nuevos países",
                descripcion =
                    "Las organizaciones estudiantiles reformistas se " +
                            "desarrollan en diferentes países y participan " +
                            "en luchas por la autonomía universitaria."
            ),
            Hito(
                periodo = "1959",
                titulo = "Revolución Cubana",
                descripcion =
                    "Aumenta la politización juvenil, las ideas " +
                            "antiimperialistas y la influencia de las " +
                            "izquierdas."
            ),
            Hito(
                periodo = "1968",
                titulo = "Gran ciclo mundial de rebeliones estudiantiles",
                descripcion =
                    "Se producen movilizaciones estudiantiles en " +
                            "Francia, Estados Unidos, Brasil, Uruguay, " +
                            "Argentina, Chile, Colombia y otros países."
            ),
            Hito(
                periodo = "2 oct. 1968",
                titulo = "Tlatelolco, México",
                descripcion =
                    "Se produce una fuerte represión y masacre contra " +
                            "el movimiento estudiantil en la Plaza de las " +
                            "Tres Culturas."
            ),
            Hito(
                periodo = "Década de 1970",
                titulo = "Luchas antiimperialistas y contra dictaduras",
                descripcion =
                    "El movimiento estudiantil se articula con obreros " +
                            "y campesinos en luchas por la democracia y la " +
                            "transformación social."
            ),
            Hito(
                periodo = "Década de 1980",
                titulo = "Redemocratización",
                descripcion =
                    "Los estudiantes participan en procesos de " +
                            "transición democrática en diferentes países de " +
                            "la región."
            ),
            Hito(
                periodo = "Década de 1990",
                titulo = "Neoliberalismo y privatización",
                descripcion =
                    "Se desarrollan movilizaciones contra la " +
                            "privatización de la educación y el ajuste " +
                            "estructural en las universidades."
            ),
            Hito(
                periodo = "Años 2000",
                titulo = "Nuevas agendas estudiantiles",
                descripcion =
                    "La defensa de la educación pública se articula con " +
                            "demandas por derechos humanos, diversidad, " +
                            "género y medio ambiente."
            ),
            Hito(
                periodo = "2010–hoy",
                titulo = "Educación pública y derechos sociales",
                descripcion =
                    "Se desarrollan movilizaciones masivas en defensa de " +
                            "la educación pública, contra la desigualdad y " +
                            "por sociedades más justas."
            )
        )
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Column {

                        Text(
                            text = "América Latina",
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
                        painter = painterResource(id = R.drawable.historia_2),
                        contentDescription = "América Latina",
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
                        text = "AMÉRICA LATINA",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Del movimiento reformista de Córdoba a las " +
                                    "nuevas agendas estudiantiles.",
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
                            text = "AMÉRICA LATINA",
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
                                painter = painterResource(id = R.drawable.historia_2),
                                contentDescription = "América Latina",
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


