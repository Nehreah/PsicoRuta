package com.renea.psicologiauv

import com.renea.psicologiauv.model.ProgramaM
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas académicas derivadas en ProgramaM. */
class ProgramaMTest {

    private fun programaCon(
        vararg materias: com.renea.psicologiauv.model.Asignatura,
        programa: ProgramaM = ProgramaM()
    ) = programa.copy(estudiante = crearEstudiante(*materias))

    // ------------------------------------------------------------------
    // Promedios
    // ------------------------------------------------------------------

    @Test fun promedioGeneralEsPonderadoPorCreditosYExcluyeReprobadasYMarcasEspeciales() {
        val programa = programaCon(
            crearAsignatura("A", creditos = 3, nota = 4.0f),
            crearAsignatura("B", creditos = 3, nota = 3.0f),
            crearAsignatura("C", creditos = 3, nota = 2.0f),            // reprobada
            crearAsignatura("D", creditos = 3, notaEspecial = "C.U")    // sin nota numérica
        )
        assertEquals(3.5f, programa.promedioGeneral(), 0.001f)
    }

    @Test fun promedioGeneralPondera() {
        val programa = programaCon(
            crearAsignatura("A", creditos = 4, nota = 5.0f),
            crearAsignatura("B", creditos = 2, nota = 3.0f)
        )
        assertEquals((20f + 6f) / 6f, programa.promedioGeneral(), 0.001f)
    }

    @Test fun promedioGeneralSinAprobadasOSinEstudianteEsCero() {
        assertEquals(0f, ProgramaM(estudiante = null).promedioGeneral(), 0f)
        assertEquals(0f, programaCon(crearAsignatura("A", nota = 1.0f)).promedioGeneral(), 0f)
    }

    @Test fun promedioDeSemestreSoloUsaMateriasDeEseSemestre() {
        val programa = programaCon(
            crearAsignatura("A", semestre = 1, nota = 4.0f),
            crearAsignatura("B", semestre = 1, nota = 3.0f),
            crearAsignatura("C", semestre = 2, nota = 5.0f)
        )
        assertEquals(3.5f, programa.promedioSemestre(1), 0.001f)
        assertEquals(5.0f, programa.promedioSemestre(2), 0.001f)
        assertEquals(0f, programa.promedioSemestre(3), 0f)
    }

    // ------------------------------------------------------------------
    // Semestres
    // ------------------------------------------------------------------

    @Test fun semestreSinMateriasNoEstaCompletado() {
        assertFalse(ProgramaM(estudiante = crearEstudiante()).semestreCompletado(1))
    }

    @Test fun semestreRegularExigeTodasLasMateriasAprobadas() {
        val completo = programaCon(
            crearAsignatura("A", semestre = 2, nota = 3.0f),
            crearAsignatura("B", semestre = 2, nota = 4.0f)
        )
        assertTrue(completo.semestreCompletado(2))

        val incompleto = programaCon(
            crearAsignatura("A", semestre = 2, nota = 3.0f),
            crearAsignatura("B", semestre = 2)
        )
        assertFalse(incompleto.semestreCompletado(2))
        assertEquals(3, incompleto.creditosPendientesSemestre(2))
    }

    @Test fun electivaComplementariaNoCuentaParaCompletarElSemestre() {
        val programa = programaCon(
            crearAsignatura("A", semestre = 2, nota = 4.0f),
            crearAsignatura("1", semestre = 2, componente = "Electiva complementaria")
        )
        assertTrue(programa.semestreCompletado(2))
        assertEquals(0, programa.creditosPendientesSemestre(2))
    }

    @Test fun semestreSeisTolerasHastaSeisCreditosPeroExigeEtica() {
        val conEtica = programaCon(
            crearAsignatura("E", nombre = "Ética del Ejercicio Profesional", semestre = 6, nota = 4.0f),
            crearAsignatura("A", semestre = 6),
            crearAsignatura("B", semestre = 6)
        )
        assertTrue(conEtica.semestreCompletado(6))

        val demasiadosPendientes = programaCon(
            crearAsignatura("E", nombre = "Ética del Ejercicio Profesional", semestre = 6, nota = 4.0f),
            crearAsignatura("A", semestre = 6),
            crearAsignatura("B", semestre = 6),
            crearAsignatura("C", semestre = 6)
        )
        assertFalse(demasiadosPendientes.semestreCompletado(6))

        val sinEtica = programaCon(
            crearAsignatura("E", nombre = "Ética del Ejercicio Profesional", semestre = 6),
            crearAsignatura("A", semestre = 6, nota = 4.0f)
        )
        assertFalse(sinEtica.semestreCompletado(6))
    }

