package com.renea.psicologiauv.model

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
        estudiante?.asignaturas
            ?.filter { it.codigo == "402203M" }
            .orEmpty()
            .all { it.aprobo() }

    fun requisitosPracticasDiagnostico(): Boolean =
        estudiante?.asignaturas
            ?.filter { it.codigo == "402204M" }
            .orEmpty()
            .all { it.aprobo() }

    fun requisitosPracticasFundamentacionI(): Boolean =
        estudiante?.asignaturas
            ?.filter { it.codigo == "402212M" }
            .orEmpty()
            .all { it.aprobo() }

    fun requisitosPracticasFundamentacionII(): Boolean =
        estudiante?.asignaturas
            ?.filter { it.codigo == "402221M" }
            .orEmpty()
            .all { it.aprobo() }

    /**
     * Cuenta cuántos de los 9 requisitos de prácticas profesionales están
     * cumplidos: 4 asignaturas obligatorias + 4 líneas cursadas + electivas.
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
            avanceLineas.containsKey("Clínica/NeuroClínica"),
            requisitoElectivas()
        ).count { it }
    }

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