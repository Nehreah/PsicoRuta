# Manual de cambios — Pantalla «Trabajo de grado»

Proyecto: **PsicoRuta / PsicologiaUV** (Kotlin + Jetpack Compose)
Ruta de los archivos: `app/src/main/java/com/renea/psicologiauv/gui/`

## Archivos entregados

| Archivo | Estado | Carpeta |
|---|---|---|
| `PantallaTrabajoGrado.kt` | Modificado | `gui/` |
| `MainActivity.kt` | Modificado (3 puntos) | `gui/` |
| `PantallaReglamentoTrabajoGrado.kt` | **Nuevo** | `gui/` |
| `diagrama_clases.png / .svg / .mmd` | Actualizado | (documentación) |

El resto del proyecto no se tocó.

---

## 1. `PantallaTrabajoGrado.kt`

### 1.1 Encabezado (hero): solo el título
**Qué se hizo:** se eliminó el `Text` con el párrafo *«Es un proceso formativo de investigación… Resolución 60-2019»* y el `Spacer` que lo separaba. El `Column` interno del hero ya no era necesario y quedó un solo `Text("Trabajo de grado")` con `weight(1f)`. Se redujo el padding vertical del hero (18 → 14 dp) para ganar espacio.
**Por qué:** pedido explícito; además libera altura para que todo quepa en pantalla.

### 1.2 «Para iniciar» → comprobador de requisito inicial
**Qué se eliminó (de la sección «Para iniciar»):**
- El título y la tarjeta desplegable `SeccionTrabajoGrado("Para iniciar", …)`.
- El texto *«Para empezar debes haber aprobado estas asignaturas…»*.
- La lista de asignaturas cumplidas (`requisitos.forEach { FilaRequisito(...) }`).
- El conteo *«Te falta(n) N por aprobar»*.
- El `Punto("Equipo", …)` (recomendación de equipo).

**Qué se agregó:** el composable privado `RequisitoInicial(pendientes: List<String>)`, que recibe solo las asignaturas **no aprobadas**:
```kotlin
RequisitoInicial(
    pendientes = programa.requisitosInicioTrabajoGrado()
        .filter { !it.second }
        .map { it.first }
)
```
- Si la lista está vacía → ✅ **«Cumples con todas las asignaturas básicas para iniciar el ciclo TG»**.
- Si falta alguna → «Te falta esta asignatura por aprobar:» / «Te faltan estas asignaturas por aprobar:» y, debajo, **solo las que faltan** (reutiliza `FilaRequisito`, con X roja).

**Por qué:** pedido explícito. La lógica de qué cuenta como aprobado sigue siendo `ProgramaM.requisitosInicioTrabajoGrado()`, sin modificar.

### 1.3 Ruta por semestres: visible en la misma pantalla
**Qué se hizo:**
- Se quitó el contenedor desplegable; ahora son un título («Ruta por semestres» con su ícono) y la línea de tiempo siempre visible.
- Los 4 hitos conservan **exactamente el mismo texto**.
- Se creó `HitoCompacto` (privado): misma línea de tiempo que `TarjetaHito`, pero con menos relleno, periodo y título en una sola línea (`Semestre VII · Seminario…`) y descripción en `bodySmall`.
- El `Column` raíz pasó a `spacedBy(10.dp)` y `vertical = 8.dp` (antes 12 dp).

**Por qué:** pedido de que todo quede en pantalla. `TarjetaHito` (compartido con `PantallaCali` y `PantallaLatino`) **no se modificó** para no afectar esas pantallas.

### 1.4 Botón al reglamento
Se agregó al final un `Button` «Ver reglamento completo» que ejecuta el nuevo parámetro `onVerReglamento: () -> Unit = {}` de `PantallaTrabajoGrado`.

### 1.5 Contenido movido a la nueva pantalla
Las secciones **Plazos y ciclo, Cómo se evalúa, Sustentación, Director y jurados, Modalidades, Ética/autoría/estilo** se **cortaron** de este archivo y se pasaron, sin cambiar ni una palabra, a `PantallaReglamentoTrabajoGrado.kt`.

### 1.6 Observación final eliminada
Se borró el `Surface` con el texto *«Resumen informativo basado en la Resolución 60-2019… tienen la última palabra»*. No se reubicó en ninguna pantalla.

### 1.7 Visibilidad de componentes
`SeccionTrabajoGrado`, `Texto`, `Punto` y `Opciones` pasaron de `private` a `internal` para que la nueva pantalla los reutilice (así no se duplica código). `FilaRequisito` sigue `private`.

### 1.8 Imports
Se retiraron los que ya no se usan (`Category, Groups, Info, Schedule, VerifiedUser, Checklist, Slideshow, TarjetaHito`) y se agregaron los nuevos (`Button, Box, CircleShape, clip, IntrinsicSize, fillMaxHeight, background, AutoMirrored.ArrowForward`).

---

## 2. `PantallaReglamentoTrabajoGrado.kt` (nuevo)

`@Composable fun PantallaReglamentoTrabajoGrado(modifier)`: pantalla con scroll que contiene, en este orden y desplegables como antes, **Plazos y ciclo → Cómo se evalúa → Sustentación → Director y jurados → Modalidades → Ética, autoría y estilo**.
Lleva un encabezado mínimo (ícono + «Reglamento») para que el usuario sepa dónde está; es lo único que no estaba en el pedido y se agregó porque una pantalla sin título desorienta. Si prefieres quitarlo, borra el primer `Surface` del `Column`.

