package com.renea.psicologiauv.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renea.psicologiauv.data.Serializador
import com.renea.psicologiauv.model.Asignatura
import com.renea.psicologiauv.model.AsignaturaNoSincronizada
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
        "melson nolima" to "estudiante_melson.json",
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
    fun cambiarCreditosAsignatura(codigoAsignatura: String, nuevosCreditos: Int) {
        val nuevoEstudiante = programa.estudiante?.cambiarCreditosAsignatura(codigoAsignatura, nuevosCreditos)?.sincronizarComplementarias()
        if (nuevoEstudiante != null) {
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
    fun cambiarEstadoSemestre(semestre: Int, completado: Boolean?) {
        val nuevoEstudiante = programa.estudiante?.cambiarEstadoSemestre(semestre, completado)
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
    /**
     * Aplica de una sola vez las notas leídas desde el PDF (código ->
     * NotaImportada, numérica o especial como "C.U"/"E.X"/"A.P").
     * Los códigos que no existan en el estudiante actual ya vienen filtrados
     * desde ImportadorNotas, así que acá solo se actualiza y se guarda una vez.
     *
     * Devuelve cuántas asignaturas quedaron con una nota distinta a la que
     * tenían, para poder mostrarle ese número al usuario.
     */
    fun importarNotasDesdePdf(notasPorCodigo: Map<String, NotaImportada>, gruposPorCodigo: Map<String, String> = emptyMap(), codigosPresentes: List<String> = emptyList(), semestresSira: Map<String, Int> = emptyMap(), creditosSira: Map<String, Int> = emptyMap()): Int {
        val estudianteActual = programa.estudiante ?: return 0

        // 0. Si Español (204001M) está aprobado en el PDF, transforma la materia
        //    básica de 1.º semestre a Español ("204001M") para que reciba su nota.
        val estudianteConEspanol = estudianteActual.aplicarReglaEspanol(notasPorCodigo)

        // 1. Actualiza las asignaturas que ya tienen su código definitivo en el pensum.
        val (estudianteConNotas, actualizadas) = estudianteConEspanol.actualizarNotasMasivo(notasPorCodigo)

        // 1b. Actualiza los créditos de cada asignatura según el SIRA: su valor tiene
        //     prioridad sobre el que traía el JSON del pensum.
        val estudianteConCreditos = estudianteConNotas.actualizarCreditosMasivo(creditosSira)

        // 2. Rellena los slots de Electiva Profesional II/III/IV con las electivas
        //    reales que el SIRA trajo y que aún no están asignadas a ningún slot.
        val catalogo = parse.leerElectivasProfesionales()
        val (estudianteConElectivas, electivasResueltas) =
            estudianteConCreditos.resolverElectivasProfesionales(notasPorCodigo, catalogo, codigosPresentes)

        // Guarda también los grupos importados, aunque no haya cambiado ninguna nota.
        val asignaturasConGrupo = estudianteConElectivas.asignaturas.map { asignatura ->
            val grupo = gruposPorCodigo[asignatura.codigo.trim().uppercase()]
            if (grupo != null) asignatura.copy(grupo = grupo) else asignatura
        }
        val estudianteConGrupos = estudianteConElectivas.copy(asignaturas = asignaturasConGrupo.toMutableList())

        // Recuerda los grupos del PDF: las asignaturas en curso que luego se asignen a una
        // electiva complementaria (ver asignarNoSincronizadasAComplementarias) los necesitan.
        gruposUltimaImportacion = gruposPorCodigo

        val sincronizado = estudianteConGrupos.sincronizarComplementarias()

        // 3. Cada periodo del PDF es un semestre: las asignaturas se ubican en el semestre SIRA
        //    en el que se cursaron (también las que se están cursando, que quedan en el último).
        val (estudianteConSemestres, reubicadas) = sincronizado.aplicarSemestresSira(semestresSira)

        // 4. Las asignaturas que aparecen en el PDF, no están canceladas y no tienen nota se
        //    están cursando ahora: si el PDF no permitió calcular su semestre SIRA, se pasan al
        //    semestre actual para que salgan en el horario.
        val semestresConocidos = semestresSira.keys.map { it.trim().uppercase() }.toSet()
        val enCursoSinSemestreSira = codigosPresentes.filter { it.trim().uppercase() !in semestresConocidos }
        val semestreActual = programa.cambiarEstudiante(estudianteConSemestres).semestreEnCurso()
        val (estudianteConEnCurso, movidas) =
            estudianteConSemestres.moverAsignaturasEnCursoAlSemestre(enCursoSinSemestreSira, semestreActual)

        val totalCambios = actualizadas + electivasResueltas
        if (totalCambios > 0 || gruposPorCodigo.isNotEmpty() || movidas > 0 || reubicadas > 0 || creditosSira.isNotEmpty()) {
            programa = programa.cambiarEstudiante(estudianteConEnCurso)
            parse.guardarEstudiante(estudianteConEnCurso)
        }
        return totalCambios
    }


    // Grupos (código -> grupo) leídos en el último PDF importado.
    private var gruposUltimaImportacion: Map<String, String> = emptyMap()

    /**
     * Sincroniza asignaturas del PDF que no estaban en el pénsum con las
     * electivas complementarias: cada par es (asignatura, posición) con
     * posición 0 = Complementaria I y 1 = Complementaria II.
     *
     * Aquellas asignaturas en [todas] que no se asignen a ninguna complementaria
     * se agregan al pensum en su respectivo semestre como asignaturas extracurriculares.
     */
    fun asignarNoSincronizadasAComplementarias(
        asignaciones: List<Pair<AsignaturaNoSincronizada, Int>>,
        todas: List<AsignaturaNoSincronizada> = emptyList()
    ) {
        if (asignaciones.isEmpty() && todas.isEmpty()) return
        var actual = programa.estudiante ?: return

        if (asignaciones.isNotEmpty()) {
            asignaciones.forEach { (item, posicion) ->
                actual = actual.asignarElectivaComplementaria(posicion, item.codigo, item.nombre, item.nota, item.creditos)
                // Semestre SIRA en el que se cursó, si el PDF lo permitió calcular.
                item.semestre?.let { actual = actual.aplicarSemestresSira(mapOf(item.codigo to it)).first }
            }
        }

        // Las asignaturas no sincronizadas sobrantes se agregan en su respectivo semestre como extracurriculares.
        val codigosAsignados = asignaciones.map { it.first.codigo.trim().uppercase() }.toSet()
        val noAsignadas = todas.filter { it.codigo.trim().uppercase() !in codigosAsignados }
        if (noAsignadas.isNotEmpty()) {
            actual = actual.agregarAsignaturasExtracurriculares(noAsignadas, gruposUltimaImportacion)
        }

        var sincronizado = actual.sincronizarComplementarias()

        // Las que se están cursando (sin nota) pasan al semestre actual y toman su grupo del
        // PDF, igual que las asignaturas del pensum, para que aparezcan en el horario.
        val semestreActual = programa.cambiarEstudiante(sincronizado).semestreEnCurso()
        val itemsTotales = asignaciones.map { it.first } + noAsignadas
        val enCurso = itemsTotales.filter { it.nota == null }.map { it.codigo.trim().uppercase() }.toSet()
        val enCursoSinSemestreSira = itemsTotales
            .filter { it.nota == null && it.semestre == null }
            .map { it.codigo.trim().uppercase() }
            .toSet()
        if (enCurso.isNotEmpty()) {
            val conGrupo = sincronizado.asignaturas.map { asignatura ->
                val codigo = asignatura.codigo.trim().uppercase()
                val grupo = gruposUltimaImportacion[codigo]
                if (codigo in enCurso && grupo != null) asignatura.copy(grupo = grupo) else asignatura
            }.toMutableList()
            sincronizado = sincronizado.copy(asignaturas = conGrupo)
                .moverAsignaturasEnCursoAlSemestre(enCursoSinSemestreSira, semestreActual).first
        }

        programa = programa.cambiarEstudiante(sincronizado)
        parse.guardarEstudiante(sincronizado)
    }

    /**
     * Si el usuario descarta o cierra el diálogo de asignaturas no sincronizadas,
     * todas se agregan como extracurriculares a su respectivo semestre.
     */
    fun descartarNoSincronizadas(todas: List<AsignaturaNoSincronizada>) {
        asignarNoSincronizadasAComplementarias(emptyList(), todas)
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