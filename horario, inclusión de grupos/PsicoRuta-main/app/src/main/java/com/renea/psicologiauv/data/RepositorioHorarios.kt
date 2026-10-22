package com.renea.psicologiauv.data

import android.content.Context
import android.util.Log
import com.renea.psicologiauv.model.AsignaturaHorario
import com.renea.psicologiauv.model.Estudiante
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RegistroHorarioSira(
    val Código: String = "",
    val Asignatura: String = "",
    val Grupo: String = "",
    val Día: String = "",
    val Hora: String = "",
    val Espacio: String = ""
)

object RepositorioHorarios {
    private val json = Json { ignoreUnknownKeys = true }

    fun cargarCatalogo(context: Context): List<AsignaturaHorario> = try {
        val texto = context.assets.open("data/Horarios SIRA.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        json.decodeFromString<List<RegistroHorarioSira>>(texto).mapNotNull { registro ->
            val rango = registro.Hora.replace('–', '-').replace('—', '-').replace('−', '-')
                .split('-').map { it.trim() }
            val inicio = rango.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val fin = rango.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            AsignaturaHorario(
                codigo = registro.Código.trim(),
                nombreAsignatura = registro.Asignatura.trim(),
                grupo = registro.Grupo.trim(),
                diaClase = normalizarDia(registro.Día),
                horasClase = registro.Hora.trim(),
                horaInicio = inicio,
                horaFin = fin,
                espacio = registro.Espacio.trim().ifBlank { "Sin espacio" }
            )
        }
    } catch (e: Exception) {
        Log.e("RepositorioHorarios", "No se pudo cargar Horarios SIRA.json", e)
        emptyList()
    }

    /** Devuelve las sesiones del catálogo cuyos códigos pertenecen al semestre actual. */
    fun horarioDelSemestre(estudiante: Estudiante, semestreActual: Int, catalogo: List<AsignaturaHorario>): List<AsignaturaHorario> {
        val matriculadas = estudiante.asignaturas
            .filter { it.semestre == semestreActual && !it.aprobo() && it.grupo.isNotBlank() }
        if (matriculadas.isEmpty()) return emptyList()

        val gruposPorCodigo = matriculadas.associate {
            it.codigo.trim().uppercase() to it.grupo.trim().uppercase().removePrefix("GR").trim()
        }
        return catalogo.filter { clase ->
            val codigo = clase.codigo.trim().uppercase()
            val grupoMatriculado = gruposPorCodigo[codigo] ?: return@filter false
            clase.grupo.trim().uppercase().removePrefix("GR").trim() == grupoMatriculado
        }.sortedWith(compareBy<AsignaturaHorario> { ordenDia(it.diaClase) }.thenBy { it.horaInicio }.thenBy { it.nombreAsignatura })
    }

    private fun normalizarDia(dia: String): String = when (dia.trim().uppercase()) {
        "LUN", "LUNES" -> "LUN"
        "MAR", "MARTES" -> "MAR"
        "MIE", "MIÉ", "MIERCOLES", "MIÉRCOLES" -> "MIE"
        "JUE", "JUEVES" -> "JUE"
        "VIE", "VIERNES" -> "VIE"
        "SAB", "SÁB", "SABADO", "SÁBADO" -> "SAB"
        "DOM", "DOMINGO" -> "DOM"
        else -> dia.trim().uppercase()
    }

    private fun ordenDia(dia: String): Int = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM").indexOf(dia).let { if (it < 0) 99 else it }
}
