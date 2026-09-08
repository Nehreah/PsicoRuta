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
fun PantallaCali(
    onVolver: () -> Unit
) {

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
                            "gobierno conservador y por conflictos sociales y " +
                            "políticos. El 8 y 9 de junio se convierten en " +
                            "fechas históricas del movimiento estudiantil."
            ),
            Hito(
                periodo = "1954",
                titulo = "Día del Estudiante Caído",
                descripcion =
                    "El 8 de junio muere Uriel Gutiérrez. El 9 de junio " +
                            "una movilización estudiantil es reprimida por el " +
                            "Ejército. Varios estudiantes fueron asesinados."
            ),
            Hito(
                periodo = "1971",
                titulo = "Gran movimiento estudiantil nacional",
                descripcion =
                    "El movimiento estudiantil se extiende por todo el " +
                            "país y se formula el Programa Mínimo del " +
                            "movimiento estudiantil."
            ),
            Hito(
                periodo = "1989–1991",
                titulo = "Séptima Papeleta y Constituyente",
                descripcion =
                    "El movimiento estudiantil participa activamente en " +
                            "el proceso que dio origen a la nueva Constitución " +
                            "de 1991."
            ),
            Hito(
                periodo = "2018",
                titulo = "Plataforma ENEES",
                descripcion =
                    "La Plataforma Estudiantil Nacional por la Educación " +
                            "Superior impulsa una movilización nacional por " +
                            "financiación y condiciones dignas en las " +
                            "universidades públicas."
            ),
            Hito(
                periodo = "2019–2020",
                titulo = "Ciclo de movilización estudiantil",
                descripcion =
                    "Participación activa en el Paro Nacional y en las " +
                            "luchas por derechos sociales, educación pública " +
                            "y democracia."
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

                Image(
                    painter =
                        painterResource(
                            id = R.drawable.historia_1
                        ),
                    contentDescription = "Colombia y Cali",
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
}