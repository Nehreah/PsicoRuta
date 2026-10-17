package com.renea.psicologiauv.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ========================================================================
// REGLAMENTO DEL TRABAJO DE GRADO
// ========================================================================
// Detalle del reglamento (Resolución 60-2019 del Consejo del Instituto de
// Psicología). Se abre desde el botón "Ver reglamento completo" de
// PantallaTrabajoGrado. Reutiliza los componentes SeccionTrabajoGrado,
// Texto, Punto y Opciones definidos en PantallaTrabajoGrado.kt.

@Composable
fun PantallaReglamentoTrabajoGrado(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ================================================================
        // ENCABEZADO (solo el título)
        // ================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = colorTinta(),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Reglamento",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTinta()
                )
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
    }
}
