package com.renea.psicologiauv.data

import android.content.Context
import com.renea.psicologiauv.model.Tarea
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Persistencia local aislada de la interfaz para mantener responsabilidades separadas. */
object RepositorioTareas {
    private const val PREFS = "psicoruta_tareas"
    private const val KEY = "tareas_v1"
    private val json = Json { ignoreUnknownKeys = true }

    @Synchronized
    fun obtener(context: Context): List<Tarea> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        json.decodeFromString(ListSerializer(Tarea.serializer()), raw)
    }.getOrDefault(emptyList())

    @Synchronized
    fun guardar(context: Context, tareas: List<Tarea>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, json.encodeToString(ListSerializer(Tarea.serializer()), tareas))
            .apply()
    }
}
