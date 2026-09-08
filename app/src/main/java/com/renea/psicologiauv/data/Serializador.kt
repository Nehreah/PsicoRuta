package com.renea.psicologiauv.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.renea.psicologiauv.model.Estudiante
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class Serializador(private val context: Context) {

    private val jsonEstudiante = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private val filesDir get() = context.filesDir

    private val preferencias =
        context.getSharedPreferences("psicologia_uv_prefs", Context.MODE_PRIVATE)

    /**
     * Indica si esta es la primera vez que se abre la app en este dispositivo
     * (o desde que se borraron los datos de la app). La primera llamada
     * devuelve true y deja registrado que la app ya se inició, de modo que
     * cualquier llamada posterior (incluso en futuras aperturas de la app)
     * devuelva false.
     */
    fun esPrimeraEjecucion(): Boolean {
        val esPrimeraVez = preferencias.getBoolean(CLAVE_PRIMERA_VEZ, true)
        if (esPrimeraVez) {
            preferencias.edit().putBoolean(CLAVE_PRIMERA_VEZ, false).apply()
        }
        return esPrimeraVez
    }

    companion object {
        private const val CLAVE_PRIMERA_VEZ = "primera_vez"
    }

    private fun copiarAInterno( destino: File) {
        try {
            context.assets.open("data/estudiante.json").use { input ->
                destino.parentFile?.mkdirs()
                destino.outputStream().use { output -> input.copyTo(output) }
            }
        } catch (e: IOException) {
            // No existe ese programa empaquetado en assets; cargarPrograma() devolverá null.
        }
    }

    fun cargarEstudiante(): Estudiante? {
        val rutaGuardado = File(filesDir, "estudiante.json")
        if(!rutaGuardado.exists()) {
            copiarAInterno(rutaGuardado)
        }
        if(!rutaGuardado.exists()) return null
        val estudianteTexto = rutaGuardado.readText(Charsets.UTF_8)
        return jsonEstudiante.decodeFromString(Estudiante.serializer(), estudianteTexto)
    }

    /**
     * Lee y parsea un [Estudiante] desde un archivo JSON empaquetado en
     * los assets de la app (carpeta "data/"). Se usa para cargar los
     * estudiantes de prueba "especiales" (ver [ControlV.ESTUDIANTES_ESPECIALES]).
     * Devuelve null si el archivo no existe o no se pudo leer.
     */
    fun cargarEstudianteDesdeAsset(nombreArchivo: String): Estudiante? {
        return try {
            context.assets.open("data/$nombreArchivo").use { entrada ->
                val texto = entrada.readBytes().toString(Charsets.UTF_8)
                jsonEstudiante.decodeFromString(Estudiante.serializer(), texto)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun guardarEstudiante(estudiante: Estudiante) {
        val estudianteTexto = jsonEstudiante.encodeToString(Estudiante.serializer(), estudiante)
        File(filesDir, "estudiante.json").writeText(estudianteTexto, Charsets.UTF_8)
    }

    /**
     * Elimina permanentemente el archivo guardado del estudiante actual
     * (estudiante.json). Después de llamar a esta función, la siguiente
     * vez que se llame a [cargarEstudiante] se volverá a copiar desde
     * cero la plantilla del pensum empaquetada en los assets de la app,
     * es decir, se pierde todo el progreso guardado del estudiante.
     */
    fun eliminarEstudiante() {
        val archivoGuardado = File(filesDir, "estudiante.json")
        if (archivoGuardado.exists()) {
            archivoGuardado.delete()
        }
    }


    /**
     * Exporta el [estudiante] como archivo JSON dentro de la carpeta pública
     * de Descargas del dispositivo, con nombre "estudiante_<codigo>.json".
     *
     * En Android 10 (API 29) en adelante usa MediaStore, que es la forma
     * recomendada de escribir en Descargas sin pedir permisos de almacenamiento.
     * En versiones anteriores escribe directamente el archivo, para lo cual se
     * necesita el permiso WRITE_EXTERNAL_STORAGE (declarado en el Manifest con
     * maxSdkVersion 28, ya que en versiones más nuevas no se usa ni se pide).
     *
     * Devuelve la Uri del archivo creado, o null si no se pudo escribir.
     */
    fun exportarEstudianteADescargas(estudiante: Estudiante): Uri? {
        val nombreArchivo = "estudiante_${estudiante.codigo}.json"
        val contenidoJson = jsonEstudiante.encodeToString(Estudiante.serializer(), estudiante)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val valores = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            try {
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                    ?: return null
                resolver.openOutputStream(uri)?.use { salida ->
                    salida.write(contenidoJson.toByteArray(Charsets.UTF_8))
                }
                uri
            } catch (e: Exception) {
                null
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val carpetaDescargas =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                carpetaDescargas.mkdirs()
                val archivo = File(carpetaDescargas, nombreArchivo)
                archivo.writeText(contenidoJson, Charsets.UTF_8)
                Uri.fromFile(archivo)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Uri "de partida" que le sugiere al selector de archivos abrir directamente
     * en la carpeta Descargas al importar un JSON (Intent.EXTRA_INITIAL_URI, API 26+).
     * En dispositivos más antiguos devuelve null y el selector abre en su carpeta
     * habitual.
     */
    fun uriCarpetaDescargas(): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            null
        }

    /**
     * Lee y parsea un [Estudiante] desde un archivo JSON ubicado en [uri]
     * (por ejemplo, uno elegido por el usuario en el selector de archivos al
     * importar desde Descargas). Devuelve null si el archivo no se pudo leer
     * o no tiene el formato esperado.
     */
    fun importarEstudianteDesdeUri(uri: Uri): Estudiante? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                val texto = entrada.readBytes().toString(Charsets.UTF_8)
                jsonEstudiante.decodeFromString(Estudiante.serializer(), texto)
            }
        } catch (e: Exception) {
            null
        }
    }


}