package com.renea.psicologiauv

import com.renea.psicologiauv.data.RepositorioHorarios
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.AsignaturaHorario
import com.renea.psicologiauv.model.Estudiante
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositorioHorariosTest {

    private fun materia(codigo: String, semestre: Int, nota: Float = 0f, grupo: String = ""): Asignatura =
        Asignatura(
            nombre = "Materia $codigo",
            codigo = codigo,
            semestre = semestre,
            creditos = 3,
            lineaProfesional = "No",
            componente = "Obligatoria",
            area = "Área",
            nota = nota,
            grupo = grupo
        )

    private val catalogo = listOf(
        AsignaturaHorario(codigo = "402180M", nombreAsignatura = "Teorías", grupo = "01", diaClase = "LUN", horaInicio = 7, horaFin = 9),
        AsignaturaHorario(codigo = "402180M", nombreAsignatura = "Teorías", grupo = "02", diaClase = "MAR", horaInicio = 9, horaFin = 11),
        AsignaturaHorario(codigo = "402181M", nombreAsignatura = "Procesos", grupo = "01", diaClase = "MIE", horaInicio = 14, horaFin = 16),
        AsignaturaHorario(codigo = "402182M", nombreAsignatura = "Psicometría", grupo = "GR01", diaClase = "JUE", horaInicio = 7, horaFin = 9)
    )

    @Test fun siNoHayMateriasConGrupoRetornaVacio() {
        val estudiante = Estudiante(
            asignaturas = mutableListOf(
                materia("402180M", semestre = 1, grupo = ""),
                materia("402181M", semestre = 1, grupo = "")
            )
        )
        val horario = RepositorioHorarios.horarioDelSemestre(estudiante, semestreActual = 1, catalogo)
        assertTrue(horario.isEmpty())
    }

    @Test fun filtraPorCodigoYGrupoMatriculado() {
        val estudiante = Estudiante(
            asignaturas = mutableListOf(
                materia("402180M", semestre = 1, grupo = "02"),
                materia("402181M", semestre = 1, grupo = "01")
            )
        )
        val horario = RepositorioHorarios.horarioDelSemestre(estudiante, semestreActual = 1, catalogo)
        assertEquals(2, horario.size)
        assertEquals("402180M", horario[0].codigo)
        assertEquals("02", horario[0].grupo)
        assertEquals("402181M", horario[1].codigo)
        assertEquals("01", horario[1].grupo)
    }

    @Test fun toleraPrefijoGREnGrupo() {
        val estudiante = Estudiante(
            asignaturas = mutableListOf(
                materia("402182M", semestre = 1, grupo = "01")
            )
        )
        val horario = RepositorioHorarios.horarioDelSemestre(estudiante, semestreActual = 1, catalogo)
        assertEquals(1, horario.size)
        assertEquals("402182M", horario[0].codigo)
    }

    @Test fun noIncluyeMateriasAprobadas() {
        val estudiante = Estudiante(
            asignaturas = mutableListOf(
                materia("402180M", semestre = 1, nota = 4.5f, grupo = "01")
            )
        )
        val horario = RepositorioHorarios.horarioDelSemestre(estudiante, semestreActual = 1, catalogo)
        assertTrue(horario.isEmpty())
    }
}