---

## 3. `MainActivity.kt` (3 puntos)

**a) Nuevo destino en el enum `PantallasApp`**
```kotlin
    PRACTICAS_INTRO(
        leyenda = "Prácticas profesionales",
        icono = Icons.Filled.Work
    ),

    REGLAMENTO_TRABAJO_GRADO(                       // ← NUEVO
        leyenda = "Reglamento de trabajo de grado",
        icono = Icons.Filled.Description
    )
```

**b) Se oculta de la lista de pantallas visibles** (igual que `PRACTICAS_INTRO`; es una subpantalla, no una pestaña)
```kotlin
pantalla != PantallasApp.PRACTICAS_INTRO &&
pantalla != PantallasApp.REGLAMENTO_TRABAJO_GRADO &&     // ← NUEVO
(pantalla != PantallasApp.GRADO || cumpleRequisitosGrado)
```

**c) Rama en el `when (destinoActual)`**
```kotlin
PantallasApp.TRABAJO_GRADO -> {
    PantallaTrabajoGrado(
        programa = viewModel.programa,
        onVerReglamento = {                                   // ← NUEVO
            destinoActual = PantallasApp.REGLAMENTO_TRABAJO_GRADO
        }
    )
}

PantallasApp.REGLAMENTO_TRABAJO_GRADO -> {                    // ← NUEVO
    PantallaReglamentoTrabajoGrado()
}
```
**Por qué:** el botón «Atrás» del sistema ya funciona con el historial existente (`historialPestanas`), así que al presionarlo desde el reglamento se vuelve a «Trabajo de grado» sin código extra.

---

## 4. Diagrama de clases
`diagrama_clases.png` (vista rápida), `.svg` (vectorial, para zoom) y `.mmd` (fuente Mermaid editable). Las novedades están marcadas con ★: el valor `REGLAMENTO_TRABAJO_GRADO`, la nueva pantalla, el parámetro `onVerReglamento`, `RequisitoInicial`, `HitoCompacto` y la relación de reutilización de componentes `internal`. El repositorio no traía diagrama previo, así que se generó a partir del código actual.

---

## 5. Notas y límites
- **No pude compilar** aquí (no hay Android SDK/Gradle en mi entorno). Revisé balance de llaves e imports a mano; al abrir en Android Studio, haz un *Sync* y *Build* por si algún import difiere en tu versión.
- **Ajuste a la pantalla:** el diseño es compacto, pero la altura útil depende del teléfono. En pantallas pequeñas o con fuente grande, la ruta podría pasar el borde; por eso se dejó `verticalScroll` como respaldo (el botón siempre queda alcanzable). Si quieres garantizarlo sin scroll en todos los equipos, habría que recortar los textos de los hitos, lo cual no hice por no alterar contenido.


---

# Versión 2 — Ajustes de estilo y botón de director

Archivos que cambian respecto a la versión 1: `PantallaTrabajoGrado.kt` y `PantallaReglamentoTrabajoGrado.kt`. `MainActivity.kt` **no cambia**.

## A. Letras negras
Se creó `colorTinta()` (en `PantallaTrabajoGrado.kt`, `internal`): devuelve `Color.Black` en tema claro y `onSurface` en tema oscuro (negro sobre fondo oscuro sería ilegible). Todos los textos e íconos de ambas pantallas lo usan: hero, requisito, hitos, títulos de sección, `Texto`, `Punto`, `Opciones` y `FilaRequisito`.

## B. Más pequeño y compacto
- Tres estilos compartidos: `TextoCuerpo` (12 sp), `TextoChico` (11 sp) y `TextoEtiqueta` (12 sp, negrita); reemplazan a `bodyMedium/bodySmall/labelLarge`.
- Márgenes de pantalla 18→14 dp; separación entre módulos 12→8 dp; hero con menos relleno y título 17 sp; íconos 30→22 dp; tarjetas con esquinas de 16 dp y relleno de 12 dp; botón del reglamento de 52→44 dp.
- Aplica a las dos pantallas (las secciones del reglamento usan los mismos componentes).

## C. Lenguaje económico (solo en la pantalla principal)
- Requisito: «Te falta aprobar:» / «Te faltan por aprobar:» (el mensaje de cumplimiento se mantiene exacto).
- Los 4 hitos se redujeron a una frase corta cada uno, conservando la idea de cada etapa.
- **No recorté el texto del reglamento** (plazos, evaluación, sustentación, etc.) porque es contenido normativo y quitar palabras puede cambiar su sentido. Si quieres, lo condenso sección por sección.

## D. Botón «Elegir director de TG y línea de investigación»
En el hero, a la derecha del título. Botón con borde, texto negro de 11 sp y ancho máximo de 150 dp. Como no se definió a qué pantalla lleva, abre un diálogo breve (`AlertDialog`, estado `mostrarDirector`) con 3 pasos tomados del reglamento: elegir línea, acordar director, oficialización. Para cambiar el destino (otra pantalla, enlace, formulario), edita `onClick = { mostrarDirector = true }` del botón.

## E. Diagrama
`diagrama_clases.*` actualizado con lo marcado «★ v2».
