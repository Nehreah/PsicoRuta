# Manual de cambios — Semestre SIRA en el importador de notas

Proyecto: **PsicoRuta / PsicologiaUV** (Kotlin + Jetpack Compose)

## Qué se pidió
Que al importar el PDF del SIRA la app refleje el historial académico real. El PDF no trae el semestre del estudiante, pero **cada `PERIODO:` equivale a un semestre**: el primero es el 1, el segundo el 2, y así hasta el último. Antes de generar el import final, cada asignatura recibe el semestre de su periodo y el pensum la muestra en ese semestre. Normalmente el semestre SIRA y el del pensum coinciden; cuando no, **gana el del SIRA**.

## Archivos entregados

| Archivo | Estado | Carpeta (`app/src/main/java/com/renea/psicologiauv/`) |
|---|---|---|
| `AnalizadorNotas.kt` | Modificado | `data/` |
| `ImportadorNotas.kt` | Modificado | `data/` |
| `ReglaSemestreSira.kt` | **Nuevo** | `data/` |
| `NotaImportada.kt` | Modificado | `model/` |
| `Estudiante.kt` | Modificado | `model/` |
| `ControlV.kt` | Modificado | `gui/` |
| `PantallaEditar.kt` | Modificado | `gui/` |
| `SemestreSiraTest.kt` | **Nuevo** (pruebas) | `app/src/test/java/com/renea/psicologiauv/` |
| `diagrama_clases.mmd / .png / .svg` | Actualizado | `docs/cambios_semestre_sira/` |

El resto del proyecto no se tocó.

## Cómo funciona (idea general)
1. `PantallaEditar` lee el PDF y llama a `extraerSemestresSiraPorCodigo`: **código del PDF → semestre SIRA**.
2. `ReglaSemestreSira` traslada esos códigos a los del pensum (Español → Cultura Escrita, alias de Deporte Formativo).
3. `ControlV.importarNotasDesdePdf` aplica las notas, resuelve las electivas profesionales y **después** llama a `Estudiante.aplicarSemestresSira`.
4. Las asignaturas no sincronizadas que el usuario asigna a una electiva complementaria llevan su semestre en `AsignaturaNoSincronizada.semestre`.

### Resultado con el PDF de ejemplo (Andrea, 8 periodos)
| Semestre SIRA | Periodo |
|---|---|
| 1 | Marzo/2023 – Julio/2023 |
| 2 | Agosto/2023 – Diciembre/2023 |
| 3 | Febrero/2024 – Junio/2024 |
| 4 | Agosto/2024 – Diciembre/2024 |
| 5 | Febrero/2025 – Junio/2025 |
| 6 | Agosto/2025 – Diciembre/2025 |
| 7 | Febrero/2026 – Junio/2026 |
| 8 | Agosto/2026 – Diciembre/2026 (en curso) |

Casos que quedan bien resueltos: Pensamiento Lógico-Matemático (cancelada en el 1, aprobada en el **3**); Estrategias de Análisis de Datos Cualitativos (cancelada en el 3, aprobada en el **5**); Tópicos en Psicología Organizacional III, Danza y Decolonialidad y Danzas y Expresión Corporal (canceladas y nunca recursadas) **no reciben semestre**.

### Reglas importantes
- **Semestres 0 y 99 no se tocan** (Trabajo de Grado, Actividades Complementarias, Idiomas).
- **Semestre mayor a 11** se ubica en 11 (la grilla del pensum llega hasta 11).
- **Sin `PERIODO:` en el PDF** → todo se comporta como antes.
- **Materias en curso:** quedan en el último periodo del PDF. Antes se movían a `semestreEnCurso()`; esa lógica queda solo de respaldo.

> **Ojo con el horario:** `RepositorioHorarios.horarioDelSemestre` filtra por `semestreEnCurso()` (el primer semestre incompleto después del último completo). En el caso normal coincide con el último periodo del SIRA. Si un estudiante tiene un semestre anterior sin completar por tolerancia de créditos, `semestreEnCurso()` podría ser menor que el último periodo y el horario saldría vacío. No se cambió para respetar el alcance de lo pedido; si ocurre, se resuelve en el siguiente ajuste.

