package com.renea.psicologiauv.data

import com.renea.psicologiauv.model.AsignaturaNoSincronizada
import com.renea.psicologiauv.model.NotaImportada

/**
 * Analizador puro del texto extraído de un tabulado SIRA. Se mantiene separado
 * del acceso al PDF para poder probar la lógica sin Android ni archivos reales.
 *
 * Las reglas de lectura (qué es una nota, cómo se delimita una fila, qué hacer
 * con materias repetidas, marcas especiales y resúmenes de periodo) son
 * exactamente las de la versión anterior. Lo que cambió es el costo:
 *
 * - Antes se recorría TODO el texto una vez por cada código conocido
 *   (pensum + catálogo de electivas), es decir, decenas de pasadas. Ahora
 *   los códigos se encuentran en UNA sola pasada con el patrón genérico y se
 *   comparan contra un conjunto (HashSet).
 * - Antes, para cada aparición se recorría linealmente la lista de límites
 *   de fila. Ahora se usa búsqueda binaria.
 * - Los patrones se compilan una sola vez, no en cada importación.
 *
 * Los códigos conocidos con una forma distinta a la genérica (por ejemplo
 * "990001M-19" o "<<990001M") se siguen buscando uno a uno, como antes, para
 * no perder ninguna coincidencia.
 */
internal object AnalizadorNotas {

    // Patrón de nota: un dígito del 0 al 5, seguido de un punto decimal y
    // 1 o 2 decimales. Exigir el decimal evita confundir la nota con
    // columnas de créditos o semestre, que casi siempre son enteras.
    private val patronNota = Regex("""[0-5]\.\d{1,2}""")

    // Marca especial que el tabulado imprime en la columna CAL en vez de un
    // número (C.U, E.X, A.P). Se busca como token completo y sin distinguir
    // mayúsculas de minúsculas.
    private val patronNotaEspecial = Regex(
        """(?<![A-Za-z0-9])(C\.U|E\.?X|A\.P)(?![A-Za-z0-9])""",
        RegexOption.IGNORE_CASE
    )

    // Patrón GENÉRICO de "forma de código de asignatura". Sirve para encontrar
    // dónde empieza cada fila (exista o no el código en el pensum) y, a la
    // vez, para ubicar los códigos conocidos en una sola pasada.
    private val patronCodigoGenerico = Regex(
        """(?<![A-Za-z0-9])\d{5,7}[A-Za-z]?(?![A-Za-z0-9])"""
    )

    // Patrones para identificar el grupo matriculado en una fila del tabulado SIRA.
    // 1. Prefijo explícito GR o GRUPO (ej: "GR 01", "GR. 01", "GRUPO 02", "GR: 03").
    private val patronGrupoExplicito = Regex(
        """(?i)\b(?:GR|GRUPO)\b[.:\s]*([0-9]{1,2})\b"""
    )
    // 2. Prefijo GR pegado al número (ej: "GR01", "GR02").
    private val patronGrupoPrefijado = Regex(
        """(?i)\bGR([0-9]{1,2})\b"""
    )
    // 3. Número con cero a la izquierda (ej: "01", "02", ..., "09").
    private val patronGrupoDosDigitos = Regex(
        """(?<!\d)(0[1-9])(?!\d)"""
    )
    // 4. Número entero de dos dígitos aislado (ej: "10", "11", "12", "50").
    private val patronNumeroDosDigitos = Regex(
        """(?<![\d.])([1-9]\d)(?![\d.])"""
    )

    // Encabezado que marca el final de la lista de materias de un periodo y
    // el comienzo del resumen (créditos, promedio semestral).
    private const val MARCADOR_RESUMEN_PERIODO = "Resumen de Finalización"

    // Los códigos reales de la universidad tienen 7 caracteres o más. Los
    // provisionales de las electivas ("1".."5") se ignoran.
    private const val LONGITUD_MINIMA_CODIGO = 5

    // Límite máximo de caracteres para el tramo de una fila.
    private const val LONGITUD_MAXIMA_FILA = 400

    // Fin de la lista de asignaturas del tabulado de matrícula ("Total Asignaturas
    // Matriculadas: ...") y marcas de cancelación/reactivación de una fila.
    private const val MARCADOR_TOTALES = "Total Asignaturas"
    private val patronEncabezadoCancelacion = Regex(
        """Cancelaci[óo]n\s*/\s*Reactivaci[óo]n""", RegexOption.IGNORE_CASE
    )
    private val patronCancelacion = Regex("""CANCELACI[ÓO]N""", RegexOption.IGNORE_CASE)
    private val patronReactivacion = Regex("""REACTIVACI[ÓO]N""", RegexOption.IGNORE_CASE)

