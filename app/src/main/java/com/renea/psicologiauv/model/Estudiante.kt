package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable
import kotlin.collections.MutableList
import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
import com.renea.psicologiauv.data.ReglaEspanol

/** Último semestre que muestra la grilla del pensum. */
private const val SEMESTRE_MAXIMO_PENSUM = 11

@Serializable
data class Estudiante(
    val nombre: String = "Padawan",
    val codigo: Int = 0,
    var linea: String = "Ninguna",
    val avatarUri: String? = null,
    var asignaturas: MutableList<Asignatura>,
    var horario: MutableList<AsignaturaHorario> = mutableListOf(),
    val semestresManuales: Map<Int, Boolean> = emptyMap()
) {

    fun cambiarNombre(nuevoNombre: String): Estudiante =
        copy(nombre = nuevoNombre)

    fun cambiarAvatar(nuevaRuta: String): Estudiante =
        copy(avatarUri = nuevaRuta)
    fun cambiarCodigo(nuevoCodigo: Int): Estudiante =
        if(nuevoCodigo > 0) copy(codigo = nuevoCodigo)
        else
            this

    fun cambiarNombreAsignatura(codigoAsignatura: String, nuevoNombre: String): Estudiante {
        val asignaturasActualizadas = asignaturas.map {
            if (it.codigo == codigoAsignatura) it.cambiarNombre(nuevoNombre)
            else
                it
        }.toMutableList()
        return copy(asignaturas = asignaturasActualizadas)
    }


    fun cambiarCodigoAsignatura(codigoAsignatura: String, nuevoCodigo: String): Estudiante{
        val nuevasAsignaturas = asignaturas.map {
            if(it.codigo == codigoAsignatura) it.cambiarCodigo(nuevoCodigo)
            else
                it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }

    /**
     * Sincroniza una asignatura del PDF con una de las dos electivas
     * complementarias del pénsum. [posicion] es 0 para Electiva
     * Complementaria I y 1 para la II (orden de la lista). El espacio toma el
     * nombre y el código reales y la nota importada.
     */
    fun asignarElectivaComplementaria(
        posicion: Int,
        codigo: String,
        nombre: String,
        nota: NotaImportada?,
        creditos: Int = 3
    ): Estudiante {
        val indices = asignaturas.indices.filter {
            asignaturas[it].componente.equals("Electiva complementaria", ignoreCase = true)
        }
        val indice = indices.getOrNull(posicion) ?: return this
        val base = asignaturas[indice].copy(
            nombre = nombre,
            codigo = codigo,
            creditos = creditos,
            nota = 0f,
            notaEspecial = null
        )
        val actualizada = when (nota) {
            is NotaImportada.Numerica -> base.cambiarNota(nota.valor)
            is NotaImportada.Especial -> base.cambiarNotaEspecial(nota.marca)
            null -> base
        }
        val nuevas = asignaturas.toMutableList()
        nuevas[indice] = actualizada
        return copy(asignaturas = nuevas)
    }

    fun cambiarCreditosAsignatura(codigoAsignatura: String, nuevosCreditos: Int): Estudiante {
        val nuevasAsignaturas = asignaturas.map {
            if (it.codigo.trim().equals(codigoAsignatura.trim(), ignoreCase = true)) it.cambiarCreditos(nuevosCreditos)
            else it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }

    fun cambiarSemestreAsignatura(codigoAsignatura: String, nuevoSemestre: Int): Estudiante{
        val nuevasAsignaturas = asignaturas.map {
            if(it.codigo == codigoAsignatura) it.cambiarSemestre(nuevoSemestre)
            else
                it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }

    /**
     * Permite fijar o revertir manualmente el estado de completitud de un
     * semestre. Si [completado] es null, se elimina el ajuste manual y se
     * vuelve al cálculo automático.
     */
    fun cambiarEstadoSemestre(semestre: Int, completado: Boolean?): Estudiante {
        val nuevos = if (completado == null) {
            semestresManuales - semestre
        } else {
            semestresManuales + (semestre to completado)
        }
        return copy(semestresManuales = nuevos)
    }

    /**
     * Cambia al [semestreActual] el semestre de las asignaturas que se están
     * cursando en este momento, para que aparezcan en el horario aunque en el
     * pensum pertenezcan a otro semestre (una de tercero que no se vio antes,
     * o una de octavo que se está adelantando).
     *
     * Una asignatura se considera en curso cuando se cumplen las dos
     * condiciones:
     * - su código está en [codigosEnCurso], es decir, aparece en el tabulado
     *   SIRA y NO está cancelada (las canceladas nunca llegan en esa lista); y
     * - no tiene nota alguna ([Asignatura.sinNota]): ni numérica ni marca
     *   especial.
     *
     * Las asignaturas que ya están en [semestreActual] no se tocan. Devuelve
     * el nuevo [Estudiante] y la cantidad de asignaturas que cambiaron de
     * semestre.
     */
    fun moverAsignaturasEnCursoAlSemestre(
        codigosEnCurso: Collection<String>,
        semestreActual: Int
    ): Pair<Estudiante, Int> {
        val enCurso = codigosEnCurso.map { it.trim().uppercase() }.toSet()
        if (enCurso.isEmpty()) return this to 0

        var movidas = 0
        val nuevasAsignaturas = asignaturas.map { asignatura ->
            val cursando = asignatura.codigo.trim().uppercase() in enCurso && asignatura.sinNota()
            if (cursando && asignatura.semestre != semestreActual) {
                val movida = asignatura.cambiarSemestre(semestreActual)
                if (movida.semestre != asignatura.semestre) movidas++
                movida
            } else {
                asignatura
            }
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas) to movidas
    }

    /**
     * Ubica cada asignatura en el semestre SIRA en el que se cursó (cada
     * periodo del tabulado es un semestre, contando desde 1).
     *
     * - Solo se tocan las asignaturas que hoy viven en la grilla del pensum
     *   (semestre 1..11). Las que usan semestres estructurales (0 = trabajo de
     *   grado y actividades complementarias, 99 = idiomas) se respetan.
     * - Un semestre SIRA mayor a [SEMESTRE_MAXIMO_PENSUM] se ubica en el último
     *   semestre del pensum, para que la asignatura no desaparezca de la grilla.
     * - Cuando el semestre SIRA coincide con el del pensum no cambia nada.
     *
     * Devuelve el nuevo [Estudiante] y la cantidad de asignaturas que cambiaron
     * de semestre.
     */
    fun aplicarSemestresSira(semestresPorCodigo: Map<String, Int>): Pair<Estudiante, Int> {
        if (semestresPorCodigo.isEmpty()) return this to 0
        val porCodigo = semestresPorCodigo.mapKeys { it.key.trim().uppercase() }

        var cambiadas = 0
        val nuevas = asignaturas.map { asignatura ->
            val semestreSira = porCodigo[asignatura.codigo.trim().uppercase()]
            if (semestreSira == null || asignatura.semestre !in 1..SEMESTRE_MAXIMO_PENSUM) {
                asignatura
            } else {
                val movida = asignatura.cambiarSemestre(semestreSira.coerceAtMost(SEMESTRE_MAXIMO_PENSUM))
                if (movida.semestre != asignatura.semestre) cambiadas++
                movida
            }
        }.toMutableList()
        return copy(asignaturas = nuevas) to cambiadas
    }

    fun cambiarNotaAsignatura(codigoAsignatura: String, nuevaNota: Float): Estudiante{
        val nuevasAsignaturas = asignaturas.map {
            if(it.codigo == codigoAsignatura) it.cambiarNota(nuevaNota)
            else
                it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }

    fun cambiarNotaEspecialAsignatura(codigoAsignatura: String, marca: String): Estudiante {
        val nuevasAsignaturas = asignaturas.map {
            if (it.codigo == codigoAsignatura) it.cambiarNotaEspecial(marca)
            else it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }


    fun cambiarLinea(nuevaLinea: String): Estudiante =
        copy(linea = nuevaLinea)
    /**
     * Aplica en un solo paso un lote de notas importadas (por ejemplo, desde el
     * PDF de notas de la universidad), identificando cada asignatura por su
     * código.
     *
     * - Los códigos del mapa que NO existan en [asignaturas] simplemente se
     *   ignoran (no se agregan asignaturas nuevas ni se lanza ningún error).
     * - Los códigos que SÍ coinciden actualizan su nota reutilizando
     *   [Asignatura.cambiarNota], por lo que se respetan las mismas reglas de
     *   validación que ya usa la app (nota entre 0 y 5).
     *
     * Devuelve el nuevo [Estudiante] junto con la cantidad de asignaturas que
     * efectivamente cambiaron de nota, para poder informarle al usuario cuántas
     * materias se importaron.
     *
     * Cada entrada del mapa puede ser una nota numérica normal
     * ([NotaImportada.Numerica]) o una marca especial sin nota numérica
     * como "C.U", "E.X" o "A.P" ([NotaImportada.Especial]), que se guarda
     * en [Asignatura.notaEspecial] en vez de en [Asignatura.nota].
     */
    fun actualizarNotasMasivo(notasPorCodigo: Map<String, NotaImportada>): Pair<Estudiante, Int> {
        var actualizadas = 0
        val nuevasAsignaturas = asignaturas.map { asignatura ->
            when (val notaImportada = notasPorCodigo[asignatura.codigo]) {
                is NotaImportada.Numerica -> {
                    val conNuevaNota = asignatura.cambiarNota(notaImportada.valor)
                    if (conNuevaNota.nota != asignatura.nota ||
                        conNuevaNota.notaEspecial != asignatura.notaEspecial
                    ) actualizadas++
                    conNuevaNota
                }
                is NotaImportada.Especial -> {
                    var conNotaEspecial = asignatura.cambiarNotaEspecial(notaImportada.marca)
                    val esEximida = notaImportada.marca.replace(".", "").equals("EX", ignoreCase = true)
                    val esEspanolOIdioma = asignatura.codigo.trim().equals(ReglaEspanol.CODIGO_ESPANOL_SIRA, ignoreCase = true) ||
                            asignatura.codigo.trim().equals(ReglaEspanol.CODIGO_CULTURA_ESCRITA, ignoreCase = true) ||
                            asignatura.componente.equals("Idioma", ignoreCase = true) ||
                            asignatura.nombre in NOMBRES_IDIOMAS ||
                            asignatura.nombre.contains("Español", ignoreCase = true) ||
                            asignatura.nombre.contains("Inglés", ignoreCase = true)
                    if (esEximida && esEspanolOIdioma) {
                        conNotaEspecial = conNotaEspecial.cambiarCreditos(0)
                    }
                    if (conNotaEspecial.notaEspecial != asignatura.notaEspecial ||
                        conNotaEspecial.nota != asignatura.nota ||
                        conNotaEspecial.creditos != asignatura.creditos
                    ) actualizadas++
                    conNotaEspecial
                }
                null -> asignatura
            }
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas) to actualizadas
    }

    /**
     * Aplica en un solo paso el lote de créditos leídos del PDF SIRA,
     * actualizando los créditos de cada asignatura encontrada por código.
     * A diferencia de [cambiarCreditosAsignatura] (que solo permite cambiar
     * electivas e idiomas desde la UI), este método aplica sin restricción
     * de componente, porque la información del SIRA tiene prioridad sobre
     * la del JSON del pensum para TODAS las asignaturas.
     *
     * Solo se actualiza si los créditos del SIRA son distintos a los actuales
     * y el código coincide (case-insensitive).
     */
    fun actualizarCreditosMasivo(creditosPorCodigo: Map<String, Int>): Estudiante {
        if (creditosPorCodigo.isEmpty()) return this
        val porCodigo = creditosPorCodigo.mapKeys { it.key.trim().uppercase() }
        val nuevasAsignaturas = asignaturas.map { asignatura ->
            val codigoUp = asignatura.codigo.trim().uppercase()
            val creditosSira = porCodigo[codigoUp]
            if (creditosSira != null && creditosSira != asignatura.creditos) {
                asignatura.cambiarCreditos(creditosSira)
            } else {
                asignatura
            }
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }


    /**
     * Agrega una nueva asignatura (por ejemplo, una asignatura o actividad
     * complementaria creada desde la pantalla de Pensum) a la lista de
     * asignaturas del estudiante.
     */
    fun agregarAsignatura(nuevaAsignatura: Asignatura): Estudiante {
        val nuevasAsignaturas = (asignaturas + nuevaAsignatura).toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }


    /**
     * Calcula el siguiente código disponible para una actividad
     * complementaria creada automáticamente: empieza en "999" y, por cada
     * actividad complementaria ya creada con código numérico >= 999,
     * continúa con el consecutivo siguiente (999, 1000, 1001, ...).
     */
    fun siguienteCodigoComplementario(): String {
        val ultimoCodigo = asignaturas
            .mapNotNull { it.codigo.toIntOrNull() }
            .filter { it >= 999 }
            .maxOrNull()
        return ((ultimoCodigo ?: 998) + 1).toString()
    }

    /**
     * Elimina una asignatura de la lista, pero SOLO si es una actividad
     * complementaria (`complementaria == true`). Si la asignatura con ese
     * código no existe, o existe pero no es complementaria, no se elimina
     * nada y se devuelve el mismo estudiante sin cambios.
     */
    fun eliminarAsignatura(codigoAsignatura: String): Estudiante {
        val esComplementaria = asignaturas.any {
            it.codigo == codigoAsignatura && it.complementaria
        }
        if (!esComplementaria) return this

        val nuevasAsignaturas = asignaturas
            .filterNot { it.codigo == codigoAsignatura }
            .toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
    }

    /**
     * Sincroniza la asignatura "Actividades Académicas Complementarias" (402226M)
     * con las actividades complementarias creadas manualmente:
     * - Si las actividades complementarias agregadas suman 5 o más créditos aprobados,
     *   marca automáticamente 402226M como cumplida con la marca especial "A.P" (si no estaba ya aprobada).
     * - Si las actividades complementarias manuales suman menos de 5 créditos y 402226M
     *   había sido marcada como "A.P" sin nota numérica real (nota == 0f), se reinicia para reflejar
     *   que aún no cumple el requisito.
     */
    fun sincronizarComplementarias(): Estudiante {
        val mat402226M = asignaturas.find { it.codigo.trim() == "402226M" } ?: return this
        val creditosManuales = asignaturas
            .filter { (it.area == "Complementario" || it.complementaria) && it.codigo.trim() != "402226M" && it.aprobo() }
            .sumOf { it.creditos }

        return if (creditosManuales >= 5) {
            if (!mat402226M.aprobo()) {
                cambiarNotaEspecialAsignatura("402226M", "A.P")
            } else {
                this
            }
        } else {
            if (mat402226M.notaEspecial == "A.P" && mat402226M.nota == 0f) {
                cambiarNotaAsignatura("402226M", 0f)
            } else {
                this
            }
        }
    }


    /**
     * Despues de extraer notas del PDF SIRA, este metodo rellena los slots de
     * Electiva Profesional II/III/IV en orden de aparicion en el PDF.
     *
     * Logica:
     * 1. Toma el [catalogoCompleto] de electivas profesionales del archivo JSON.
     * 2. Filtra los codigos del [notasPorCodigo] que existen en el catalogo
     *    pero NO en las asignaturas del estudiante (son electivas reales cursadas
     *    que aun no tienen slot asignado).
     * 3. Identifica los slots placeholder de II/III/IV (codigos "3", "4", "5")
     *    que todavia no tienen nota, en ese orden.
     * 4. Por cada electiva encontrada en el PDF, toma el siguiente slot vacio,
     *    le cambia nombre y codigo segun el catalogo, y le aplica la nota.
     *
     * Devuelve el [Estudiante] actualizado y el numero de electivas resueltas.
     */
    fun resolverElectivasProfesionales(
        notasPorCodigo: Map<String, NotaImportada>,
        catalogoCompleto: List<ElectivaProfesionalCatalogo>,
        codigosPresentes: List<String> = emptyList()
    ): Pair<Estudiante, Int> {
        // Codigos ya presentes en las asignaturas del estudiante.
        val codigosEnPensum = asignaturas.map { it.codigo }.toSet()

        // Codigos del catalogo que el PDF trajo pero el estudiante no tiene aun.
        val codigosElectivasCatalogo = catalogoCompleto.map { it.codigo }.toSet()
        // Se incluyen también las electivas que el PDF muestra SIN nota (en curso),
        // ordenadas por aparición; luego las que solo llegaron con nota.
        val codigosCandidatos = (codigosPresentes + notasPorCodigo.keys).distinct()
        val electivasNuevas = codigosCandidatos
            .filter { it in codigosElectivasCatalogo && it !in codigosEnPensum }
            // Mantenemos el orden en que aparecen en el mapa (que ya viene
            // ordenado por posicion en el PDF desde ImportadorNotas).
            .mapNotNull { codigo -> catalogoCompleto.find { it.codigo == codigo } }

        if (electivasNuevas.isEmpty()) return this to 0

        // Slots placeholder vacios: codigos "3", "4", "5" sin nota aun,
        // ordenados por su posicion en la lista de asignaturas.
        val codigosPlaceholder = listOf("3", "4", "5")
        val slotsVacios = asignaturas
            .filter { it.codigo in codigosPlaceholder && !it.aprobo() }
            .sortedBy { codigosPlaceholder.indexOf(it.codigo) }

        var resueltas = 0
        val asignaturasActualizadas = asignaturas.toMutableList()
        val slotsIterador = slotsVacios.iterator()

        for (electiva in electivasNuevas) {
            if (!slotsIterador.hasNext()) break
            val slot = slotsIterador.next()
            val indice = asignaturasActualizadas.indexOfFirst { it.codigo == slot.codigo }
            if (indice == -1) continue

            val nota = notasPorCodigo[electiva.codigo]
            val asignaturaActualizada = asignaturasActualizadas[indice]
                .copy(nombre = electiva.nombre, codigo = electiva.codigo)
                .let { a ->
                    when (nota) {
                        is NotaImportada.Numerica -> a.cambiarNota(nota.valor)
                        is NotaImportada.Especial -> a.cambiarNotaEspecial(nota.marca)
                        null -> a
                    }
                }
            asignaturasActualizadas[indice] = asignaturaActualizada
            resueltas++
        }

        return copy(asignaturas = asignaturasActualizadas) to resueltas
    }

    /**
     * Si Español (204001M) está presente y aprobado en las notas importadas,
     * transforma la asignatura original del pensum (Cultura Escrita 402180M)
     * a código "204001M" y nombre "Español", para que pueda recibir la nota
     * y semestre de Español, liberando el código 402180M para las complementarias.
     */
    fun aplicarReglaEspanol(notasPorCodigo: Map<String, NotaImportada>): Estudiante {
        val notaEspanol = notasPorCodigo[ReglaEspanol.CODIGO_ESPANOL_SIRA] ?: return this
        if (!ReglaEspanol.aprobada(notaEspanol)) return this

        // Si ya tiene una materia con el código de Español, ya fue homologada previamente.
        if (asignaturas.any { it.codigo.trim().equals(ReglaEspanol.CODIGO_ESPANOL_SIRA, ignoreCase = true) }) {
            return this
        }

        // Buscar la Cultura Escrita original del pensum (básica, no complementaria).
        val indice = asignaturas.indexOfFirst {
            it.codigo.trim().equals(ReglaEspanol.CODIGO_CULTURA_ESCRITA, ignoreCase = true) &&
                    !it.componente.equals("Electiva complementaria", ignoreCase = true)
        }
        if (indice == -1) return this

        val esEximida = (notaEspanol as? NotaImportada.Especial)?.marca?.replace(".", "")?.equals("EX", ignoreCase = true) == true
        val nuevasAsignaturas = asignaturas.toMutableList()
        nuevasAsignaturas[indice] = nuevasAsignaturas[indice].copy(
            codigo = ReglaEspanol.CODIGO_ESPANOL_SIRA,
            nombre = ReglaEspanol.NOMBRE_ESPANOL,
            creditos = if (esEximida) 0 else nuevasAsignaturas[indice].creditos
        )
        return copy(asignaturas = nuevasAsignaturas)
    }

    /**
     * Agrega al pensum en su respectivo semestre las asignaturas que no se asignaron
     * a ninguna electiva complementaria (asignaturas sobrantes/descartadas del SIRA).
     * Se registran con componente y área "Extracurricular" para que cuenten hacia los
     * créditos y el promedio ponderado de ese semestre, pero no hacia los créditos de carrera
     * ni hacia los requisitos de la misma.
     */
    fun agregarAsignaturasExtracurriculares(
        noAsignadas: List<AsignaturaNoSincronizada>,
        grupos: Map<String, String> = emptyMap()
    ): Estudiante {
        if (noAsignadas.isEmpty()) return this

        val nuevasAsignaturas = asignaturas.toMutableList()
        for (item in noAsignadas) {
            val codigoTrim = item.codigo.trim().uppercase()
            val indiceExistente = nuevasAsignaturas.indexOfFirst {
                it.codigo.trim().uppercase() == codigoTrim
            }

            val grupo = grupos[codigoTrim] ?: ""
            if (indiceExistente != -1) {
                val existente = nuevasAsignaturas[indiceExistente]
                var actualizada = if (item.semestre != null) existente.copy(semestre = item.semestre) else existente
                actualizada = when (val n = item.nota) {
                    is NotaImportada.Numerica -> actualizada.cambiarNota(n.valor)
                    is NotaImportada.Especial -> actualizada.cambiarNotaEspecial(n.marca)
                    null -> actualizada
                }
                if (grupo.isNotBlank()) actualizada = actualizada.copy(grupo = grupo)
                nuevasAsignaturas[indiceExistente] = actualizada
            } else {
                val nueva = Asignatura(
                    nombre = item.nombre,
                    codigo = item.codigo,
                    semestre = item.semestre ?: 1,
                    creditos = item.creditos,
                    lineaProfesional = "No",
                    componente = "Extracurricular",
                    area = "Extracurricular",
                    nota = (item.nota as? NotaImportada.Numerica)?.valor ?: 0f,
                    notaEspecial = (item.nota as? NotaImportada.Especial)?.marca,
                    complementaria = false,
                    grupo = grupo
                )
                nuevasAsignaturas.add(nueva)
            }
        }

        return copy(asignaturas = nuevasAsignaturas)
    }

}