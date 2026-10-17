package com.renea.psicologiauv

import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
import com.renea.psicologiauv.model.Estudiante
import kotlinx.serialization.json.Json
import java.io.File

/** Utilidades compartidas por las pruebas unitarias (JVM, sin Android). */

internal const val AREA_FDI = "Fundamentación Disciplinar e Interdisciplinar"
internal const val AREA_FPCA = "Formación Profesional en Campos de Aplicación"
internal const val AREA_FII = "Formación en investigación e Intervención"

internal fun crearAsignatura(
    codigo: String,
    nombre: String = "Materia $codigo",
    semestre: Int = 1,
    creditos: Int = 3,
    componente: String = "Obligatoria",
    area: String = AREA_FDI,
    lineaProfesional: String = "No",
    nota: Float = 0f,
    notaEspecial: String? = null,
    complementaria: Boolean = false
) = Asignatura(
    nombre = nombre,
    codigo = codigo,
    semestre = semestre,
    creditos = creditos,
    lineaProfesional = lineaProfesional,
    componente = componente,
    area = area,
    nota = nota,
    notaEspecial = notaEspecial,
    complementaria = complementaria
)

internal fun crearEstudiante(vararg materias: Asignatura) = Estudiante(
    nombre = "Estudiante de prueba",
    codigo = 123,
    asignaturas = materias.toMutableList()
)

/** Espacio de Electiva Profesional con código provisional ("3", "4" o "5"). */
internal fun crearEspacioElectivaProfesional(
    codigoProvisional: String,
    semestre: Int = 5,
    nota: Float = 0f
) = crearAsignatura(
    codigo = codigoProvisional,
    nombre = "Electiva profesional $codigoProvisional",
    semestre = semestre,
    componente = "Electiva profesional",
    area = AREA_FPCA,
    nota = nota
)

internal fun crearElectivaProfesionalI(nota: Float = 0f) = crearAsignatura(
    codigo = "402197M",
    nombre = "Electiva Profesional I",
    semestre = 5,
    componente = "Electiva profesional",
    area = AREA_FPCA,
    nota = nota
)

internal fun crearCatalogo(vararg codigos: String): List<ElectivaProfesionalCatalogo> =
    codigos.map {
        ElectivaProfesionalCatalogo(
            nombre = "Electiva real $it",
            codigo = it,
            creditos = 3
        )
    }

internal val jsonDePrueba = Json {
    ignoreUnknownKeys = true
}

/**
 * Lee un archivo de assets/data desde el disco. Las pruebas unitarias corren
 * con la carpeta del módulo `app` como directorio de trabajo, pero se prueba
 * también la ruta desde la raíz del repositorio por si se ejecutan de otra
 * forma.
 */
internal fun leerAssetDeDatos(nombre: String): String {
    val candidatos = listOf(
        File("src/main/assets/data/$nombre"),
        File("app/src/main/assets/data/$nombre")
    )
    val archivo = candidatos.firstOrNull { it.exists() }
        ?: error("No se encontró $nombre en: ${candidatos.map { it.absolutePath }}")
    return archivo.readText(Charsets.UTF_8)
}
