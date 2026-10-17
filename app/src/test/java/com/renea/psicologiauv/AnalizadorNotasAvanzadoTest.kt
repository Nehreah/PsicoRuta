package com.renea.psicologiauv

import com.renea.psicologiauv.data.AnalizadorNotas
import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas adicionales del analizador de texto del tabulado: orden de
 * aparición, códigos con formato no estándar, bordes de entrada y lectura de
 * textos grandes.
 */
class AnalizadorNotasAvanzadoTest {

    private fun numerica(m: Map<String, NotaImportada>, codigo: String): Float =
        (m[codigo] as NotaImportada.Numerica).valor

    @Test fun textoVacioDevuelveMapaVacio() {
        assertTrue(AnalizadorNotas.extraerNotasPorCodigo("", listOf("402180M")).isEmpty())
    }

    @Test fun sinCodigosConocidosDevuelveMapaVacio() {
        assertTrue(AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA 3 4.0", emptyList()).isEmpty())
    }

    @Test fun soloCodigosCortosDevuelveMapaVacio() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("1 ELECTIVA 3 4.5", listOf("1", "2", "3"))
        assertTrue(r.isEmpty())
    }

    @Test fun codigoConocidoQueNoApareceNoSeInventa() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA 3 4.0", listOf("402180M", "402999M"))
        assertEquals(setOf("402180M"), r.keys)
    }

    @Test fun elOrdenDelMapaEsElOrdenDeAparicionEnElPdf() {
        // La lista de conocidos está en otro orden a propósito.
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402163M UNO 3 4.0 402161M DOS 3 3.5 402175M TRES 3 5.0",
            listOf("402175M", "402161M", "402163M")
        )
        assertEquals(listOf("402163M", "402161M", "402175M"), r.keys.toList())
    }

    @Test fun materiaSinNotaNoApareceEnElMapaNiOcupaPosicion() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402163M CANCELADA 3 402161M REAL 3 3.5 402175M OTRA 3 4.5",
            listOf("402163M", "402161M", "402175M")
        )
        assertEquals(listOf("402161M", "402175M"), r.keys.toList())
    }

    @Test fun codigoCaseSensitiveNoCoincideConMinusculas() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180m MATERIA 3 4.0", listOf("402180M"))
        assertTrue(r.isEmpty())
    }

    @Test fun codigoPegadoAOtroTextoNoCuentaComoCodigo() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("X402180M MATERIA 3 4.0 402180MM 3 4.1", listOf("402180M"))
        assertTrue(r.isEmpty())
    }

    @Test fun codigosConFormatoNoGenericoSeSiguenEncontrando() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "990001M-19 MATERIA 3 4.4 402180M OTRA 3 3.9",
            listOf("990001M-19", "402180M")
        )
        assertEquals(4.4f, numerica(r, "990001M-19"), 0.001f)
        assertEquals(3.9f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun codigoConPrefijoEspecialSeEncuentra() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("<<990001M MATERIA 3 4.2", listOf("<<990001M"))
        assertEquals(4.2f, numerica(r, "<<990001M"), 0.001f)
    }

    @Test fun notaFueraDeEscalaSeDescarta() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA 3 5.99", listOf("402180M"))
        assertFalse(r.containsKey("402180M"))
    }

    @Test fun enLaMismaFilaGanaLaUltimaNotaNumerica() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA 3.0 3 4.5", listOf("402180M"))
        assertEquals(4.5f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun numeroTienePrioridadSobreMarcaEspecialEnLaMismaFila() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA C.U 3 4.0", listOf("402180M"))
        assertEquals(4.0f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun marcasEspecialesEnMinusculasSeReconocen() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M A 3 e.x 402181M B 3 a.p",
            listOf("402180M", "402181M")
        )
        assertEquals(NotaImportada.Especial("E.X"), r["402180M"])
        assertEquals(NotaImportada.Especial("A.P"), r["402181M"])
    }

    @Test fun marcaEspecialDentroDeOtraPalabraNoCuenta() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M MATERIA AC.UX 3", listOf("402180M"))
        assertTrue(r.isEmpty())
    }

    @Test fun ultimaFilaSinSiguienteCodigoUsaLimiteDeLongitud() {
        // 400 caracteres de relleno: la nota queda fuera del tramo de la fila.
        val relleno = "x".repeat(450)
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M $relleno 4.0", listOf("402180M"))
        assertTrue(r.isEmpty())
    }

    @Test fun ultimaFilaDentroDelLimiteDeLongitudSiLeeLaNota() {
        val r = AnalizadorNotas.extraerNotasPorCodigo("402180M " + "x".repeat(100) + " 4.0", listOf("402180M"))
        assertEquals(4.0f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun elResumenCortaSoloLaFilaQueLoContiene() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA 3 4.0 Resumen de Finalización Promedio Semestral: 2.10",
            listOf("402180M")
        )
        assertEquals(4.0f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun materiaCanceladaYLuegoRecursadaConservaLaNotaDelSegundoIntento() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA 3 402181M OTRA 3 3.0 402180M MATERIA 3 4.6",
            listOf("402180M", "402181M")
        )
        assertEquals(4.6f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun materiaAprobadaYLuegoCanceladaConservaLaNotaValida() {
        val r = AnalizadorNotas.extraerNotasPorCodigo(
            "402180M MATERIA 3 3.5 402181M OTRA 3 3.0 402180M MATERIA 3 Resumen de Finalización 4.9",
            listOf("402180M", "402181M")
        )
        assertEquals(3.5f, numerica(r, "402180M"), 0.001f)
    }

    @Test fun tabuladoGrandeSeProcesaCorrectamente() {
        val codigos = (0 until 300).map { "4%05dM".format(it) }
        val texto = codigos.mapIndexed { i, c -> "$c MATERIA $i 3 ${(i % 5) + 0.5}" }.joinToString("\n")
        val r = AnalizadorNotas.extraerNotasPorCodigo(texto, codigos)
        assertEquals(300, r.size)
        assertEquals(codigos, r.keys.toList())
        assertEquals(0.5f, numerica(r, codigos[0]), 0.001f)
        assertEquals(4.5f, numerica(r, codigos[299]), 0.001f)
    }
}