    @Test fun semestreOchoTolerasNueveCreditosConLasTresCriticasAprobadas() {
        fun materiasCriticas(aprobadas: Boolean) = arrayOf(
            // Se escriben sin tildes y en mayúsculas a propósito: la comparación las normaliza.
            crearAsignatura("1", nombre = "EVALUACION Y DIAGNOSTICO", semestre = 8, nota = if (aprobadas) 4f else 0f),
            crearAsignatura("2", nombre = "Seminario de Trabajo de Grado II", semestre = 8, nota = if (aprobadas) 4f else 0f),
            crearAsignatura("3", nombre = "Practica de Fundamentacion Profesional II", semestre = 8, nota = if (aprobadas) 4f else 0f)
        )
        assertTrue(programaCon(*materiasCriticas(true)).semestreCompletado(8))
        assertFalse(programaCon(*materiasCriticas(false)).semestreCompletado(8))
    }

    private fun semestreAprobado(semestre: Int) =
        crearAsignatura("S$semestre", semestre = semestre, nota = 4.0f)

    @Test fun semestreEnCursoEsElSiguienteAlUltimoCompletado() {
        val programa = programaCon(
            semestreAprobado(1),
            semestreAprobado(2),
            crearAsignatura("B", semestre = 3)
        )
        assertEquals(3, programa.semestreEnCurso())
    }

    @Test fun sinNingunSemestreCompletadoElEstudianteEstaEnPrimero() {
        assertEquals(1, ProgramaM(estudiante = null).semestreEnCurso())
        assertEquals(1, ProgramaM(estudiante = crearEstudiante()).semestreEnCurso())
        assertEquals(1, programaCon(crearAsignatura("A", semestre = 1)).semestreEnCurso())
    }

    @Test fun conSoloElPrimerSemestreCompletadoEstaEnSegundo() {
        assertEquals(2, programaCon(semestreAprobado(1)).semestreEnCurso())
    }

    @Test fun siHayUnSemestreIncompletoEnMedioSeUsaElUltimoCompletado() {
        // 1, 2, 3 y 5 completos; el 4 quedó con una materia pendiente.
        val programa = programaCon(
            semestreAprobado(1), semestreAprobado(2), semestreAprobado(3),
            crearAsignatura("P4", semestre = 4),
            semestreAprobado(5)
        )
        assertEquals(6, programa.semestreEnCurso())
    }

    @Test fun elSemestreEnCursoNoSuperaElUltimoDelPlan() {
        assertEquals(10, programaCon(semestreAprobado(10)).semestreEnCurso())
        assertEquals(10, programaCon(semestreAprobado(9)).semestreEnCurso())
    }

    @Test fun elSemestreOnceSoloEsPosibleSiElEstudianteTieneMateriasEnEl() {
        assertEquals(11, programaCon(semestreAprobado(10), crearAsignatura("O", semestre = 11)).semestreEnCurso())
        assertEquals(11, programaCon(semestreAprobado(10), semestreAprobado(11)).semestreEnCurso())
    }

    @Test fun semestresDelPlanIncluyeElOnceSoloSiTieneMaterias() {
        assertEquals((1..10).toList(), programaCon(crearAsignatura("A", semestre = 1)).semestresDelPlan())
        assertEquals((1..11).toList(), programaCon(crearAsignatura("A", semestre = 11)).semestresDelPlan())
    }

    @Test fun asignaturasPorSemestreIgnoraSemestresFueraDelPlan() {
        val programa = programaCon(
            crearAsignatura("A", semestre = 0),
            crearAsignatura("B", semestre = 3),
            crearAsignatura("C", semestre = 99)
        )
        assertEquals(setOf(3), programa.asignaturasPorSemestre().keys)
    }

    // ------------------------------------------------------------------
    // Créditos y avance
    // ------------------------------------------------------------------

