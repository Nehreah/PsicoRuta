package com.renea.psicologiauv.data

import com.renea.psicologiauv.model.AsignaturaNoSincronizada
import com.renea.psicologiauv.model.NotaImportada

/**
 * Regla de equivalencia entre Español (SIRA) y Cultura Escrita y Pensamiento
 * Disciplinar (pensum):
 *
 * - Si el estudiante tiene Español (204001M) aprobado de cualquier forma
 *   (eximido "EX", otra marca especial o nota numérica), la asignatura original
 *   del pensum pasa a llamarse "Español" con código 204001M y toma esa nota.
 * - Si además el SIRA trae Cultura Escrita (402180M) aprobada, esa materia
 *   queda libre como "flotante" con su código real 402180M (sin sufijo -CE): se
 *   ofrece al usuario para usarla como electiva complementaria o descartarla
 *   (ver [AsignaturaNoSincronizada]).
 * - Si Español no está aprobado, no cambia nada: Cultura Escrita se sincroniza
 *   con 402180M como siempre.
 */
object ReglaEspanol {

    const val CODIGO_ESPANOL_SIRA = "204001M"
    const val CODIGO_CULTURA_ESCRITA = "402180M"

    /** Código propio de la Cultura Escrita flotante al asignarla a una complementaria. */
    const val CODIGO_CULTURA_FLOTANTE = "402180M"

    const val NOMBRE_ESPANOL = "Español"
    const val NOMBRE_CULTURA_FLOTANTE = "Cultura Escrita"
    const val CREDITOS_CULTURA_ESCRITA = 3

    class Resultado(
        val notas: Map<String, NotaImportada>,
        val culturaFlotante: AsignaturaNoSincronizada?
    )

    fun aprobada(nota: NotaImportada?): Boolean = when (nota) {
        is NotaImportada.Numerica -> nota.valor > 2.9f
        is NotaImportada.Especial -> true
        null -> false
    }

    /** Devuelve las notas a aplicar al pensum y la Cultura Escrita flotante, si aplica. */
    fun aplicar(notas: Map<String, NotaImportada>): Resultado {
        val espanol = notas[CODIGO_ESPANOL_SIRA]
        val sinEspanol = notas.filterKeys { it != CODIGO_ESPANOL_SIRA }
        if (!aprobada(espanol)) return Resultado(sinEspanol, null)

        val cultura = notas[CODIGO_CULTURA_ESCRITA]
        val flotante = if (aprobada(cultura)) {
            AsignaturaNoSincronizada(
                codigo = CODIGO_CULTURA_FLOTANTE,
                nombre = NOMBRE_CULTURA_FLOTANTE,
                nota = cultura,
                creditos = CREDITOS_CULTURA_ESCRITA
            )
        } else null

        val aplicadas = LinkedHashMap(notas)
        return Resultado(aplicadas, flotante)
    }
}
