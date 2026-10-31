package com.renea.psicologiauv

import com.renea.psicologiauv.ui.theme.TipoLinea
import com.renea.psicologiauv.ui.theme.obtenerTemaLinea
import org.junit.Assert.assertEquals
import org.junit.Test

class TemaLineaTest {

    @Test
    fun clinicaResuelveTipoClinica() {
        assertEquals(TipoLinea.CLINICA, obtenerTemaLinea("Clínica").tipo)
        assertEquals(TipoLinea.CLINICA, obtenerTemaLinea("clinica").tipo)
        assertEquals(TipoLinea.CLINICA, obtenerTemaLinea("CLÍNICA").tipo)
    }

    @Test
    fun neuroclinicaResuelveTipoNeuroclinicaYNoClinica() {
        assertEquals(TipoLinea.NEUROCLINICA, obtenerTemaLinea("NeuroClínica").tipo)
        assertEquals(TipoLinea.NEUROCLINICA, obtenerTemaLinea("neuroclinica").tipo)
        assertEquals(TipoLinea.NEUROCLINICA, obtenerTemaLinea("neuro").tipo)
    }

    @Test
    fun socialResuelveTipoSocial() {
        assertEquals(TipoLinea.SOCIAL, obtenerTemaLinea("Social").tipo)
        assertEquals(TipoLinea.SOCIAL, obtenerTemaLinea("social").tipo)
    }

    @Test
    fun organizacionalResuelveTipoOrganizacional() {
        assertEquals(TipoLinea.ORGANIZACIONAL, obtenerTemaLinea("Organizacional").tipo)
        assertEquals(TipoLinea.ORGANIZACIONAL, obtenerTemaLinea("organizacional").tipo)
    }

    @Test
    fun educativaResuelveTipoEducativa() {
        assertEquals(TipoLinea.EDUCATIVA, obtenerTemaLinea("Educativa").tipo)
        assertEquals(TipoLinea.EDUCATIVA, obtenerTemaLinea("educativa").tipo)
    }

    @Test
    fun nuloOVacioODesconocidoResuelveTipoNinguna() {
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea(null).tipo)
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("").tipo)
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("   ").tipo)
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("Otra Carrera").tipo)
    }
}
