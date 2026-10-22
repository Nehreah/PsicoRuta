package com.renea.psicologiauv.data

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
        """(?<![A-Za-z0-9])(C\.U|E\.X|A\.P)(?![A-Za-z0-9])""",
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