    // Fila del tabulado: "<código> <Gr> <Sde> <Asignatura (1 o 2 líneas)> <Sede> <TM> <Fun> <Crd>".
    // Se aplica al texto que sigue al código: salta Gr y Sde y toma el nombre
    // hasta las columnas "00 N AB 3".
    private val patronNombreFila = Regex(
        """^\s*\S+\s+\d{2}\s+(.+?)\s+\d{2}\s+[A-Za-z]\s+[A-Za-z]{2}\s+\d""",
        RegexOption.DOT_MATCHES_ALL
    )

    /** Verdadero si el fragmento de una fila indica que la materia fue cancelada (y no reactivada). */
    private fun filaCancelada(fragmento: String): Boolean {
        val limpio = patronEncabezadoCancelacion.replace(fragmento, "")
        return patronCancelacion.containsMatchIn(limpio) && !patronReactivacion.containsMatchIn(limpio)
    }

    private class Ocurrencia(val inicio: Int, val orden: Int, val codigo: String)

    fun extraerNotasPorCodigo(
        texto: String,
        codigosConocidos: List<String>
    ): Map<String, NotaImportada> {

        // Algunos reportes usan coma decimal ("4,2") en vez de punto ("4.2").
        val textoNormalizado = texto.replace(',', '.')

        // Código -> posición en la lista original (sin duplicados, solo
        // códigos reales). La posición se usa para desempatar el orden.
        val ordenPorCodigo = LinkedHashMap<String, Int>()
        for (codigo in codigosConocidos) {
            if (codigo.length >= LONGITUD_MINIMA_CODIGO && codigo !in ordenPorCodigo) {
                ordenPorCodigo[codigo] = ordenPorCodigo.size
            }
        }
        if (ordenPorCodigo.isEmpty()) return emptyMap()

        // UNA sola pasada: cada token con forma de código es un límite de
        // fila y, si además es un código conocido, una ocurrencia.
        val limitesLista = ArrayList<Int>()
        val ocurrencias = ArrayList<Ocurrencia>()

        for (coincidencia in patronCodigoGenerico.findAll(textoNormalizado)) {
            val inicio = coincidencia.range.first
            limitesLista.add(inicio)
            val orden = ordenPorCodigo[coincidencia.value]
            if (orden != null) {
                ocurrencias.add(Ocurrencia(inicio, orden, coincidencia.value))
            }
        }

        // Códigos conocidos que NO tienen la forma genérica: búsqueda
        // individual (igual que la versión anterior).
        for ((codigo, orden) in ordenPorCodigo) {
            if (patronCodigoGenerico.matches(codigo)) continue

            val patronCodigo = Regex(
                "(?<![A-Za-z0-9])" +
                        Regex.escape(codigo) +
                        "(?![A-Za-z0-9])"
            )
            for (coincidencia in patronCodigo.findAll(textoNormalizado)) {
                ocurrencias.add(Ocurrencia(coincidencia.range.first, orden, codigo))
            }
        }

        if (ocurrencias.isEmpty()) return emptyMap()

        // Orden de aparición en el documento; ante un empate, el orden de la
        // lista de códigos conocidos.
        ocurrencias.sortWith(compareBy<Ocurrencia>({ it.inicio }, { it.orden }))

        val limites = limitesLista.toIntArray()
        val resultado = LinkedHashMap<String, NotaImportada>()

        for (ocurrencia in ocurrencias) {
            val indiceInicio = ocurrencia.inicio
            val codigo = ocurrencia.codigo
            val indiceFinCodigo = indiceInicio + codigo.length

            /*
             * El siguiente código que aparece después del código actual marca
             * el final REAL de esta fila. No se busca la nota fuera de ese
             * límite: la nota 4.7 de otra materia nunca debe asignarse a esta.
             */
            // Se busca a partir del FINAL del código (no de su inicio) para que
            // un token con forma de código que quede dentro de un código con
            // prefijo (p. ej. "990001M" dentro de "<<990001M") no se tome como
            // límite: eso dejaba el final de la fila antes de su propio
            // inicio y hacía fallar el recorte del texto.
            val posicionSiguiente = primerIndiceMayorQue(limites, indiceFinCodigo - 1)
            val siguienteLimite =
                if (posicionSiguiente < limites.size) limites[posicionSiguiente] else null

            val indiceFinDeFila = minOf(
                siguienteLimite ?: (indiceFinCodigo + LONGITUD_MAXIMA_FILA),
                indiceFinCodigo + LONGITUD_MAXIMA_FILA,
                textoNormalizado.length
            )

            var fragmentoFila = textoNormalizado.substring(
                indiceFinCodigo,
                indiceFinDeFila
            )

            // Corta el fragmento antes del resumen del periodo, para no leer
            // el "Promedio Semestral" como si fuera la nota de esta materia.
            val indiceResumen = fragmentoFila.indexOf(MARCADOR_RESUMEN_PERIODO)
            if (indiceResumen >= 0) {
                fragmentoFila = fragmentoFila.substring(0, indiceResumen)
            }

            val ultimaNota = patronNota
                .findAll(fragmentoFila)
                .map { it.value.toFloat() }
                .filter { it in 0f..5f }
                .lastOrNull()

            /*
             * Si la materia aparece varias veces (cancelada y recursada), esta
             * sobreescritura hace que quede la nota de la aparición MÁS
             * RECIENTE que sí tenga nota numérica (o marca especial). Dentro
             * de una misma fila el número tiene prioridad sobre la marca.
             */
            if (ultimaNota != null) {
                resultado[codigo] = NotaImportada.Numerica(ultimaNota)
            } else {
                val marcaEncontrada = patronNotaEspecial
                    .find(fragmentoFila)
                    ?.value
                    ?.uppercase()

                if (marcaEncontrada != null) {
                    resultado[codigo] = NotaImportada.Especial(marcaEncontrada)
                }
            }
        }

        return resultado
    }