---

## 1. `data/AnalizadorNotas.kt` — Modificado

### Qué cambió
Se agregó el patrón `patronPeriodo` y la función `extraerSemestresSiraPorCodigo`, y `extraerNoSincronizadas` ahora rellena el semestre de cada asignatura no sincronizada.

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -75,4 +75,9 @@
     private const val LONGITUD_MAXIMA_FILA = 400
 
+    // Encabezado de cada periodo del tabulado ("PERIODO: MARZO/2023 - JULIO/2023").
+    // Es sensible a mayúsculas a propósito: el resumen de cada periodo dice
+    // "Resumen de Finalización por periodo" (minúscula y sin dos puntos).
+    private val patronPeriodo = Regex("""PERIODO\s*:""")
+
     // Fin de la lista de asignaturas del tabulado de matrícula ("Total Asignaturas
     // Matriculadas: ...") y marcas de cancelación/reactivación de una fila.
@@ -274,4 +279,87 @@
 
     /**
+     * Semestre SIRA de cada código de [codigosConocidos] presente en el tabulado.
+     *
+     * Cada encabezado "PERIODO:" abre un semestre nuevo: el primero es el 1, el
+     * siguiente el 2, y así sucesivamente. Cada materia recibe el número del
+     * periodo en el que aparece. Reglas:
+     * - Las apariciones canceladas (y no reactivadas) se ignoran.
+     * - Si la materia aparece en varios periodos (cancelada y recursada, o
+     *   reprobada y repetida) queda el semestre de la aparición MÁS RECIENTE.
+     * - Una fila con nota siempre cuenta; una fila sin nota solo cuenta en el
+     *   último periodo (materia en curso) y si no está cancelada.
+     * - Si el texto no trae ningún "PERIODO:" el resultado es vacío.
+     */
+    fun extraerSemestresSiraPorCodigo(
+        texto: String,
+        codigosConocidos: List<String>
+    ): Map<String, Int> {
+        val textoNormalizado = texto.replace(',', '.')
+        val conocidos = codigosConocidos
+            .filter { it.length >= LONGITUD_MINIMA_CODIGO }
+            .toSet()
+        if (conocidos.isEmpty()) return emptyMap()
+
+        val periodos = patronPeriodo.findAll(textoNormalizado)
+            .map { it.range.first }
+            .toList()
+            .toIntArray()
+        if (periodos.isEmpty()) return emptyMap()
+
+        val limitesLista = ArrayList<Int>()
+        val apariciones = ArrayList<Pair<Int, String>>()
+        for (coincidencia in patronCodigoGenerico.findAll(textoNormalizado)) {
+            limitesLista.add(coincidencia.range.first)
+            if (coincidencia.value in conocidos) {
+                apariciones.add(coincidencia.range.first to coincidencia.value)
+            }
+        }
+        for (codigo in conocidos) {
+            if (patronCodigoGenerico.matches(codigo)) continue
+            val patron = Regex("(?<![A-Za-z0-9])" + Regex.escape(codigo) + "(?![A-Za-z0-9])")
+            for (c in patron.findAll(textoNormalizado)) apariciones.add(c.range.first to codigo)
+        }
+        apariciones.sortBy { it.first }
+
+        val limites = limitesLista.toIntArray()
+        val resultado = LinkedHashMap<String, Int>()
+        for ((inicio, codigo) in apariciones) {
+            val fin = inicio + codigo.length
+            val siguiente = primerIndiceMayorQue(limites, fin - 1)
+            val finFila = minOf(
+                if (siguiente < limites.size) limites[siguiente] else fin + LONGITUD_MAXIMA_FILA,
+                fin + LONGITUD_MAXIMA_FILA,
+                textoNormalizado.length
+            )
+            var fragmento = textoNormalizado.substring(fin, finFila)
+            val resumen = fragmento.indexOf(MARCADOR_RESUMEN_PERIODO)
+            if (resumen >= 0) fragmento = fragmento.substring(0, resumen)
+            val totales = fragmento.indexOf(MARCADOR_TOTALES)
+            if (totales >= 0) fragmento = fragmento.substring(0, totales)
+
+            // Cantidad de encabezados "PERIODO:" que hay antes de esta fila = su semestre.
+            val semestre = primerIndiceMayorQue(periodos, inicio)
+            if (semestre == 0) continue
+
+            // El orden en que la librería de PDF entrega el texto puede dejar el aviso de
+            // cancelación de una fila dentro del tramo de la vecina, así que NO se confía
+            // solo en él:
+            // - Una fila con nota o marca especial nunca está cancelada: se acepta.
+            // - Una fila sin nota solo puede estar "en curso" en el ÚLTIMO periodo; en un
+            //   periodo anterior significa que la materia se canceló.
+            // - En el último periodo, una fila sin nota se acepta si no dice "cancelada".
+            val tieneNota = patronNota.containsMatchIn(fragmento) ||
+                patronNotaEspecial.containsMatchIn(fragmento)
+            val vigente = when {
+                tieneNota -> true
+                semestre < periodos.size -> false
+                else -> !filaCancelada(fragmento)
+            }
+            if (vigente) resultado[codigo] = semestre
+        }
+        return resultado
+    }
+
+    /**
      * Busca en [texto] asignaturas cuyo código no está en [codigosConocidos] y que
      * están APROBADAS (nota numérica mayor a 2.9 o marca especial C.U/E.X/A.P) o
@@ -302,4 +390,5 @@
         val notas = extraerNotasPorCodigo(texto, lista)
         val enCurso = extraerCodigosPresentes(texto, lista).toSet()
+        val semestres = extraerSemestresSiraPorCodigo(texto, lista)
 
         val resultado = ArrayList<AsignaturaNoSincronizada>()
@@ -319,5 +408,6 @@
                     codigo = codigo,
                     nombre = extraerNombreTrasCodigo(textoNormalizado, codigo) ?: "Asignatura $codigo",
-                    nota = nota
+                    nota = nota,
+                    semestre = semestres[codigo]
                 )
             )
```

### Por qué
- **`patronPeriodo`** busca el encabezado `PERIODO:` con mayúsculas exactas. Así no se confunde con «Resumen de Finalización por periodo» (minúscula y sin dos puntos).
- **`extraerSemestresSiraPorCodigo`** recorre el texto con el mismo método de las otras funciones (un solo paso, límites de fila, búsqueda binaria). El semestre de una fila es la cantidad de encabezados `PERIODO:` que hay antes de ella (`primerIndiceMayorQue(periodos, inicio)`): el primer periodo es el 1, el segundo el 2, etc.
- Si una materia aparece varias veces (cancelada y recursada, o repetida) el mapa se sobrescribe en orden de aparición, así que queda el semestre de la aparición **más reciente y vigente**. Ejemplo real: *Pensamiento Lógico-Matemático* se canceló en el periodo 1 y se aprobó en el 3, queda en **3**.
- **Por qué no se confía solo en el aviso «CANCELACIÓN»:** según el orden en que la librería de PDF entregue el texto, el aviso de una fila puede caer dentro del tramo de la fila vecina (se comprobó con el PDF de ejemplo). Por eso: una fila con nota o marca C.U/E.X/A.P siempre es válida (una materia cancelada nunca tiene nota); una fila sin nota solo es válida en el **último** periodo (materia en curso) y si no dice «cancelada»; sin nota en un periodo anterior es una cancelación.
- Si el PDF no trae ningún `PERIODO:` devuelve un mapa vacío y la importación se comporta exactamente como antes.

---

## 2. `data/ImportadorNotas.kt` — Modificado

### Qué cambió
Se agregó el método `extraerSemestresSiraPorCodigo`, que delega en `AnalizadorNotas` (igual que los demás métodos de la clase).

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -93,4 +93,14 @@
 
     /**
+     * Semestre SIRA (1 = primer periodo del tabulado) de cada código conocido
+     * presente en el PDF. Cada "PERIODO:" del documento equivale a un semestre.
+     */
+    fun extraerSemestresSiraPorCodigo(
+        texto: String,
+        codigosConocidos: List<String>
+    ): Map<String, Int> =
+        AnalizadorNotas.extraerSemestresSiraPorCodigo(texto, codigosConocidos)
+
+    /**
      * Asignaturas aprobadas del PDF que no coinciden con ningún código de
      * [codigosConocidos] (pensum + catálogo de electivas profesionales).
```

### Por qué
- Es la puerta de entrada que usa la pantalla; no contiene lógica propia.

---

## 3. `data/ReglaSemestreSira.kt` — **Nuevo**

### Qué cambió
Archivo nuevo. Traslada los semestres SIRA a los códigos del pensum con las mismas equivalencias que ya usan las notas.

### Código
Archivo completo entregado (≈70 líneas); ver `ReglaSemestreSira.kt`.

### Por qué
- **Español → Cultura Escrita:** si Español (204001M) está aprobado, su nota llena Cultura Escrita (402180M); por coherencia 402180M toma también el semestre de Español. La Cultura Escrita real queda «flotante» (`402180M-CE`) y conserva su propio semestre.
- **Deporte Formativo:** el espacio `404010C` toma el semestre del alias cuya nota fue elegida (mismo criterio de comparación que `ReglaDeporteFormativo`). Si el SIRA ya trae `404010C`, no se toca.
- Elimina del resultado los códigos de Español y de los alias, igual que ocurre con las notas, para que no generen entradas desconocidas.

---

## 4. `model/NotaImportada.kt` — Modificado

### Qué cambió
`AsignaturaNoSincronizada` recibe un campo nuevo `semestre: Int? = null`.

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -36,4 +36,9 @@
     val nombre: String,
     /** null = la asignatura se está cursando (aún sin nota). */
