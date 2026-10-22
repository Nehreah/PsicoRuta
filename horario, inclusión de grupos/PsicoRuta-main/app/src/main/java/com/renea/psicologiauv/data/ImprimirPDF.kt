package com.renea.psicologiauv.data

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.ProgramaM
import java.io.File
import java.io.FileOutputStream

class ImprimirPDF(private val context: Context) {

    private val anchoPagina = 595
    private val altoPagina = 842
    private val margenIzquierdo = 40f
    private val margenDerecho = 40f
    private val limiteInferior = 800f

    private val estiloTitulo = Paint().apply { textSize = 18f; isFakeBoldText = true }
    private val estiloSubtitulo = Paint().apply { textSize = 14f; isFakeBoldText = true }
    private val estiloTexto = Paint().apply { textSize = 12f }
    private val estiloTextoBold = Paint().apply { textSize = 12f; isFakeBoldText = true }
    private val estiloCabecera = Paint().apply { textSize = 12f; isFakeBoldText = true }
    private val estiloDatos = Paint().apply { textSize = 13f }

    private val colorFondoCabecera = 0xFFE0E0E0.toInt()
    private val colorFondoFilaPar = 0xFFF5F5F5.toInt()
    private val colorFondoFilaImpar = 0xFFFFFFFF.toInt()

    /**
     * Las 5 líneas profesionales que existen en el pénsum (Social,
     * Organizacional, Educativa, Clínica y NeuroClínica) se agrupan en 4
     * "líneas reales" a efectos de este informe: Clínica y NeuroClínica
     * cuentan como una sola línea (el estudiante debe completar los 3
     * niveles en UNA de las 4, y al menos 1 nivel en cada una de las otras
     * 3; si esa línea completa es Clínica o NeuroClínica, la otra del par
     * queda excluida por completo, no cuenta como "otra línea" aparte).
     */
    private val gruposLinea = listOf(
        listOf("Social"),
        listOf("Organizacional"),
        listOf("Educativa"),
        listOf("Clínica", "NeuroClínica")
    )

    private fun grupoDe(linea: String): List<String> =
        gruposLinea.find { it.contains(linea) } ?: listOf(linea)

    private fun etiquetaGrupo(grupo: List<String>): String = grupo.joinToString("/")

