package com.renea.psicologiauv

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.renea.psicologiauv.data.Serializador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas en dispositivo/emulador: leen los assets reales empaquetados en la
 * app a través de [Serializador], que es lo que usa el importador de notas.
 */
@RunWith(AndroidJUnit4::class)
class ImportacionInstrumentedTest {

    private val serializador
        get() = Serializador(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test fun elCatalogoDeElectivasProfesionalesSeLeeDesdeAssets() {
        val catalogo = serializador.leerElectivasProfesionales()
        assertTrue("El catálogo no debería estar vacío", catalogo.isNotEmpty())
        assertEquals(catalogo.size, catalogo.map { it.codigo }.toSet().size)
        assertTrue(catalogo.all { it.componente == "Electiva profesional" })
    }

    @Test fun elEstudianteBaseSeCargaDesdeAssetsConLosEspaciosDeElectivas() {
        val estudiante = serializador.cargarEstudianteDesdeAsset("estudiante.json")
        assertNotNull(estudiante)
        val codigos = estudiante!!.asignaturas.map { it.codigo }
        assertTrue(listOf("3", "4", "5").all { it in codigos })
    }

    @Test fun unAssetInexistenteDevuelveNullSinLanzarExcepcion() {
        assertEquals(null, serializador.cargarEstudianteDesdeAsset("no_existe.json"))
    }
}
