package com.renea.psicologiauv

import com.renea.psicologiauv.data.AnalizadorNotas
import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del análisis de texto independientes del lector de archivos PDF. */
class ImportadorNotasTest {
    @Test fun leeNotaConPuntoDecimal() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M PSICOLOGIA 3 4.25 402181M OTRA MATERIA 3 3.8",
            listOf("402180M", "402181M")
        )
        assertEquals(4.25f, (resultado["402180M"] as NotaImportada.Numerica).valor, 0.001f)
        assertEquals(3.8f, (resultado["402181M"] as NotaImportada.Numerica).valor, 0.001f)
    }

    @Test fun normalizaComaDecimal() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M PSICOLOGIA 3 4,2 402181M OTRA 3 3,7",
            listOf("402180M", "402181M")
        )
        assertEquals(4.2f, (resultado["402180M"] as NotaImportada.Numerica).valor, 0.001f)
    }

    @Test fun reconoceMarcasEspecialesSinConfundirlasConNotasNumericas() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M SEMINARIO 3 C.U 402181M OTRA 3 4.1",
            listOf("402180M", "402181M")
        )
        assertEquals(NotaImportada.Especial("C.U"), resultado["402180M"])
    }

    @Test fun noTomaElPromedioDelResumenComoNotaDeLaMateria() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA CANCELADA 3 Resumen de Finalización Promedio Semestral: 4.90 402181M OTRA 3 3.8",
            listOf("402180M", "402181M")
        )
        assertFalse(resultado.containsKey("402180M"))
        assertTrue(resultado.containsKey("402181M"))
    }

    @Test fun codigoDesconocidoDelPdfDelimitaLaFilaAnterior() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA SIN NOTA 3 504057C OTRA MATERIA 3 4.7",
            listOf("402180M")
        )
        assertFalse(resultado.containsKey("402180M"))
    }

    @Test fun ignoraCodigosProvisionalesCortos() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "1 ELECTIVA 3 4.5 402180M MATERIA REAL 3 4.0",
            listOf("1", "402180M")
        )
        assertFalse(resultado.containsKey("1"))
        assertTrue(resultado.containsKey("402180M"))
    }

    @Test fun enMateriasRepetidasConservaLaUltimaNotaValida() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA 3 3.0 402181M OTRA 3 4.0 402180M MATERIA REPETIDA 3 4.5",
            listOf("402180M", "402181M")
        )
        assertEquals(4.5f, (resultado["402180M"] as NotaImportada.Numerica).valor, 0.001f)
    }

    @Test fun ignoraCodigosConocidosDuplicados() {
        val resultado = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA 3 4.0",
            listOf("402180M", "402180M")
        )
        assertEquals(4.0f, (resultado["402180M"] as NotaImportada.Numerica).valor, 0.001f)
    }
}
