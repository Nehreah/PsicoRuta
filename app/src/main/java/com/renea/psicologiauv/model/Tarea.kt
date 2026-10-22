package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable

@Serializable
data class Tarea(
    val id: String,
    val codigoAsignatura: String,
    val nombreAsignatura: String,
    val nombre: String,
    val descripcion: String,
    /** ISO-8601: yyyy-MM-dd */
    val fecha: String,
    val pdfUri: String = "",
    val companero: String = ""
)
