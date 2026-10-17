package com.renea.psicologiauv.gui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.ProgramaM

/**
 * Pantalla principal de Prácticas Profesionales.
 *
 * Esta pantalla es únicamente introductoria: no duplica ni reemplaza la
 * pantalla de evaluación. El botón Evaluar abre la pantalla de Prácticas,
 * donde continúan estando todos los requisitos y su interacción.
 * Toda la lógica académica pertenece a ProgramaM.
 */
@Composable
fun PantallaPracticasIntro(
    programa: ProgramaM,
    onEvaluar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val requisitosCompletos = programa.contarRequisitosAprobadosPracticas() == 8
    val scheme = MaterialTheme.colorScheme
    val estadoColor = if (requisitosCompletos) {
        scheme.primary
    } else {
        scheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.surface.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = scheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Work,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prácticas Profesionales",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "Tu preparación para la experiencia profesional",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Las prácticas profesionales son el espacio en el que integras los conocimientos, habilidades y competencias desarrollados durante tu formación en Psicología, llevándolos a contextos reales de actuación profesional.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = scheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "¿Cómo iniciar?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "Para iniciar las prácticas profesionales es necesario elegir una de las cuatro líneas de profesionalización. La línea Clínica cuenta con dos enfoques: Clínica y Neuropsicología.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )

                    LineaRow(Icons.Filled.Groups, "Social")
                    LineaRow(Icons.Filled.Business, "Organizacional")
                    LineaRow(Icons.Filled.School, "Educativa")
                    LineaRow(Icons.Filled.Psychology, "Clínica o Neuropsicología")
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEvaluar),
                shape = CircleShape,
                color = if (requisitosCompletos) scheme.primary else scheme.surface,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (requisitosCompletos) scheme.primary else scheme.outlineVariant
                ),
                shadowElevation = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (requisitosCompletos) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.Work
                        },
                        contentDescription = "Evaluar requisitos de prácticas",
                        tint = if (requisitosCompletos) scheme.onPrimary else scheme.primary,
                        modifier = Modifier.size(43.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evaluar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (requisitosCompletos) scheme.onPrimary else scheme.primary
                    )
                }
            }

            Text(
                text = if (requisitosCompletos) {
                    "Sí se cumple con los requisitos"
                } else {
                    "No se cumple con los requisitos"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = estadoColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Selecciona Evaluar para consultar el detalle completo de requisitos y líneas de profesionalización.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LineaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
