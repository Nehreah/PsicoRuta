package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable

/**
 * Representa una fila del catálogo de electivas profesionales
 * (archivo assets/data/ElectivasProfesionales.json).
 *
 * Solo se usan los campos necesarios para construir una [Asignatura]
 * cuando el importador detecta ese código en el SIRA.
 */
@Serializable
data class ElectivaProfesionalCatalogo(
    val nombre: String,
    val codigo: String,
    val creditos: Int,
    val lineaProfesional: String = "No",
    val componente: String = "Electiva profesional",
    val area: String = "Formación Profesional en Campos de Aplicación"
)
