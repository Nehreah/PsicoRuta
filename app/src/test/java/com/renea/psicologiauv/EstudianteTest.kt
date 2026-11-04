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

    @Test fun cambiarEstadoSemestreManualPermiteFijarYRevertir() {
        val inicial = estudiante()
        assertEquals(emptyMap<Int, Boolean>(), inicial.semestresManuales)

        val conSemestre1 = inicial.cambiarEstadoSemestre(1, true)
        assertEquals(mapOf(1 to true), conSemestre1.semestresManuales)

        val conSemestre2 = conSemestre1.cambiarEstadoSemestre(2, false)
        assertEquals(mapOf(1 to true, 2 to false), conSemestre2.semestresManuales)

        val revertido = conSemestre2.cambiarEstadoSemestre(1, null)
        assertEquals(mapOf(2 to false), revertido.semestresManuales)
    }

    @Test fun actualizarNotasMasivoAsignaCeroCreditosAExencionesDeEspanolEIngles() {
        val inicial = estudiante(
            materia("204001M").copy(nombre = "Español", creditos = 3),
            materia("204355M").copy(nombre = "Idiomas I", componente = "Idioma", creditos = 2),
            materia("402181M").copy(nombre = "Psicología General", creditos = 3)
        )
        val (actualizado, _) = inicial.actualizarNotasMasivo(
            mapOf(
                "204001M" to NotaImportada.Especial("EX"),
                "204355M" to NotaImportada.Especial("E.X"),
                "402181M" to NotaImportada.Especial("A.P")
            )
        )
        // Español e Inglés eximidos quedan con 0 créditos
        assertEquals(0, actualizado.asignaturas.first { it.codigo == "204001M" }.creditos)
        assertEquals(0, actualizado.asignaturas.first { it.codigo == "204355M" }.creditos)
        // Otra materia con A.P mantiene sus créditos
        assertEquals(3, actualizado.asignaturas.first { it.codigo == "402181M" }.creditos)
    }

    @Test fun aplicarReglaEspanolAsignaCeroCreditosSiEsEximido() {
        val inicial = estudiante(
            materia("402180M").copy(nombre = "Cultura Escrita", componente = "Asignatura básica", creditos = 3)
        )
        val actualizado = inicial.aplicarReglaEspanol(
            mapOf("204001M" to NotaImportada.Especial("EX"))
        )
        val espanol = actualizado.asignaturas.first { it.codigo == "204001M" }
        assertEquals("Español", espanol.nombre)
        assertEquals(0, espanol.creditos)
    }

    @Test fun actualizarCreditosMasivoAplicaCreditosDelSira() {
        val inicial = estudiante(
            materia("402180M").copy(creditos = 3),
            materia("402181M").copy(creditos = 3)
        )
        val actualizado = inicial.actualizarCreditosMasivo(
            mapOf("402180M" to 4, "402181M" to 2)
        )
        assertEquals(4, actualizado.asignaturas.first { it.codigo == "402180M" }.creditos)
        assertEquals(2, actualizado.asignaturas.first { it.codigo == "402181M" }.creditos)
    }
}

