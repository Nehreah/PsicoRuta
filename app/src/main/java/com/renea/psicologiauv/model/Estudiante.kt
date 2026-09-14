package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable
import kotlin.collections.MutableList

@Serializable
data class Estudiante(
    val nombre: String = "Padawan",
    val codigo: Int = 0,
    var linea: String = "Ninguna",
    val avatarUri: String? = null,
    var asignaturas: MutableList<Asignatura>
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

    fun cambiarSemestreAsignatura(codigoAsignatura: String, nuevoSemestre: Int): Estudiante{
        val nuevasAsignaturas = asignaturas.map {
            if(it.codigo == codigoAsignatura) it.cambiarSemestre(nuevoSemestre)
            else
                it
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas)
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
                    val conNotaEspecial = asignatura.cambiarNotaEspecial(notaImportada.marca)
                    if (conNotaEspecial.notaEspecial != asignatura.notaEspecial ||
                        conNotaEspecial.nota != asignatura.nota
                    ) actualizadas++
                    conNotaEspecial
                }
                null -> asignatura
            }
        }.toMutableList()
        return copy(asignaturas = nuevasAsignaturas) to actualizadas
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
        catalogoCompleto: List<ElectivaProfesionalCatalogo>
    ): Pair<Estudiante, Int> {
        // Codigos ya presentes en las asignaturas del estudiante.
        val codigosEnPensum = asignaturas.map { it.codigo }.toSet()

        // Codigos del catalogo que el PDF trajo pero el estudiante no tiene aun.
        val codigosElectivasCatalogo = catalogoCompleto.map { it.codigo }.toSet()
        val electivasNuevas = notasPorCodigo.keys
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

}