    @Test fun creditosPorAreaSeLimitanAlTotalDelPrograma() {
        val programa = programaCon(
            crearAsignatura("A", nota = 4.0f, area = AREA_FDI),
            crearAsignatura("B", nota = 4.0f, area = AREA_FDI),
            crearAsignatura("C", nota = 4.0f, area = AREA_FDI),
            programa = ProgramaM(creditosFDI = 6)
        )
        assertEquals(6, programa.creditosFDIAprobados())
    }

    @Test fun creditosFPCAEIFIIContanSoloAprobadas() {
        val programa = programaCon(
            crearAsignatura("A", nota = 4.0f, area = AREA_FPCA),
            crearAsignatura("B", nota = 1.0f, area = AREA_FPCA),
            crearAsignatura("C", nota = 4.0f, area = AREA_FII)
        )
        assertEquals(3, programa.creditosFPCAAprobados())
        assertEquals(3, programa.creditosFIIAprobados())
    }

    @Test fun actividadesComplementariasSumanHastaElMaximo() {
        val tres = programaCon(crearAsignatura("999", area = "Complementario", nota = 5f, complementaria = true))
        assertEquals(3, tres.creditosComplementariosAprobados())

        val siete = programaCon(
            crearAsignatura("999", area = "Complementario", creditos = 4, nota = 5f, complementaria = true),
            crearAsignatura("1000", area = "Complementario", creditos = 3, nota = 5f, complementaria = true)
        )
        assertEquals(5, siete.creditosComplementariosAprobados())
        assertTrue(siete.requisitoCreditosComplementariosCumplido())
    }

    @Test fun laMateria402226MAprobadaCuentaLosCincoCreditosComplementarios() {
        val programa = programaCon(crearAsignatura("402226M", area = AREA_FII, notaEspecial = "A.P"))
        assertEquals(5, programa.creditosComplementariosAprobados())
        assertEquals(5, programa.creditosFIIAprobados())
    }

    @Test fun asignaturaComplementariaParaCreditoReparteLosSegmentos() {
        val a = crearAsignatura("999", area = "Complementario", creditos = 2, nota = 5f, complementaria = true)
        val b = crearAsignatura("1000", area = "Complementario", creditos = 2, nota = 5f, complementaria = true)
        val programa = programaCon(a, b)
        assertEquals("999", programa.asignaturaComplementariaParaCredito(0)?.codigo)
        assertEquals("999", programa.asignaturaComplementariaParaCredito(1)?.codigo)
        assertEquals("1000", programa.asignaturaComplementariaParaCredito(2)?.codigo)
        assertNull(programa.asignaturaComplementariaParaCredito(4))
        assertNull(programa.asignaturaComplementariaParaCredito(-1))
        assertNull(programa.asignaturaComplementariaParaCredito(5))
    }