    /**
     * Códigos de [codigosConocidos] que aparecen en el tabulado, TENGAN NOTA O
     * NO (materias que se están cursando). Se devuelven en orden de aparición.
     * Una aparición cuya fila dice "cancelada" no cuenta; si la materia tiene
     * otra aparición no cancelada (cancelada y vuelta a cursar), sí cuenta.
     */
    fun extraerCodigosPresentes(
        texto: String,
        codigosConocidos: List<String>
    ): List<String> {
        val textoNormalizado = texto.replace(',', '.')
        val conocidos = codigosConocidos
            .filter { it.length >= LONGITUD_MINIMA_CODIGO }
            .toSet()
        if (conocidos.isEmpty()) return emptyList()

        val limitesLista = ArrayList<Int>()
        val apariciones = ArrayList<Pair<Int, String>>()
        for (coincidencia in patronCodigoGenerico.findAll(textoNormalizado)) {
            limitesLista.add(coincidencia.range.first)
            if (coincidencia.value in conocidos) {
                apariciones.add(coincidencia.range.first to coincidencia.value)
            }
        }
        for (codigo in conocidos) {
            if (patronCodigoGenerico.matches(codigo)) continue
            val patron = Regex("(?<![A-Za-z0-9])" + Regex.escape(codigo) + "(?![A-Za-z0-9])")
            for (c in patron.findAll(textoNormalizado)) apariciones.add(c.range.first to codigo)
        }
        apariciones.sortBy { it.first }

        val limites = limitesLista.toIntArray()
        val resultado = LinkedHashSet<String>()
        for ((inicio, codigo) in apariciones) {
            val fin = inicio + codigo.length
            val siguiente = primerIndiceMayorQue(limites, fin - 1)
            val finFila = minOf(
                if (siguiente < limites.size) limites[siguiente] else fin + LONGITUD_MAXIMA_FILA,
                fin + LONGITUD_MAXIMA_FILA,
                textoNormalizado.length
            )
            var fragmento = textoNormalizado.substring(fin, finFila)
            val resumen = fragmento.indexOf(MARCADOR_RESUMEN_PERIODO)
            if (resumen >= 0) fragmento = fragmento.substring(0, resumen)
            val totales = fragmento.indexOf(MARCADOR_TOTALES)
            if (totales >= 0) fragmento = fragmento.substring(0, totales)
            if (!filaCancelada(fragmento)) resultado.add(codigo)
        }
        return resultado.toList()
    }