-    val nota: NotaImportada?
+    val nota: NotaImportada?,
+    /**
+     * Semestre SIRA en el que se cursó (1 = primer periodo del tabulado), o null
+     * si el tabulado no permitió calcularlo.
+     */
+    val semestre: Int? = null
 )
```

### Por qué
- Las asignaturas del PDF que no están en el pensum se ofrecen luego como electivas complementarias. Necesitan llevar su semestre SIRA para ubicarlas cuando el usuario las asigne.
- Tiene valor por defecto `null`, así que el código y las pruebas existentes compilan sin cambios.

---

## 5. `model/Estudiante.kt` — Modificado

### Qué cambió
Se agregó la constante privada `SEMESTRE_MAXIMO_PENSUM` y el método `aplicarSemestresSira`.

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -5,4 +5,7 @@
 import com.renea.psicologiauv.model.ElectivaProfesionalCatalogo
 
+/** Último semestre que muestra la grilla del pensum. */
+private const val SEMESTRE_MAXIMO_PENSUM = 11
+
 @Serializable
 data class Estudiante(
@@ -15,4 +18,5 @@
     val semestresManuales: Map<Int, Boolean> = emptyMap()
 ) {
+
     fun cambiarNombre(nuevoNombre: String): Estudiante =
         copy(nombre = nuevoNombre)
@@ -132,4 +136,36 @@
     }
 
+    /**
+     * Ubica cada asignatura en el semestre SIRA en el que se cursó (cada
+     * periodo del tabulado es un semestre, contando desde 1).
+     *
+     * - Solo se tocan las asignaturas que hoy viven en la grilla del pensum
+     *   (semestre 1..11). Las que usan semestres estructurales (0 = trabajo de
+     *   grado y actividades complementarias, 99 = idiomas) se respetan.
+     * - Un semestre SIRA mayor a [SEMESTRE_MAXIMO_PENSUM] se ubica en el último
+     *   semestre del pensum, para que la asignatura no desaparezca de la grilla.
+     * - Cuando el semestre SIRA coincide con el del pensum no cambia nada.
+     *
+     * Devuelve el nuevo [Estudiante] y la cantidad de asignaturas que cambiaron
+     * de semestre.
+     */
+    fun aplicarSemestresSira(semestresPorCodigo: Map<String, Int>): Pair<Estudiante, Int> {
+        if (semestresPorCodigo.isEmpty()) return this to 0
+        val porCodigo = semestresPorCodigo.mapKeys { it.key.trim().uppercase() }
+
+        var cambiadas = 0
+        val nuevas = asignaturas.map { asignatura ->
+            val semestreSira = porCodigo[asignatura.codigo.trim().uppercase()]
+            if (semestreSira == null || asignatura.semestre !in 1..SEMESTRE_MAXIMO_PENSUM) {
+                asignatura
+            } else {
+                val movida = asignatura.cambiarSemestre(semestreSira.coerceAtMost(SEMESTRE_MAXIMO_PENSUM))
+                if (movida.semestre != asignatura.semestre) cambiadas++
+                movida
+            }
+        }.toMutableList()
+        return copy(asignaturas = nuevas) to cambiadas
+    }
+
     fun cambiarNotaAsignatura(codigoAsignatura: String, nuevaNota: Float): Estudiante{
         val nuevasAsignaturas = asignaturas.map {
```

