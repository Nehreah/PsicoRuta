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
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("Ninguna").tipo)
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("Sin línea").tipo)
        assertEquals(TipoLinea.NINGUNA, obtenerTemaLinea("Otra Carrera").tipo)
    }

    @Test
    fun temaNeutroTieneColoresDeAltoContrasteBlancoYNegro() {
        val tema = obtenerTemaLinea(null)
        assertEquals(TipoLinea.NINGUNA, tema.tipo)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), tema.fondoClaro)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF0F0F12), tema.fondoOscuro)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF111114), tema.colorPrimario)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFF3F3F5), tema.chipFondoClaro)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF111114), tema.colorTextoChip)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), tema.colorTextoChipOscuro)
    }
}
