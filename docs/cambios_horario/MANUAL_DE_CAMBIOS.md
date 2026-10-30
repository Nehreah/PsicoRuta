# Manual de cambios — Horario: asignaturas en curso van al semestre actual

Proyecto: **PsicoRuta / PsicologiaUV** (Kotlin + Jetpack Compose)

## Qué se pidió
Al sincronizar (importar el tabulado SIRA), buscar las asignaturas que **no tienen nota** y **no están canceladas**.
Esas se están cursando ahora, así que se les cambia el semestre al **semestre actual** (el mismo que usa el programa:
`ProgramaM.semestreEnCurso()`), y por eso aparecen en el horario aunque en el pensum sean de otro semestre
(una de tercero que no viste, o una de octavo que estás adelantando).

## Archivos entregados

| Archivo | Estado | Carpeta (`app/src/main/java/com/renea/psicologiauv/`) |
|---|---|---|
| `Asignatura.kt` | Modificado (1 función nueva) | `model/` |
| `Estudiante.kt` | Modificado (1 función nueva) | `model/` |
| `ControlV.kt` | Modificado (2 funciones) | `gui/` |
| `diagrama_clases.mmd` | Actualizado | `docs/cambios_horario/` |

El resto del proyecto no se tocó (incluidos `RepositorioHorarios.kt` y `PantallaEditar.kt`).

## Cómo funciona (idea general)
- **"No está cancelada"**: ya lo resuelve `AnalizadorNotas.extraerCodigosPresentes`, que devuelve los códigos del PDF
  cuya fila no dice CANCELACIÓN (o que fueron reactivados). Esa lista llega a `importarNotasDesdePdf` como `codigosPresentes`.
- **"No tiene nota"**: se evalúa después de aplicar las notas del PDF, con la nueva `Asignatura.sinNota()`
  (sin nota numérica y sin marca especial C.U / E.X / A.P).
- **Semestre actual**: se calcula con `semestreEnCurso()` sobre el estudiante ya con las notas importadas.
- El grupo de cada asignatura ya se guardaba en la importación; el horario sigue filtrando por semestre + grupo,
  así que no hubo que cambiarlo.

---

## 1. `model/Asignatura.kt`

### Qué cambió
Se agregó una función, justo debajo de `aprobo(...)`:
```kotlin
fun sinNota(): Boolean = nota <= 0f && notaEspecial == null
```
(con su comentario KDoc).

### Por qué
Da un nombre claro a "no tiene nota". Una materia reprobada con nota (p. ej. 2.0) **no** cuenta como sin nota.

### Cómo replicarlo en tu código
Pega la función (con su comentario) en `Asignatura`, después de `aprobo`.

---

## 2. `model/Estudiante.kt`

### Qué cambió
Se agregó `moverAsignaturasEnCursoAlSemestre`, justo antes de `cambiarNotaAsignatura`:
```kotlin
fun moverAsignaturasEnCursoAlSemestre(
    codigosEnCurso: Collection<String>,
    semestreActual: Int
): Pair<Estudiante, Int> {
    val enCurso = codigosEnCurso.map { it.trim().uppercase() }.toSet()
    if (enCurso.isEmpty()) return this to 0

    var movidas = 0
    val nuevasAsignaturas = asignaturas.map { asignatura ->
        val cursando = asignatura.codigo.trim().uppercase() in enCurso && asignatura.sinNota()
        if (cursando && asignatura.semestre != semestreActual) {
            val movida = asignatura.cambiarSemestre(semestreActual)
            if (movida.semestre != asignatura.semestre) movidas++
            movida
        } else {
            asignatura
        }
    }.toMutableList()
    return copy(asignaturas = nuevasAsignaturas) to movidas
}
```

### Por qué
Es la regla de negocio, aislada en el modelo (sin Android) para que se pueda probar con tests unitarios.
Solo mueve asignaturas que están en la lista de "presentes y no canceladas" **y** no tienen nota;
las que ya están en el semestre actual no se tocan. Devuelve cuántas movió.

### Cómo replicarlo
Agrega la función a `Estudiante` (necesita `Asignatura.sinNota()` del punto 1).

---

## 3. `gui/ControlV.kt`

### 3.1 `importarNotasDesdePdf` — parte final

**Antes:**
```kotlin
val totalCambios = actualizadas + electivasResueltas
if (totalCambios > 0 || gruposPorCodigo.isNotEmpty()) {
    val sincronizado = estudianteConGrupos.sincronizarComplementarias()
    programa = programa.cambiarEstudiante(sincronizado)
    parse.guardarEstudiante(sincronizado)
}
return totalCambios
```

