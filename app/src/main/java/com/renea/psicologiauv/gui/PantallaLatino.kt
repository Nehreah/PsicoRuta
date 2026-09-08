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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

                Image(
                    painter =
                        painterResource(
                            id = R.drawable.historia_2
                        ),
                    contentDescription = "América Latina",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                    contentScale = ContentScale.Crop
                )

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
}


