package com.renea.psicologiauv.model

import kotlinx.serialization.Serializable

@Serializable
data class Asignatura (
    val nombre: String,
    val codigo: String,
    var semestre: Int,
    val creditos: Int,
    var lineaProfesional: String,
    val componente: String,
    val area: String,
    var nota: Float = 0f,
    var notaEspecial: String? = null,
    var complementaria: Boolean = false
    /**
     * Marca especial del tabulado cuando la materia no tiene nota numérica
     * (por ejemplo "C.U", "E.X", "A.P"). Si es distinta de null, la materia
     * se considera aprobada (ver [aprobo]) aunque [nota] sea 0, pero NO debe
     * incluirse en el cálculo de promedios: eso lo controla ProgramaM
     * filtrando explícitamente por este campo.
     */
)  {
    fun cambiarNota(nuevaNota: Float): Asignatura =
        when {
            // Ingresar 0 reinicia la materia: borra la nota numérica y
            // cualquier marca especial (C.U, E.X, A.P), dejándola como si
            // no se hubiera cursado.
            nuevaNota == 0f -> copy(nota = 0f, notaEspecial = null)
            nuevaNota > 0f && nuevaNota <= 5f -> copy(nota = nuevaNota, notaEspecial = null)
            else -> this
        }
    fun cambiarComplementaria(nuevaNota: Float): Asignatura =
        when {
            // Ingresar 0 reinicia la materia: borra la nota numérica y
            // cualquier marca especial (C.U, E.X, A.P), dejándola como si
            // no se hubiera cursado.
            nuevaNota == 0f -> copy(nota = 0f, notaEspecial = null)
            nuevaNota > 0f && nuevaNota <= 5f -> copy(nota = nuevaNota, notaEspecial = null)
            else -> this
        }


    /**
     * Registra que la materia fue aprobada mediante una marca especial del
     * tabulado (sin nota numérica), por ejemplo "C.U", "E.X" o "A.P".
     * Deja [nota] en 0 porque no existe una calificación numérica real.
     */
    fun cambiarNotaEspecial(marca: String): Asignatura =
        if (marca.isNotBlank()) copy(nota = 0f, notaEspecial = marca)
        else
            this
    fun cambiarSemestre(nuevoSemestre: Int): Asignatura =
        if((nuevoSemestre in 0..15)) copy(semestre = nuevoSemestre)
        else
            this

    fun cambiarNombre(nuevoNombre: String): Asignatura =
        if(componente == "Electiva complementaria" || componente == "Electiva profesional" ) copy(nombre = nuevoNombre)
        else
            this

    fun cambiarCodigo(nuevoCodigo: String): Asignatura =
        if(componente == "Electiva complementaria" || componente == "Electiva profesional" || componente == "Idioma" ) copy(codigo = nuevoCodigo)
        else
            this


    /**
     * Aprobada si tiene una nota numérica suficiente, O si fue aprobada por
     * una marca especial del tabulado (C.U, E.X, A.P) que no trae nota
     * numérica. Esto es lo que se debe usar para créditos, porcentajes de
     * avance y requisitos. Para promedios (que sí necesitan un número real),
     * usar además `notaEspecial == null` — ver ProgramaM.
     */
    fun aprobo(notaMinima: Float = 2.9f): Boolean = (nota > notaMinima) || (notaEspecial != null)

    /**
     * Texto para mostrar en la interfaz: la nota numérica formateada, la
     * marca especial si no hay nota numérica, o "-" si no hay ninguna de
     * las dos.
     */
    fun textoNota(): String = when {
        nota > 0f -> "%.1f".format(nota)
        notaEspecial != null -> notaEspecial!!
        else -> "-"
    }
}

