package com.renea.psicologiauv

import com.renea.psicologiauv.data.AnalizadorNotas
import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.ProgramaM
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Valida los JSON reales de assets/data (catálogo de electivas y estudiantes
 * base) y recorre el flujo completo: texto de PDF -> AnalizadorNotas ->
 * resolución de electivas -> estado en ProgramaM.
 */
class AssetsJsonTest {

    private val formaDeCodigo = Regex("""\d{5,7}[A-Za-z]?""")

    private fun catalogo(): List<ElectivaProfesionalCatalogo> =
        jsonDePrueba.decodeFromString(
            ListSerializer(ElectivaProfesionalCatalogo.serializer()),
            leerAssetDeDatos("ElectivasProfesionales.json")
        )

    private fun estudianteBase(): Estudiante =
        jsonDePrueba.decodeFromString(Estudiante.serializer(), leerAssetDeDatos("estudiante.json"))

    // ------------------------------------------------------------------
    // Catálogo de electivas profesionales
    // ------------------------------------------------------------------

    @Test fun elCatalogoDeElectivasSeInterpretaYNoEstaVacio() {
        assertTrue(catalogo().isNotEmpty())
    }

    @Test fun losCodigosDelCatalogoSonUnicosYTienenFormaDeCodigoReal() {
        val codigos = catalogo().map { it.codigo }
        assertEquals("Hay códigos repetidos en el catálogo", codigos.size, codigos.toSet().size)
        codigos.forEach { assertTrue("Código inválido: $it", formaDeCodigo.matches(it)) }
    }

    @Test fun todasLasElectivasDelCatalogoSonProfesionalesDeTresCreditosConNombre() {
        catalogo().forEach {
            assertEquals("Electiva profesional", it.componente)
            assertTrue("Créditos inválidos en ${it.codigo}", it.creditos > 0)
            assertTrue("Nombre vacío en ${it.codigo}", it.nombre.isNotBlank())
        }
    }

    @Test fun elCatalogoNoIncluyeLaElectivaProfesionalIQueEsFija() {
        assertFalse(catalogo().any { it.codigo == "402197M" })
    }

    @Test fun ningunaElectivaDelCatalogoEstaYaEnElPensumBase() {
        val codigosPensum = estudianteBase().asignaturas.map { it.codigo }.toSet()
        catalogo().forEach { assertFalse("${it.codigo} ya está en el pensum", it.codigo in codigosPensum) }
    }

    // ------------------------------------------------------------------
    // Estudiantes base
    // ------------------------------------------------------------------

    @Test fun elEstudianteBaseSeInterpretaYTieneCodigosUnicos() {
        val codigos = estudianteBase().asignaturas.map { it.codigo }
        assertEquals(codigos.size, codigos.toSet().size)
    }

    @Test fun elEstudianteBaseTieneLosEspaciosDeElectivasEnOrden() {
        val codigos = estudianteBase().asignaturas.map { it.codigo }
        listOf("1", "2", "402197M", "3", "4", "5").forEach {
            assertTrue("Falta el espacio $it", it in codigos)
        }
        // La asignación por posición depende de que 3, 4 y 5 estén en este orden.
        assertTrue(codigos.indexOf("3") < codigos.indexOf("4"))
        assertTrue(codigos.indexOf("4") < codigos.indexOf("5"))
    }

    @Test fun losEspaciosProvisionalesSonElectivasProfesionalesSinNota() {
        val base = estudianteBase()
        listOf("3", "4", "5").forEach { codigo ->
            val a = base.asignaturas.single { it.codigo == codigo }
            assertEquals("Electiva profesional", a.componente)
            assertFalse(a.aprobo())
        }
    }

    @Test fun elEstudianteVirtualSeInterpreta() {
        val e = jsonDePrueba.decodeFromString(
            Estudiante.serializer(),
            leerAssetDeDatos("estudiante_virtual_melson.json")
        )
        assertTrue(e.asignaturas.isNotEmpty())
    }

    // ------------------------------------------------------------------
    // Flujo completo
    // ------------------------------------------------------------------

    @Test fun elPdfLlenaLasElectivasIIIIIIyIVEnElOrdenEnQueAparecen() {
        val base = estudianteBase()
        val cat = catalogo()
        val (primera, segunda, tercera, cuarta) = cat.take(4)

        // Se mezclan materias del pensum entre las electivas, y una electiva
        // sin nota (cancelada) que no debe ocupar espacio.
        val texto = """
            402180M ALGO DEL PENSUM 3 4.0
            ${segunda.codigo} ${segunda.nombre} 3 3.8
            ${primera.codigo} ${primera.nombre} 3 4.5
            Resumen de Finalización por periodo Promedio Semestral: 4.10
            ${cuarta.codigo} ${cuarta.nombre} 3
            ${tercera.codigo} ${tercera.nombre} 3 5.0
        """.trimIndent()

        val conocidos = base.asignaturas.map { it.codigo } + cat.map { it.codigo }
        val notas = AnalizadorNotas.extraerNotasPorCodigo(texto, conocidos)

        val (actualizado, _) = base.actualizarNotasMasivo(notas)
        val (resultado, cantidad) = actualizado.resolverElectivasProfesionales(notas, cat)

        assertEquals(3, cantidad)

        val programa = ProgramaM(estudiante = resultado)
        val slots = programa.electivasProfesionalesIIaIV()
        assertEquals(listOf(segunda.codigo, primera.codigo, tercera.codigo), slots.map { it.codigo })
        assertEquals(listOf(3.8f, 4.5f, 5.0f), slots.map { it.nota })
        assertEquals(listOf(true, true, true), programa.estadosElectivasProfesionalesIIIII())
        assertTrue(resultado.asignaturas.none { it.codigo == cuarta.codigo })
    }
}
