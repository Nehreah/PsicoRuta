package com.renea.psicologiauv

import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
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
}