    /**
     * Busca en [texto] asignaturas cuyo código no está en [codigosConocidos] y que
     * están APROBADAS (nota numérica mayor a 2.9 o marca especial C.U/E.X/A.P) o
     * EN CURSO (aparecen sin nota y sin cancelación). Las canceladas se ignoran.
     * Solo se consideran códigos con forma de asignatura que terminan en letra
     * (p. ej. "402203M"), para no confundir otros números del tabulado.
     * Las notas se leen con exactamente las mismas reglas de
     * [extraerNotasPorCodigo]. El nombre se toma de la columna "Asignatura" de la
     * fila; si no se puede leer, se usa "Asignatura <código>".
     */
    fun extraerNoSincronizadas(
        texto: String,
        codigosConocidos: List<String>
    ): List<AsignaturaNoSincronizada> {
        val textoNormalizado = texto.replace(',', '.')
        val conocidos = codigosConocidos.map { it.trim().uppercase() }.toSet()

        val candidatos = LinkedHashSet<String>()
        for (coincidencia in patronCodigoGenerico.findAll(textoNormalizado)) {
            val codigo = coincidencia.value
            if (codigo.last().isLetter() && codigo.uppercase() !in conocidos) {
                candidatos.add(codigo)
            }
        }
        if (candidatos.isEmpty()) return emptyList()

        val lista = candidatos.toList()
        val notas = extraerNotasPorCodigo(texto, lista)
        val enCurso = extraerCodigosPresentes(texto, lista).toSet()

        val resultado = ArrayList<AsignaturaNoSincronizada>()
        for (codigo in lista) {
            val nota = notas[codigo]
            if (nota != null) {
                val aprobada = when (nota) {
                    is NotaImportada.Numerica -> nota.valor > 2.9f
                    is NotaImportada.Especial -> true
                }
                if (!aprobada) continue
            } else if (codigo !in enCurso) {
                continue
            }
            resultado.add(
                AsignaturaNoSincronizada(
                    codigo = codigo,
                    nombre = extraerNombreTrasCodigo(textoNormalizado, codigo) ?: "Asignatura $codigo",
                    nota = nota
                )
            )
        }
        return resultado
    }

    // Líneas del tabulado que no forman parte del nombre de una asignatura.
    private val patronesLineaEstructural = listOf(
        Regex("""^\d{1,3}$"""),                              // grupo o sede sueltos
        Regex("""^\d{1,3}\s+\d{2}$"""),                      // "Gr Sde"
        Regex("""^\d{2}\s+[A-Za-z]\s+[A-Za-z]{2}\s+\d.*$"""),  // "00 N AB 3"
        Regex("""^\d{4}-\d{2}-\d{2}.*$"""),                  // fecha de cancelación
        Regex(
            """(?i)cancelaci|reactivaci|total\s+(asignaturas|cr)|verifique|^c[oó]digo\b|""" +
                    """^listado|^cal\.?$|^sede$|^gr\s+sde|^asignatura$|^hab\.?$|https?:"""
        )
    )
    private val patronPrefijoGrupoSede = Regex("""^\S{1,3}\s+\d{2}\s+(?=.*\p{L})""")
    private val patronColasColumnas = Regex("""\s+\d{2}\s+[A-Za-z]\s+[A-Za-z]{2}\s+\d.*$""")

    /** Parte de una línea que corresponde al nombre, o null si es solo estructura. */
    private fun limpiarLineaNombre(linea: String): String? {
        var x = linea.trim()
        if (x.isEmpty()) return null
        x = patronPrefijoGrupoSede.replace(x, "")
        x = patronColasColumnas.replace(x, "").trim()
        if (x.isEmpty() || !x.any { it.isLetter() }) return null
        if (patronesLineaEstructural.any { it.containsMatchIn(x) }) return null
        return x
    }