    @Test fun avanceYFaltantesDeCarrera() {
        val programa = programaCon(crearAsignatura("A", nota = 4.0f, area = AREA_FDI))
        assertEquals(3, programa.creditosQueSirvenParaCarrera())
        assertEquals(165, programa.creditosFaltantesCarrera())
        assertEquals(3f / 168f * 100f, programa.avanceCarrera(), 0.001f)
        assertEquals(3, programa.avanceAreas().size)
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Test fun asignaturasFiltradasPorAreaYEstado() {
        val programa = programaCon(
            crearAsignatura("A", area = AREA_FDI, nota = 4f),
            crearAsignatura("B", area = AREA_FDI),
            crearAsignatura("C", area = AREA_FPCA, nota = 4f)
        )
        assertEquals(3, programa.asignaturasFiltradas().size)
        assertEquals(listOf("A"), programa.asignaturasFiltradas(AREA_FDI, true).map { it.codigo })
        assertEquals(listOf("B"), programa.asignaturasFiltradas(AREA_FDI, false).map { it.codigo })
        assertEquals(listOf(AREA_FDI, AREA_FPCA).sorted(), programa.areasDelEstudiante())
    }

    @Test fun lineasProfesionalesSumanSoloAprobadas() {
        val programa = programaCon(
            crearAsignatura("A", lineaProfesional = "Social", nota = 4f),
            crearAsignatura("B", lineaProfesional = "Social"),
            crearAsignatura("C", lineaProfesional = "Clínica", nota = 4f)
        )
        assertEquals(3, programa.creditosLineaProfesional("Social"))
        assertEquals(2, programa.asignaturasDeLinea("Social").size)
        assertTrue(programa.asignaturasDeLinea(null).isEmpty())
    }

    // ------------------------------------------------------------------
    // Idiomas
    // ------------------------------------------------------------------

    @Test fun idiomasSeEvaluanPorNombreSinImportarElCodigo() {
        val programa = programaCon(
            crearAsignatura("204355M", nombre = "Idiomas I", nota = 4f),
            crearAsignatura("999999M", nombre = "Idiomas II", nota = 3.5f)
        )
        assertEquals(setOf("Idiomas I", "Idiomas II"), programa.cursosInglesAprobados())
        assertTrue(programa.requisitoInglesCumplido())

        val datos = programa.datosIdiomas()
        assertTrue(datos.existeIdiomasI && datos.idiomasIAprobado)
        assertTrue(datos.existeIdiomasII && datos.idiomasIIAprobado)
    }

    @Test fun materiaIdiomaActivaPrefiereAprobadaLuegoConNotaLuegoCualquiera() {
        val programa = ProgramaM()
        val sinNota = crearAsignatura("A", nombre = "Idiomas I")
        val reprobada = crearAsignatura("B", nombre = "Idiomas I", nota = 2.0f)
        val aprobada = crearAsignatura("C", nombre = "Idiomas I", nota = 4.0f)

        assertEquals("C", programa.materiaIdiomaActiva(listOf(sinNota, reprobada, aprobada))?.codigo)
        assertEquals("B", programa.materiaIdiomaActiva(listOf(sinNota, reprobada))?.codigo)
        assertEquals("A", programa.materiaIdiomaActiva(listOf(sinNota))?.codigo)
        assertNull(programa.materiaIdiomaActiva(emptyList()))
    }

    // ------------------------------------------------------------------
    // Requisitos de grado
    // ------------------------------------------------------------------

    private fun materiasDeGrado(omitir: String? = null) = listOf(
        crearAsignatura("A", nota = 4.0f, area = AREA_FDI),
        crearAsignatura("402226M", area = AREA_FII, notaEspecial = "A.P"),
        crearAsignatura("402222M", nota = 4f),
        crearAsignatura("402224M", nota = 4f),
        crearAsignatura("402223M", nota = 4f),
        crearAsignatura("402225M", nota = 4f),
        crearAsignatura("204355M", nombre = "Idiomas I", nota = 4f),
        crearAsignatura("204357M", nombre = "Idiomas II", nota = 4f)
    ).filter { it.codigo != omitir }

    // Programa reducido a 3 créditos de carrera para no tener que armar 168.
    private fun programaDeGrado(omitir: String? = null) =
        ProgramaM(creditosCarrera = 3).copy(
            estudiante = crearEstudiante(*materiasDeGrado(omitir).toTypedArray())
        )

    @Test fun cumpleRequisitosDeGradoCuandoTodoEstaAprobado() {
        assertTrue(programaDeGrado().cumpleRequisitosGrado())
    }

    @Test fun faltarUnaPracticaImpideGraduarse() {
        val programa = programaDeGrado(omitir = "402224M")
        assertFalse(programa.requisitoPracticasProfesionalesCumplido())
        assertFalse(programa.cumpleRequisitosGrado())
    }

    @Test fun faltarElTrabajoDeGradoImpideGraduarse() {
        val programa = programaDeGrado(omitir = "402225M")
        assertFalse(programa.requisitoTrabajoDeGradoCumplido())
        assertFalse(programa.cumpleRequisitosGrado())
    }

    @Test fun faltarIdiomasImpideGraduarse() {
        val programa = programaDeGrado(omitir = "204357M")
        assertFalse(programa.requisitoInglesCumplido())
        assertFalse(programa.cumpleRequisitosGrado())
    }

    @Test fun faltarLosCreditosDeCarreraImpideGraduarse() {
        val programa = programaDeGrado(omitir = "A").copy(creditosCarrera = 168)
        assertFalse(programa.requisitoCreditosCarreraCumplido())
        assertFalse(programa.cumpleRequisitosGrado())
    }

    @Test fun unEstudianteNuevoNoCumpleNingunRequisito() {
        val programa = ProgramaM(estudiante = crearEstudiante())
        assertFalse(programa.cumpleRequisitosGrado())
        assertFalse(programa.requisitoPracticasProfesionalesCumplido())
        assertFalse(programa.requisitoTrabajoDeGradoCumplido())
        assertFalse(programa.requisitoCreditosComplementariosCumplido())
    }
}
