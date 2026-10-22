package com.renea.psicologiauv.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AtomicFile
import android.util.Log
import android.os.Environment
import android.provider.MediaStore
import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
import com.renea.psicologiauv.model.Estudiante
import kotlinx.serialization.builtins.ListSerializer
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
        private const val TAG = "PsicoRuta/Serializador"
    }

    private fun copiarAInterno(destino: File) {
        val archivoAtomico = AtomicFile(destino)
        var salida: java.io.FileOutputStream? = null
        try {
            destino.parentFile?.mkdirs()
            salida = archivoAtomico.startWrite()
            context.assets.open("data/estudiante.json").use { input ->
                input.copyTo(salida)
            }
            salida.fd.sync()
            archivoAtomico.finishWrite(salida)
        } catch (e: IOException) {
            if (salida != null) {
                try {
                    archivoAtomico.failWrite(salida)
                } catch (cleanupError: Exception) {
                    Log.w(TAG, "No se pudo limpiar una copia inicial incompleta.", cleanupError)
                }
            }
            Log.w(TAG, "No se pudo copiar la plantilla inicial del estudiante.", e)
        }
    }

    fun cargarEstudiante(): Estudiante? {
        val rutaGuardado = File(filesDir, "estudiante.json")
        if(!rutaGuardado.exists()) {
            copiarAInterno(rutaGuardado)
        }
        if(!rutaGuardado.exists()) return null
        return try {
            val archivoAtomico = AtomicFile(rutaGuardado)
            val estudianteTexto = archivoAtomico.openRead().bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            jsonEstudiante.decodeFromString(Estudiante.serializer(), estudianteTexto)
        } catch (e: Exception) {
            // No se elimina ni sobrescribe el archivo que falló: puede contener
            // datos recuperables. El llamador recibe null, como en otros métodos
            // de lectura, y el detalle técnico queda únicamente en Logcat.
            Log.e(TAG, "No se pudo leer el archivo local del estudiante.", e)
            null
        }
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
            Log.w(TAG, "No se pudo cargar el archivo de estudiante solicitado desde assets.", e)
            null
        }
    }

    /**
     * Guarda el perfil mediante escritura atómica. Si el proceso se interrumpe
     * durante la escritura, AtomicFile conserva el archivo anterior en lugar de
     * dejar estudiante.json parcialmente escrito.
     * Las excepciones de escritura se propagan para no ocultar un guardado fallido.
     */
    fun guardarEstudiante(estudiante: Estudiante) {
        val estudianteTexto = jsonEstudiante.encodeToString(Estudiante.serializer(), estudiante)
        val archivoAtomico = AtomicFile(File(filesDir, "estudiante.json"))
        var salida: java.io.FileOutputStream? = null
        try {
            salida = archivoAtomico.startWrite()
            salida.write(estudianteTexto.toByteArray(Charsets.UTF_8))
            salida.fd.sync()
            archivoAtomico.finishWrite(salida)
        } catch (e: Exception) {
            if (salida != null) {
                try {
                    archivoAtomico.failWrite(salida)
                } catch (cleanupError: Exception) {
                    Log.w(TAG, "No se pudo restaurar el archivo anterior tras un error de escritura.", cleanupError)
                }
            }
            Log.e(TAG, "No se pudo guardar el perfil del estudiante.", e)
            throw e
        }
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
        try {
            // AtomicFile también elimina los respaldos temporales que podría
            // dejar una escritura interrumpida; borrar solo el archivo base
            // podría permitir que un respaldo antiguo reapareciera después.
            AtomicFile(archivoGuardado).delete()
        } catch (e: Exception) {
            // Se mantiene el contrato anterior: la operación no propaga errores
            // de almacenamiento a la interfaz, pero deja diagnóstico en Logcat.
            Log.e(TAG, "No se pudo eliminar el archivo local del estudiante.", e)
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
            var uriCreada: Uri? = null
            try {
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                    ?: return null
                uriCreada = uri
                val salida = resolver.openOutputStream(uri)
                    ?: throw IOException("No se pudo abrir el archivo de destino.")
                salida.use { it.write(contenidoJson.toByteArray(Charsets.UTF_8)) }
                uri
            } catch (e: Exception) {
                // Evita dejar un archivo vacío o parcial si la exportación falla.
                uriCreada?.let { uriFallida ->
                    try {
                        resolver.delete(uriFallida, null, null)
                    } catch (cleanupError: Exception) {
                        Log.w(TAG, "No se pudo limpiar una exportación incompleta.", cleanupError)
                    }
                }
                Log.e(TAG, "No se pudo exportar el perfil a Descargas.", e)
                null
            }
        } else {
            var archivoCreado: File? = null
            try {
                @Suppress("DEPRECATION")
                val carpetaDescargas =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!carpetaDescargas.exists() && !carpetaDescargas.mkdirs()) {
                    throw IOException("No se pudo crear la carpeta Descargas.")
                }
                val archivo = File(carpetaDescargas, nombreArchivo)
                archivoCreado = archivo
                archivo.writeText(contenidoJson, Charsets.UTF_8)
                Uri.fromFile(archivo)
            } catch (e: Exception) {
                // Si la escritura falla, no dejar un JSON incompleto que el
                // usuario pueda confundir con una copia de seguridad válida.
                archivoCreado?.let { archivo ->
                    try {
                        if (archivo.exists() && !archivo.delete()) {
                            Log.w(TAG, "No se pudo eliminar la exportación incompleta.")
                        }
                    } catch (cleanupError: Exception) {
                        Log.w(TAG, "No se pudo limpiar una exportación incompleta.", cleanupError)
                    }
                }
                Log.e(TAG, "No se pudo exportar el perfil a Descargas.", e)
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
            val flujo = context.contentResolver.openInputStream(uri)
            if (flujo == null) {
                Log.w(TAG, "El proveedor de archivos no pudo abrir el JSON seleccionado.")
                return null
            }
            flujo.use { entrada ->
                val texto = entrada.readBytes().toString(Charsets.UTF_8)
                jsonEstudiante.decodeFromString(Estudiante.serializer(), texto)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo importar el archivo JSON seleccionado.", e)
            null
        }
    }



    /**
     * Lee el catalogo de electivas profesionales empaquetado en
     * assets/data/ElectivasProfesionales.json y lo devuelve como lista.
     * Devuelve lista vacia si el archivo no existe o no se pudo leer.
     * Esta lista se usa durante la importacion del PDF SIRA para rellenar
     * los slots de Electiva Profesional II/III/IV en orden de aparicion.
     */
    fun leerElectivasProfesionales(): List<ElectivaProfesionalCatalogo> {
        return try {
            context.assets.open("data/ElectivasProfesionales.json").use { entrada ->
                val texto = entrada.readBytes().toString(Charsets.UTF_8)
                jsonEstudiante.decodeFromString(
                    ListSerializer(ElectivaProfesionalCatalogo.serializer()),
                    texto
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo leer el catálogo de electivas profesionales.", e)
            emptyList()
        }
    }

}