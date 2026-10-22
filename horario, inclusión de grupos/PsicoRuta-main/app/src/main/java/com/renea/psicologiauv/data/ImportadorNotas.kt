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

    // PdfBox-Android necesita inicializarse una única vez antes de usarse. Se
    // hace de forma perezosa (la primera vez que realmente se lee un PDF) y no
    // al construir el importador: así no se ejecuta en el hilo principal
    // durante la composición de la pantalla, sino en el hilo de E/S donde ya
    // se llama a [extraerTexto].
    private val pdfBoxInicializado: Boolean by lazy {
        PDFBoxResourceLoader.init(context.applicationContext)
        true
    }

    /** Extrae todo el texto plano del PDF ubicado en [uri]. */
    fun extraerTexto(uri: Uri): String {
        check(pdfBoxInicializado)
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
    ): Map<String, NotaImportada> =
        AnalizadorNotas.extraerNotasPorCodigo(texto, codigosConocidos)
}