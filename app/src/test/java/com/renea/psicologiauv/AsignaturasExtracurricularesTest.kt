package com.renea.psicologiauv

import com.renea.psicologiauv.data.ReglaEspanol
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.AsignaturaNoSincronizada
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.NotaImportada
import com.renea.psicologiauv.model.ProgramaM
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AsignaturasExtracurricularesTest {

    private fun baseEstudiante(): Estudiante = Estudiante(
        nombre = "Test",
        codigo = 12345,
        asignaturas = mutableListOf(
            Asignatura(
                nombre = "Cultura Escrita Y Pensamiento Disciplinar",
                codigo = "402180M",
                semestre = 1,
                creditos = 3,
                lineaProfesional = "No",
                componente = "Asignatura básica",
                area = "Fundamentación Disciplinar e Interdisciplinar"
            ),
            Asignatura(
                nombre = "Materia Semestre 1",
                codigo = "402001M",
                semestre = 1,
                creditos = 3,
                lineaProfesional = "No",
                componente = "Asignatura básica",
                area = "Fundamentación Disciplinar e Interdisciplinar",
                nota = 4.0f
            ),
            Asignatura(
                nombre = "I. Electiva complementaria",
                codigo = "1",
                semestre = 2,
                creditos = 3,
                lineaProfesional = "No",
                componente = "Electiva complementaria",
                area = "Fundamentación Disciplinar e Interdisciplinar"
            )
        )
    )

    @Test
    fun aplicarReglaEspanolTransformaCulturaEscritaAEspanol() {
        val est = baseEstudiante()
        val notas = mapOf(ReglaEspanol.CODIGO_ESPANOL_SIRA to NotaImportada.Especial("EX"))
        val actualizado = est.aplicarReglaEspanol(notas)

        val matEspanol = actualizado.asignaturas.find { it.codigo == "204001M" }
        assertNotNull(matEspanol)
        assertEquals("Español", matEspanol?.nombre)
        assertEquals(1, matEspanol?.semestre)

        // No debe quedar 402180M como básica
        assertNull(actualizado.asignaturas.find { it.codigo == "402180M" })
    }

    @Test
    fun extracurricularSumaACreditosYPromedioSemestralPeroNoACarrera() {
        val estInicial = baseEstudiante()
        // Materia 402001M en semestre 1 tiene nota 4.0 y 3 créditos.
        // Agregamos una extracurricular al semestre 1 con nota 5.0 y 3 créditos.
        val extra = AsignaturaNoSincronizada(
            codigo = "EXT100M",
            nombre = "Teatro Experimental",
            nota = NotaImportada.Numerica(5.0f),
            semestre = 1,
            creditos = 3
        )

        val estConExtra = estInicial.agregarAsignaturasExtracurriculares(listOf(extra))

        val programaSinExtra = ProgramaM(estudiante = estInicial)
        val programaConExtra = ProgramaM(estudiante = estConExtra)

        // 1. Créditos del semestre 1 aumentan en 3
        assertEquals(programaSinExtra.creditosTotalesSemestre(1) + 3, programaConExtra.creditosTotalesSemestre(1))
        assertEquals(programaSinExtra.creditosAprobadosSemestre(1) + 3, programaConExtra.creditosAprobadosSemestre(1))

        // 2. Promedio semestral cambia: (3*4.0 + 3*5.0) / 6 = 4.5f (antes era 4.0f)
        assertEquals(4.0f, programaSinExtra.promedioSemestre(1), 0.001f)
        assertEquals(4.5f, programaConExtra.promedioSemestre(1), 0.001f)

        // 3. Créditos que sirven para carrera NO aumentan
        assertEquals(
            programaSinExtra.creditosQueSirvenParaCarrera(),
            programaConExtra.creditosQueSirvenParaCarrera()
        )

        // 4. Promedio general de la carrera SÍ incluye materias extracurriculares
        assertEquals(4.0f, programaSinExtra.promedioGeneral(), 0.001f)
        assertEquals(4.5f, programaConExtra.promedioGeneral(), 0.001f)
    }

    @Test
    fun asignarElectivaComplementariaAplicaCreditosPersonalizados() {
        val est = baseEstudiante()
        val actualizado = est.asignarElectivaComplementaria(
            posicion = 0,
            codigo = "402999M",
            nombre = "Complementaria Personalizada",
            nota = NotaImportada.Numerica(4.8f),
            creditos = 4
        )
        val comp = actualizado.asignaturas.find { it.codigo == "402999M" }
        assertNotNull(comp)
        assertEquals(4, comp?.creditos)
        assertEquals(4.8f, comp?.nota ?: 0f, 0.001f)
    }

    @Test
    fun cambiarCreditosAsignaturaActualizaCorrectamente() {
        val est = baseEstudiante()
        val actualizado = est.cambiarCreditosAsignatura("402001M", 5)
        assertEquals(5, actualizado.asignaturas.find { it.codigo == "402001M" }?.creditos)
    }

    @Test
    fun extracurricularNoAprobadaNoImpideCompletarSemestre() {
        val est = baseEstudiante()
        // Agregamos una extracurricular reprobada en semestre 1
        val extraReprobada = AsignaturaNoSincronizada(
            codigo = "EXT200M",
            nombre = "Ajedrez Avanzado",
            nota = NotaImportada.Numerica(2.0f),
            semestre = 1,
            creditos = 3
        )
        val estConExtra = est.agregarAsignaturasExtracurriculares(listOf(extraReprobada))

        // Simulamos aprobación de las materias del pensum en semestre 1
        val estAprobado = estConExtra.copy(
            asignaturas = estConExtra.asignaturas.map {
                if (it.codigo == "402180M" || it.codigo == "402001M") it.copy(nota = 4.0f) else it
            }.toMutableList()
        )

        val prog = ProgramaM(estudiante = estAprobado)
        // Semestre 1 debe completarse automáticamente a pesar de la extracurricular reprobada
        assertTrue(prog.semestreCompletadoAutomatico(1))
    }
}
