# Manual de cambios — Actualizaciones dentro de la app (Google Play, modo flexible)

Proyecto: **PsicoRuta / PsicologiaUV** (Kotlin + Jetpack Compose)

## Qué se pidió
Que la app detecte si hay una versión más reciente en Google Play y la ofrezca al usuario.
Se implementó el modo **flexible**: Play descarga la actualización en segundo plano y la app muestra un aviso
«Actualización lista para instalar · Reiniciar». El usuario sigue usando la app mientras tanto.

## Archivos entregados

| Archivo | Estado | Ruta |
|---|---|---|
| `libs.versions.toml` | Modificado (2 líneas) | `gradle/` |
| `build.gradle.kts` | Modificado (1 línea) | `app/` |
| `MainActivity.kt` | Modificado | `app/src/main/java/com/renea/psicologiauv/gui/` |
| `diagrama_clases.mmd` | Actualizado | `docs/cambios_actualizaciones/` |

El resto del proyecto no se tocó. Si ya aplicaste el paquete anterior (horario), no hay conflicto: son otros archivos.

---

## 1. `gradle/libs.versions.toml`

En `[versions]`, al final de la sección, agrega:
```toml
playAppUpdate = "2.1.0"
```
En `[libraries]`, al final de la sección, agrega:
```toml
play-app-update = { group = "com.google.android.play", name = "app-update", version.ref = "playAppUpdate" }
```
**Por qué:** declara la librería de actualizaciones in-app de Play (`com.google.android.play:app-update`).
Se usa la versión sin `-ktx` para no depender de extensiones de corrutinas.

## 2. `app/build.gradle.kts`

En `dependencies { ... }`, debajo de `implementation(libs.pdfbox.android)`:
```kotlin
implementation(libs.play.app.update)
```
**Por qué:** el catálogo convierte `play-app-update` en `libs.play.app.update`. No requiere permisos nuevos en el manifiesto.

## 3. `gui/MainActivity.kt`

### 3.1 Imports nuevos
```kotlin
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
```

### 3.2 Dentro de `class MainActivity`, antes de `onCreate`
Se agregaron:
- `appUpdateManager`: el gestor de Play.
- `actualizacionDescargada` (`mutableStateOf`): hace que la UI muestre el aviso cuando termina la descarga.
- `lanzadorActualizacion`: recibe el resultado del diálogo de Play (no necesita hacer nada: si el usuario rechaza, la app sigue normal).
- `oyenteInstalacion`: marca `actualizacionDescargada = true` cuando el estado pasa a `DOWNLOADED`.
- `buscarActualizacion()`: consulta a Play. Si ya hay una descarga lista, lo marca; si hay versión nueva y el modo
  flexible está permitido, lanza el flujo `AppUpdateType.FLEXIBLE`. Si falla (por ejemplo, app instalada desde Android Studio),
  solo escribe un `Log.w` y no afecta nada.
- `onResume()` llama a `buscarActualizacion()`; `onDestroy()` quita el oyente.
- `companion object { private var ofertaActualizacionMostrada }`: la oferta se muestra una sola vez por sesión
  (no se repite al rotar la pantalla). El estado "descargada" sí se vuelve a detectar en cada `onResume`.

### 3.3 Dentro de `onCreate`
Justo después de `enableEdgeToEdge()`:
```kotlin
appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
appUpdateManager.registerListener(oyenteInstalacion)
```

### 3.4 En `setContent`
El `Surface(...)` existente se envolvió en un `Box(modifier = Modifier.fillMaxSize())` (su contenido no cambió, solo se
indentó) y, debajo del `Surface`, dentro del mismo `Box`:
```kotlin
if (actualizacionDescargada) {
    AvisoActualizacionLista(
        onReiniciar = { appUpdateManager.completeUpdate() },
        modifier = Modifier.align(Alignment.BottomCenter)
    )
}
```
**Por qué:** el aviso flota sobre cualquier pantalla (incluido el aviso inicial) sin tocar ninguna otra pantalla.

### 3.5 Composable nuevo `AvisoActualizacionLista`
Se agregó, antes de `AvisoInicialPsicoRuta`: una barra inferior con los colores `inverseSurface` del tema
(sirve en modo claro y oscuro), el texto «Actualización lista para instalar» y el botón **Reiniciar**,
que llama a `completeUpdate()` (Play instala y reinicia la app).

---

## Cómo probarlo
1. Sube a Play Console una versión con `versionCode` **mayor** que la instalada. Ahora es `7`; en cada lanzamiento
   debes subirlo (8, 9, ...). Sin eso Play no detecta nada.
2. Instala la versión anterior **desde Play** (pista de prueba interna sirve) y publica la nueva en esa misma pista.
3. Abre la app: aparece el diálogo de Play; al terminar la descarga sale el aviso «Reiniciar».
4. Para pruebas rápidas puedes usar *Compartir con prueba interna de aplicaciones* en Play Console.

## Observaciones
- Solo funciona con la app instalada desde Google Play. Desde Android Studio o un APK suelto no hace nada.
- Play puede tardar horas en ofrecer la actualización después de publicarla.
- Si más adelante quieres forzar una actualización crítica, el modo inmediato usa `AppUpdateType.IMMEDIATE`
  en lugar de `FLEXIBLE` (bloquea la app hasta actualizar).
- **Verificación:** en este entorno no hay Kotlin ni Gradle ni acceso a internet, así que no se compiló ni se probó contra Play.
  Sincroniza Gradle y compila en Android Studio antes de publicar.
- **Diagrama:** se entrega el `.mmd`; no pude renderizar PNG/SVG aquí (sin Chrome). Puedes generarlos en https://mermaid.live
  o con `mmdc -i diagrama_clases.mmd -o diagrama_clases.png`.
