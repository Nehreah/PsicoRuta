package com.renea.psicologiauv

import com.renea.psicologiauv.model.Asignatura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de regresión para comportamientos existentes de Asignatura. */
class AsignaturaTest {
    private fun asignatura(
        nota: Float = 0f,
        notaEspecial: String? = null,
        componente: String = "Obligatoria",
        semestre: Int = 1
    ) = Asignatura(
        nombre = "Asignatura de prueba",
        codigo = "402180M",
        semestre = semestre,
        creditos = 3,
        lineaProfesional = "Ninguna",
        componente = componente,
        area = "Fundamentación",
        nota = nota,
        notaEspecial = notaEspecial
    )

    @Test fun notaValidaActualizaLaAsignaturaYEliminaMarcaEspecial() {
        val actual = asignatura(notaEspecial = "A.P").cambiarNota(4.2f)
        assertEquals(4.2f, actual.nota, 0.001f)
        assertEquals(null, actual.notaEspecial)
    }

    @Test fun notaCeroReiniciaNotaYMarcaEspecial() {
        val actual = asignatura(nota = 4.2f, notaEspecial = "A.P").cambiarNota(0f)
        assertEquals(0f, actual.nota, 0.001f)
        assertEquals(null, actual.notaEspecial)
    }

    @Test fun notaFueraDeRangoNoModificaLaAsignatura() {
        val original = asignatura(nota = 3.5f)
        assertSame(original, original.cambiarNota(5.1f))
        assertSame(original, original.cambiarNota(-0.1f))
    }

    @Test fun marcaEspecialCuentaComoAprobadaSinNotaNumerica() {
        assertTrue(asignatura(notaEspecial = "C.U").aprobo())
        assertEquals("C.U", asignatura(notaEspecial = "C.U").textoNota())
    }

    @Test fun cambioDeSemestreRespetaRangoExistente() {
        assertEquals(5, asignatura().cambiarSemestre(5).semestre)
        assertEquals(1, asignatura().cambiarSemestre(16).semestre)
        assertEquals(1, asignatura().cambiarSemestre(-1).semestre)
    }

    @Test fun soloComponentesEditablesPermitenCambiarNombre() {
        assertEquals("Nuevo", asignatura(componente = "Electiva profesional").cambiarNombre("Nuevo").nombre)
        assertEquals("Asignatura de prueba", asignatura().cambiarNombre("Nuevo").nombre)
        assertFalse(asignatura().esElectivaOIdioma())
        assertTrue(asignatura(componente = "Idioma").esElectivaOIdioma())
    }
}
