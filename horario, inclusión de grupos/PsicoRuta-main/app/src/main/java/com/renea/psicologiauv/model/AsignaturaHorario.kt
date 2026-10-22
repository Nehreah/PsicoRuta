package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable

/** Una sesión de clase obtenida del catálogo Horarios SIRA. */
@Serializable
data class AsignaturaHorario(
    val codigo: String = "",
    val nombreAsignatura: String = "",
    val grupo: String = "",
    val diaClase: String = "",
    val horasClase: String = "",
    val horaInicio: Int = 0,
    val horaFin: Int = 0,
    val espacio: String = ""
)
