package com.renea.psicologiauv.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.model.ProgramaM
import com.renea.psicologiauv.ui.common.Hito
import com.renea.psicologiauv.ui.common.TarjetaHito
import com.renea.psicologiauv.ui.common.colorEstado

// ========================================================================
// TRABAJO DE GRADO
// ========================================================================
// Contenido basado en la Resolución 60-2019 del Consejo del Instituto de
// Psicología (Reglamentación de Trabajo de Grado del Programa de Psicología,
// Universidad del Valle). Es un resumen informativo: el texto oficial es el
// que prevalece.

@Composable
fun PantallaTrabajoGrado(
    modifier: Modifier = Modifier,
    programa: ProgramaM
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ================================================================
        // ENCABEZADO
        // ================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trabajo de grado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Es un proceso formativo de investigación, reflexión o sistematización de experiencias, no solo un requisito para graduarte. Reglamento del Programa de Psicología, Resolución 60-2019.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ================================================================
        // PARA INICIAR (con el avance real del estudiante)
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Para iniciar",
            icono = Icons.Default.Checklist,
            inicialmenteAbierta = true
        ) {
            Texto(
                "Para empezar debes haber aprobado estas asignaturas. Con eso puedes matricular el Seminario de Trabajo de Grado I y, después, los demás niveles."
            )

            val requisitos = programa.requisitosInicioTrabajoGrado()
            requisitos.forEach { (nombre, aprobada) ->
                FilaRequisito(nombre = nombre, aprobada = aprobada)
            }

            val faltan = requisitos.count { !it.second }
            Texto(
                if (faltan == 0) {
                    "Ya cumples todos los requisitos de inicio."
                } else {
                    "Te falta${if (faltan == 1) "" else "n"} $faltan por aprobar."
                },
                negrita = true
            )

            Punto(
                "Equipo",
                "Lo óptimo es trabajar entre dos o tres estudiantes. Si necesitas hacerlo de forma individual, el Director y tú deben pedir un aval al Coordinador del Área Académica del Director, que se entrega a la Dirección del Programa."
            )
        }

        // ================================================================
        // RUTA POR SEMESTRES
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Ruta por semestres",
            icono = Icons.Default.Timeline,
            inicialmenteAbierta = true
        ) {
            val hitos = listOf(
                Hito(
                    periodo = "Semestre VII",
                    titulo = "Seminario de Trabajo de Grado I",
                    descripcion = "Identificas temas de interés y formas de abordarlos, en línea con las líneas de investigación y programas de prácticas e intervención del Instituto. Termina con el Anteproyecto, avalado por el Director de Trabajo de Grado."
                ),
                Hito(
                    periodo = "Semestre VIII",
                    titulo = "Seminario de Trabajo de Grado II",
                    descripcion = "Elaboras el Proyecto de Trabajo de Grado con el acompañamiento de un Director. Termina con la entrega del proyecto a la Dirección del Programa y su sustentación ante dos jurados lectores."
                ),
                Hito(
                    periodo = "Semestre IX",
                    titulo = "Trabajo de Grado I",
                    descripcion = "Ejecutas el proyecto según el tipo de trabajo y la modalidad elegida. Entregas el documento “Avances del Trabajo de Grado” al Director, que emite el concepto: Aprobado o Desaprobado."
                ),
                Hito(
                    periodo = "Semestre X",
                    titulo = "Trabajo de Grado II",
                    descripcion = "Culminas el trabajo con la entrega del “Informe final de Trabajo de Grado” a los dos jurados, para su posterior sustentación.",
                )
            )
            hitos.forEachIndexed { indice, hito ->
                TarjetaHito(hito = hito, esUltimo = indice == hitos.lastIndex)
            }
        }

        // ================================================================
        // PLAZOS Y CICLO
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Plazos y ciclo",
            icono = Icons.Default.Schedule
        ) {
            Punto(
                "Plazo",
                "Tienes hasta dos (2) semestres para concluir. Se cuentan desde tu primera matrícula de Trabajo de Grado I y terminan con el semestre en que matriculas Trabajo de Grado II."
            )
            Punto(
                "Cuándo inicia el ciclo",
                "Una vez el Proyecto fue sustentado y aprobado por los dos jurados lectores, es decir, con la matrícula de Trabajo de Grado I."
            )
            Punto(
                "Continuación de Trabajo de Grado",
                "Hasta dos semestres adicionales, que el Comité de Programa autoriza solo en casos excepcionales y bien justificados. Debes presentar con anticipación una comunicación con tu interés de prolongar la entrega, las causas, un plan de trabajo para finalizar y un acta de compromiso, todo avalado por tu Director."
            )
            Punto(
                "Si no sustentas a tiempo",
                "Si terminas el primer ciclo de dos años y el trabajo no se sustentó en las fechas del calendario, tu calificación será No Cumplió (N.C.). Puedes solicitar reingreso y el Comité de Programa define qué asignaturas adicionales matricular."
            )
            Punto(
                "Segundo vencimiento",
                "Si por segunda vez se vence el plazo máximo de dos años sin aprobar el trabajo, pierdes el derecho a optar por el título y se registra “No aprobó”."
            )
        }

        // ================================================================
        // EVALUACIÓN
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Cómo se evalúa",
            icono = Icons.Default.VerifiedUser
        ) {
            Punto(
                "Seminario de Trabajo de Grado I",
                "Se califica Cumplió o No cumplió, según tus avances en la revisión de literatura y la precisión de tu problema de investigación. El Anteproyecto incluye el planteamiento del problema y la justificación, y de él sale la modalidad final de tu trabajo. Para matricular el Seminario II necesitas aprobar el I y presentar el Anteproyecto avalado por un docente del Instituto que acepte dirigirte."
            )
            Punto(
                "Sustentación del Proyecto (Seminario II)",
                "Ante dos jurados lectores, con estos resultados posibles:"
            )
            Opciones(
                "Aprobado" to "Cumples todos los requisitos conceptuales, metodológicos y de forma.",
                "Aprobado con recomendaciones" to "La formulación es clara y rigurosa, pero faltan uno o varios requisitos. Tienes máximo un (1) mes calendario para cumplir las recomendaciones; si no lo haces, la Dirección puede registrar No Cumplió.",
                "Aplazado para nueva sustentación" to "No se demostró claridad en el problema, el procedimiento o la presentación. Tienes un (1) mes calendario no prorrogable para sustentar de nuevo con el Director y los jurados.",
                "Rechazado" to "Hay errores sustanciales en la formulación y no se logran las condiciones requeridas."
            )
            Punto(
                "Trabajo de Grado I",
                "Para iniciar este nivel debe estar aprobado el Proyecto y matriculado Trabajo de Grado I. Debes lograr avances sustanciales en la implementación: recolección, sistematización y avances preliminares de análisis. El Director emite un concepto de cumplimiento o incumplimiento del plan de trabajo."
            )
            Punto(
                "Trabajo de Grado II",
                "Requiere aprobar Trabajo de Grado I. Presentas el informe final, con el aval de tu Director, para que lo lean los jurados y se programe la sustentación. Las opciones de evaluación son las mismas: Aprobado, Aprobado con recomendaciones (un mes calendario) y Aplazado para nueva sustentación (un mes no prorrogable)."
            )
            Texto(
                "El acta original, firmada por el Director y los jurados, se entrega a la Dirección del Programa en las fechas institucionales para registrar Cumplió o No cumplió en la Oficina de Registro y Control."
            )
        }

        // ================================================================
        // SUSTENTACIÓN
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Sustentación",
            icono = Icons.Default.Slideshow
        ) {
            Punto(
                "Formato",
                "Es una sesión pública con los estudiantes, el Director y los jurados lectores. Tienes de 20 a 25 minutos para presentar; luego los jurados hacen sus observaciones, se abren 10 minutos de participación con preguntas y comentarios, y por último los jurados presentan su evaluación."
            )
            Punto(
                "Asistencia",
                "Es necesario que estén todos los integrantes: el Director, los jurados lectores y todos los estudiantes autores. Si alguien falta sin un motivo justificado, se deja constancia y no se puede llevar a cabo la sustentación."
            )
            Punto(
                "Excepciones",
                "En casos excepcionales y sustentados, si un jurado no puede asistir, se puede aceptar su concepto por escrito, firmado y enviado antes al Director con copia a la Dirección del Programa. También se pueden usar recursos virtuales, como videoconferencia."
            )
            Texto("Las sesiones se realizan en espacios del Instituto de Psicología y se protocolizan con las actas firmadas.")
        }

        // ================================================================
        // DIRECTOR Y JURADOS
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Director y jurados",
            icono = Icons.Default.Groups
        ) {
            Punto(
                "Cómo se designa al Director",
                "Parte de un acuerdo entre un docente y los estudiantes, que además considera el aval de las áreas académicas relacionadas. La Dirección del Programa lo oficializa. Puede ser docente del Instituto o de la Universidad; si es de otra área de la Universidad o externo, se pide autorización al Comité de Programa."
            )
            Punto(
                "Qué hace el Director",
                "Orienta todo el proceso hasta la sustentación del informe final. Programa encuentros semanales o quincenales de al menos una hora, avala el anteproyecto, hace seguimiento a los requisitos, avala la solicitud de jurados, emite la nota de Trabajo de Grado II y asesora las observaciones de los jurados. No está para enseñar metodología ni redacción."
            )
            Punto(
                "Jurados lectores",
                "Se nombran dos por cada evaluación (proyecto o informe final), sugeridos por el Director o los estudiantes y designados por la Dirección del Programa. Pueden ser profesores del Instituto, de la Universidad o profesionales con trayectoria en investigación o conocimiento del tema. Si no aceptan, responden por escrito en los ocho días siguientes a la notificación."
            )
            Punto(
                "Si no hay consenso",
                "Si los jurados y el Director no llegan a un consenso en el concepto, el Director puede solicitar por escrito al Comité de Programa el nombramiento de un tercer jurado. Esa solicitud queda registrada en el acta de sustentación."
            )
        }

        // ================================================================
        // MODALIDADES
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Modalidades",
            icono = Icons.Default.Category
        ) {
            Opciones(
                "Estudios empíricos" to "Pueden usar datos cuantitativos, cualitativos o mixtos, recolectados por ti o ya existentes en bases de datos u otros medios. Duran no más de 12 meses calendario, sin extenderse más de 18, y deben estar vinculados a un proyecto de investigación en curso de un grupo del Instituto de Psicología.",
                "Monografía" to "Trabajo sistemático sobre un tema conceptual específico, completo en sí mismo. No es un resumen de puntos de vista: el énfasis está en la unidad conceptual, ya sea siguiendo un concepto en un autor o contrastando varios.",
                "Revisión (actualizada) de la literatura" to "Exploración y análisis de literatura empírica o teórica sobre un tema relevante de la Psicología, con un análisis de un conjunto de textos. El producto puede ser una revisión narrativa o sistemática, cualitativa o cuantitativa.",
                "Sistematización de práctica" to "Teorizar una experiencia concreta y continua en el tiempo, articulando teoría e investigación a partir de la práctica profesional. Parte de una pregunta de investigación y va más allá de describir lo sucedido.",
                "Publicación de artículo en revista indexada" to "Revistas de Publindex (Colciencias) desde la categoría C. El artículo debe estar publicado o aceptado al postular el documento para evaluar Trabajo de Grado II, hacer parte de un proceso registrado como Proyecto de Trabajo de Grado y contemplar la autoría del docente que dirigió el proyecto en segundo o tercer lugar. No se somete a evaluación interna del Programa."
            )
        }

        // ================================================================
        // CASOS EXCEPCIONALES, ÉTICA Y ESTILO
        // ================================================================
        SeccionTrabajoGrado(
            titulo = "Ética, autoría y estilo",
            icono = Icons.Default.Gavel
        ) {
            Punto(
                "Homologación en casos excepcionales",
                "El Comité de Programa puede estudiar equivalencias, por ejemplo con certificación de pertenecer a un grupo o institución de investigación reconocida, o con publicaciones indexadas de proyectos realizados con tu participación. En ambos casos se sustenta de forma presencial."
            )
            Punto(
                "Ética",
                "El trabajo debe respetar la Ley 1090 de 2006 (ejercicio de la Psicología), la Resolución 8430 de 1993 (normas para la investigación en salud) y los principios de la Declaración de Helsinki."
            )
            Punto(
                "Derechos de autor",
                "Se rige por el Acuerdo 023 de 2003 (Estatuto sobre Propiedad Intelectual de la Universidad del Valle). Si alguien se retira de un proyecto, se envía una carta al Comité de Programa firmada por los integrantes y el Director; quien se retira de un proyecto sin terminar debe reiniciar el proceso."
            )
            Punto(
                "Estilo",
                "Para el Proyecto y el Informe Final se recomienda el Manual de estilo y redacción de la APA en su versión más actualizada, aunque se pueden aplicar de forma sistemática otros sistemas de normas."
            )
        }

        // ================================================================
        // AVISO
        // ================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Resumen informativo basado en la Resolución 60-2019 del Consejo del Instituto de Psicología (9 de septiembre de 2019). Ante cualquier duda, el texto oficial y la Dirección del Programa tienen la última palabra.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ========================================================================
