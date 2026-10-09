package com.renea.psicologiauv

import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de regresión para operaciones puras de Estudiante. */
class EstudianteTest {
    private fun materia(codigo: String, nota: Float = 0f, complementaria: Boolean = false) = Asignatura(
        nombre = "Materia $codigo",
        codigo = codigo,
        semestre = 1,
        creditos = 3,
        lineaProfesional = "Ninguna",
        componente = "Obligatoria",
        area = "Fundamentación",
        nota = nota,
        complementaria = complementaria
    )

    private fun estudiante(vararg materias: Asignatura) = Estudiante(
        nombre = "Estudiante de prueba",
        codigo = 123,
        asignaturas = materias.toMutableList()
    )

    @Test fun actualizarNotasMasivoSoloModificaCodigosConocidos() {
        val inicial = estudiante(materia("402180M"), materia("402181M"))
        val (actualizado, cantidad) = inicial.actualizarNotasMasivo(
            mapOf("402180M" to NotaImportada.Numerica(4.2f), "999999M" to NotaImportada.Numerica(4.8f))
        )
        assertEquals(1, cantidad)
        assertEquals(4.2f, actualizado.asignaturas.first { it.codigo == "402180M" }.nota, 0.001f)
        assertEquals(0f, actualizado.asignaturas.first { it.codigo == "402181M" }.nota, 0.001f)
        assertEquals(2, actualizado.asignaturas.size)
    }

    @Test fun actualizarNotasMasivoReconoceMarcasEspeciales() {
        val (actualizado, cantidad) = estudiante(materia("402180M")).actualizarNotasMasivo(
            mapOf("402180M" to NotaImportada.Especial("A.P"))
        )
        assertEquals(1, cantidad)
        assertEquals("A.P", actualizado.asignaturas.single().notaEspecial)
        assertTrue(actualizado.asignaturas.single().aprobo())
    }

    @Test fun siguienteCodigoComplementarioEsConsecutivo() {
        assertEquals("999", estudiante().siguienteCodigoComplementario())
        assertEquals("1000", estudiante(materia("999", complementaria = true)).siguienteCodigoComplementario())
        assertEquals("1002", estudiante(materia("999"), materia("1001")).siguienteCodigoComplementario())
    }

    @Test fun eliminarAsignaturaSoloEliminaComplementarias() {
        val inicial = estudiante(materia("402180M"), materia("999", complementaria = true))
        val resultado = inicial.eliminarAsignatura("999")
        assertEquals(listOf("402180M"), resultado.asignaturas.map { it.codigo })
        assertEquals(2, inicial.asignaturas.size) // la instancia inicial no cambia
        assertFalse(resultado.asignaturas.any { it.codigo == "999" })
    }
}
