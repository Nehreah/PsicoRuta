package com.renea.psicologiauv

import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.AsignaturaHorario
import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.Tarea
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifica que el formato JSON del modelo pueda serializarse y restaurarse. */
class SerializacionModeloTest {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test fun estudianteConservaSusDatosEnUnCicloJson() {
        val original = Estudiante(
            nombre = "Estudiante de prueba",
            codigo = 12345,
            linea = "Social",
            avatarUri = "content://avatar/prueba",
            asignaturas = mutableListOf(
                Asignatura(
                    nombre = "Materia de prueba",
                    codigo = "402180M",
                    semestre = 2,
                    creditos = 3,
                    lineaProfesional = "Social",
                    componente = "Obligatoria",
                    area = "Fundamentación",
                    nota = 4.2f,
                    notaEspecial = null,
                    complementaria = false
                )
            )
        )

        val serializado = json.encodeToString(Estudiante.serializer(), original)
        val restaurado = json.decodeFromString(Estudiante.serializer(), serializado)

        assertEquals(original, restaurado)
        assertEquals(original.asignaturas, restaurado.asignaturas)
    }

    @Test fun jsonConCamposAdicionalesSigueSiendoCompatible() {
        val original = Estudiante(
            nombre = "Prueba",
            codigo = 10,
            asignaturas = mutableListOf()
        )
        val jsonConCampoFuturo = json.encodeToString(Estudiante.serializer(), original)
            .dropLast(1) + ",\"campoDeUnaVersionFutura\":true}"

        val restaurado = json.decodeFromString(Estudiante.serializer(), jsonConCampoFuturo)
        assertEquals(original, restaurado)
    }

    @Test fun estudianteConHorarioYSemestresManualesConservaTodo() {
        val original = Estudiante(
            nombre = "Estudiante completo",
            codigo = 67890,
            linea = "Clínica",
            asignaturas = mutableListOf(),
            horario = mutableListOf(
                AsignaturaHorario(
                    codigo = "402180M",
                    nombreAsignatura = "Teorías",
                    grupo = "01",
                    diaClase = "LUN",
                    horasClase = "07-09",
                    horaInicio = 7,
                    horaFin = 9,
                    espacio = "D10-101"
                )
            ),
            semestresManuales = mapOf(1 to true, 2 to false)
        )

        val serializado = json.encodeToString(Estudiante.serializer(), original)
        val restaurado = json.decodeFromString(Estudiante.serializer(), serializado)

        assertEquals(original, restaurado)
        assertEquals(original.horario, restaurado.horario)
        assertEquals(original.semestresManuales, restaurado.semestresManuales)
    }

    @Test fun tareaConservaSusDatosEnUnCicloJson() {
        val tarea = Tarea(
            id = "t1",
            codigoAsignatura = "402180M",
            nombreAsignatura = "Teorías",
            nombre = "Ensayo final",
            descripcion = "Leer capítulo 4 y resumir",
            fecha = "2026-11-15",
            pdfUri = "content://pdf/tarea1",
            companero = "Juan"
        )
        val serializado = json.encodeToString(Tarea.serializer(), tarea)
        val restaurado = json.decodeFromString(Tarea.serializer(), serializado)
        assertEquals(tarea, restaurado)
    }

    @Test fun asignaturaHorarioConservaSusDatosEnUnCicloJson() {
        val horario = AsignaturaHorario(
            codigo = "402181M",
            nombreAsignatura = "Procesos Psicológicos",
            grupo = "02",
            diaClase = "MIE",
            horasClase = "14-17",
            horaInicio = 14,
            horaFin = 17,
            espacio = "Edificio 385"
        )
        val serializado = json.encodeToString(AsignaturaHorario.serializer(), horario)
        val restaurado = json.decodeFromString(AsignaturaHorario.serializer(), serializado)
        assertEquals(horario, restaurado)
    }

    @Test fun electivaProfesionalCatalogoConservaSusDatosEnUnCicloJson() {
        val electiva = ElectivaProfesionalCatalogo(
            nombre = "Psicología Forense",
            codigo = "402200M",
            creditos = 3,
            lineaProfesional = "Social",
            componente = "Electiva profesional",
            area = "Formación Profesional en Campos de Aplicación"
        )
        val serializado = json.encodeToString(ElectivaProfesionalCatalogo.serializer(), electiva)
        val restaurado = json.decodeFromString(ElectivaProfesionalCatalogo.serializer(), serializado)
        assertEquals(electiva, restaurado)
    }
}
