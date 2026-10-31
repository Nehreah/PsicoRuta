package com.renea.psicologiauv

import com.renea.psicologiauv.data.ReglaEspanol
import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglaEspanolTest {

    @Test
    fun aprobadaVerificaNotasNumericasYMarcasEspeciales() {
        assertTrue(ReglaEspanol.aprobada(NotaImportada.Numerica(3.0f)))
        assertTrue(ReglaEspanol.aprobada(NotaImportada.Numerica(4.5f)))
        assertFalse(ReglaEspanol.aprobada(NotaImportada.Numerica(2.9f)))
        assertFalse(ReglaEspanol.aprobada(NotaImportada.Numerica(0.0f)))

        assertTrue(ReglaEspanol.aprobada(NotaImportada.Especial("EX")))
        assertTrue(ReglaEspanol.aprobada(NotaImportada.Especial("C.U")))
        assertTrue(ReglaEspanol.aprobada(NotaImportada.Especial("A.P")))

        assertFalse(ReglaEspanol.aprobada(null))
    }

    @Test
    fun aplicarSinEspanolDevuelveMismoMapaYFlotanteNull() {
        val notas = mapOf(
            "402180M" to NotaImportada.Numerica(4.0f),
            "402181M" to NotaImportada.Numerica(3.8f)
        )
        val resultado = ReglaEspanol.aplicar(notas)

        assertEquals(notas, resultado.notas)
        assertNull(resultado.culturaFlotante)
    }

    @Test
    fun aplicarConEspanolReprobadoDescartaEspanolYNoAlteraCulturaEscrita() {
        val notas = mapOf(
            ReglaEspanol.CODIGO_ESPANOL_SIRA to NotaImportada.Numerica(2.0f),
            ReglaEspanol.CODIGO_CULTURA_ESCRITA to NotaImportada.Numerica(3.5f)
        )
        val resultado = ReglaEspanol.aplicar(notas)

        assertFalse(resultado.notas.containsKey(ReglaEspanol.CODIGO_ESPANOL_SIRA))
        assertEquals(NotaImportada.Numerica(3.5f), resultado.notas[ReglaEspanol.CODIGO_CULTURA_ESCRITA])
        assertNull(resultado.culturaFlotante)
    }

    @Test
    fun aplicarConEspanolAprobadoYCulturaEscritaAusenteAsignaEspanolACultura() {
        val notaEspanol = NotaImportada.Numerica(4.5f)
        val notas = mapOf(
            ReglaEspanol.CODIGO_ESPANOL_SIRA to notaEspanol,
            "402181M" to NotaImportada.Numerica(3.8f)
        )
        val resultado = ReglaEspanol.aplicar(notas)

        assertFalse(resultado.notas.containsKey(ReglaEspanol.CODIGO_ESPANOL_SIRA))
        assertEquals(notaEspanol, resultado.notas[ReglaEspanol.CODIGO_CULTURA_ESCRITA])
        assertNull(resultado.culturaFlotante)
    }

    @Test
    fun aplicarConEspanolAprobadoYCulturaAprobadaGeneraCulturaFlotante() {
        val notaEspanol = NotaImportada.Especial("EX")
        val notaCultura = NotaImportada.Numerica(4.2f)
        val notas = mapOf(
            ReglaEspanol.CODIGO_ESPANOL_SIRA to notaEspanol,
            ReglaEspanol.CODIGO_CULTURA_ESCRITA to notaCultura
        )
        val resultado = ReglaEspanol.aplicar(notas)

        assertFalse(resultado.notas.containsKey(ReglaEspanol.CODIGO_ESPANOL_SIRA))
        assertEquals(notaEspanol, resultado.notas[ReglaEspanol.CODIGO_CULTURA_ESCRITA])

        assertNotNull(resultado.culturaFlotante)
        assertEquals(ReglaEspanol.CODIGO_CULTURA_FLOTANTE, resultado.culturaFlotante?.codigo)
        assertEquals("Cultura Escrita", resultado.culturaFlotante?.nombre)
        assertEquals(notaCultura, resultado.culturaFlotante?.nota)
    }

    @Test
    fun aplicarConEspanolAprobadoYCulturaReprobadaNoGeneraFlotante() {
        val notaEspanol = NotaImportada.Numerica(4.0f)
        val notaCultura = NotaImportada.Numerica(2.5f)
        val notas = mapOf(
            ReglaEspanol.CODIGO_ESPANOL_SIRA to notaEspanol,
            ReglaEspanol.CODIGO_CULTURA_ESCRITA to notaCultura
        )
        val resultado = ReglaEspanol.aplicar(notas)

        assertEquals(notaEspanol, resultado.notas[ReglaEspanol.CODIGO_CULTURA_ESCRITA])
        assertNull(resultado.culturaFlotante)
    }
}