### Por qué
- **Solo mueve asignaturas cuyo semestre actual está en 1..11** (la grilla del pensum). Las que usan semestres estructurales —**0** (Trabajo de Grado y Actividades Académicas Complementarias) y **99** (Idiomas)— se respetan; si no, Idiomas dejaría de reconocerse.
- Si el semestre SIRA es mayor a 11 (estudiante con muchos periodos), se ubica en 11 para que la asignatura no desaparezca de la grilla, que solo muestra 1..11.
- Reutiliza `Asignatura.cambiarSemestre` y devuelve cuántas asignaturas cambiaron.
- La constante es de nivel de archivo (no un `companion object`) para no interferir con el companion que genera `kotlinx.serialization` en la clase `@Serializable`.

---

## 6. `gui/ControlV.kt` — Modificado

### Qué cambió
`importarNotasDesdePdf` recibe `semestresSira` y aplica el semestre; `asignarNoSincronizadasAComplementarias` aplica el semestre de cada asignatura asignada.

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -164,5 +164,5 @@
      * tenían, para poder mostrarle ese número al usuario.
      */
-    fun importarNotasDesdePdf(notasPorCodigo: Map<String, NotaImportada>, gruposPorCodigo: Map<String, String> = emptyMap(), codigosPresentes: List<String> = emptyList()): Int {
+    fun importarNotasDesdePdf(notasPorCodigo: Map<String, NotaImportada>, gruposPorCodigo: Map<String, String> = emptyMap(), codigosPresentes: List<String> = emptyList(), semestresSira: Map<String, Int> = emptyMap()): Int {
         val estudianteActual = programa.estudiante ?: return 0
 
@@ -189,13 +189,19 @@
         val sincronizado = estudianteConGrupos.sincronizarComplementarias()
 
-        // 3. Las asignaturas que aparecen en el PDF, no están canceladas y no tienen nota se
-        //    están cursando ahora: se pasan al semestre actual para que salgan en el horario,
-        //    aunque en el pensum pertenezcan a otro semestre.
-        val semestreActual = programa.cambiarEstudiante(sincronizado).semestreEnCurso()
+        // 3. Cada periodo del PDF es un semestre: las asignaturas se ubican en el semestre SIRA
+        //    en el que se cursaron (también las que se están cursando, que quedan en el último).
+        val (estudianteConSemestres, reubicadas) = sincronizado.aplicarSemestresSira(semestresSira)
+
+        // 4. Las asignaturas que aparecen en el PDF, no están canceladas y no tienen nota se
+        //    están cursando ahora: si el PDF no permitió calcular su semestre SIRA, se pasan al
+        //    semestre actual para que salgan en el horario.
+        val semestresConocidos = semestresSira.keys.map { it.trim().uppercase() }.toSet()
+        val enCursoSinSemestreSira = codigosPresentes.filter { it.trim().uppercase() !in semestresConocidos }
+        val semestreActual = programa.cambiarEstudiante(estudianteConSemestres).semestreEnCurso()
         val (estudianteConEnCurso, movidas) =
-            sincronizado.moverAsignaturasEnCursoAlSemestre(codigosPresentes, semestreActual)
+            estudianteConSemestres.moverAsignaturasEnCursoAlSemestre(enCursoSinSemestreSira, semestreActual)
 
         val totalCambios = actualizadas + electivasResueltas
-        if (totalCambios > 0 || gruposPorCodigo.isNotEmpty() || movidas > 0) {
+        if (totalCambios > 0 || gruposPorCodigo.isNotEmpty() || movidas > 0 || reubicadas > 0) {
             programa = programa.cambiarEstudiante(estudianteConEnCurso)
             parse.guardarEstudiante(estudianteConEnCurso)
@@ -217,4 +223,6 @@
         asignaciones.forEach { (item, posicion) ->
             actual = actual.asignarElectivaComplementaria(posicion, item.codigo, item.nombre, item.nota)
+            // Semestre SIRA en el que se cursó, si el PDF lo permitió calcular.
+            item.semestre?.let { actual = actual.aplicarSemestresSira(mapOf(item.codigo to it)).first }
         }
         var sincronizado = actual.sincronizarComplementarias()
@@ -224,4 +232,8 @@
         val semestreActual = programa.cambiarEstudiante(sincronizado).semestreEnCurso()
         val enCurso = asignaciones.filter { it.first.nota == null }.map { it.first.codigo.trim().uppercase() }.toSet()
+        val enCursoSinSemestreSira = asignaciones
+            .filter { it.first.nota == null && it.first.semestre == null }
+            .map { it.first.codigo.trim().uppercase() }
+            .toSet()
         if (enCurso.isNotEmpty()) {
             val conGrupo = sincronizado.asignaturas.map { asignatura ->
@@ -231,5 +243,5 @@
             }.toMutableList()
             sincronizado = sincronizado.copy(asignaturas = conGrupo)
-                .moverAsignaturasEnCursoAlSemestre(enCurso, semestreActual).first
+                .moverAsignaturasEnCursoAlSemestre(enCursoSinSemestreSira, semestreActual).first
         }
```

### Por qué
- Los semestres se aplican **después** de resolver las electivas profesionales: así sus códigos ya son los reales, que son los que están en el mapa.
- `moverAsignaturasEnCursoAlSemestre` (la lógica anterior del horario) **se conserva solo como respaldo** para las materias en curso cuyo semestre SIRA no se pudo calcular. Si se calculó, la materia en curso queda en el último periodo del PDF, que es su semestre real.
- Se guarda el estudiante también cuando solo cambiaron semestres (`reubicadas > 0`).

---

## 7. `gui/PantallaEditar.kt` — Modificado

### Qué cambió
La lectura del PDF calcula los semestres SIRA, los pasa al importador y deja de preguntar el semestre de las electivas que ya lo tienen.

### Partes exactas del código (diff: `-` antes, `+` después)
```diff
@@ -117,4 +117,5 @@
 import com.renea.psicologiauv.data.ReglaEspanol
 import com.renea.psicologiauv.data.ReglaDeporteFormativo
+import com.renea.psicologiauv.data.ReglaSemestreSira
 import com.renea.psicologiauv.data.ImprimirPDF
 import com.renea.psicologiauv.data.Serializador
@@ -449,4 +450,7 @@
     var codigosPresentesPdf by remember { mutableStateOf<Set<String>>(emptySet()) }
 
+    // Códigos del último PDF cuyo semestre se calculó con los periodos del SIRA (ya no hay que preguntarlo).
+    var codigosConSemestreSira by remember { mutableStateOf<Set<String>>(emptySet()) }
+
     // Asignaturas del PDF que no coincidieron con el pensum; se resuelven antes de pedir los semestres.
     var noSincronizadas by remember { mutableStateOf<List<AsignaturaNoSincronizada>>(emptyList()) }
@@ -699,13 +703,21 @@
                                     codigosConocidos
                                 )
+                                // Cada "PERIODO:" del SIRA es un semestre (1, 2, 3...). Se traslada a los
+                                // códigos del pensum con las mismas equivalencias de Español y Deporte.
+                                val semestres = ReglaSemestreSira.aplicar(
+                                    importadorNotas.extraerSemestresSiraPorCodigo(texto, codigosConocidos),
+                                    notasCrudas,
+                                    codigosDeporte
+                                )
                                 // Cultura Escrita flotante: solo si no se asignó antes a una complementaria.
                                 val flotante = regla.culturaFlotante?.takeIf { f ->
                                     estudiante.asignaturas.none { it.codigo == f.codigo }
-                                }
+                                }?.let { it.copy(semestre = semestres[it.codigo]) }
                                 ResultadoLecturaPdf(
                                     notas,
                                     grupos,
                                     listOfNotNull(flotante) + sinSincronizar,
-                                    presentes
+                                    presentes,
+                                    semestres
                                 )
                             }
@@ -749,5 +761,6 @@
                                     resultado.notas,
                                     resultado.grupos,
-                                    resultado.presentes
+                                    resultado.presentes,
+                                    resultado.semestres
                                 )
 
