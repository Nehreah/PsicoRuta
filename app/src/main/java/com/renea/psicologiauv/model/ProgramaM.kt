package com.renea.psicologiauv.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

/**
 * Resultado de evaluar el avance de un estudiante en una línea de
 * profundización profesional.
 *
 * @property linea Nombre de la línea. Usa "Clínica/NeuroClínica" para
 *   agrupar esas dos, ya que son mutuamente excluyentes entre sí, pero
 *   cuentan como una sola línea.
 * @property opcion Cuál de las opciones de la línea fue la que se
 *   aprobó ("Clínica" o "NeuroClínica" en esa línea; igual a [linea] en
 *   las demás líneas, que no tienen opciones).
 * @property nivel Nivel más bajo que el estudiante ya aprobó en esa
 *   línea (I = 1, II = 2, III = 3).
 * @property asignatura Nombre de la asignatura aprobada que sustenta el
 *   nivel encontrado.
 */
data class AvanceLineaProfesional(
    val linea: String,
    val opcion: String,
    val nivel: Int,
    val asignatura: String
) {
    /**
     * Texto legible para mostrar en la UI: "Nivel I", "Nivel II", "Nivel III"
     * o "Nivel N" para valores fuera de rango.
     */
    fun textoNivel(): String = when (nivel) {
        1 -> "Nivel I"
        2 -> "Nivel II"
        3 -> "Nivel III"
        else -> "Nivel $nivel"
    }
}

/**
 * Datos derivados del estado de los idiomas del estudiante.
 * Se obtiene mediante [ProgramaM.datosIdiomas].
 */
data class DatosIdiomas(
    val idiomasICandidatas: List<Asignatura>,
    val idiomasIICandidatas: List<Asignatura>,
    val existeIdiomasI: Boolean,
    val existeIdiomasII: Boolean,
    val idiomasIAprobado: Boolean,
    val idiomasIIAprobado: Boolean
)

/**
 * Nombres de las asignaturas de idioma que cuentan para el requisito de
 * grado. La Universidad puede tener varios códigos distintos para un mismo
 * nivel (uno por cada idioma que el estudiante elija: inglés, francés,
 * etc.), así que en vez de mantener una lista fija de códigos, el
 * requisito se evalúa por el NOMBRE de la asignatura ("Idiomas I",
 * "Idiomas II"): basta con que exista, entre todas las asignaturas del
 * estudiante, alguna aprobada con ese nombre exacto, sin importar cuál
 * código tenga.
 */
val NOMBRES_IDIOMAS: Set<String> = setOf(
    "Idiomas I",
    "Idiomas II"
)


/** Consejo contextual que puede mostrar la interfaz del pensum. */
data class ConsejoPensum(
    val titulo: String,
    val texto: String
)

