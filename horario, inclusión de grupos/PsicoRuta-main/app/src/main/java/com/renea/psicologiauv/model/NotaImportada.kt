package com.renea.psicologiauv.model

/**
 * Resultado de leer la "nota" de una materia en el tabulado (PDF) de la
 * universidad.
 *
 * En el tabulado, la columna CAL no siempre tiene un número: algunas
 * materias muestran una marca especial en su lugar (por ejemplo "C.U",
 * "E.X" o "A.P"). Esas materias SÍ cuentan como aprobadas para efectos de
 * créditos, porcentajes de avance y requisitos, pero al no tener una nota
 * numérica real, NO deben entrar en el cálculo del promedio ponderado (ni
 * de semestre ni de carrera): promediarlas con un número inventado
 * distorsionaría el promedio real del estudiante.
 */
sealed class NotaImportada {

    /** La materia tiene una nota numérica normal (0.0 a 5.0). */
    data class Numerica(val valor: Float) : NotaImportada()

    /**
     * La materia no tiene nota numérica, sino una marca especial del
     * tabulado (p. ej. "C.U", "E.X", "A.P") que indica que fue aprobada
     * por una vía distinta a la calificación numérica.
     */
    data class Especial(val marca: String) : NotaImportada()
}