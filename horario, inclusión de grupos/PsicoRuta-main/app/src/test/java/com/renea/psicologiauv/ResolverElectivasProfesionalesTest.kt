package com.renea.psicologiauv

import com.renea.psicologiauv.model.NotaImportada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de [com.renea.psicologiauv.model.Estudiante.resolverElectivasProfesionales]:
 * las electivas del PDF que están en el catálogo JSON ocupan, en orden de
 * aparición, Electiva Profesional II, III y IV.
 */
class ResolverElectivasProfesionalesTest {

    private val politica = "402161M"
    private val organizacional = "402163M"
    private val clinica = "402175M"
    private val violencia = "402165M"
    private val catalogo = crearCatalogo(politica, organizacional, clinica, violencia)

    private fun estudianteConEspacios() = crearEstudiante(
        crearAsignatura("402180M", nota = 4.0f),
        crearElectivaProfesionalI(nota = 4.2f),
        crearEspacioElectivaProfesional("3", semestre = 5),
        crearEspacioElectivaProfesional("4", semestre = 6),
        crearEspacioElectivaProfesional("5", semestre = 7)
    )

    @Test fun llenaIIIIIIyIVEnElOrdenDelPdf() {
        val notas = linkedMapOf<String, NotaImportada>(
            organizacional to NotaImportada.Numerica(4.0f),
            politica to NotaImportada.Numerica(3.5f),
            clinica to NotaImportada.Especial("A.P")
        )
        val (resultado, cantidad) = estudianteConEspacios().resolverElectivasProfesionales(notas, catalogo)

        assertEquals(3, cantidad)
        val a = resultado.asignaturas
        assertEquals(organizacional, a[2].codigo)
        assertEquals("Electiva real $organizacional", a[2].nombre)
        assertEquals(4.0f, a[2].nota, 0.001f)
        assertEquals(politica, a[3].codigo)
        assertEquals(3.5f, a[3].nota, 0.001f)
        assertEquals(clinica, a[4].codigo)
        assertEquals("A.P", a[4].notaEspecial)
    }

    @Test fun conservaSemestreCreditosComponenteYAreaDelEspacio() {
        val notas = linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f))
        val (resultado, _) = estudianteConEspacios().resolverElectivasProfesionales(notas, catalogo)
        val ocupada = resultado.asignaturas[2]
        assertEquals(5, ocupada.semestre)
        assertEquals(3, ocupada.creditos)
        assertEquals("Electiva profesional", ocupada.componente)
        assertEquals(AREA_FPCA, ocupada.area)
    }

    @Test fun conMasDeTresElectivasLasSobrantesSeIgnoran() {
        val notas = linkedMapOf<String, NotaImportada>(
            politica to NotaImportada.Numerica(4.0f),
            organizacional to NotaImportada.Numerica(4.0f),
            clinica to NotaImportada.Numerica(4.0f),
            violencia to NotaImportada.Numerica(4.0f)
        )
        val (resultado, cantidad) = estudianteConEspacios().resolverElectivasProfesionales(notas, catalogo)
        assertEquals(3, cantidad)
        assertEquals(5, resultado.asignaturas.size)
        assertTrue(resultado.asignaturas.none { it.codigo == violencia })
    }

    @Test fun conMenosDeTresElectivasLosEspaciosRestantesQuedanProvisionales() {
        val notas = linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f))
        val (resultado, cantidad) = estudianteConEspacios().resolverElectivasProfesionales(notas, catalogo)
        assertEquals(1, cantidad)
        assertEquals(listOf("402180M", "402197M", politica, "4", "5"), resultado.asignaturas.map { it.codigo })
    }

    @Test fun unaSegundaImportacionContinuaEnElSiguienteEspacioLibre() {
        val primera = estudianteConEspacios().resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            catalogo
        ).first
        // El PDF nuevo trae la misma electiva (ya asignada) y una nueva.
        val (segunda, cantidad) = primera.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(
                politica to NotaImportada.Numerica(4.0f),
                clinica to NotaImportada.Numerica(3.8f)
            ),
            catalogo
        )
        assertEquals(1, cantidad)
        assertEquals(5, segunda.asignaturas.size)
        assertEquals(listOf(politica, clinica, "5"), segunda.asignaturas.drop(2).map { it.codigo })
    }

    @Test fun noDuplicaUnaElectivaQueYaEstaEnElPensum() {
        val inicial = crearEstudiante(
            crearAsignatura(politica, componente = "Electiva profesional", area = AREA_FPCA, nota = 4.0f),
            crearEspacioElectivaProfesional("4")
        )
        val (resultado, cantidad) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            catalogo
        )
        assertEquals(0, cantidad)
        assertEquals(2, resultado.asignaturas.size)
    }

    @Test fun respetaUnEspacioProvisionalYaAprobadoManualmente() {
        val inicial = crearEstudiante(
            crearEspacioElectivaProfesional("3", nota = 4.5f),
            crearEspacioElectivaProfesional("4"),
            crearEspacioElectivaProfesional("5")
        )
        val (resultado, cantidad) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            catalogo
        )
        assertEquals(1, cantidad)
        assertEquals("3", resultado.asignaturas[0].codigo)
        assertEquals(politica, resultado.asignaturas[1].codigo)
    }

    @Test fun ignoraCodigosQueNoEstanEnElCatalogo() {
        val inicial = estudianteConEspacios()
        val (resultado, cantidad) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>("402999M" to NotaImportada.Numerica(5.0f)),
            catalogo
        )
        assertEquals(0, cantidad)
        assertSame(inicial, resultado)
    }

    @Test fun nuncaTocaLaElectivaProfesionalI() {
        val inicial = estudianteConEspacios()
        val (resultado, _) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(
                "402197M" to NotaImportada.Numerica(1.0f),
                politica to NotaImportada.Numerica(4.0f)
            ),
            catalogo
        )
        val primeraI = resultado.asignaturas.first { it.codigo == "402197M" }
        assertEquals(4.2f, primeraI.nota, 0.001f)
    }

    @Test fun catalogoVacioNoHaceNada() {
        val inicial = estudianteConEspacios()
        val (resultado, cantidad) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            emptyList()
        )
        assertEquals(0, cantidad)
        assertSame(inicial, resultado)
    }

    @Test fun sinEspaciosLibresNoHaceNada() {
        val inicial = crearEstudiante(crearAsignatura("402180M"))
        val (resultado, cantidad) = inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            catalogo
        )
        assertEquals(0, cantidad)
        assertEquals(1, resultado.asignaturas.size)
    }

    @Test fun noModificaLaInstanciaOriginal() {
        val inicial = estudianteConEspacios()
        inicial.resolverElectivasProfesionales(
            linkedMapOf<String, NotaImportada>(politica to NotaImportada.Numerica(4.0f)),
            catalogo
        )
        assertEquals("3", inicial.asignaturas[2].codigo)
    }
}
