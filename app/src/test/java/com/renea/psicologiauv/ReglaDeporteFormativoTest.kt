package com.renea.psicologiauv

import com.renea.psicologiauv.data.ReglaDeporteFormativo
import com.renea.psicologiauv.model.NotaImportada
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglaDeporteFormativoTest {

    @Test
    fun jsonDeporteFormativoContieneCodigosEsperados() {
        val contenido = leerAssetDeDatos("DeporteFormativo.json")
        val jsonElement = Json.parseToJsonElement(contenido)
        val codigos = jsonElement.jsonArray.map { it.jsonPrimitive.content }

        val esperados = listOf("404032C", "404002C", "404001M", "404010M")
        assertEquals(esperados, codigos)
        assertEquals(esperados, ReglaDeporteFormativo.CODIGOS_ALIAS)
    }

    @Test
    fun aprobadaVerificaNotasNumericasYMarcasEspeciales() {
        assertTrue(ReglaDeporteFormativo.aprobada(NotaImportada.Numerica(3.0f)))
        assertTrue(ReglaDeporteFormativo.aprobada(NotaImportada.Numerica(5.0f)))
        assertFalse(ReglaDeporteFormativo.aprobada(NotaImportada.Numerica(2.9f)))
        assertFalse(ReglaDeporteFormativo.aprobada(NotaImportada.Numerica(1.0f)))

        assertTrue(ReglaDeporteFormativo.aprobada(NotaImportada.Especial("EX")))
        assertTrue(ReglaDeporteFormativo.aprobada(NotaImportada.Especial("A.P")))
        assertTrue(ReglaDeporteFormativo.aprobada(NotaImportada.Especial("C.U")))

        assertFalse(ReglaDeporteFormativo.aprobada(null))
    }

    @Test
    fun aplicarSinAliasDevuelveMismoMapa() {
        val notas = mapOf(
            "402180M" to NotaImportada.Numerica(4.0f),
            "402181M" to NotaImportada.Numerica(3.8f)
        )
        val resultado = ReglaDeporteFormativo.aplicar(notas)
        assertEquals(notas, resultado)
    }

    @Test
    fun aplicarConCadaUnoDeLosAliasAprobadosAsignaADeporteFormativo() {
        val alias = listOf("404032C", "404002C", "404001M", "404010M")

        for (codigoAlias in alias) {
            val nota = NotaImportada.Numerica(4.5f)
            val notas = mapOf(
                codigoAlias to nota,
                "402181M" to NotaImportada.Numerica(3.8f)
            )

            val resultado = ReglaDeporteFormativo.aplicar(notas)

            // El alias debe desaparecer
            assertFalse("El alias $codigoAlias no debe quedar en el mapa", resultado.containsKey(codigoAlias))
            // El destino debe estar aprobado con la nota
            assertEquals(nota, resultado[ReglaDeporteFormativo.CODIGO_DESTINO])
            // Las demás materias no deben verse afectadas
            assertEquals(NotaImportada.Numerica(3.8f), resultado["402181M"])
        }
    }

    @Test
    fun aplicarConAliasEspecialEximidoAsignaADeporteFormativo() {
        val nota = NotaImportada.Especial("EX")
        val notas = mapOf(
            "404032C" to nota
        )

        val resultado = ReglaDeporteFormativo.aplicar(notas)
        assertFalse(resultado.containsKey("404032C"))
        assertEquals(nota, resultado[ReglaDeporteFormativo.CODIGO_DESTINO])
    }

    @Test
    fun aplicarConAliasReprobadoNoAsignaDeporteFormativo() {
        val notas = mapOf(
            "404032C" to NotaImportada.Numerica(2.5f),
            "402181M" to NotaImportada.Numerica(4.0f)
        )

        val resultado = ReglaDeporteFormativo.aplicar(notas)
        assertFalse(resultado.containsKey("404032C"))
        assertFalse(resultado.containsKey(ReglaDeporteFormativo.CODIGO_DESTINO))
        assertEquals(NotaImportada.Numerica(4.0f), resultado["402181M"])
    }

    @Test
    fun aplicarConMultiplesAliasAprobadosEligeLaMejorNota() {
        val notas = mapOf(
            "404032C" to NotaImportada.Numerica(3.5f),
            "404002C" to NotaImportada.Numerica(4.8f),
            "404001M" to NotaImportada.Especial("EX")
        )

        val resultado = ReglaDeporteFormativo.aplicar(notas)
        assertEquals(NotaImportada.Numerica(4.8f), resultado[ReglaDeporteFormativo.CODIGO_DESTINO])
        for (codigo in ReglaDeporteFormativo.CODIGOS_ALIAS) {
            assertFalse(resultado.containsKey(codigo))
        }
    }

    @Test
    fun aplicarSiDestinoYaPresenteNoLoSobreescribe() {
        val notaDirecta = NotaImportada.Numerica(4.0f)
        val notaAlias = NotaImportada.Numerica(4.9f)
        val notas = mapOf(
            ReglaDeporteFormativo.CODIGO_DESTINO to notaDirecta,
            "404032C" to notaAlias
        )

        val resultado = ReglaDeporteFormativo.aplicar(notas)
        assertEquals(notaDirecta, resultado[ReglaDeporteFormativo.CODIGO_DESTINO])
        assertFalse(resultado.containsKey("404032C"))
    }
}