    fun generarYGuardar(programa: ProgramaM): Uri? {
        val estudiante = programa.estudiante ?: return null
        val documento = generarDocumento(programa, estudiante)
        val nombreArchivo = "informe_avance_${estudiante.codigo}.pdf"

        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                guardarConMediaStore(documento, nombreArchivo)
            } else {
                guardarEnAlmacenamientoExterno(documento, nombreArchivo)
            }
        } finally {
            documento.close()
        }
    }

    private fun generarDocumento(programa: ProgramaM, estudiante: Estudiante): PdfDocument {
        val documento = PdfDocument()
        try {
            var numeroPagina = 1
            var pagina = documento.startPage(nuevaPaginaInfo(numeroPagina))
            var canvas = pagina.canvas
            var y = 50f

            fun saltoDePaginaSiHaceFalta(espacioNecesario: Float = 30f) {
                if (y > limiteInferior - espacioNecesario) {
                    documento.finishPage(pagina)
                    numeroPagina++
                    pagina = documento.startPage(nuevaPaginaInfo(numeroPagina))
                    canvas = pagina.canvas
                    y = 50f
                }
            }

            // --- TÍTULO CENTRADO ---
            val titulo = "INFORME DE AVANCE ACADÉMICO"
            val anchoTitulo = estiloTitulo.measureText(titulo)
            val xTitulo = (anchoPagina - anchoTitulo) / 2
            canvas.drawText(titulo, xTitulo, y, estiloTitulo)
            y += 40f

            // --- DATOS DEL ESTUDIANTE ---
            canvas.drawText("DATOS DEL ESTUDIANTE", margenIzquierdo, y, estiloSubtitulo)
            y += 25f

            // Línea 1: Estudiante y Código
            canvas.drawText("Estudiante: ${estudiante.nombre}", margenIzquierdo, y, estiloDatos)
            val textoCodigo = "Código: ${if (estudiante.codigo > 0) estudiante.codigo else "N/A"}"
            val anchoCodigo = estiloDatos.measureText(textoCodigo)
            canvas.drawText(textoCodigo, anchoPagina - margenDerecho - anchoCodigo, y, estiloDatos)
            y += 25f

            // Línea 2: Programa y Línea profesional
            val textoPrograma = "Programa: ${programa.nombre} (${programa.codigo})"
            canvas.drawText(textoPrograma, margenIzquierdo, y, estiloDatos)
            val lineaEstado = if (programa.requisitosPracticasLinea()) "Completa" else "Incompleta"
            val textoLinea = "Línea profesional: ${estudiante.linea} ($lineaEstado)"
            val anchoLinea = estiloDatos.measureText(textoLinea)
            canvas.drawText(textoLinea, anchoPagina - margenDerecho - anchoLinea, y, estiloDatos)
            y += 35f

            // --- TABLA 1: RESUMEN GENERAL ---
            val anchoTabla = 320f
            val xTabla = (anchoPagina - anchoTabla) / 2
            y = dibujarTablaResumenGeneral(canvas, programa, xTabla, y)
            y += 25f

            // --- TABLA 2: AVANCE POR ÁREA ---
            val avances = programa.avanceAreas()
            val anchoTablaAreas = 420f
            val xTablaAreas = (anchoPagina - anchoTablaAreas) / 2
            y = dibujarTablaAvanceAreas(canvas, avances, xTablaAreas, y)
            y += 25f

            // --- TABLA DE REQUISITOS: PRÁCTICAS O GRADUARSE ---
            val practicasSupervisadasI = estudiante.asignaturas.find { it.codigo.trim() == "402222M" }
            val practicasSupervisadasII = estudiante.asignaturas.find { it.codigo.trim() == "402224M" }
            val yaCursoPracticas = (practicasSupervisadasI != null && (practicasSupervisadasI.nota > 0f || practicasSupervisadasI.notaEspecial != null))

            saltoDePaginaSiHaceFalta(200f)
            y = if (yaCursoPracticas) {
                dibujarTablaRequisitosGraduacion(canvas, programa, estudiante, y)
            } else {
                dibujarTablaRequisitosPracticas(canvas, estudiante, y)
            }
            y += 20f

            // --- MATERIAS CURSADAS POR SEMESTRE ---
            saltoDePaginaSiHaceFalta()
            canvas.drawText("MATERIAS CURSADAS POR SEMESTRE", margenIzquierdo, y, estiloSubtitulo)
            y += 25f

            val cursadasPorSemestre = estudiante.asignaturas
                .filter { (it.nota > 0f || it.notaEspecial != null) && it.semestre != 99 }
                .groupBy { it.semestre }
                .toSortedMap()

            if (cursadasPorSemestre.isEmpty()) {
                canvas.drawText("Todavía no hay materias con nota registrada.", margenIzquierdo + 10f, y, estiloTexto)
                y += 20f
            } else {
                val anchoCodigoCol = 70f
                val anchoNombreCol = 280f
                val anchoNotaCol = 80f
                val anchoTotal = anchoCodigoCol + anchoNombreCol + anchoNotaCol + 20f
                val margenTabla = (anchoPagina - anchoTotal - margenIzquierdo - margenDerecho) / 2 + margenIzquierdo

                cursadasPorSemestre.forEach { (semestre, materias) ->
                    saltoDePaginaSiHaceFalta(40f)

                    val etiquetaSemestre = if (semestre == 0) "ACTIVIDADES COMPLEMENTARIAS" else "SEMESTRE $semestre"
                    val anchoTexto = estiloSubtitulo.measureText(etiquetaSemestre)
                    val xCentrado = (anchoPagina - anchoTexto) / 2
                    canvas.drawText(etiquetaSemestre, xCentrado, y, estiloSubtitulo)
                    y += 20f

                    canvas.drawLine(margenTabla, y, margenTabla + anchoTotal, y, Paint().apply { strokeWidth = 1f })
                    y += 6f

                    val paintFondo = Paint().apply { color = colorFondoCabecera; style = Paint.Style.FILL }
                    canvas.drawRect(margenTabla, y - 14f, margenTabla + anchoTotal, y + 4f, paintFondo)

                    val paintBorde = Paint().apply { strokeWidth = 1f; style = Paint.Style.STROKE }
                    canvas.drawRect(margenTabla, y - 14f, margenTabla + anchoTotal, y + 4f, paintBorde)

                    var x = margenTabla + 5f
                    canvas.drawText("CÓDIGO", x, y, estiloCabecera)
                    x += anchoCodigoCol + 5f
                    canvas.drawText("ASIGNATURA", x, y, estiloCabecera)
                    x += anchoNombreCol + 5f
                    canvas.drawText("NOTA", x, y, estiloCabecera)
                    y += 18f

                    var filaIndex = 0
                    materias.forEach { materia ->
                        saltoDePaginaSiHaceFalta(20f)

                        val colorFondo = if (filaIndex % 2 == 0) colorFondoFilaPar else colorFondoFilaImpar
                        val paintFondoFila = Paint().apply { color = colorFondo; style = Paint.Style.FILL }
                        canvas.drawRect(margenTabla, y - 14f, margenTabla + anchoTotal, y + 4f, paintFondoFila)
                        canvas.drawRect(margenTabla, y - 14f, margenTabla + anchoTotal, y + 4f, paintBorde)

                        x = margenTabla + 5f
                        canvas.drawText(materia.codigo, x, y, estiloTexto)
                        x += anchoCodigoCol + 5f

                        val nombreMostrar = if (materia.nombre.length > 35) {
                            materia.nombre.substring(0, 32) + "..."
                        } else {
                            materia.nombre
                        }
                        canvas.drawText(nombreMostrar, x, y, estiloTexto)
                        x += anchoNombreCol + 5f

                        val notaTexto = materia.textoNota()
                        canvas.drawText(notaTexto, x, y, estiloTexto)

                        y += 18f
                        filaIndex++
                    }

                    y += 4f
                    canvas.drawLine(margenTabla, y, margenTabla + anchoTotal, y, Paint().apply { strokeWidth = 1f })
                    y += 14f

                    val promedio = programa.promedioSemestre(semestre)
                    if (promedio > 0f) {
                        val textoPromedio = "Promedio del semestre: ${"%.2f".format(promedio)}"
                        canvas.drawText(textoPromedio, margenTabla + 5f, y, estiloTextoBold)
                        y += 18f
                    }

                    y += 10f
                }
            }

            documento.finishPage(pagina)
            return documento

        } catch (e: Exception) {
            e.printStackTrace()
            documento.close()
            throw e
        }
    }

    /**
     * Dibuja una tabla con el título "REQUISITOS PRÁCTICAS" que incluye:
     * - Las 3 asignaturas de la línea elegida
     * - Ética del Ejercicio Profesional
     * - Evaluación y Diagnóstico Psicológico
     * - Práctica de Fundamentación Profesional I
     * - Práctica de Fundamentación Profesional II
     * - Créditos de profesionalización (ej: "Créditos de profesionalización 24/27")
     * - Una fila por cada una de las OTRAS 3 líneas (agrupando Clínica/NeuroClínica como una sola)
     *
     * Las palabras NO se cortan, la tabla se ajusta automáticamente.
     */
    private fun dibujarTablaRequisitosPracticas(
        canvas: android.graphics.Canvas,
        estudiante: Estudiante,
        y: Float
    ): Float {
        var yActual = y

        // Título de la tabla (cambiado a "REQUISITOS PRÁCTICAS")
        canvas.drawText("REQUISITOS PRÁCTICAS", margenIzquierdo, yActual, estiloSubtitulo)
        yActual += 20f

        // Obtener las asignaturas de la línea del estudiante
        val lineaEstudiante = estudiante.linea
        val materiasLinea = estudiante.asignaturas
            .filter { it.lineaProfesional == lineaEstudiante && it.aprobo() }
            .sortedBy { it.semestre }
            .take(3)  // Tomar solo las 3 primeras

        // Obtener Ética del Ejercicio Profesional (402203M)
        val etica = estudiante.asignaturas
            .find { it.codigo == "402203M" }
            ?.takeIf { it.aprobo() }

        // Obtener Evaluación y Diagnóstico Psicológico (402204M)
        val diagnostico = estudiante.asignaturas
            .find { it.codigo == "402204M" }
            ?.takeIf { it.aprobo() }

        // Obtener Práctica de Fundamentación Profesional I (402212M)
        val fundamentacionI = estudiante.asignaturas
            .find { it.codigo == "402212M" }
            ?.takeIf { it.aprobo() }

        // Obtener Práctica de Fundamentación Profesional II (402221M)
        val fundamentacionII = estudiante.asignaturas
            .find { it.codigo == "402221M" }
            ?.takeIf { it.aprobo() }

        // Construir la lista de materias a mostrar
        val materiasMostrar = mutableListOf<Pair<String, String>>()

        // Agregar las materias de la línea
        materiasLinea.forEach { materia ->
            materiasMostrar.add(
                materia.nombre to materia.textoNota()
            )
        }

        // Si hay menos de 3 materias de línea, agregar placeholders
        while (materiasMostrar.size < 3) {
            materiasMostrar.add("---" to "---")
        }

        // Agregar Ética y Diagnóstico
        materiasMostrar.add(
            (etica?.nombre ?: "Ética del Ejercicio Profesional") to
                    (etica?.textoNota() ?: "No cursada")
        )
        materiasMostrar.add(
            (diagnostico?.nombre ?: "Evaluación y Diagnóstico Psicológico") to
                    (diagnostico?.textoNota() ?: "No cursada")
        )

        // Agregar Práctica de Fundamentación Profesional I y II
        materiasMostrar.add(
            (fundamentacionI?.nombre ?: "Práctica de Fundamentación Profesional I") to
                    (fundamentacionI?.textoNota() ?: "No cursada")
        )
        materiasMostrar.add(
            (fundamentacionII?.nombre ?: "Práctica de Fundamentación Profesional II") to
                    (fundamentacionII?.textoNota() ?: "No cursada")
        )

        // Agregar una materia aprobada de cada una de las OTRAS 3 líneas
        val grupoPropio = grupoDe(lineaEstudiante)
        val otrosGrupos = gruposLinea.filter { it != grupoPropio }

        otrosGrupos.forEach { grupo ->
            val etiquetaLinea = etiquetaGrupo(grupo)

            val materiaAprobada = estudiante.asignaturas
                .filter { it.lineaProfesional in grupo && it.aprobo() }
                .minByOrNull { it.semestre }

            if (materiaAprobada != null) {
                materiasMostrar.add(
                    materiaAprobada.nombre to materiaAprobada.textoNota()
                )
            } else {
                materiasMostrar.add(
                    "No hay asignatura aprobada en la línea de $etiquetaLinea" to ""
                )
            }
        }

        // Calcular créditos de profesionalización
        // Asignaturas donde lineaProfesional es diferente de "No" (es decir, tienen una línea asignada)
        val asignaturasProfesionalizacion = estudiante.asignaturas
            .filter { it.lineaProfesional != "No" && it.lineaProfesional.isNotBlank() }
        val creditosAprobados = asignaturasProfesionalizacion
            .filter { it.aprobo() }
            .sumOf { it.creditos }
        val creditosConstante = 27  // Constante definida

        // Agregar la fila de créditos de profesionalización
        materiasMostrar.add(
            "Créditos de profesionalización" to "$creditosAprobados/$creditosConstante"
        )

        // Calcular dimensiones de la tabla - usando el ancho completo de la página
        // para que no se corten las palabras
        val anchoNombreCol = 480f  // Aumentado para evitar cortes
        val anchoNotaCol = 80f
        val anchoTotal = anchoNombreCol + anchoNotaCol + 20f
        val margenTabla = (anchoPagina - anchoTotal - margenIzquierdo - margenDerecho) / 2 + margenIzquierdo

        // Dibujar la tabla
        val paintFondo = Paint().apply { color = colorFondoCabecera; style = Paint.Style.FILL }
        canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintFondo)

        val paintBorde = Paint().apply { strokeWidth = 1f; style = Paint.Style.STROKE }
        canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintBorde)

        // Cabeceras
        var xActual = margenTabla + 5f
        canvas.drawText("ASIGNATURA", xActual, yActual, estiloCabecera)
        xActual += anchoNombreCol + 5f
        canvas.drawText("NOTA", xActual, yActual, estiloCabecera)
        yActual += 18f

        // Dibujar filas - SIN CORTAR palabras
        var filaIndex = 0
        materiasMostrar.forEach { (nombre, nota) ->
            val colorFondoFila = if (filaIndex % 2 == 0) colorFondoFilaPar else colorFondoFilaImpar
            val paintFondoFila = Paint().apply { color = colorFondoFila; style = Paint.Style.FILL }
            canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintFondoFila)
            canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintBorde)

            // Nombre SIN truncar - se muestra completo
            xActual = margenTabla + 5f
            canvas.drawText(nombre, xActual, yActual, estiloTexto)

            // Nota
            xActual = margenTabla + anchoNombreCol + 5f + (anchoNotaCol - estiloTexto.measureText(nota)) / 2
            canvas.drawText(nota, xActual, yActual, estiloTexto)

            yActual += 18f
            filaIndex++
        }

        yActual += 15f
        return yActual
    }

    /**
     * Dibuja una tabla con el título "REQUISITOS PARA GRADUARSE" que incluye:
     * - Práctica Profesional Supervisada I (código 402222M)
     * - Práctica Profesional Supervisada II (código 402224M)
     * - Idiomas I (buscada por nombre)
     * - Idiomas II (buscada por nombre)
     * - Créditos complementarios (formato "#/5")
     * - Relación de créditos de la carrera ("#/168") y créditos faltantes
     *
     * Idiomas es la única asignatura que se busca por nombre, las demás por código.
     */
    private fun dibujarTablaRequisitosGraduacion(
        canvas: android.graphics.Canvas,
        programa: ProgramaM,
        estudiante: Estudiante,
        y: Float
    ): Float {
        var yActual = y

        // Título de la tabla
        canvas.drawText("REQUISITOS PARA GRADUARSE", margenIzquierdo, yActual, estiloSubtitulo)
        yActual += 20f

        val asignaturas = estudiante.asignaturas

        // 1. Prácticas profesionales supervisadas (buscadas por código)
        val practicaI = asignaturas.find { it.codigo.trim() == "402222M" }
        val practicaII = asignaturas.find { it.codigo.trim() == "402224M" }

        // 2. Idiomas I y II (única asignatura buscada por nombre, no por código)
        val idiomasICandidatas = asignaturas.filter {
            it.nombre.equals("Idiomas I", ignoreCase = true) || it.nombre.equals("Inglés I", ignoreCase = true)
        }
        val idiomaI = idiomasICandidatas.firstOrNull { it.aprobo() }
            ?: idiomasICandidatas.firstOrNull { it.nota > 0f || it.notaEspecial != null }
            ?: idiomasICandidatas.firstOrNull()

        val idiomasIICandidatas = asignaturas.filter {
            it.nombre.equals("Idiomas II", ignoreCase = true) || it.nombre.equals("Inglés II", ignoreCase = true)
        }
        val idiomaII = idiomasIICandidatas.firstOrNull { it.aprobo() }
            ?: idiomasIICandidatas.firstOrNull { it.nota > 0f || it.notaEspecial != null }
            ?: idiomasIICandidatas.firstOrNull()

        // 3. Créditos complementarios
        val compAprobados = programa.creditosComplementariosAprobados()
        val compTotales = programa.creditosComplementarios

        // 4. Créditos de la carrera y créditos faltantes
        val creditosCursados = programa.creditosQueSirvenParaCarrera()
        val creditosCarrera = programa.creditosCarrera
        val creditosFaltantes = (creditosCarrera - creditosCursados).coerceAtLeast(0)

        fun notaTextoDe(materia: Asignatura?): String = when {
            materia == null -> "No cursada"
            materia.nota > 0f || materia.notaEspecial != null -> materia.textoNota()
            else -> "No cursada"
        }

        val filas = listOf(
            (practicaI?.nombre ?: "Práctica Profesional Supervisada I") to notaTextoDe(practicaI),
            (practicaII?.nombre ?: "Práctica Profesional Supervisada II") to notaTextoDe(practicaII),
            (idiomaI?.nombre ?: "Idiomas I") to notaTextoDe(idiomaI),
            (idiomaII?.nombre ?: "Idiomas II") to notaTextoDe(idiomaII),
            "Créditos complementarios" to "$compAprobados/$compTotales",
            "Créditos de la carrera" to "$creditosCursados/$creditosCarrera",
            "Créditos faltantes" to "$creditosFaltantes"
        )

        // Dimensiones de la tabla usando el ancho completo para evitar cortes
        val anchoNombreCol = 480f
        val anchoNotaCol = 80f
        val anchoTotal = anchoNombreCol + anchoNotaCol + 20f
        val margenTabla = (anchoPagina - anchoTotal - margenIzquierdo - margenDerecho) / 2 + margenIzquierdo

        val paintFondo = Paint().apply { color = colorFondoCabecera; style = Paint.Style.FILL }
        canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintFondo)

        val paintBorde = Paint().apply { strokeWidth = 1f; style = Paint.Style.STROKE }
        canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintBorde)

        // Cabeceras
        var xActual = margenTabla + 5f
        canvas.drawText("ASIGNATURA", xActual, yActual, estiloCabecera)
        xActual += anchoNombreCol + 5f
        canvas.drawText("NOTA", xActual, yActual, estiloCabecera)
        yActual += 18f

        // Filas
        var filaIndex = 0
        filas.forEach { (nombre, nota) ->
            val colorFondoFila = if (filaIndex % 2 == 0) colorFondoFilaPar else colorFondoFilaImpar
            val paintFondoFila = Paint().apply { color = colorFondoFila; style = Paint.Style.FILL }
            canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintFondoFila)
            canvas.drawRect(margenTabla, yActual - 14f, margenTabla + anchoTotal, yActual + 4f, paintBorde)

            xActual = margenTabla + 5f
            canvas.drawText(nombre, xActual, yActual, estiloTexto)

            xActual = margenTabla + anchoNombreCol + 5f + (anchoNotaCol - estiloTexto.measureText(nota)) / 2
            canvas.drawText(nota, xActual, yActual, estiloTexto)

            yActual += 18f
            filaIndex++
        }

        yActual += 15f
        return yActual
    }

    private fun dibujarTablaResumenGeneral(
        canvas: android.graphics.Canvas,
        programa: ProgramaM,
        x: Float,
        y: Float
    ): Float {
        var yActual = y
        val ancho = 320f

        canvas.drawText("RESUMEN GENERAL", x, yActual, estiloSubtitulo)
        yActual += 20f

        val paintFondo = Paint().apply { color = colorFondoCabecera; style = Paint.Style.FILL }
        canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintFondo)

        val paintBorde = Paint().apply { strokeWidth = 1f; style = Paint.Style.STROKE }
        canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintBorde)

        val anchoCol1 = ancho * 0.6f

        var xActual = x + 5f
        canvas.drawText("CONCEPTO", xActual, yActual, estiloCabecera)
        xActual += anchoCol1 + 5f
        canvas.drawText("VALOR", xActual, yActual, estiloCabecera)
        yActual += 18f

        val filas = listOf(
            "Promedio general" to "%.2f".format(programa.promedioGeneral()),
            "Avance total" to "%.1f%%".format(programa.avanceCarrera())
        )

        var index = 0
        filas.forEach { (concepto, valor) ->
            val colorFondo = if (index % 2 == 0) colorFondoFilaPar else colorFondoFilaImpar
            val paintFondoFila = Paint().apply { color = colorFondo; style = Paint.Style.FILL }
            canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintFondoFila)
            canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintBorde)

            xActual = x + 5f
            canvas.drawText(concepto, xActual, yActual, estiloTexto)
            xActual += anchoCol1 + 5f
            canvas.drawText(valor, xActual, yActual, estiloTextoBold)

            yActual += 18f
            index++
        }

        yActual += 10f
        return yActual
    }

    private fun dibujarTablaAvanceAreas(
        canvas: android.graphics.Canvas,
        avances: Map<String, Float>,
        x: Float,
        y: Float
    ): Float {
        var yActual = y
        val ancho = 420f

        canvas.drawText("AVANCE POR ÁREA", x, yActual, estiloSubtitulo)
        yActual += 20f

        val paintFondo = Paint().apply { color = colorFondoCabecera; style = Paint.Style.FILL }
        canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintFondo)

        val paintBorde = Paint().apply { strokeWidth = 1f; style = Paint.Style.STROKE }
        canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintBorde)

        val anchoCol1 = ancho * 0.7f

        val xActualCabecera1 = x + 5f
        canvas.drawText("ÁREA", xActualCabecera1, yActual, estiloCabecera)
        val xActualCabecera2 = x + anchoCol1 + 5f
        canvas.drawText("AVANCE", xActualCabecera2, yActual, estiloCabecera)
        yActual += 18f

        var index = 0
        avances.entries.forEach { entry ->
            val area = entry.key
            val porcentaje = entry.value

            val colorFondo = if (index % 2 == 0) colorFondoFilaPar else colorFondoFilaImpar
            val paintFondoFila = Paint().apply { color = colorFondo; style = Paint.Style.FILL }
            canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintFondoFila)
            canvas.drawRect(x, yActual - 14f, x + ancho, yActual + 4f, paintBorde)

            val xActualCol1 = x + 5f
            canvas.drawText(area, xActualCol1, yActual, estiloTexto)

            val xActualCol2 = x + anchoCol1 + 5f
            canvas.drawText("%.1f%%".format(porcentaje), xActualCol2, yActual, estiloTextoBold)

            yActual += 18f
            index++
        }

        yActual += 10f
        return yActual
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun eliminarInformesAnterioresEnDescargas(nombreArchivo: String) {
        val resolver = context.contentResolver
        val seleccion = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val argumentos = arrayOf(nombreArchivo)

        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            seleccion,
            argumentos,
            null
        )?.use { cursor ->
            val columnaId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val uriExistente = ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    cursor.getLong(columnaId)
                )
                resolver.delete(uriExistente, null, null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun guardarConMediaStore(documento: PdfDocument, nombreArchivo: String): Uri {
        eliminarInformesAnterioresEnDescargas(nombreArchivo)

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IllegalStateException(
            "MediaStore no pudo crear el archivo en Descargas (insert() devolvió null)"
        )

        val streamAbierto = context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            documento.writeTo(outputStream)
        }
        if (streamAbierto == null) {
            throw IllegalStateException(
                "No se pudo abrir el archivo creado en Descargas para escribir el PDF"
            )
        }

        return uri
    }

    private fun guardarEnAlmacenamientoExterno(documento: PdfDocument, nombreArchivo: String): Uri? {
        val descargasDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!descargasDir.exists()) {
            descargasDir.mkdirs()
        }

        val archivo = File(descargasDir, nombreArchivo)
        FileOutputStream(archivo).use { outputStream ->
            documento.writeTo(outputStream)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    fun abrirPDF(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "No hay ninguna app instalada para abrir archivos PDF.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No se encontró ninguna app para abrir el PDF.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Error al abrir el PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun nuevaPaginaInfo(numeroPagina: Int) =
        PdfDocument.PageInfo.Builder(anchoPagina, altoPagina, numeroPagina).create()
}