@@ -761,9 +774,15 @@
                             noSincronizadas = resultado.sinSincronizar
                             codigosPresentesPdf = resultado.presentes.toSet()
+                            codigosConSemestreSira = resultado.semestres.keys
+                                .map { it.trim().uppercase() }
+                                .toSet()
 
                             // Si ya hay electivas profesionales cursadas, pide el semestre de cada una.
                             val hayElectivasCursadas = viewModel.programa.estudiante
                                 ?.asignaturas.orEmpty()
-                                .any { it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) }
+                                .any {
+                                    it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) &&
+                                        it.codigo.trim().uppercase() !in codigosConSemestreSira
+                                }
                             if (hayElectivasCursadas) {
                                 mostrandoSemestresElectivas = true
@@ -910,5 +929,8 @@
 
         val electivasCursadas = estudiante.asignaturas
-            .filter { it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) }
+            .filter {
+                it.esElectivaProfesionalParaSemestre(codigosPresentesPdf) &&
+                    it.codigo.trim().uppercase() !in codigosConSemestreSira
+            }
 
         if (electivasCursadas.isEmpty()) {
@@ -2431,5 +2453,7 @@
     val grupos: Map<String, String>,
     val sinSincronizar: List<AsignaturaNoSincronizada>,
-    val presentes: List<String>
+    val presentes: List<String>,
+    /** Código del pensum -> semestre SIRA (cada periodo del PDF es un semestre). */
+    val semestres: Map<String, Int> = emptyMap()
 )
