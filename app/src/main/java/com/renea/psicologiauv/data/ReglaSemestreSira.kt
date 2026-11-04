package com.renea.psicologiauv.data

import com.renea.psicologiauv.model.NotaImportada

/**
 * Traslada a los códigos del PENSUM los semestres SIRA leídos por
 * [AnalizadorNotas.extraerSemestresSiraPorCodigo], aplicando las mismas
 * equivalencias de código que ya usan las notas:
 *
 * - [ReglaEspanol]: si Español (204001M) está aprobado, conserva su semestre
 *   bajo 204001M (el código que toma la materia en el pensum). La Cultura Escrita
 *   real del SIRA conserva su propio semestre bajo 402180M.
 * - [ReglaDeporteFormativo]: el espacio [ReglaDeporteFormativo.CODIGO_DESTINO]
 *   toma el semestre del alias cuya nota se eligió (la misma comparación que
 *   usa esa regla). Si el SIRA ya trae el código destino, no se toca.
 *
 * Los alias de deporte y el código de Español no aprobado se eliminan del
 * resultado.
 */
object ReglaSemestreSira {

    fun aplicar(
        semestres: Map<String, Int>,
        notasCrudas: Map<String, NotaImportada>,
        codigosAliasDeporte: List<String> = ReglaDeporteFormativo.CODIGOS_ALIAS
    ): Map<String, Int> {
        val resultado = LinkedHashMap(semestres)

        // Español: Si está aprobado, conserva su semestre bajo 204001M.
        // Si no está aprobado, se elimina 204001M para que no altere el pensum.
        val espanolAprobado = ReglaEspanol.aprobada(notasCrudas[ReglaEspanol.CODIGO_ESPANOL_SIRA])
        if (!espanolAprobado) {
            resultado.remove(ReglaEspanol.CODIGO_ESPANOL_SIRA)
        }

        // Alias de Deporte Formativo -> espacio del pensum.
        val alias = codigosAliasDeporte.ifEmpty { ReglaDeporteFormativo.CODIGOS_ALIAS }
        val destinoYaReportado = notasCrudas.containsKey(ReglaDeporteFormativo.CODIGO_DESTINO)
        if (!destinoYaReportado) {
            val mejorAlias = alias
                .mapNotNull { codigo ->
                    val nota = notasCrudas[codigo]
                        ?.takeIf { ReglaDeporteFormativo.aprobada(it) }
                    val semestre = semestres[codigo]
                    if (nota != null && semestre != null) Triple(codigo, nota, semestre) else null
                }
                .maxWithOrNull(compareBy {
                    when (val n = it.second) {
                        is NotaImportada.Numerica -> n.valor
                        is NotaImportada.Especial -> 3.0f
                    }
                })
            if (mejorAlias != null) {
                resultado[ReglaDeporteFormativo.CODIGO_DESTINO] = mejorAlias.third
            }
        }
        alias.forEach { resultado.remove(it) }

        return resultado
    }
}
