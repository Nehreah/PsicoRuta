package com.renea.psicologiauv.data

import com.renea.psicologiauv.model.NotaImportada

/**
 * Analizador puro del texto extraído de un tabulado SIRA. Se mantiene separado
 * del acceso al PDF para poder probar la lógica sin Android ni archivos reales.
 * La lógica de extracción se conserva tal como estaba en ImportadorNotas.
 */
internal object AnalizadorNotas {
    fun extraerNotasPorCodigo(
        texto: String,
        codigosConocidos: List<String>
    ): Map<String, NotaImportada> {

        // Algunos reportes usan coma decimal ("4,2") en vez de punto ("4.2").
        val textoNormalizado = texto.replace(',', '.')

        // Patrón de nota: un dígito del 0 al 5, seguido de un punto decimal y
        // 1 o 2 decimales. Exigir el decimal evita confundir la nota con
        // columnas de créditos o semestre, que casi siempre son enteras.
        val patronNota = Regex("""[0-5]\.\d{1,2}""")

        // Marca especial que el tabulado imprime en la columna CAL en vez de
        // un número, cuando la materia se aprueba sin nota numérica (p. ej.
        // seminarios/prácticas de grado marcados "C.U", o materias
        // exoneradas marcadas "E.X"). Se busca como token completo (los
        // mismos límites que patronCodigoGenerico usa) para no confundirla
        // con fragmentos de otras palabras, y sin distinguir mayúsculas de
        // minúsculas por si el PDF la imprime distinto.
        val patronNotaEspecial = Regex(
            """(?<![A-Za-z0-9])(C\.U|E\.X|A\.P)(?![A-Za-z0-9])""",
            RegexOption.IGNORE_CASE
        )

        // Encabezado que marca el final de la lista de materias de un
        // periodo y el comienzo del resumen (créditos, promedio semestral).
        val marcadorResumenPeriodo = "Resumen de Finalización"

        // Los códigos reales de la universidad tienen 7 caracteres o más
        // (p. ej. "402180M"). Las electivas que el estudiante aún no ha
        // resuelto usan códigos provisionales de un solo dígito ("1", "2",
        // "3"...) que NO existen así en el tabulado real.
        val longitudMinimaCodigo = 5

        val codigosValidos = codigosConocidos
            .distinct()
            .filter { it.length >= longitudMinimaCodigo }

        // Patrón GENÉRICO de "forma de código de asignatura".
        //
        // Se usa únicamente para encontrar dónde empieza la siguiente fila,
        // independientemente de si el código existe en el pensum del estudiante.
        val patronCodigoGenerico = Regex(
            """(?<![A-Za-z0-9])\d{5,7}[A-Za-z]?(?![A-Za-z0-9])"""
        )

        // Todas las apariciones de cualquier código de asignatura del PDF.
        // Sirven únicamente como límites de fila.
        val limitesDeFila = patronCodigoGenerico.findAll(textoNormalizado)
            .map { it.range.first }
            .toList()

        // Todas las apariciones de los códigos CONOCIDOS, en el orden en que
        // aparecen en el documento.
        val ocurrencias = mutableListOf<Pair<Int, String>>()

        for (codigo in codigosValidos) {
            val patronCodigo = Regex(
                "(?<![A-Za-z0-9])" +
                        Regex.escape(codigo) +
                        "(?![A-Za-z0-9])"
            )

            for (coincidencia in patronCodigo.findAll(textoNormalizado)) {
                ocurrencias.add(coincidencia.range.first to codigo)
            }
        }

        ocurrencias.sortBy { it.first }

        val resultado = mutableMapOf<String, NotaImportada>()

        // Límite máximo de caracteres para el tramo de una fila.
        val longitudMaximaFila = 400

        for ((indiceInicio, codigo) in ocurrencias) {

            val indiceFinCodigo = indiceInicio + codigo.length

            /*
             * IMPORTANTE:
             *
             * El siguiente código que aparece después del código actual
             * marca el final REAL de esta fila.
             *
             * No buscamos la nota más cercana fuera de este límite.
             *
             * Por ejemplo, en el PDF:
             *
             * 402205M ... ESTADÍSTICA ... 3 4.8
             *
             * 404028C ... DANZAS ... 3
             *
             * 504057C ... BALLET FIT ... 3 4.7
             *
             * La nota 4.7 nunca debe pertenecer a 402205M.
             */
            val siguienteLimite = limitesDeFila
                .firstOrNull { it > indiceInicio }

            val indiceFinDeFila = minOf(
                siguienteLimite ?: (indiceFinCodigo + longitudMaximaFila),
                indiceFinCodigo + longitudMaximaFila,
                textoNormalizado.length
            )

            var fragmentoFila = textoNormalizado.substring(
                indiceFinCodigo,
                indiceFinDeFila
            )

            // Corta el fragmento antes del resumen del periodo, para no leer
            // el "Promedio Semestral" como si fuera la nota de esta materia.
            val indiceResumen = fragmentoFila.indexOf(marcadorResumenPeriodo)

            if (indiceResumen >= 0) {
                fragmentoFila = fragmentoFila.substring(0, indiceResumen)
            }

            val notasEncontradas = patronNota
                .findAll(fragmentoFila)
                .map { it.value.toFloat() }
                .filter { it in 0f..5f }
                .toList()

            /*
             * Si la materia aparece varias veces (cancelada y recursada),
             * esta sobreescritura hace que quede la nota de la aparición
             * MÁS RECIENTE que sí tenga nota numérica (o marca especial).
             *
             * Se prioriza el número sobre la marca especial dentro de la
             * MISMA fila: si por algún motivo ambos aparecieran en el mismo
             * fragmento, la nota numérica es la que realmente calificó la
             * materia. Solo se busca la marca especial cuando no apareció
             * ningún número en esta fila.
             */
            if (notasEncontradas.isNotEmpty()) {
                resultado[codigo] = NotaImportada.Numerica(notasEncontradas.last())
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
}
