package com.renea.psicologiauv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ========================================================================
// Hito / TarjetaHito
// ========================================================================
// Pieza reutilizable para mostrar recorridos históricos como una línea de
// tiempo (punto + línea vertical a la izquierda, tarjeta con el contenido
// a la derecha). La usan PantallaCali y PantallaLatino para no repetir el
// mismo diseño dos veces.

/**
 * Un momento puntual dentro de un recorrido histórico.
 */
data class Hito(
    val periodo: String,
    val titulo: String,
    val descripcion: String
)

/**
 * Dibuja un [hito] dentro de la línea de tiempo. Cuando [esUltimo] es
 * verdadero no se dibuja la línea vertical de continuación hacia abajo.
 */
@Composable
fun TarjetaHito(
    hito: Hito,
    esUltimo: Boolean = false
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
    ) {

        // Punto + línea vertical de la línea de tiempo.
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier =
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary
                        )
            )

            if (!esUltimo) {

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .width(2.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant
                            )
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Card(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        bottom = if (esUltimo) 0.dp else 20.dp
                    ),
            shape =
                RoundedCornerShape(18.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant
                )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = hito.periodo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = hito.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = hito.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
