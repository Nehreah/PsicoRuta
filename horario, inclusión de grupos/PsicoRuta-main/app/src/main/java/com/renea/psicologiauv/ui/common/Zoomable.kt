package com.renea.psicologiauv.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

// ========================================================================
// Zoomable
// ========================================================================
// Envoltorio reutilizable que agrega zoom con gestos de pellizco
// (pinch-to-zoom) a cualquier contenido. Se usa UNA sola vez, envolviendo
// el contenido de PsicologiaUVApp en MainActivity.kt, para que el zoom
// funcione igual sin importar qué pantalla esté visible en ese momento
// (en vez de repetir esta lógica en cada pantalla por separado).
//
// Comportamiento:
//   - Pellizcar con dos dedos acerca o aleja el contenido (entre 1x y
//     4x), manteniendo fijo el punto que queda entre los dedos.
//   - Mientras el contenido está acercado (escala > 1x), mover UN solo
//     dedo también desplaza el contenido (igual que un visor de fotos
//     acercado), para poder recorrerlo cómodamente con una sola mano.
//   - Cuando el contenido está en su tamaño original (sin zoom), un solo
//     dedo NO se intercepta: el deslizar de un dedo sigue siendo el que ya
//     usan el HorizontalPager (para cambiar de pestaña) y el scroll de
//     listas de cada pantalla, exactamente como antes.
//   - Si el contenido está acercado (escala > 1x) y el usuario presiona
//     "atrás" (botón o gesto del sistema), en vez de salir de la pantalla
//     actual, primero se restaura el tamaño original; "atrás" solo vuelve
//     a comportarse como siempre una vez ya está en su tamaño normal.

private const val ESCALA_MINIMA = 1f
private const val ESCALA_MAXIMA = 4f

/**
 * Envuelve [content] agregándole zoom por gestos de pellizco, sin importar
 * qué pantalla sea. Ver comentario de cabecera de este archivo.
 */
@Composable
fun ZoomableAnyScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val escala = remember { Animatable(ESCALA_MINIMA) }
    val traslacionX = remember { Animatable(0f) }
    val traslacionY = remember { Animatable(0f) }
    val alcance = rememberCoroutineScope()

    var tamanoContenedor by remember {
        mutableStateOf(IntSize.Zero)
    }

    // "Atrás" mientras hay zoom -> restaura el tamaño original en vez de
    // navegar. Solo queda habilitado cuando realmente hay zoom aplicado.
    BackHandler(
        enabled = escala.value > ESCALA_MINIMA
    ) {
        alcance.launch { escala.animateTo(ESCALA_MINIMA) }
        alcance.launch { traslacionX.animateTo(0f) }
        alcance.launch { traslacionY.animateTo(0f) }
    }

    Box(
        modifier = modifier
            .onSizeChanged { tamanoContenedor = it }
            .pointerInput(Unit) {
                detectarPellizcoYArrastre(
                    hayZoomAplicado = { escala.value > ESCALA_MINIMA }
                ) { centroide, pan, cambioZoom ->

                    val ancho = tamanoContenedor.width.toFloat()
                    val alto = tamanoContenedor.height.toFloat()

                    val escalaAnterior = escala.value
                    val nuevaEscala =
                        (escalaAnterior * cambioZoom)
                            .coerceIn(ESCALA_MINIMA, ESCALA_MAXIMA)

                    // Mantiene fijo en pantalla el punto bajo los dedos:
                    // se traslada según el arrastre (pan) y se corrige
                    // por el cambio de escala respecto al centroide.
                    val traXSinLimite =
                        traslacionX.value + pan.x +
                                (escalaAnterior - nuevaEscala) * centroide.x

                    val traYSinLimite =
                        traslacionY.value + pan.y +
                                (escalaAnterior - nuevaEscala) * centroide.y

                    // El contenido nunca debe dejar huecos: se limita para
                    // que siempre cubra por completo el contenedor.
                    val minTraX = ancho * (1f - nuevaEscala)
                    val minTraY = alto * (1f - nuevaEscala)

                    alcance.launch { escala.snapTo(nuevaEscala) }
                    alcance.launch {
                        traslacionX.snapTo(traXSinLimite.coerceIn(minTraX, 0f))
                    }
                    alcance.launch {
                        traslacionY.snapTo(traYSinLimite.coerceIn(minTraY, 0f))
                    }
                }
            }
            .graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
                translationX = traslacionX.value
                translationY = traslacionY.value
                transformOrigin = TransformOrigin(0f, 0f)
            }
    ) {
        content()
    }
}

/**
 * Variante de detectTransformGestures que:
 *   - Con DOS O MÁS dedos: siempre actúa como pellizco (zoom + arrastre).
 *   - Con UN solo dedo: solo actúa (arrastre, sin cambiar el zoom) si
 *     [hayZoomAplicado] indica que el contenido ya está acercado; si está
 *     en su tamaño original, el dedo se deja pasar sin consumir, para no
 *     interferir con el HorizontalPager ni con el scroll de listas.
 *
 * Se procesa en [PointerEventPass.Initial] (antes que los hijos, en vez
 * de después) para que, mientras haya zoom aplicado, el arrastre de un
 * dedo quede consumido aquí ANTES de que el HorizontalPager alcance a
 * interpretarlo como un cambio de pestaña.
 */
private suspend fun PointerInputScope.detectarPellizcoYArrastre(
    hayZoomAplicado: () -> Boolean,
    onGesto: (centroide: Offset, pan: Offset, cambioZoom: Float) -> Unit
) {
    awaitEachGesture {
        awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Initial
        )

        do {
            val evento = awaitPointerEvent(PointerEventPass.Initial)
            val dedosActivos = evento.changes.filter { it.pressed }

            when {

                // Dos o más dedos -> pellizco: zoom + arrastre juntos.
                dedosActivos.size >= 2 -> {
                    val cambioZoom = evento.calculateZoom()
                    val pan = evento.calculatePan()

                    if (cambioZoom != 1f || pan != Offset.Zero) {
                        val centroide = evento.calculateCentroid(useCurrent = false)

                        onGesto(centroide, pan, cambioZoom)

                        evento.changes.forEach {
                            if (it.positionChanged()) it.consume()
                        }
                    }
                }

                // Un solo dedo, pero ya hay zoom aplicado -> ese dedo
                // desplaza el contenido acercado (sin cambiar la escala).
                dedosActivos.size == 1 && hayZoomAplicado() -> {
                    val pan = evento.calculatePan()

                    if (pan != Offset.Zero) {
                        onGesto(Offset.Zero, pan, 1f)

                        evento.changes.forEach {
                            if (it.positionChanged()) it.consume()
                        }
                    }
                }

                // Un solo dedo y sin zoom -> se deja pasar tal cual,
                // para que la navegación normal siga funcionando.
            }
        } while (evento.changes.any { it.pressed })
    }
}