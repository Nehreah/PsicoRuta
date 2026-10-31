package com.renea.psicologiauv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Operaciones de edición y sincronización de Estudiante que aún no tenían pruebas. */
class EstudianteOperacionesTest {

    @Test fun cambiarNombreYLineaYAvatar() {
        val e = crearEstudiante()
        assertEquals("Nuevo", e.cambiarNombre("Nuevo").nombre)
        assertEquals("Social", e.cambiarLinea("Social").linea)
        assertEquals("content://a", e.cambiarAvatar("content://a").avatarUri)
    }

    @Test fun cambiarCodigoDelEstudianteRechazaValoresNoPositivos() {
        val e = crearEstudiante()
        assertEquals(555, e.cambiarCodigo(555).codigo)
        assertSame(e, e.cambiarCodigo(0))
        assertSame(e, e.cambiarCodigo(-3))
    }

    @Test fun cambiarNotaDeUnaAsignaturaSoloAfectaAEsaAsignatura() {
        val e = crearEstudiante(crearAsignatura("A"), crearAsignatura("B"))
        val r = e.cambiarNotaAsignatura("A", 4.5f)
        assertEquals(4.5f, r.asignaturas.first { it.codigo == "A" }.nota, 0.001f)
        assertEquals(0f, r.asignaturas.first { it.codigo == "B" }.nota, 0.001f)
    }

    @Test fun cambiarNotaEspecialMarcaLaAsignaturaComoAprobadaSinNota() {
        val e = crearEstudiante(crearAsignatura("A", nota = 4.0f))
        val r = e.cambiarNotaEspecialAsignatura("A", "C.U").asignaturas.single()
        assertEquals("C.U", r.notaEspecial)
        assertEquals(0f, r.nota, 0f)
        assertTrue(r.aprobo())
    }

    @Test fun cambiarSemestreRespetaElRangoDeLaAsignatura() {
        val e = crearEstudiante(crearAsignatura("A", semestre = 3))
        assertEquals(7, e.cambiarSemestreAsignatura("A", 7).asignaturas.single().semestre)
        assertEquals(3, e.cambiarSemestreAsignatura("A", 40).asignaturas.single().semestre)
    }

    @Test fun nombreYCodigoSoloSePuedenEditarEnElectivasEIdiomas() {
        val e = crearEstudiante(
            crearAsignatura("OBL"),
            crearEspacioElectivaProfesional("3"),
            crearAsignatura("IDI", componente = "Idioma")
        )
        val r = e.cambiarNombreAsignatura("OBL", "X")
            .cambiarNombreAsignatura("3", "Electiva X")
            .cambiarCodigoAsignatura("OBL", "NUEVO")
            .cambiarCodigoAsignatura("3", "402161M")
            .cambiarCodigoAsignatura("IDI", "204355M")

        assertEquals("Materia OBL", r.asignaturas[0].nombre)
        assertEquals("OBL", r.asignaturas[0].codigo)
        assertEquals("Electiva X", r.asignaturas[1].nombre)
        assertEquals("402161M", r.asignaturas[1].codigo)
        assertEquals("204355M", r.asignaturas[2].codigo)
    }

    @Test fun agregarAsignaturaNoModificaLaInstanciaOriginal() {
        val e = crearEstudiante(crearAsignatura("A"))
        val r = e.agregarAsignatura(crearAsignatura("B"))
        assertEquals(1, e.asignaturas.size)
        assertEquals(listOf("A", "B"), r.asignaturas.map { it.codigo })
    }

    @Test fun eliminarAsignaturaInexistenteDevuelveElMismoEstudiante() {
        val e = crearEstudiante(crearAsignatura("A"))
        assertSame(e, e.eliminarAsignatura("ZZZ"))
        assertSame(e, e.eliminarAsignatura("A")) // no es complementaria
    }

    // ------------------------------------------------------------------
    // sincronizarComplementarias (402226M)
    // ------------------------------------------------------------------

    private fun actividad(codigo: String, creditos: Int) = crearAsignatura(
        codigo = codigo,
        creditos = creditos,
        area = "Complementario",
        nota = 5f,
        notaEspecial = "A.P",
        complementaria = true
    )

    private fun materia402226M(nota: Float = 0f, especial: String? = null) =
        crearAsignatura("402226M", area = AREA_FII, nota = nota, notaEspecial = especial)

    @Test fun sincronizarSinLaMateria402226MNoHaceNada() {
        val e = crearEstudiante(actividad("999", 5))
        assertSame(e, e.sincronizarComplementarias())
    }

    @Test fun cincoCreditosComplementariosApruebanLa402226M() {
        val e = crearEstudiante(materia402226M(), actividad("999", 3), actividad("1000", 2))
        val r = e.sincronizarComplementarias()
        val materia = r.asignaturas.first { it.codigo == "402226M" }
        assertEquals("A.P", materia.notaEspecial)
        assertTrue(materia.aprobo())
    }

    @Test fun menosDeCincoCreditosNoApruebanLa402226M() {
        val e = crearEstudiante(materia402226M(), actividad("999", 3))
        assertTrue(!e.sincronizarComplementarias().asignaturas.first { it.codigo == "402226M" }.aprobo())
    }