data class ProgramaM(
    val nombre: String = "Psicología",
    val codigo: Int = 3461,
    val facultad: String = "Psicología",
    var estudiante: Estudiante? = null,
    val ano: Int = 2026,
    val creditosFDI: Int = 74,
    val creditosFPCA: Int = 39,
    val creditosFII: Int = 55,
    val creditosCarrera: Int = 168,
    val creditosProfesionales: Int = 27,
    val creditosLinea: Int = 9,
    val creditosComplementarios: Int = 5
) {

    fun cambiarEstudiante(nuevoEstudiante: Estudiante): ProgramaM =
        copy(estudiante = nuevoEstudiante)

    // ========================================================================
    // PENSUM — ESTADO ACADÉMICO DERIVADO
    // ========================================================================

    /**
     * Créditos que pueden quedar pendientes para considerar completado cada
     * semestre. En los semestres 1-5, 9 y 10 no hay tolerancia; en 6 y 7
     * se permiten hasta 6 créditos pendientes y en 8 hasta 9.
     */
    private fun toleranciaCreditosSemestre(semestre: Int): Int = when (semestre) {
        6 -> 6
        7, 8 -> 9
        else -> 0
    }

    /**
     * Asignaturas que deben estar aprobadas obligatoriamente en los
     * semestres que tienen requisitos críticos, independientemente de la
     * tolerancia de créditos permitida en esos semestres.
     */
    private fun nombresAsignaturasObligatoriasSemestre(semestre: Int): Set<String> = when (semestre) {
        6 -> setOf(
            "Ética del Ejercicio Profesional"
        )
        7 -> setOf(
            "Psicopatología",
            "Práctica de Fundamentación Profesional I",
            "Seminario de Trabajo de Grado I"
        )
        8 -> setOf(
            "Evaluación y Diagnóstico",
            "Seminario de Trabajo de Grado II",
            "Práctica de Fundamentación Profesional II"
        )
        else -> emptySet()
    }

    /**
     * Normaliza nombres para que pequeñas diferencias de mayúsculas, tildes
     * o espacios no impidan reconocer una asignatura obligatoria.
     */
    private fun normalizarNombreAsignatura(nombre: String): String =
        java.text.Normalizer.normalize(nombre.trim(), java.text.Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace(Regex("\\s+"), " ")
            .lowercase()

    /**
     * Comprueba que todas las asignaturas críticas del semestre estén
     * aprobadas. La tolerancia de créditos NO aplica a estas asignaturas.
     */
    private fun asignaturasObligatoriasAprobadas(semestre: Int): Boolean {
        val obligatorias = nombresAsignaturasObligatoriasSemestre(semestre)
            .map(::normalizarNombreAsignatura)
            .toSet()

        if (obligatorias.isEmpty()) return true

        val materiasDelSemestre = asignaturasPorSemestre()[semestre].orEmpty()
        return obligatorias.all { nombreObligatorio ->
            materiasDelSemestre.any { materia ->
                normalizarNombreAsignatura(materia.nombre) == nombreObligatorio && materia.aprobo()
            }
        }
    }

    /** Asignaturas del plan agrupadas por semestre. */
    fun asignaturasPorSemestre(): Map<Int, List<Asignatura>> =
        estudiante?.asignaturas
            .orEmpty()
            .filter { it.semestre in 1..11 }
            .groupBy { it.semestre }

    /** Semestres que realmente existen en los datos del estudiante. */
    fun semestresDelPlan(): List<Int> {
        val porSemestre = asignaturasPorSemestre()
        return (1..10).toList() +
            if (porSemestre[11].orEmpty().isNotEmpty()) listOf(11) else emptyList()
    }

    /**
     * Asignaturas que realmente deben contar para determinar si un semestre
     * está completado. Las electivas complementarias se excluyen; las
     * electivas profesionales sí cuentan.
     */
    private fun asignaturasContablesDelSemestre(semestre: Int): List<Asignatura> =
        asignaturasPorSemestre()[semestre]
            .orEmpty()
            .filterNot {
                it.componente.contains("Electiva complementaria", ignoreCase = true)
            }

    /**
     * Créditos pendientes de las asignaturas que sí cuentan para el estado
     * del semestre. Una asignatura pendiente aporta todos sus créditos.
     */
    fun creditosPendientesSemestre(semestre: Int): Int =
        asignaturasContablesDelSemestre(semestre)
            .filterNot { it.aprobo() }
            .sumOf { it.creditos }

    /**
     * Verdadero cuando el semestre cumple la regla académica definida para
     * el pensum:
     *
     * - Semestres 1-5, 9 y 10: deben estar aprobadas todas las asignaturas
     *   asignadas que cuentan (las electivas complementarias no cuentan).
     * - Semestres 6 y 7: se permiten hasta 6 créditos pendientes.
     * - Semestre 8: se permiten hasta 9 créditos pendientes.
     */
    fun semestreCompletado(semestre: Int): Boolean {
        val materias = asignaturasContablesDelSemestre(semestre)
        if (materias.isEmpty()) return false

        // Primero se deben cumplir siempre las asignaturas críticas de los
        // semestres 6, 7 y 8. La tolerancia de créditos no puede usarse para
        // omitir ninguna de ellas.
        if (!asignaturasObligatoriasAprobadas(semestre)) return false

        val creditosPendientes = materias
            .filterNot { it.aprobo() }
            .sumOf { it.creditos }

        return creditosPendientes <= toleranciaCreditosSemestre(semestre)
    }

    /**
     * Semestre en el que se encuentra el estudiante: el inmediatamente
     * siguiente al ÚLTIMO semestre completado. Si no ha completado ninguno,
     * está en el semestre 1. Nunca supera el último semestre del plan (10, o
     * 11 si el estudiante tiene materias en ese semestre).
     *
     * Con este valor se abre la pantalla de Pensum y se elige el consejo que
     * se muestra en el inicio.
     */
    fun semestreEnCurso(): Int {
        val semestres = semestresDelPlan()
        val ultimoCompletado = semestres.lastOrNull { semestreCompletado(it) }
            ?: return semestres.firstOrNull() ?: 1
        return (ultimoCompletado + 1).coerceAtMost(semestres.lastOrNull() ?: ultimoCompletado)
    }

    /**
     * Requisitos para iniciar el proceso de Trabajo de Grado (Resolución
     * 60-2019, Art. 5): haber aprobado cinco asignaturas previas. Se
     * identifican por palabras clave del nombre, sin importar mayúsculas ni
     * tildes, para tolerar pequeñas diferencias en cómo vienen nombradas.
     * Devuelve pares (nombre a mostrar, aprobada).
     */
    fun requisitosInicioTrabajoGrado(): List<Pair<String, Boolean>> {
        val asignaturas = estudiante?.asignaturas.orEmpty()

        fun normalizar(texto: String): String =
            java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
                .lowercase()

        fun aprobada(vararg claves: String): Boolean =
            asignaturas.any { asignatura ->
                val nombre = normalizar(asignatura.nombre)
                claves.all { nombre.contains(it) } && asignatura.aprobo()
            }

        return listOf(
            "Metodología de la Investigación I" to aprobada("metodolog", "cualitativ"),
            "Metodología de la Investigación II" to aprobada("metodolog", "cuantitativ"),
            "Estrategias de Análisis de Datos Cualitativos" to aprobada("estrategias", "datos"),
            "Estadística" to aprobada("estadistic"),
            "Diseño de Instrumentos de Medición en Psicología" to aprobada("instrumentos", "medicion")
        )
    }

    /** Consejo contextual para mostrar debajo del héroe del semestre. */
    fun consejoPensum(semestre: Int): ConsejoPensum = when (semestre) {
        1 -> ConsejoPensum(
            titulo = "Semestre 1:",
            texto = "Si no obtuviste buen puntaje en inglés durante el icfes debes cursar 2 niveles de idioma extranjero: Inglés, alemán, etc."
        )
        2 -> ConsejoPensum(
            titulo = "Semestre 2:",
            texto = "Lo mejor es ver Deporte Formativo y constitución política en tus primeros semestres."
        )
        3 -> ConsejoPensum(
            titulo = "Semestre 3:",
            texto = "Durante tus primeros semestres, matricula 2 electivas complementarias como baile, teatro, investigación, etc."
        )
        4 -> ConsejoPensum(
            titulo = "Semestre 4:",
            texto = "Los semilleros y grupos de investigación son una buena opción para ampliar tu formación profesional"
        )
        5 -> ConsejoPensum(
            titulo = "Semestre 5:",
            texto = "Matricula 2 electivas profesionales, una de ellas debe ser Problemas del Individuo en la Sociedad."
        )
        6 -> ConsejoPensum(
            titulo = "Semestre 6:",
            texto = "Lo mejor es que aproveches este semestre para ver el primer nivel de las 4 líneas profesionales."
        )
        7 -> ConsejoPensum(
            titulo = "Semestre 7:",
            texto = "Continua tu progreso en la línea que deseas hacer practicas profesionales y matricula al menos otras dos líneas."
        )
        8 -> ConsejoPensum(
            titulo = "Semestre 8:",
            texto = "Termina tu línea de prácticas y revisa tus requisitos faltantes para aplicar a las prácticas profesionales."
        )
        9 -> ConsejoPensum(
            titulo = "Semestre 9:",
            texto = "Elige un director de trabajo de grado: Inicia tu ciclo de trabajo de grado el cual dura mínimo un año y máximo 2-3, en caso de interrumpir un semestre el ciclo se reinicia. <Link a la nueva pagina trabajo de grado.>."
        )
        10 -> ConsejoPensum(
            titulo = "Semestre 10:",
            texto = "No lo olvides, cancelar trabajo de grado un semestre obliga a repetir el ciclo desde el principio."
        )
        else -> ConsejoPensum(
            titulo = "Semestre $semestre:",
            texto = "Si pierdes continuación de trabajo de grado I puedes matricular continuación de trabajo de grado II. Si interrumpes el ciclo, se reinicia tu proceso"
        )
    }

    // ========================================================================
    // PROMEDIOS
    // ========================================================================

    fun promedioSemestre(semestre: Int): Float {
        // notaEspecial == null: las materias aprobadas por marca especial
        // (CU, EX, AP) cuentan como aprobadas para créditos y requisitos,
        // pero no tienen una nota numérica real, así que no deben entrar en
        // el promedio ponderado.
        val lista =
            estudiante?.asignaturas
                ?.filter { it.semestre == semestre && it.aprobo() && it.notaEspecial == null }
                .orEmpty()
        val sumaCreditos = lista.sumOf { it.creditos }
        if (sumaCreditos == 0) return 0f
        val sumaNotas = lista.sumOf { (it.creditos * it.nota).toDouble() }
        if (sumaNotas.toFloat() == 0f) return 0f
        return (sumaNotas / sumaCreditos).toFloat()
    }

    fun promedioGeneral(): Float {
        // Mismo criterio que promedioSemestre: se excluyen las materias
        // aprobadas por marca especial (CU, EX, AP), que no tienen nota
        // numérica real.
        val lista = estudiante?.asignaturas?.filter { it.aprobo() && it.notaEspecial == null }.orEmpty()
        val sumaCreditos = lista.sumOf { it.creditos }
        if (sumaCreditos == 0) return 0f
        val sumaNotas = lista.sumOf { (it.nota * it.creditos).toDouble() }
        return (sumaNotas / sumaCreditos).toFloat()
    }


    // ========================================================================
    // CRÉDITOS POR ÁREA
    // ========================================================================

    /**
     * Devuelve la asignatura complementaria asociada al crédito visual
     * seleccionado en Pensum. Si una asignatura aporta varios créditos,
     * ocupa varios segmentos y todos abren la misma asignatura.
     * El código 402226M es un caso especial: si está aprobada completa
     * los cinco créditos, por lo que cualquiera de los cinco segmentos
     * apunta a ella.
     */
    fun asignaturaComplementariaParaCredito(indiceCredito: Int): Asignatura? {
        if (indiceCredito !in 0 until creditosComplementarios) return null

        val asigs = estudiante?.asignaturas.orEmpty()
        val especial = asigs.find { it.codigo.trim() == "402226M" }
        if (especial?.aprobo() == true) return especial

        var acumulado = 0
        asigs
            .filter {
                (it.area == "Complementario" || it.complementaria) &&
                        it.codigo.trim() != "402226M" &&
                        it.aprobo()
            }
            .forEach { materia ->
                val siguiente = acumulado + materia.creditos
                if (indiceCredito in acumulado until siguiente) return materia
                acumulado = siguiente
            }

        return null
    }

    fun creditosComplementariosAprobados(): Int {
        val asigs = estudiante?.asignaturas.orEmpty()
        val materia402226M = asigs.find { it.codigo.trim() == "402226M" }
        val manualesAprobados = asigs
            .filter { (it.area == "Complementario" || it.complementaria) && it.codigo.trim() != "402226M" && it.aprobo() }
            .sumOf { it.creditos }

        return when {
            materia402226M != null && materia402226M.aprobo() -> creditosComplementarios
            else -> manualesAprobados.coerceAtMost(creditosComplementarios)
        }
    }

    fun creditosFDIAprobados(): Int =
        estudiante?.asignaturas
            ?.filter { it.area == "Fundamentación Disciplinar e Interdisciplinar" && it.aprobo() }
            .orEmpty()
            .sumOf { it.creditos }
            .coerceAtMost(creditosFDI)

    fun creditosFPCAAprobados(): Int =
        estudiante?.asignaturas
            ?.filter { it.area == "Formación Profesional en Campos de Aplicación" && it.aprobo() }
            .orEmpty()
            .sumOf { it.creditos }
            .coerceAtMost(creditosFPCA)

    fun creditosFIIAprobados(): Int {
        val regularFII = estudiante?.asignaturas
            ?.filter {
                it.area == "Formación en investigación e Intervención" &&
                        it.codigo.trim() != "402226M" &&
                        it.aprobo()
            }
            .orEmpty()
            .sumOf { it.creditos }

        val compCreditos = creditosComplementariosAprobados()
        return (regularFII + compCreditos).coerceAtMost(creditosFII)
    }

    /** Total de créditos que cuentan hacia los 168 de carrera. */
    fun creditosQueSirvenParaCarrera(): Int =
        (creditosFDIAprobados() + creditosFPCAAprobados() + creditosFIIAprobados())
            .coerceAtMost(creditosCarrera)

    /** Créditos que le faltan al estudiante para completar la carrera. */
    fun creditosFaltantesCarrera(): Int =
        (creditosCarrera - creditosQueSirvenParaCarrera()).coerceAtLeast(0)

    // ========================================================================
    // AVANCE
    // ========================================================================

    fun avanceAreas(): Map<String, Float> {
        val avanceAreas = mutableMapOf<String, Float>()
        avanceAreas["Fundamentación Disciplinar e Interdisciplinar"] =
            (creditosFDIAprobados().toFloat() / creditosFDI.toFloat() * 100f).coerceIn(0f, 100f)
        avanceAreas["Formación Profesional en Campos de Aplicación"] =
            (creditosFPCAAprobados().toFloat() / creditosFPCA.toFloat() * 100f).coerceIn(0f, 100f)
        avanceAreas["Formación en investigación e Intervención"] =
            (creditosFIIAprobados().toFloat() / creditosFII.toFloat() * 100f).coerceIn(0f, 100f)
        return avanceAreas
    }

    /** Porcentaje de avance total de la carrera (0–100). */
    fun avanceCarrera(): Float {
        return (creditosQueSirvenParaCarrera().toFloat() / creditosCarrera.toFloat() * 100f)
            .coerceIn(0f, 100f)
    }


    // ========================================================================
    // ASIGNATURAS — CONSULTAS GENÉRICAS
    // ========================================================================

    /**
     * Lista de áreas distintas que tiene el estudiante, ordenadas alfabéticamente.
     * Útil para construir filtros en la UI del pensum.
     */
    fun areasDelEstudiante(): List<String> =
        estudiante?.asignaturas
            .orEmpty()
            .map { it.area }
            .distinct()
            .sorted()

    /**
     * Asignaturas del estudiante filtradas por área y/o estado de aprobación.
     * - [filtroArea] null = sin filtro de área.
     * - [soloAprobadas] null = sin filtro; true = solo aprobadas; false = solo no aprobadas.
     */
    fun asignaturasFiltradas(
        filtroArea: String? = null,
        soloAprobadas: Boolean? = null
    ): List<Asignatura> =
        estudiante?.asignaturas.orEmpty().filter { materia ->
            val cumpleArea = filtroArea == null || materia.area == filtroArea
            val cumpleEstado = soloAprobadas == null || materia.aprobo() == soloAprobadas
            cumpleArea && cumpleEstado
        }


    // ========================================================================
    // IDIOMAS
    // ========================================================================

    /**
     * Devuelve toda la información derivada del estado de los idiomas del
     * estudiante. Centraliza la lógica que antes vivía en PantallaAvance.
     */
    fun datosIdiomas(): DatosIdiomas {
        val asignaturas = estudiante?.asignaturas.orEmpty()
        val candidatasI = asignaturas.filter { it.nombre == "Idiomas I" }
        val candidatasII = asignaturas.filter { it.nombre == "Idiomas II" }
        return DatosIdiomas(
            idiomasICandidatas = candidatasI,
            idiomasIICandidatas = candidatasII,
            existeIdiomasI = candidatasI.isNotEmpty(),
            existeIdiomasII = candidatasII.isNotEmpty(),
            idiomasIAprobado = candidatasI.any { it.aprobo() },
            idiomasIIAprobado = candidatasII.any { it.aprobo() }
        )
    }

    /**
     * De una lista de candidatas para un mismo nivel de idioma, elige la más
     * relevante para mostrar en la UI: primero la aprobada, luego la que tiene
     * alguna nota, luego cualquiera.
     */
    fun materiaIdiomaActiva(candidatas: List<Asignatura>): Asignatura? =
        candidatas.firstOrNull { it.aprobo() }
            ?: candidatas.firstOrNull { it.nota > 0f || it.notaEspecial != null }
            ?: candidatas.firstOrNull()

    fun cursosInglesAprobados(): Set<String> {
        val nombresAprobados = estudiante?.asignaturas
            ?.filter { it.aprobo() }
            ?.map { it.nombre }
            ?.toSet()
            .orEmpty()
        return NOMBRES_IDIOMAS.filter { it in nombresAprobados }.toSet()
    }

    // ========================================================================
    // LÍNEAS PROFESIONALES
    // ========================================================================

    /**
     * Créditos aprobados en una línea profesional específica.
     */
    fun creditosLineaProfesional(linea: String): Int =
        estudiante?.asignaturas.orEmpty()
            .filter { it.lineaProfesional == linea && it.aprobo() }
            .sumOf { it.creditos }

    /**
     * Asignaturas que pertenecen a una línea profesional específica,
     * independientemente de si están aprobadas o no.
     */
    fun asignaturasDeLinea(linea: String?): List<Asignatura> =
        if (linea == null) emptyList()
        else estudiante?.asignaturas.orEmpty().filter { it.lineaProfesional == linea }

    fun creditosDeProfesionalizacion(): Int {
        val suma = estudiante?.asignaturas
            .orEmpty()
            .filter { it.lineaProfesional != "No" && it.aprobo() }
            .sumOf { it.creditos }
        return suma.coerceAtLeast(0)
    }

    /**
     * Líneas profesionales (Social, Organizacional, Educativa,
     * Clínica/NeuroClínica) en las que el estudiante NO ha aprobado
     * todavía ninguna asignatura.
     */
    fun lineasProfesionalesFaltantes(): List<String> {
        val estudiante = estudiante ?: return listOf("Social", "Organizacional", "Educativa", "Clínica/NeuroClínica")
        val gruposLinea = listOf("Social", "Organizacional", "Educativa", "Clínica/NeuroClínica")

        val lineasCursadas = estudiante.asignaturas
            .filter { it.aprobo() }
            .map { it.lineaProfesional }
            .distinct()
            .toMutableSet()

        // Clínica y NeuroClínica cuentan como una sola línea
        if (lineasCursadas.contains("Clínica") || lineasCursadas.contains("NeuroClínica")) {
            lineasCursadas.remove("Clínica")
            lineasCursadas.remove("NeuroClínica")
            lineasCursadas.add("Clínica/NeuroClínica")
        }

        return gruposLinea.filter { it !in lineasCursadas }
    }

    /** Verdadero si el estudiante cursó al menos un nivel de cada línea. */
    fun todasLasLineasCursadas(): Boolean = lineasProfesionalesFaltantes().isEmpty()

    /**
     * Evalúa, línea por línea de profundización profesional, si el
     * estudiante ya aprobó al menos un nivel (I, II o III).
     */
    fun avanceLineasProfundizacion(): Map<String, AvanceLineaProfesional> {
        val asignaturas = estudiante?.asignaturas.orEmpty()
        val lineas = listOf(
            "Clínica/NeuroClínica" to listOf("Clínica", "NeuroClínica"),
            "Social" to listOf("Social"),
            "Educativa" to listOf("Educativa"),
            "Organizacional" to listOf("Organizacional")
        )

        val resultado = mutableMapOf<String, AvanceLineaProfesional>()

        for ((nombreLinea, opciones) in lineas) {
            var encontrada = false
            for (nivel in 1..3) {
                if (encontrada) break
                for (opcion in opciones) {
                    val materia = asignaturas.firstOrNull {
                        it.lineaProfesional == opcion &&
                                it.aprobo() &&
                                nivelDeAsignatura(it.nombre) == nivel
                    }
                    if (materia != null) {
                        resultado[nombreLinea] = AvanceLineaProfesional(
                            linea = nombreLinea,
                            opcion = opcion,
                            nivel = nivel,
                            asignatura = materia.nombre
                        )
                        encontrada = true
                        break
                    }
                }
            }
        }

        return resultado
    }

    /** Extrae el nivel (I=1, II=2, III=3) del nombre de la asignatura. */
    private fun nivelDeAsignatura(nombre: String): Int? {
        val texto = nombre.trim()
        return when {
            texto.endsWith("III") -> 3
            texto.endsWith("II") -> 2
            texto.endsWith("I") -> 1
            else -> null
        }
    }

    // ========================================================================
    // REQUISITOS PARA PRÁCTICAS
    // ========================================================================


    fun requisitosPracticasLinea(): Boolean {
        val sumaLinea = estudiante?.asignaturas
            ?.filter { (it.lineaProfesional == estudiante?.linea) && it.aprobo() }
            .orEmpty()
            .sumOf { it.creditos }
        return sumaLinea >= creditosLinea
    }

    fun requisitosPracticasEtica(): Boolean =
        estudiante?.asignaturas.orEmpty()
            .any { it.codigo == "402203M" && it.aprobo() }

    fun requisitosPracticasDiagnostico(): Boolean =
        estudiante?.asignaturas.orEmpty()
            .any { it.codigo == "402204M" && it.aprobo() }

    fun requisitosPracticasFundamentacionI(): Boolean =
        estudiante?.asignaturas.orEmpty()
            .any { it.codigo == "402212M" && it.aprobo() }

    fun requisitosPracticasFundamentacionII(): Boolean =
        estudiante?.asignaturas.orEmpty()
            .any { it.codigo == "402221M" && it.aprobo() }

    /**
     * Cuenta cuántos de los 8 requisitos de prácticas profesionales están
     * cumplidos: 4 asignaturas obligatorias + 4 líneas de profundización.
     * Las electivas se evalúan por separado en su propio banner de estado.
     */
    fun contarRequisitosAprobadosPracticas(): Int {
        val avanceLineas = avanceLineasProfundizacion()
        return listOf(
            requisitosPracticasEtica(),
            requisitosPracticasDiagnostico(),
            requisitosPracticasFundamentacionI(),
            requisitosPracticasFundamentacionII(),
            avanceLineas.containsKey("Social"),
            avanceLineas.containsKey("Organizacional"),
            avanceLineas.containsKey("Educativa"),
            avanceLineas.containsKey("Clínica/NeuroClínica")
        ).count { it }
    }

    /** La línea elegida en configuración debe tener aprobados sus niveles I, II y III. */
    fun nivelesCompletosLineaSeleccionada(): Boolean {
        val lineaElegida = estudiante?.linea?.takeIf { it.isNotBlank() && it != "No" && it != "Ninguna" }
            ?: return false
        val opcionesLinea = when (lineaElegida) {
            "Clínica/NeuroClínica", "Clínica", "NeuroClínica" -> setOf("Clínica", "NeuroClínica")
            else -> setOf(lineaElegida)
        }
        val nivelesAprobados = estudiante?.asignaturas.orEmpty()
            .filter { it.lineaProfesional in opcionesLinea && it.aprobo() }
            .mapNotNull { nivelDeAsignatura(it.nombre) }
            .toSet()
        return (1..3).all { it in nivelesAprobados }
    }

    /** Criterios completos que habilitan el botón de inicio de prácticas. */
    fun requisitosInicioPracticasCompletos(): Boolean =
        contarRequisitosAprobadosPracticas() == 8 &&
            creditosDeProfesionalizacion() >= 27 &&
            estadosElectivasPensum().count { it } >= 4 &&
            nivelesCompletosLineaSeleccionada()

    // ========================================================================
    // ELECTIVAS
    // ========================================================================

    fun creditoselectivasComplementarias(): Int =
        estudiante?.asignaturas.orEmpty()
            .filter { it.aprobo() && it.componente.contains("Electiva complementaria", ignoreCase = true) }
            .sumOf { it.creditos }

    fun creditoselectivasProfesionales(): Int =
        estudiante?.asignaturas.orEmpty()
            .filter { it.aprobo() && it.componente.contains("Electiva profesional", ignoreCase = true) }
            .sumOf { it.creditos }

    fun requisitoElectivas(): Boolean =
        creditoselectivasProfesionales() + creditoselectivasComplementarias() >= 12

    /**
     * Estado de las dos electivas complementarias que aparecen como
     * espacios independientes en el pensum (códigos 1 y 2).
     */
    fun estadosElectivasComplementarias(): List<Boolean> =
        listOf("1", "2").map { codigo ->
            estudiante?.asignaturas.orEmpty()
                .find { it.codigo.trim() == codigo }
                ?.aprobo() == true
        }

    /**
     * Estado de Electiva Profesional II, III y IV.
     *
     * Estos tres espacios empiezan con los códigos provisionales "3", "4" y
     * "5", pero al importar el PDF cada uno pasa a tener el código real de la
     * electiva cursada. Por eso se identifican por posición: son las
     * asignaturas de componente "Electiva profesional" (sin contar la
     * Electiva Profesional I, 402197M), en el orden de la lista.
     */
    fun estadosElectivasProfesionalesIIIII(): List<Boolean> {
        val slots = electivasProfesionalesIIaIV()
        return (0 until 3).map { i -> slots.getOrNull(i)?.aprobo() == true }
    }

    /** Asignaturas que ocupan Electiva Profesional II, III y IV, en orden. */
    fun electivasProfesionalesIIaIV(): List<Asignatura> =
        estudiante?.asignaturas.orEmpty()
            .filter {
                it.componente.equals("Electiva profesional", ignoreCase = true) &&
                        it.codigo.trim() != "402197M"
            }
            .take(3)

    /**
     * Estado completo de las seis posiciones de electivas que muestra el
     * Pensum: complementarias I-II y profesionales I-IV.
     */
    fun estadosElectivasPensum(): List<Boolean> =
        listOf(
            *estadosElectivasComplementarias().toTypedArray(),
            estudiante?.asignaturas.orEmpty()
                .find { it.codigo.trim() == "402197M" }
                ?.aprobo() == true,
            *estadosElectivasProfesionalesIIIII().toTypedArray()
        )

    /** Electivas que aparecen como materias del semestre 0 en el Pensum. */
    fun materiasElectivasPensum(): List<Asignatura> =
        estudiante?.asignaturas.orEmpty().filter { it.semestre == 0 }

    // ========================================================================
    // REQUISITOS PARA GRADO
    // ========================================================================

    /**
     * Verdadero si el estudiante ya aprobó las dos Prácticas
     * Profesionales Supervisadas (I y II).
     */
    fun requisitoPracticasProfesionalesCumplido(): Boolean {
        val codigos = listOf("402222M", "402224M")
        val aprobados = estudiante?.asignaturas
            ?.filter { it.codigo in codigos && it.aprobo() }
            ?.map { it.codigo }
            ?.toSet()
            .orEmpty()
        return codigos.all { it in aprobados }
    }

    /** Verdadero si el estudiante ya cursó los 168 créditos de la carrera. */
    fun requisitoCreditosCarreraCumplido(): Boolean =
        creditosQueSirvenParaCarrera() >= creditosCarrera

    /**
     * Verdadero si el estudiante ya aprobó las dos partes del Trabajo
     * de Grado (I y II).
     */
    fun requisitoTrabajoDeGradoCumplido(): Boolean {
        val codigos = listOf("402223M", "402225M")
        val aprobados = estudiante?.asignaturas
            ?.filter { it.codigo in codigos && it.aprobo() }
            ?.map { it.codigo }
            ?.toSet()
            .orEmpty()
        return codigos.all { it in aprobados }
    }

    /** Verdadero si el estudiante ya completó los 5 créditos complementarios. */
    fun requisitoCreditosComplementariosCumplido(): Boolean =
        creditosComplementariosAprobados() >= creditosComplementarios

    /**
     * Verdadero si el estudiante ya aprobó todos los niveles de
     * [NOMBRES_IDIOMAS] (Idiomas I e Idiomas II).
     */
    fun requisitoInglesCumplido(): Boolean =
        cursosInglesAprobados().size >= NOMBRES_IDIOMAS.size

    /**
     * Verdadero solo si el estudiante cumple TODOS los requisitos de
     * grado: prácticas profesionales, 168 créditos, trabajo de grado,
     * créditos complementarios e inglés.
     */
    fun cumpleRequisitosGrado(): Boolean =
        requisitoPracticasProfesionalesCumplido() &&
                requisitoCreditosCarreraCumplido() &&
                requisitoTrabajoDeGradoCumplido() &&
                requisitoCreditosComplementariosCumplido() &&
                requisitoInglesCumplido()
}


// ========================================================================
// CONSEJOS: PARTE GRÁFICA (compartida por Perfil y Pensum)
// ========================================================================

/** Tarjeta de consejo con bombillo, usada en la pantalla de Perfil. */
@Composable
fun ModuloConsejoPrincipal(
    consejo: ConsejoPensum,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = scheme.secondaryContainer.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, scheme.secondary.copy(alpha = 0.18f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = scheme.secondary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = "Consejo",
                        tint = scheme.secondary,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = consejo.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.onSecondaryContainer
                )
                Text(
                    text = consejo.texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSecondaryContainer.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Consejo compacto del hero de cada semestre en el Pensum.
 * En los semestres 8 y 9 incluye enlaces de navegación.
 */
@Composable
fun ModuloConsejoPensum(
    programa: ProgramaM,
    semestre: Int,
    onIrPracticas: () -> Unit,
    onIrTrabajoGrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val consejo = programa.consejoPensum(semestre)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (semestre) {
                8 -> {
                    Text(
                        text = "Termina tu línea de prácticas y revisa tus requisitos faltantes para aplicar a las",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ir a prácticas profesionales",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2F76B9),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { onIrPracticas() }
                    )
                }

                9 -> {
                    Text(
                        text = "Elige un director de trabajo de grado e inicia tu ciclo de trabajo de grado, el cual dura mínimo un año y máximo 2-3. En caso de interrumpir un semestre, el ciclo se reinicia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ir a Trabajo de grado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2F76B9),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { onIrTrabajoGrado() }
                    )
                }

                else -> {
                    Text(
                        text = consejo.texto,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
