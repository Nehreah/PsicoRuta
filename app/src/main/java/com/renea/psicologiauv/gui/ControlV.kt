package com.renea.psicologiauv.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.Estudiante
import com.renea.psicologiauv.model.NotaImportada
import com.renea.psicologiauv.model.ProgramaM



class ControlV (private val parse: Serializador): ViewModel() {


    var programa by mutableStateOf(ProgramaM())
        private set

    /**
     * Nombres "especiales" que, al escribirlos en Usuario > Nombre y
     * darle a "Guardar datos personales", cargan automáticamente un
     * estudiante de prueba desde app/src/main/assets/data/, en vez de
     * solo cambiar el nombre. Sirve para probar rápido distintos
     * escenarios (todo en 1, todo en 4.9 y graduable, todo en 5 y
     * graduable). Para agregar otro nombre especial: añade una entrada
     * aquí (nombre en minúsculas -> nombre del archivo) y pon el .json
     * correspondiente en esa misma carpeta de assets.
     */
    private val ESTUDIANTES_ESPECIALES: Map<String, String> = mapOf(
        "melson nolima" to "estudiante_melson_nolima.json",
        "pedro rodriguez" to "estudiante_pedro_rodriguez.json",
        "luke skywalker" to "estudiante_luke_skywalker.json"
    )

    /**
     * True solo la primera vez que se abre la app en el dispositivo.
     * Se calcula una única vez al crear el ViewModel (sobrevive rotaciones
     * y cambios de configuración, pero no aperturas nuevas de la app).
     */
    val primeraVezQueAbreLaApp: Boolean = parse.esPrimeraEjecucion()
    private fun cargar() {
        val estudianteGuardado = parse.cargarEstudiante()
        if(estudianteGuardado != null){
            val sincronizado = estudianteGuardado.sincronizarComplementarias()
            val nuevoPrograma = programa.cambiarEstudiante(sincronizado)
            programa = nuevoPrograma
        }
    }
    init {
        cargar()
    }



