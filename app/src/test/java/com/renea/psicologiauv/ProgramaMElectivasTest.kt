package com.renea.psicologiauv

import com.renea.psicologiauv.model.NotaImportada
import com.renea.psicologiauv.model.ProgramaM
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Estado de las electivas en ProgramaM, incluido el caso posterior a importar el PDF. */
class ProgramaMElectivasTest {

    private fun programaCon(vararg materias: com.renea.psicologiauv.model.Asignatura) =
        ProgramaM(estudiante = crearEstudiante(*materias))

    @Test fun sinEstudianteTodasLasBarrasEstanVacias() {
        val programa = ProgramaM(estudiante = null)
        assertEquals(listOf(false, false), programa.estadosElectivasComplementarias())
        assertEquals(listOf(false, false, false), programa.estadosElectivasProfesionalesIIIII())
        assertEquals(6, programa.estadosElectivasPensum().size)
        assertTrue(programa.estadosElectivasPensum().none { it })
    }

    @Test fun espaciosProvisionalesAprobadosSeReflejanEnLasBarras() {
        val programa = programaCon(
            crearElectivaProfesionalI(nota = 4.0f),
            crearEspacioElectivaProfesional("3", nota = 4.0f),
            crearEspacioElectivaProfesional("4"),
            crearEspacioElectivaProfesional("5", nota = 3.2f)
        )
        assertEquals(listOf(true, false, true), programa.estadosElectivasProfesionalesIIIII())
    }

    @Test fun lasBarrasSiguenFuncionandoCuandoElEspacioYaTieneCodigoReal() {
        // Regresión: tras importar el PDF, "3"/"4"/"5" se reemplazan por el
        // código real de la electiva y las barras deben seguir marcándose.
        val inicial = crearEstudiante(
            crearElectivaProfesionalI(),
            crearEspacioElectivaProfesional("3"),
            crearEspacioElectivaProfesional("4"),
            crearEspacioElectivaProfesional("5")
        )
        val (resuelto, _) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(
                "402161M" to NotaImportada.Numerica(4.0f),
                "402163M" to NotaImportada.Numerica(3.5f)
            ),
            crearCatalogo("402161M", "402163M")
        )
        val programa = ProgramaM(estudiante = resuelto)
        assertEquals(listOf(true, true, false), programa.estadosElectivasProfesionalesIIIII())
    }

    @Test fun electivasIIaIVExcluyenLaIYSeLimitanATres() {
        val programa = programaCon(
            crearElectivaProfesionalI(),
            crearEspacioElectivaProfesional("402161M"),
            crearEspacioElectivaProfesional("4"),
            crearEspacioElectivaProfesional("5"),
            crearEspacioElectivaProfesional("6")
        )
        assertEquals(listOf("402161M", "4", "5"), programa.electivasProfesionalesIIaIV().map { it.codigo })
    }

    @Test fun electivasIIaIVIgnoranMateriasQueNoSonElectivasProfesionales() {
        val programa = programaCon(
            crearAsignatura("402180M"),
            crearAsignatura("1", componente = "Electiva complementaria"),
            crearEspacioElectivaProfesional("3")
        )
        assertEquals(listOf("3"), programa.electivasProfesionalesIIaIV().map { it.codigo })
    }

    @Test fun electivaProfesionalIAparecePorSeparadoEnElEstadoDelPensum() {
        val programa = programaCon(
            crearElectivaProfesionalI(nota = 4.0f),
            crearEspacioElectivaProfesional("3")
        )
        val estados = programa.estadosElectivasPensum()
        assertEquals(6, estados.size)
        assertTrue(estados[2])
        assertFalse(estados[3])
    }

    @Test fun electivasComplementariasSeIdentificanPorCodigoUnoYDos() {
        val programa = programaCon(
            crearAsignatura("1", componente = "Electiva complementaria", nota = 4.0f),
            crearAsignatura("2", componente = "Electiva complementaria")
        )
        assertEquals(listOf(true, false), programa.estadosElectivasComplementarias())
    }

    @Test fun creditosDeElectivasProfesionalesSoloSumanLasAprobadas() {
        val programa = programaCon(
            crearElectivaProfesionalI(nota = 4.0f),
            crearEspacioElectivaProfesional("3", nota = 2.0f),
            crearEspacioElectivaProfesional("4", nota = 3.5f)
        )
        assertEquals(6, programa.creditoselectivasProfesionales())
    }

    @Test fun requisitoDeElectivasExigeDoceCreditosEntreProfesionalesYComplementarias() {
        val once = programaCon(
            crearElectivaProfesionalI(nota = 4.0f),
            crearEspacioElectivaProfesional("3", nota = 4.0f),
            crearEspacioElectivaProfesional("4", nota = 4.0f),
            crearAsignatura("1", componente = "Electiva complementaria", nota = 4.0f).copy(creditos = 2)
        )
        assertFalse(once.requisitoElectivas())
        assertEquals(11, once.creditoselectivasProfesionales() + once.creditoselectivasComplementarias())

        val doce = programaCon(
            crearElectivaProfesionalI(nota = 4.0f),
            crearEspacioElectivaProfesional("3", nota = 4.0f),
            crearEspacioElectivaProfesional("4", nota = 4.0f),
            crearAsignatura("1", componente = "Electiva complementaria", nota = 4.0f)
        )
        assertTrue(doce.requisitoElectivas())
    }
}
