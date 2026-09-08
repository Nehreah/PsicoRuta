package com.renea.psicologiauv.data

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.renea.psicologiauv.model.NotaImportada

/**
 * Se encarga de leer un PDF de notas (el "tabulado" / historial académico que
 * exporta el sistema de la universidad) y de emparejar cada código de
 * asignatura conocido con la nota que le corresponde.
 *
 * No depende de un formato exacto de tabla ni de que cada fila esté en una
 * sola línea del PDF: busca cada código de asignatura dentro del texto
 * completo extraído y, en el tramo de texto que va desde ese código hasta el
 * siguiente límite de fila, busca números con forma de nota (0.0 a 5.0).
 *
 * Reglas verificadas contra un tabulado real:
 *
 * 1. Una misma materia puede aparecer más de una vez si el estudiante la
 *    canceló y la volvió a cursar más adelante (mismo código, dos filas en
 *    periodos distintos). En ese caso se usa la nota de la aparición MÁS
 *    RECIENTE que sí tenga una nota numérica válida, que es el resultado
 *    académico vigente de esa materia.
 * 2. Al final de cada periodo el tabulado imprime un resumen ("Resumen de
 *    Finalización por periodo ... Promedio Semestral: X.XX"). Si la última
 *    materia de un periodo quedó cancelada (sin nota), ese resumen cae
 *    dentro de su tramo de texto y el "Promedio Semestral" se podría leer
 *    por error como si fuera la nota de esa materia. Por eso el tramo de
 *    cada fila se corta apenas aparece "Resumen de Finalización".
 * 3. El límite de una fila NO se calcula solo contra la siguiente materia
 *    CONOCIDA: se calcula contra la siguiente aparición de CUALQUIER token
 *    con forma de código de asignatura, exista o no en el pensum cargado.
 * 4. Los códigos de menos de 5 caracteres (los provisionales "1".."5" que
 *    usan las electivas mientras no se resuelven a su código real) se ignoran
 *    por completo al buscar coincidencias PROPIAS en el PDF.
 * 5. Algunas materias no tienen nota numérica en el tabulado sino una marca
 *    especial como "C.U", "E.X" o "A.P" (por ejemplo seminarios de grado o
 *    materias exoneradas), que indica que la materia quedó aprobada sin una
 *    calificación en la escala 0.0-5.0. Esas materias se reportan como
 *    [NotaImportada.Especial] en vez de [NotaImportada.Numerica], para que
 *    quien las reciba (Estudiante/ProgramaM) las cuente como aprobadas pero
 *    las excluya del cálculo de promedios.
 */
class ImportadorNotas(private val context: Context) {

    init {
        // Necesario una única vez antes de usar cualquier clase de PdfBox-Android:
        // carga las fuentes/recursos que la librería necesita desde los assets.
        // Llamarlo varias veces no genera problema, PdfBox lo controla internamente.
        PDFBoxResourceLoader.init(context.applicationContext)
    }

    /** Extrae todo el texto plano del PDF ubicado en [uri]. */
    fun extraerTexto(uri: Uri): String {
        context.contentResolver.openInputStream(uri).use { entrada ->
            requireNotNull(entrada) { "No se pudo abrir el archivo seleccionado." }
            PDDocument.load(entrada).use { documento ->
                val extractor = PDFTextStripper()
                return extractor.getText(documento)
            }
        }
    }

    /**
     * Recorre [texto] buscando cada uno de los [codigosConocidos] y, para cada
     * aparición, extrae la nota asociada más cercana: un número (0.0 a 5.0)
     * o, si no hay número, una marca especial como "C.U", "E.X" o "A.P".
     *
     * Devuelve un mapa código -> [NotaImportada], únicamente con los códigos
     * que sí se encontraron y para los que se pudo leer una nota válida
     * (numérica o especial).
     */
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