    /**
     * Nombre de la asignatura en la fila de [codigo]. Formato del tabulado:
     * "<código> <Gr> <Sde> <Asignatura> <Sede> <TM> <Fun> <Crd>", donde el
     * nombre puede ocupar dos líneas. Se usa la última aparición del código.
     *
     * Estrategias, de la más precisa a la más tolerante al orden en que la
     * librería de PDF entregue el texto:
     * 1. Texto entre "Gr Sde" y las columnas "Sede TM Fun Crd".
     * 2. Primeras líneas con letras después del código (sin columnas ni fechas).
     * 3. Últimas líneas con letras antes del código, desde la fila anterior.
     */
    private fun extraerNombreTrasCodigo(texto: String, codigo: String): String? {
        val inicio = texto.lastIndexOf(codigo)
        if (inicio < 0) return null
        val desde = inicio + codigo.length
        val siguiente = patronCodigoGenerico.find(texto, desde)?.range?.first ?: texto.length
        val hasta = minOf(siguiente, desde + 250)
        val fragmento = texto.substring(desde, maxOf(desde, hasta))

        fun compactar(crudo: String): String? =
            // Los nombres de asignaturas solo tienen letras: se corta en el primer número.
            crudo.takeWhile { !it.isDigit() }
                .replace(Regex("""\s+"""), " ")
                .trim(' ', '-', '.', ':', ',')
                .takeIf { it.length >= 4 }
                ?.take(80)

        // 1. Patrón completo de la fila (sobre un tramo más largo, por si la
        //    siguiente fila empieza antes de las columnas).
        val tramoLargo = texto.substring(desde, minOf(texto.length, desde + 250))
        patronNombreFila.find(tramoLargo)?.groupValues?.get(1)?.let { crudo ->
            compactar(crudo)?.let { return it }
        }

        // 2. Líneas con letras después del código.
        val despues = fragmento.lines().mapNotNull { limpiarLineaNombre(it) }.take(2)
        compactar(despues.joinToString(" "))?.let { return it }

        // 3. Líneas con letras antes del código (nombre impreso encima de la fila).
        val anterior = patronCodigoGenerico.findAll(texto.substring(0, inicio)).lastOrNull()
        val desdeAnterior = anterior?.let { it.range.last + 1 } ?: maxOf(0, inicio - 200)
        val antes = texto.substring(desdeAnterior, inicio).lines()
            .mapNotNull { limpiarLineaNombre(it) }
            .takeLast(2)
        return compactar(antes.joinToString(" "))
    }

    fun extraerGruposPorCodigo(texto: String): Map<String, String> {
        val textoNormalizado = texto.replace(',', '.')

        val limitesLista = ArrayList<Int>()
        val codigosLista = ArrayList<Pair<Int, String>>()

        for (coincidencia in patronCodigoGenerico.findAll(textoNormalizado)) {
            val inicio = coincidencia.range.first
            limitesLista.add(inicio)
            codigosLista.add(inicio to coincidencia.value)
        }

        if (codigosLista.isEmpty()) return emptyMap()

        val limites = limitesLista.toIntArray()
        val resultado = LinkedHashMap<String, String>()

        for ((indiceInicio, codigo) in codigosLista) {
            val indiceFinCodigo = indiceInicio + codigo.length
            val posicionSiguiente = primerIndiceMayorQue(limites, indiceFinCodigo - 1)
            val siguienteLimite =
                if (posicionSiguiente < limites.size) limites[posicionSiguiente] else null

            val indiceFinDeFila = minOf(
                siguienteLimite ?: (indiceFinCodigo + LONGITUD_MAXIMA_FILA),
                indiceFinCodigo + LONGITUD_MAXIMA_FILA,
                textoNormalizado.length
            )

            var fragmentoFila = textoNormalizado.substring(
                indiceFinCodigo,
                indiceFinDeFila
            )

            val indiceResumen = fragmentoFila.indexOf(MARCADOR_RESUMEN_PERIODO)
            if (indiceResumen >= 0) {
                fragmentoFila = fragmentoFila.substring(0, indiceResumen)
            }

            val grupo = extraerGrupoDeFragmento(fragmentoFila)
            if (grupo != null) {
                resultado[codigo.uppercase()] = grupo
            }
        }

        return resultado
    }

    private fun extraerGrupoDeFragmento(fragmento: String): String? {
        val matchExplicito = patronGrupoExplicito.find(fragmento)
        if (matchExplicito != null) {
            return matchExplicito.groupValues[1].padStart(2, '0')
        }

        val matchPrefijado = patronGrupoPrefijado.find(fragmento)
        if (matchPrefijado != null) {
            return matchPrefijado.groupValues[1].padStart(2, '0')
        }

        val matchDosDigitos = patronGrupoDosDigitos.find(fragmento)
        if (matchDosDigitos != null) {
            return matchDosDigitos.value
        }

        val matchEnteroDosDigitos = patronNumeroDosDigitos.find(fragmento)
        if (matchEnteroDosDigitos != null) {
            return matchEnteroDosDigitos.value
        }

        return null
    }

    /**
     * Posición del primer elemento de [ordenado] estrictamente mayor que
     * [valor] (o `ordenado.size` si no hay ninguno). Búsqueda binaria.
     */
    private fun primerIndiceMayorQue(ordenado: IntArray, valor: Int): Int {
        var bajo = 0
        var alto = ordenado.size
        while (bajo < alto) {
            val medio = (bajo + alto) ushr 1
            if (ordenado[medio] > valor) alto = medio else bajo = medio + 1
        }
        return bajo
    }
}
