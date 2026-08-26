package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/**
 * A zoomable image component that supports pinch-to-zoom, pan, double-tap zoom,
 * and smooth animated snap-back to normal scale on release (Instagram style).
 */
@Composable
fun ZoomableImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    maxScale: Float = 4.5f,
    minScale: Float = 1.0f,
    onTap: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var pan = Offset.Zero
                    var zoom = 1f
                    var pastTouchSlop = false

                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoom *= zoomChange
                                pan += panChange

                                if (zoom != 1f || pan != Offset.Zero) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                                scale = newScale

                                if (scale > 1f) {
                                    // Allow panning when zoomed in
                                    offset = Offset(
                                        x = offset.x + panChange.x,
                                        y = offset.y + panChange.y
                                    )
                                    // Consume events to prevent outer pagers from intercepting
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // When fingers are lifted, if zoomed in slightly or heavily, animate smoothly back to 1.0x
                    // or keep zoomed if user double tapped
                    if (scale > 1f) {
                        coroutineScope.launch {
                            val animScale = Animatable(scale)
                            val animOffset = Animatable(offset, Offset.VectorConverter)
                            launch {
                                animScale.animateTo(1f) {
                                    scale = this.value
                                }
                            }
                            launch {
                                animOffset.animateTo(Offset.Zero) {
                                    offset = this.value
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(onTap) {
                detectTapGestures(
                    onDoubleTap = { tapOffset: Offset ->
                        coroutineScope.launch {
                            if (scale > 1.2f) {
                                val animScale = Animatable(scale)
                                val animOffset = Animatable(offset, Offset.VectorConverter)
                                launch { animScale.animateTo(1f) { scale = this.value } }
                                launch { animOffset.animateTo(Offset.Zero) { offset = this.value } }
                            } else {
                                val targetScale = 2.5f
                                val animScale = Animatable(scale)
                                val animOffset = Animatable(offset, Offset.VectorConverter)
                                launch { animScale.animateTo(targetScale) { scale = this.value } }
                                launch {
                                    animOffset.animateTo(Offset(-tapOffset.x / 2f, -tapOffset.y / 2f)) {
                                        offset = this.value
                                    }
                                }
                            }
                        }
                    },
                    onTap = {
                        onTap?.invoke()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}