**Ahora:**
```kotlin
gruposUltimaImportacion = gruposPorCodigo

val sincronizado = estudianteConGrupos.sincronizarComplementarias()

val semestreActual = programa.cambiarEstudiante(sincronizado).semestreEnCurso()
val (estudianteConEnCurso, movidas) =
    sincronizado.moverAsignaturasEnCursoAlSemestre(codigosPresentes, semestreActual)

val totalCambios = actualizadas + electivasResueltas
if (totalCambios > 0 || gruposPorCodigo.isNotEmpty() || movidas > 0) {
    programa = programa.cambiarEstudiante(estudianteConEnCurso)
    parse.guardarEstudiante(estudianteConEnCurso)
}
return totalCambios
```
Y, dentro de la clase, un campo privado nuevo (por ejemplo justo después de la función):
```kotlin
private var gruposUltimaImportacion: Map<String, String> = emptyMap()
```

**Por qué:**
- `sincronizarComplementarias()` ahora se calcula siempre (es una función pura) pero solo se guarda si hubo cambios, igual que antes.
- El semestre actual se calcula **después** de aplicar notas y electivas, para que refleje lo que ya aprobaste.
- La condición de guardado incluye `movidas > 0` para que se persista aunque no haya cambiado ninguna nota.
- La función sigue devolviendo `totalCambios` (notas importadas), así que el mensaje de `PantallaEditar` no cambia.
- `gruposUltimaImportacion` guarda los grupos del PDF para el punto 3.2.

### 3.2 `asignarNoSincronizadasAComplementarias`

**Antes:**
```kotlin
val sincronizado = actual.sincronizarComplementarias()
programa = programa.cambiarEstudiante(sincronizado)
parse.guardarEstudiante(sincronizado)
```

**Ahora:** `sincronizado` pasa a ser `var` y, antes de guardar, se agrega:
```kotlin
val semestreActual = programa.cambiarEstudiante(sincronizado).semestreEnCurso()
val enCurso = asignaciones.filter { it.first.nota == null }.map { it.first.codigo.trim().uppercase() }.toSet()
if (enCurso.isNotEmpty()) {
    val conGrupo = sincronizado.asignaturas.map { asignatura ->
        val codigo = asignatura.codigo.trim().uppercase()
        val grupo = gruposUltimaImportacion[codigo]
        if (codigo in enCurso && grupo != null) asignatura.copy(grupo = grupo) else asignatura
    }.toMutableList()
    sincronizado = sincronizado.copy(asignaturas = conGrupo)
        .moverAsignaturasEnCursoAlSemestre(enCurso, semestreActual).first
}
```

**Por qué:** una asignatura del PDF que no está en el pensum y que asignas a una electiva complementaria también puede
estar en curso (sin nota). Esa asignación ocurre después, en un diálogo, y el espacio de la electiva no tenía el grupo
(el grupo se asigna por código y el código cambia al asignarla). Se le copia el grupo guardado y se le pasa al semestre actual,
con la misma regla, para que salga en el horario.

---

## Casos de prueba sugeridos (a mano)
1. Estás en 7.º y el PDF trae una materia de 3.º sin nota y sin cancelar → pasa a 7.º y aparece en el horario.
2. Una materia de 8.º que adelantas, sin nota → pasa a 7.º y aparece.
3. Una materia con CANCELACIÓN y sin reactivación → no se mueve.
4. Una materia cancelada y vuelta a matricular (sin nota en la nueva fila) → sí se mueve.
5. Una materia con nota (aprobada o reprobada) o con C.U / E.X / A.P → no se mueve.

## Observaciones (no se cambiaron, por estar fuera de lo pedido)
- **Grupo de materias canceladas:** `AnalizadorNotas.extraerGruposPorCodigo` no descarta las filas canceladas, así que una
  materia cancelada que ya estaba en tu semestre actual conserva el grupo y podría verse en el horario. Si te pasa, la solución
  es filtrar `gruposPorCodigo` por `codigosPresentes` al aplicarlos en `importarNotasDesdePdf`.
- **Materia reprobada y vuelta a cursar:** si la nota anterior (p. ej. 2.0) sigue guardada, la materia "tiene nota" y no se mueve sola;
  la regla pedida es solo para materias sin nota.
- **Verificación:** en este entorno no hay Kotlin ni Gradle, así que el código no se compiló ni se ejecutaron los tests.
  Compílalo y corre los tests existentes en Android Studio antes de usarlo.
- **Diagrama:** se entrega el `.mmd`; no pude renderizar PNG/SVG aquí (sin Chrome). Puedes generarlos en https://mermaid.live
  o con `mmdc -i diagrama_clases.mmd -o diagrama_clases.png`.