// COMPONENTES
// ========================================================================

/** Tarjeta desplegable: título con ícono y contenido que se abre al tocar. */
@Composable
private fun SeccionTrabajoGrado(
    titulo: String,
    icono: ImageVector,
    inicialmenteAbierta: Boolean = false,
    contenido: @Composable () -> Unit
) {
    var abierta by remember { mutableStateOf(inicialmenteAbierta) }
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { abierta = !abierta }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = scheme.primary
                )
                Text(
                    text = titulo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Icon(
                    imageVector = if (abierta) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (abierta) "Contraer" else "Expandir",
                    tint = scheme.primary
                )
            }

            AnimatedVisibility(visible = abierta) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    contenido()
                }
            }
        }
    }
}

@Composable
private fun Texto(texto: String, negrita: Boolean = false) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** Párrafo con un título corto en negrilla al inicio. */
@Composable
private fun Punto(titulo: String, texto: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(titulo)
                append(". ")
            }
            append(texto)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** Lista de opciones con nombre en negrilla y explicación debajo. */
@Composable
private fun Opciones(vararg opciones: Pair<String, String>) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { (nombre, descripcion) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Fila de requisito: verde con check si está aprobada, rojo con X si no. */
@Composable
private fun FilaRequisito(nombre: String, aprobada: Boolean) {
    val color = colorEstado(aprobada)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (aprobada) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (aprobada) "Aprobada" else "Pendiente",
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = nombre,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
