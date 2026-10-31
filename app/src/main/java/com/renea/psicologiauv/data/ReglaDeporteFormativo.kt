package com.renea.psicologiauv.data

import com.renea.psicologiauv.model.NotaImportada

/**
 * Regla de equivalencia para Deporte Formativo.
 *
 * El SIRA puede registrar la materia con distintos códigos según el semestre y
 * la oferta. En el pensum solo existe un único espacio [CODIGO_DESTINO]. Si
 * alguno de los [CODIGOS_ALIAS] aparece aprobado en el PDF del SIRA, ese espacio
 * se marca como aprobado con la nota correspondiente.
 *
 * Si más de un alias viene aprobado, se usa el que tenga mayor valor numérico; si
 * todos son notas especiales ("EX", etc.) se usa el primero encontrado.
 */
object ReglaDeporteFormativo {

    /** Código del espacio en el pensum. */
    const val CODIGO_DESTINO = "404010C"

    /** Códigos con los que el SIRA puede registrar la misma materia. */
    val CODIGOS_ALIAS: List<String> = listOf("404032C", "404002C", "404001M", "404010M")

    /** Devuelve true si la nota representa una materia aprobada. */
    fun aprobada(nota: NotaImportada?): Boolean = when (nota) {
        is NotaImportada.Numerica -> nota.valor > 2.9f
        is NotaImportada.Especial -> true
        null -> false
    }

    /**
     * Aplica la regla al mapa de notas crudas proveniente del PDF.
     *
     * - Si algún alias está aprobado, inyecta [CODIGO_DESTINO] con la mejor nota
     *   y elimina todos los alias del mapa (para que no generen entradas desconocidas).
     * - Si ningún alias está aprobado, solo elimina los alias del mapa y deja el
     *   resto intacto.
     * - Si [CODIGO_DESTINO] ya venía en el mapa (el SIRA ya lo reportó directamente),
     *   no lo sobreescribe.
     *
     * @param notas Mapa codigo→NotaImportada tal como lo entrega [ReglaEspanol.aplicar].
     * @return Nuevo mapa con la regla aplicada.
     */
    fun aplicar(
        notas: Map<String, NotaImportada>,
        codigosAlias: List<String> = CODIGOS_ALIAS
    ): Map<String, NotaImportada> {
        val alias = codigosAlias.ifEmpty { CODIGOS_ALIAS }
        val aliasAprobados = alias.mapNotNull { codigo ->
            notas[codigo]?.takeIf { aprobada(it) }?.let { codigo to it }
        }

        // Eliminar siempre los alias del mapa resultado (ya los procesamos o
        // simplemente no corresponden a ningún espacio del pensum).
        val sinAlias = notas.filterKeys { it !in alias }

        if (aliasAprobados.isEmpty()) return sinAlias

        // Si el destino ya está en el mapa (el SIRA lo reportó directamente) no tocar.
        if (sinAlias.containsKey(CODIGO_DESTINO)) return sinAlias

        // Elegir la mejor nota: numérica más alta > nota especial.
        val mejorNota = aliasAprobados
            .maxWithOrNull(compareBy {
                when (val n = it.second) {
                    is NotaImportada.Numerica -> n.valor
                    is NotaImportada.Especial -> 3.0f // EX/etc. se trata como aprobado justo
                }
            })!!.second

        return LinkedHashMap(sinAlias).also { it[CODIGO_DESTINO] = mejorNota }
    }
}
