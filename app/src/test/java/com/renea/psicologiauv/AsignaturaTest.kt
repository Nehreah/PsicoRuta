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

    @Test fun cambiarNotaEspecialValidaModificaMarcaYReiniciaNota() {
        val actual = asignatura(nota = 4.0f).cambiarNotaEspecial("E.X")
        assertEquals("E.X", actual.notaEspecial)
        assertEquals(0f, actual.nota, 0f)
        assertTrue(actual.aprobo())
    }

    @Test fun cambiarNotaEspecialEnBlancoNoModificaLaAsignatura() {
        val original = asignatura(nota = 3.5f)
        assertSame(original, original.cambiarNotaEspecial(""))
        assertSame(original, original.cambiarNotaEspecial("   "))
    }

    @Test fun soloComponentesEditablesPermitenCambiarCodigo() {
        assertEquals("NUEVO1", asignatura(componente = "Electiva complementaria").cambiarCodigo("NUEVO1").codigo)
        assertEquals("NUEVO2", asignatura(componente = "Electiva profesional").cambiarCodigo("NUEVO2").codigo)
        assertEquals("NUEVO3", asignatura(componente = "Idioma").cambiarCodigo("NUEVO3").codigo)
        assertEquals("402180M", asignatura(componente = "Obligatoria").cambiarCodigo("NUEVO").codigo)
    }

    @Test fun sinNotaIdentificaMateriasSinCalificacion() {
        assertTrue(asignatura(nota = 0f, notaEspecial = null).sinNota())
        assertFalse(asignatura(nota = 3.0f, notaEspecial = null).sinNota())
        assertFalse(asignatura(nota = 0f, notaEspecial = "A.P").sinNota())
        // Una materia con nota reprobatoria (2.0) no es "sin nota": ya fue calificada
        assertFalse(asignatura(nota = 2.0f, notaEspecial = null).sinNota())
    }

    @Test fun textoNotaFormateaCorrectamente() {
        val texto = asignatura(nota = 4.2f).textoNota()
        assertTrue(texto == "4.2" || texto == "4,2")
        assertEquals("A.P", asignatura(notaEspecial = "A.P").textoNota())
        assertEquals("-", asignatura(nota = 0f, notaEspecial = null).textoNota())
    }

    @Test fun aproboConNotaMinimaPersonalizada() {
        val mat = asignatura(nota = 3.2f)
        assertTrue(mat.aprobo(2.9f))
        assertFalse(mat.aprobo(3.5f))
        // Con marca especial siempre aprueba independientemente de la nota mínima
        assertTrue(asignatura(nota = 0f, notaEspecial = "EX").aprobo(4.0f))
    }
}
