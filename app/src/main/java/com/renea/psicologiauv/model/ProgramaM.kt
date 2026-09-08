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
    val año: Int = 2026,
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

    fun promedioSemestre(semestre: Int): Float {
        // notaEspecial == null: las materias aprobadas por marca especial
        // (CU, EX, AP) cuentan como aprobadas para créditos y requisitos,
        // pero no tienen una nota numérica real, así que no deben entrar en
        // el promedio ponderado.
        val lista =
            estudiante?.asignaturas
                ?.filter { it.semestre == semestre && it.aprobo() && it.notaEspecial == null }
                .orEmpty()
        val sumaCreditos = lista.sumOf { it.creditos}
        if (sumaCreditos == 0) return 0f
        val sumaNotas = lista.sumOf { (it.creditos * it.nota).toDouble() }
        if (sumaNotas.toFloat() == 0f) return 0f
        return (sumaNotas / sumaCreditos).toFloat()
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

    fun promedioGeneral(): Float {
        // Mismo criterio que promedioSemestre: se excluyen las materias
        // aprobadas por marca especial (CU, EX, AP), que no tienen nota
        // numérica real.
        val lista = estudiante?.asignaturas?.filter { it.aprobo() && it.notaEspecial == null }.orEmpty()
        val sumaCreditos = lista.sumOf { it.creditos}
        if (sumaCreditos == 0) return 0f
        val sumaNotas = lista.sumOf { (it.nota * it.creditos).toDouble() }
        return (sumaNotas / sumaCreditos).toFloat()
    }

    fun avanceAreas(): Map<String, Float> {
        val avanceAreas = mutableMapOf<String, Float>()
        val sumaCreditosFDI = creditosFDIAprobados()
        val sumaCreditosFPCA = creditosFPCAAprobados()
        val sumaCreditosFII = creditosFIIAprobados()

        avanceAreas["Fundamentación Disciplinar e Interdisciplinar"] =
            (sumaCreditosFDI.toFloat() / creditosFDI.toFloat() * 100f).coerceIn(0f, 100f)
        avanceAreas["Formación Profesional en Campos de Aplicación"] =
            (sumaCreditosFPCA.toFloat() / creditosFPCA.toFloat() * 100f).coerceIn(0f, 100f)
        avanceAreas["Formación en investigación e Intervención"] =
            (sumaCreditosFII.toFloat() / creditosFII.toFloat() * 100f).coerceIn(0f, 100f)

        return avanceAreas
    }

    fun avanceCarrera(): Float {
        val creditosCursados = creditosQueSirvenParaCarrera()
        return (creditosCursados.toFloat() / creditosCarrera.toFloat() * 100f).coerceIn(0f, 100f)
    }

    fun requisitosEtapaProfesional(): Boolean {
        val listaAprobadas = estudiante?.asignaturas?.filter {it.aprobo() && it.area == "Fundamentación Disciplinar e Interdisciplinar"}.orEmpty()
        val creditosAprobados: Int = listaAprobadas.sumOf {it.creditos}
        return (creditosAprobados.toFloat() / creditosFDI.toFloat()) >= 0.8f
    }

    fun requisitosPracticasLinea(): Boolean {
        val listaLinea = estudiante?.asignaturas?.filter { (it.lineaProfesional == estudiante?.linea) && it.aprobo()}.orEmpty()
        val sumaLinea = listaLinea.sumOf {it.creditos}
        return sumaLinea >= creditosLinea
    }

    fun requisitosPracticasOtraLinea(otraLinea: String): Boolean {
        val listaLinea = estudiante?.asignaturas?.filter { it.lineaProfesional == otraLinea && it.aprobo()}.orEmpty()
        val sumaLinea = listaLinea.sumOf { it.creditos }
        return sumaLinea >= creditosLinea
    }


    fun requisitosPracticasCreditos(): Boolean {
        val listaCreditos = estudiante?.asignaturas?.filter {(it.lineaProfesional != "No") && it.aprobo()}.orEmpty()
        val sumaLinea = listaCreditos.sumOf {it.creditos}
        return sumaLinea >= creditosProfesionales
    }

    fun creditosDeProfesionalizacion(): Int {
        val listaCreditos = estudiante?.asignaturas?.filter {(it.lineaProfesional != "No") && it.aprobo()}.orEmpty()
        val sumaLinea = listaCreditos.sumOf {it.creditos}
        return if (sumaLinea > 0) sumaLinea else 0
    }

    fun requisitosPracticasEtica(): Boolean {
        val listaAsignaturas = estudiante?.asignaturas?.filter { it.codigo == "402203M"}.orEmpty()
        val resultado = listaAsignaturas.all { it.aprobo()}
        return resultado
    }
    fun requisitosPracticasDiagnostico(): Boolean {
        val listaAsignaturas = estudiante?.asignaturas?.filter { it.codigo == "402204M"}.orEmpty()
        val resultado = listaAsignaturas.all { it.aprobo()}
        return resultado
    }
    fun requisitosPracticasFundamentacionI(): Boolean {
        val listaAsignaturas = estudiante?.asignaturas?.filter { it.codigo == "402212M"}.orEmpty()
        val resultado = listaAsignaturas.all { it.aprobo()}
        return resultado
    }
    fun requisitosPracticasFundamentacionII(): Boolean {
        val listaAsignaturas = estudiante?.asignaturas?.filter { it.codigo == "402221M"}.orEmpty()
        val resultado = listaAsignaturas.all { it.aprobo()}
        return resultado
    }
    fun avanceRequisitoProfesional(): Float {
        val listaAprobadas =
            estudiante
                ?.asignaturas
                ?.filter {
                    it.aprobo() &&
                            it.area == "Fundamentación Disciplinar e Interdisciplinar"
                }
                .orEmpty()

        val creditosAprobados =
            listaAprobadas.sumOf {
                it.creditos
            }

        return (
                creditosAprobados.toFloat() /
                        creditosFDI.toFloat() *
                        100f
                ).coerceIn(0f, 100f)
    }

    /**
     * Evalúa, línea por línea de profundización profesional, si el
     * estudiante ya aprobó al menos un nivel (I, II o III).
     *
     * Psicología tiene 4 líneas: Clínica, Social, Educativa y
     * Organizacional. La línea Clínica tiene dos opciones mutuamente
     * excluyentes entre sí -Clínica y NeuroClínica-, así que aunque
     * existan 5 "familias" de materias de línea con 3 niveles cada una,
     * en la práctica solo hay 4 líneas.
     *
     * Para cada línea se busca primero si se aprobó el nivel I; si no
     * se encuentra, se busca el nivel II; si tampoco, se busca el
     * nivel III. En cuanto se encuentra un nivel aprobado se guarda esa
     * información (línea, opción, nivel y nombre de la asignatura) y no
     * se sigue buscando en los niveles superiores de esa misma línea.
     * Si no se aprobó ningún nivel de una línea, esa línea simplemente
     * no aparece en el resultado.
     */
    fun avanceLineasProfundizacion(): Map<String, AvanceLineaProfesional> {

        val asignaturas = estudiante?.asignaturas.orEmpty()

        // Cada línea puede tener una o varias "opciones" de
        // lineaProfesional que cuentan para ella. Clínica y NeuroClínica
        // son excluyentes entre sí, pero ambas alimentan la misma línea.
        val lineas =
            listOf(
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

                    val materia =
                        asignaturas.firstOrNull {
                            it.lineaProfesional == opcion &&
                                    it.aprobo() &&
                                    nivelDeAsignatura(it.nombre) == nivel
                        }

                    if (materia != null) {
                        resultado[nombreLinea] =
                            AvanceLineaProfesional(
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

    /**
     * Extrae el nivel (I = 1, II = 2, III = 3) a partir del nombre de la
     * materia, ya que las 5 familias de materias de línea profesional
     * (Psicología Clínica, Neuropsicología, Psicología Social,
     * Psicología Educativa y Psicología Organizacional) siempre
     * terminan su nombre en "I", "II" o "III". Devuelve null si el
     * nombre no termina en ninguno de esos tres.
     */
    private fun nivelDeAsignatura(nombre: String): Int? {
        val texto = nombre.trim()
        return when {
            texto.endsWith("III") -> 3
            texto.endsWith("II") -> 2
            texto.endsWith("I") -> 1
            else -> null
        }
    }
    fun creditosQueSirvenParaCarrera(): Int =
        (creditosFDIAprobados() + creditosFPCAAprobados() + creditosFIIAprobados())
            .coerceAtMost(creditosCarrera)

    /* -------------------------------------------------------------- */
    /* REQUISITOS PARA GRADO                                           */
    /* -------------------------------------------------------------- */

    /**
     * Verdadero si el estudiante ya aprobó las dos Prácticas
     * Profesionales Supervisadas (I y II).
     */
    fun requisitoPracticasProfesionalesCumplido(): Boolean {
        val codigos = listOf("402222M", "402224M") // Práctica Profesional Supervisada I y II
        val aprobados = estudiante?.asignaturas
            ?.filter { it.codigo in codigos && it.aprobo() }
            ?.map { it.codigo }
            ?.toSet()
            .orEmpty()
        return codigos.all { it in aprobados }
    }

    /**
     * Verdadero si el estudiante ya cursó (y le cuentan) los 168
     * créditos de la carrera.
     */
    fun requisitoCreditosCarreraCumplido(): Boolean =
        creditosQueSirvenParaCarrera() >= creditosCarrera

    /**
     * Verdadero si el estudiante ya aprobó las dos partes del Trabajo
     * de Grado (I y II).
     */
    fun requisitoTrabajoDeGradoCumplido(): Boolean {
        val codigos = listOf("402223M", "402225M") // Trabajo De Grado I y II
        val aprobados = estudiante?.asignaturas
            ?.filter { it.codigo in codigos && it.aprobo() }
            ?.map { it.codigo }
            ?.toSet()
            .orEmpty()
        return codigos.all { it in aprobados }
    }

    /**
     * Verdadero si el estudiante ya completó los 5 créditos
     * complementarios.
     */
    fun requisitoCreditosComplementariosCumplido(): Boolean =
        creditosComplementariosAprobados() >= creditosComplementarios

    /**
     * Nombres de [NOMBRES_IDIOMAS] (p. ej. "Idiomas I", "Idiomas II") para
     * los que el estudiante tiene AL MENOS UNA asignatura aprobada con ese
     * nombre exacto. Como puede haber varias asignaturas con el mismo
     * nombre y distinto código (una por cada idioma disponible), no
     * importa cuál código tenga la aprobada, solo que el nombre coincida.
     */
    fun cursosInglesAprobados(): Set<String> {
        val nombresAprobados = estudiante?.asignaturas
            ?.filter { it.aprobo() }
            ?.map { it.nombre }
            ?.toSet()
            .orEmpty()
        return NOMBRES_IDIOMAS.filter { it in nombresAprobados }.toSet()
    }

    /**
     * Verdadero si el estudiante ya aprobó todos los niveles de
     * [NOMBRES_IDIOMAS] (Idiomas I e Idiomas II), cada uno identificado
     * por su nombre y sin importar el código de la asignatura que lo
     * cumplió.
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

    /**
     * Promedio de las notas numéricas válidas (> 0) de una lista de
     * asignaturas. Se usa para mostrar el promedio de un semestre en
     * PantallaPensum. Ignora materias sin nota numérica todavía (nota == 0)
     * y no filtra por marca especial: si se necesita excluir C.U/E.X/A.P,
     * el llamador debe filtrar antes por notaEspecial == null.
     */
    fun promedioSemestreGeneral(materias: List<Asignatura>): Float {
        val notasValidas = materias
            .map { it.nota }
            .filter { it > 0 }

        return if (notasValidas.isNotEmpty()) {
            notasValidas.average().toFloat()
        } else {
            0f
        }
    }
}