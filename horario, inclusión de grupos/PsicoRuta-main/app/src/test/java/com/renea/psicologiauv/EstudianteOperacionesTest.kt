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
}