    fun cambiarNombreEstudiante(nuevoNombre: String) {

        /*
         * Nombres "especiales": si el nombre coincide con alguno de estos
         * (sin importar mayúsculas/minúsculas ni espacios de sobra), en
         * vez de solo cambiar el nombre se carga un estudiante de prueba
         * completo desde assets/data/, pensado para probar rápidamente
         * distintos escenarios.
         */
        val archivoEspecial =
            ESTUDIANTES_ESPECIALES[nuevoNombre.trim().lowercase()]

        if (archivoEspecial != null) {
            val estudianteEspecial = parse.cargarEstudianteDesdeAsset(archivoEspecial)
            if (estudianteEspecial != null) {
                val sincronizado = estudianteEspecial.sincronizarComplementarias()
                programa = programa.cambiarEstudiante(sincronizado)
                parse.guardarEstudiante(sincronizado)
                return
            }
        }

        val nuevoEstudiante = programa.estudiante?.cambiarNombre(nuevoNombre)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarCodigoEstudiante(nuevoCodigo: Int) {
        val nuevoEstudiante = programa.estudiante?.cambiarCodigo(nuevoCodigo)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarLineaEstudiante(nuevaLinea: String) {
        val nuevoEstudiante = programa.estudiante?.cambiarLinea(nuevaLinea)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarNombreAsignatura(codigoAsignatura: String, nuevoNombre: String) {
        val nuevoEstudiante = programa.estudiante?.cambiarNombreAsignatura(codigoAsignatura, nuevoNombre)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarCodigoAsignatura(codigoAsignatura: String, nuevoCodigo: String) {
        val nuevoEstudiante = programa.estudiante?.cambiarCodigoAsignatura(codigoAsignatura, nuevoCodigo)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarSemestreAsignatura(codigoAsignatura: String, nuevoSemestre: Int) {
        val nuevoEstudiante = programa.estudiante?.cambiarSemestreAsignatura(codigoAsignatura, nuevoSemestre)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarNotaAsignatura(codigoAsignatura: String, nuevaNota: Float) {
        val nuevoEstudiante = programa.estudiante?.cambiarNotaAsignatura(codigoAsignatura, nuevaNota)?.sincronizarComplementarias()
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    fun cambiarNotaEspecialAsignatura(codigoAsignatura: String, marca: String) {
        val nuevoEstudiante = programa.estudiante?.cambiarNotaEspecialAsignatura(codigoAsignatura, marca)?.sincronizarComplementarias()
        if (nuevoEstudiante != null) {
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    class Factory(private val parse: Serializador): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ControlV(parse) as T
        }
    }
    fun cambiarAvatarEstudiante(nuevaRuta: String) {
        val nuevoEstudiante = programa.estudiante?.cambiarAvatar(nuevaRuta)
        if(nuevoEstudiante != null){
            val nuevoPrograma = programa.cambiarEstudiante(nuevoEstudiante)
            programa = nuevoPrograma
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }
    /**
     * Aplica de una sola vez las notas leídas desde el PDF (código ->
     * NotaImportada, numérica o especial como "C.U"/"E.X"/"A.P").
     * Los códigos que no existan en el estudiante actual ya vienen filtrados
     * desde ImportadorNotas, así que acá solo se actualiza y se guarda una vez.
     *
     * Devuelve cuántas asignaturas quedaron con una nota distinta a la que
     * tenían, para poder mostrarle ese número al usuario.
     */
    fun importarNotasDesdePdf(notasPorCodigo: Map<String, NotaImportada>): Int {
        val estudianteActual = programa.estudiante ?: return 0
        val (nuevoEstudiante, actualizadas) = estudianteActual.actualizarNotasMasivo(notasPorCodigo)
        if (actualizadas > 0) {
            val sincronizado = nuevoEstudiante.sincronizarComplementarias()
            programa = programa.cambiarEstudiante(sincronizado)
            parse.guardarEstudiante(sincronizado)
        }
        return actualizadas
    }

    /**
     * Reemplaza por completo al estudiante actual por uno importado desde un
     * archivo JSON (por ejemplo, un respaldo exportado antes desde esta misma
     * app) y lo persiste de inmediato en el almacenamiento interno.
     */
    fun importarEstudianteDesdeJson(nuevoEstudiante: Estudiante) {
        val sincronizado = nuevoEstudiante.sincronizarComplementarias()
        programa = programa.cambiarEstudiante(sincronizado)
        parse.guardarEstudiante(sincronizado)
    }

    /**
     * Crea una asignatura complementaria completa (botón "Agregar
     * asignatura" de Pensum), donde el usuario ingresa nombre, código,
     * semestre, créditos y nota. Línea profesional, componente y área se
     * fijan automáticamente porque siempre son complementarias: el área
     * ("Complementario") es la que usa ProgramaM para sumar los créditos
     * complementarios; el componente es descriptivo.
     */
    fun agregarAsignaturaComplementaria(
        nombre: String,
        codigo: String,
        semestre: Int,
        creditos: Int,
        nota: Float
    ) {
        val estudianteActual = programa.estudiante ?: return
        val nuevaAsignatura = Asignatura(
            nombre = nombre,
            codigo = codigo,
            semestre = semestre,
            creditos = creditos,
            lineaProfesional = "No",
            componente = "Formación en investigación e Intervención",
            area = "Complementario",
            nota = nota,
            complementaria = true
        )
        val nuevoEstudiante = estudianteActual.agregarAsignatura(nuevaAsignatura).sincronizarComplementarias()
        programa = programa.cambiarEstudiante(nuevoEstudiante)
        parse.guardarEstudiante(nuevoEstudiante)
    }

    /**
     * Crea una actividad complementaria (botón "Agregar actividad" de
     * Pensum), donde el usuario solo ingresa nombre y créditos. El código
     * se autogenera empezando en "999", el semestre siempre es 0, la nota
     * siempre queda en 5 con la marca especial "A.P" (Aprobado), y línea
     * profesional, componente y área se fijan automáticamente porque
     * siempre son complementarias: el área ("Complementario") es la que
     * usa ProgramaM para sumar los créditos complementarios; el
     * componente es descriptivo.
     */
    fun agregarActividadComplementaria(
        nombre: String,
        creditos: Int
    ) {
        val estudianteActual = programa.estudiante ?: return
        val nuevoCodigo = estudianteActual.siguienteCodigoComplementario()
        val nuevaAsignatura = Asignatura(
            nombre = nombre,
            codigo = nuevoCodigo,
            semestre = 0,
            creditos = creditos,
            lineaProfesional = "No",
            componente = "Formación en investigación e Intervención",
            area = "Complementario",
            nota = 5f,
            notaEspecial = "A.P",
            complementaria = true
        )
        val nuevoEstudiante = estudianteActual.agregarAsignatura(nuevaAsignatura).sincronizarComplementarias()
        programa = programa.cambiarEstudiante(nuevoEstudiante)
        parse.guardarEstudiante(nuevoEstudiante)
    }

    /**
     * Elimina una actividad/asignatura complementaria (creada desde los
     * botones "Agregar asignatura" o "Agregar actividad" de Pensum). Solo
     * elimina si la asignatura tiene `complementaria == true`; para
     * cualquier otra asignatura del pensum no hace nada, ya que
     * `Estudiante.eliminarAsignatura` protege esa regla.
     */
    fun eliminarAsignaturaComplementaria(codigoAsignatura: String) {
        val estudianteActual = programa.estudiante ?: return
        val nuevoEstudiante = estudianteActual.eliminarAsignatura(codigoAsignatura).sincronizarComplementarias()
        if (nuevoEstudiante !== estudianteActual) {
            programa = programa.cambiarEstudiante(nuevoEstudiante)
            parse.guardarEstudiante(nuevoEstudiante)
        }
    }

    /**
     * Elimina permanentemente al estudiante actual: borra el archivo
     * guardado en disco y vuelve a cargar la plantilla original del
     * pensum (empaquetada en los assets de la app), como si la app se
     * abriera por primera vez.
     */
    fun eliminarEstudiante() {
        parse.eliminarEstudiante()
        val estudianteReiniciado = parse.cargarEstudiante()
        programa = if (estudianteReiniciado != null) {
            programa.cambiarEstudiante(estudianteReiniciado)
        } else {
            ProgramaM()
        }
    }
}