    @Test fun siBajanLosCreditosSeReiniciaLaMarcaAutomatica() {
        val e = crearEstudiante(materia402226M(especial = "A.P"), actividad("999", 3))
        val materia = e.sincronizarComplementarias().asignaturas.first { it.codigo == "402226M" }
        assertNull(materia.notaEspecial)
        assertEquals(0f, materia.nota, 0f)
    }

    @Test fun unaNotaRealEn402226MNoSeReiniciaAunqueFaltenCreditos() {
        val e = crearEstudiante(materia402226M(nota = 4.0f), actividad("999", 1))
        val materia = e.sincronizarComplementarias().asignaturas.first { it.codigo == "402226M" }
        assertEquals(4.0f, materia.nota, 0.001f)
    }

    @Test fun unaMateria402226MYaAprobadaPorPdfNoSeModifica() {
        val e = crearEstudiante(materia402226M(especial = "C.U"), actividad("999", 5))
        val materia = e.sincronizarComplementarias().asignaturas.first { it.codigo == "402226M" }
        assertEquals("C.U", materia.notaEspecial)
    }

    // ------------------------------------------------------------------
    // semestresManuales y cambiarEstadoSemestre
    // ------------------------------------------------------------------

    @Test fun cambiarEstadoSemestreAgregaModificaYRevierte() {
        val e = crearEstudiante()
        val e1 = e.cambiarEstadoSemestre(3, true)
        assertEquals(mapOf(3 to true), e1.semestresManuales)

        val e2 = e1.cambiarEstadoSemestre(4, false)
        assertEquals(mapOf(3 to true, 4 to false), e2.semestresManuales)

        val e3 = e2.cambiarEstadoSemestre(3, false)
        assertEquals(mapOf(3 to false, 4 to false), e3.semestresManuales)

        val e4 = e3.cambiarEstadoSemestre(3, null)
        assertEquals(mapOf(4 to false), e4.semestresManuales)
    }

    // ------------------------------------------------------------------
    // asignarElectivaComplementaria
    // ------------------------------------------------------------------

    @Test fun asignarElectivaComplementariaModificaElSlotCorrespondiente() {
        val comp1 = crearAsignatura("C1", componente = "Electiva complementaria", nota = 0f)
        val comp2 = crearAsignatura("C2", componente = "Electiva complementaria", nota = 0f)
        val e = crearEstudiante(comp1, comp2)

        val r1 = e.asignarElectivaComplementaria(
            posicion = 0,
            codigo = "COMP_NUEVA",
            nombre = "Teatro",
            nota = com.renea.psicologiauv.model.NotaImportada.Numerica(4.5f)
        )
        val m1 = r1.asignaturas.first { it.codigo == "COMP_NUEVA" }
        assertEquals("Teatro", m1.nombre)
        assertEquals(4.5f, m1.nota, 0.001f)
        assertEquals("C2", r1.asignaturas[1].codigo)

        val r2 = r1.asignarElectivaComplementaria(
            posicion = 1,
            codigo = "COMP_DANZA",
            nombre = "Danza",
            nota = com.renea.psicologiauv.model.NotaImportada.Especial("EX")
        )
        val m2 = r2.asignaturas.first { it.codigo == "COMP_DANZA" }
        assertEquals("Danza", m2.nombre)
        assertEquals("EX", m2.notaEspecial)
        assertEquals(0f, m2.nota, 0f)
    }

    @Test fun asignarElectivaComplementariaFueraDeRangoNoModificaNada() {
        val comp1 = crearAsignatura("C1", componente = "Electiva complementaria")
        val e = crearEstudiante(comp1)
        assertSame(e, e.asignarElectivaComplementaria(5, "X", "Nombre", null))
        assertSame(e, e.asignarElectivaComplementaria(-1, "X", "Nombre", null))
    }

    // ------------------------------------------------------------------
    // moverAsignaturasEnCursoAlSemestre
    // ------------------------------------------------------------------

    @Test fun moverAsignaturasEnCursoAlSemestreMueveSoloMateriasSinNotaEnCurso() {
        val m1 = crearAsignatura("402180M", semestre = 1, nota = 0f) // en curso, sin nota
        val m2 = crearAsignatura("402181M", semestre = 1, nota = 4.0f) // aprobada, no debe moverse
        val m3 = crearAsignatura("402182M", semestre = 4, nota = 0f) // ya en semestre 4
        val e = crearEstudiante(m1, m2, m3)

        val (actualizado, movidas) = e.moverAsignaturasEnCursoAlSemestre(
            codigosEnCurso = listOf("402180M", "402181M", "402182M"),
            semestreActual = 4
        )
        assertEquals(1, movidas)
        assertEquals(4, actualizado.asignaturas.first { it.codigo == "402180M" }.semestre)
        assertEquals(1, actualizado.asignaturas.first { it.codigo == "402181M" }.semestre)
        assertEquals(4, actualizado.asignaturas.first { it.codigo == "402182M" }.semestre)
    }

    @Test fun moverAsignaturasConListaVaciaRetornaCeroMovidas() {
        val e = crearEstudiante(crearAsignatura("A", semestre = 1))
        val (actualizado, movidas) = e.moverAsignaturasEnCursoAlSemestre(emptyList(), 3)
        assertEquals(0, movidas)
        assertSame(e, actualizado)
    }
}
