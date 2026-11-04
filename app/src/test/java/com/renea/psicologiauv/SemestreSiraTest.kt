package com.renea.psicologiauv

import com.renea.psicologiauv.data.AnalizadorNotas
import com.renea.psicologiauv.data.ReglaSemestreSira
import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas del semestre SIRA: cada "PERIODO:" del tabulado es un semestre, contando desde 1. */
class SemestreSiraTest {

    private val tabulado = """
        PERIODO: MARZO/2023 - JULIO/2023
        402180M 01 00 CULT. ESCRITA 3 5.0
        402264M 01 00 PENSAMIENTO LOGICO 3
        CANCELACIÓN: 2023-04-28 09:01:25
        Resumen de Finalización por periodo Promedio Semestral : 4.79
        PERIODO: AGOSTO/2023 - DICIEMBRE/2023
        402181M 01 00 FUND. FILOS 3 4.4
        Resumen de Finalización por periodo Promedio Semestral : 4.61
        PERIODO: FEBRERO/2024 - JUNIO/2024
        402264M 01 00 PENSAMIENTO LOGICO 3 4.4
        Resumen de Finalización por periodo Promedio Semestral : 4.3
        PERIODO: AGOSTO/2024 - DICIEMBRE/2024
        402204M 01 00 EVALUACION 4
        402267M 03 00 TOPICOS 3
        CANCELACIÓN: 2025-09-10 17:38:55
        Resumen de Finalización por periodo
    """.trimIndent()

    private val codigos = listOf("402180M", "402264M", "402181M", "402204M", "402267M")

    @Test fun cadaPeriodoEsUnSemestreContandoDesdeUno() {
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo(tabulado, codigos)
        assertEquals(1, r["402180M"])
        assertEquals(2, r["402181M"])
    }

    @Test fun materiaCanceladaYRecursadaQuedaEnElSemestreDeLaAparicionVigente() {
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo(tabulado, codigos)
        assertEquals(3, r["402264M"])
    }

    @Test fun materiaEnCursoQuedaEnElUltimoPeriodo() {
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo(tabulado, codigos)
        assertEquals(4, r["402204M"])
    }

    @Test fun materiaCanceladaSinRecursarNoRecibeSemestre() {
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo(tabulado, codigos)
        assertFalse(r.containsKey("402267M"))
    }

    @Test fun sinEncabezadosDePeriodoNoSeCalculaNada() {
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo("402180M CULT 3 5.0", listOf("402180M"))
        assertEquals(emptyMap<String, Int>(), r)
    }

    @Test fun elResumenDelPeriodoNoSeCuentaComoPeriodo() {
        // "por periodo" (minúscula, sin dos puntos) no es un encabezado.
        val r = AnalizadorNotas.extraerSemestresSiraPorCodigo(tabulado, codigos)
        assertEquals(4, r.values.maxOrNull()!!)
    }

    @Test fun noSincronizadaTraeSuSemestreSira() {
        val lista = AnalizadorNotas.extraerNoSincronizadas(tabulado, listOf("402180M"))
        assertEquals(3, lista.first { it.codigo == "402264M" }.semestre)
    }

    @Test fun espanolAprobadoConservaSuSemestreYNoUsaSufijoCE() {
        val semestres = mapOf("204001M" to 1, "402180M" to 2)
        val notas = mapOf<String, NotaImportada>(
            "204001M" to NotaImportada.Especial("E.X"),
            "402180M" to NotaImportada.Numerica(5.0f)
        )
        val r = ReglaSemestreSira.aplicar(semestres, notas)
        assertEquals(1, r["204001M"])
        assertEquals(2, r["402180M"])
        assertNull(r["402180M-CE"])
    }

    @Test fun deporteFormativoTomaElSemestreDelAliasElegido() {
        val semestres = mapOf("404002C" to 3, "404001M" to 5)
        val notas = mapOf<String, NotaImportada>(
            "404002C" to NotaImportada.Numerica(4.0f),
            "404001M" to NotaImportada.Numerica(4.8f)
        )
        val r = ReglaSemestreSira.aplicar(semestres, notas)
        assertEquals(5, r["404010C"])
        assertNull(r["404002C"])
    }

    @Test fun estudianteUbicaCadaMateriaEnSuSemestreSiraYRespetaLosEstructurales() {
        val estudiante = crearEstudiante(
            crearAsignatura("402181M", semestre = 4),
            crearAsignatura("204355M", semestre = 99, componente = "Idioma"),
            crearAsignatura("402226M", semestre = 0),
            crearAsignatura("402204M", semestre = 8)
        )
        val (nuevo, cambiadas) = estudiante.aplicarSemestresSira(
            mapOf("402181M" to 2, "204355M" to 1, "402226M" to 5, "402204M" to 8)
        )
        assertEquals(1, cambiadas)
        assertEquals(2, nuevo.asignaturas.first { it.codigo == "402181M" }.semestre)
        assertEquals(99, nuevo.asignaturas.first { it.codigo == "204355M" }.semestre)
        assertEquals(0, nuevo.asignaturas.first { it.codigo == "402226M" }.semestre)
    }

    @Test fun semestreSiraMayorAlDelPensumSeLimitaAlUltimo() {
        val estudiante = crearEstudiante(crearAsignatura("402181M", semestre = 4))
        val (nuevo, _) = estudiante.aplicarSemestresSira(mapOf("402181M" to 13))
        assertEquals(11, nuevo.asignaturas.first().semestre)
    }
}