```

### Por qué
- Se calcula `semestres` con `ReglaSemestreSira.aplicar(...)` usando las notas crudas y los alias de Deporte.
- A la Cultura Escrita flotante se le asigna su semestre (`copy(semestre = ...)`).
- `ResultadoLecturaPdf` incorpora `semestres` (con valor por defecto).
- El módulo «Selecciona el semestre en el que cursaste cada electiva» **solo aparece para las electivas cuyo semestre el SIRA no permitió calcular** (estado nuevo `codigosConSemestreSira`). Si no, se le pediría al usuario algo que el PDF ya respondió.

---

## 8. `SemestreSiraTest.kt` — Nuevo
Pruebas unitarias JVM (sin Android): cada periodo es un semestre desde 1; materia cancelada y recursada; materia en curso; cancelada sin recursar; PDF sin periodos; resumen que no cuenta como periodo; no sincronizada con semestre; equivalencias de Español y Deporte; estudiante que respeta semestres 0/99; tope en 11.

## Verificación
- La lógica de lectura se validó con un port fiel sobre el texto real del PDF de ejemplo: **48 materias, ubicadas en los 8 semestres esperados**, con las canceladas excluidas.
- **No pude compilar ni ejecutar Gradle en mi entorno**. Antes de usarlo, corre `./gradlew testDebugUnitTest` y compila la app; si algo no compila, envíame el error.

## Diagrama de clases
Actualizado en `docs/cambios_semestre_sira/diagrama_clases.mmd` (más `.png` y `.svg`). Lo nuevo está marcado con ★ **semestre SIRA**.
