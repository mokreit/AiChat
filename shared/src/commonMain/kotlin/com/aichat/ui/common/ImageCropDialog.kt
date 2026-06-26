package com.aichat.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aichat.design.strings
import com.aichat.platform.cropImageBitmap
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ImageCropDialog(
    bitmap: ImageBitmap,
    aspectRatio: Float? = null,
    circularPreview: Boolean = false,
    onConfirm: (ImageBitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    if (circularPreview) {
        CircularCropDialog(bitmap = bitmap, onConfirm = onConfirm, onDismiss = onDismiss)
    } else {
        RectCropDialog(bitmap = bitmap, onConfirm = onConfirm, onDismiss = onDismiss)
    }
}

// ==================== Circular crop (avatar) ====================

@Composable
private fun CircularCropDialog(
    bitmap: ImageBitmap,
    onConfirm: (ImageBitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = strings()
    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()
    var zoom by remember { mutableStateOf(100f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val cropSizeDp = 280.dp

    // Store actual canvas pixel size for crop calculation
    var canvasPixelSize by remember { mutableStateOf(0f) }
    var previewPixelSize by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.cropImage) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(cropSizeDp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .clipToBounds()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    if (event.type == PointerEventType.Scroll) {
                                        val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                                        if (scrollDelta != null) {
                                            zoom = (zoom - scrollDelta.y * 5f).coerceIn(30f, 300f)
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                var lastDist = 0f
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val pressed = event.changes.filter { it.pressed }
                                    if (pressed.size >= 2) {
                                        val p0 = pressed[0].position
                                        val p1 = pressed[1].position
                                        val dist = (p1 - p0).getDistance()
                                        if (lastDist > 0f) {
                                            zoom = (zoom + (dist - lastDist) * 0.3f).coerceIn(30f, 300f)
                                        }
                                        lastDist = dist
                                        pressed.forEach { it.consume() }
                                    } else {
                                        lastDist = 0f
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        },
                ) {
                    // Image layer
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        canvasPixelSize = size.width
                        val scale = zoom / 100f
                        val fitScale = min(size.width / imgW, size.height / imgH) * scale
                        val drawW = imgW * fitScale
                        val drawH = imgH * fitScale
                        val x = (size.width - drawW) / 2f + offsetX
                        val y = (size.height - drawH) / 2f + offsetY
                        drawImage(image = bitmap, dstOffset = IntOffset(x.roundToInt(), y.roundToInt()), dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()))
                    }
                    // Mask layer
                    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
                        drawRect(Color.Black.copy(alpha = 0.4f))
                        drawCircle(Color.Transparent, radius = size.minDimension / 2, center = Offset(size.width / 2, size.height / 2), blendMode = BlendMode.Clear)
                        drawCircle(Color.White, radius = size.minDimension / 2, center = Offset(size.width / 2, size.height / 2), style = Stroke(2f))
                        val r = size.minDimension / 2; val cx = size.width / 2; val cy = size.height / 2
                        drawLine(Color.White.copy(0.3f), Offset(cx - r, cy), Offset(cx + r, cy))
                        drawLine(Color.White.copy(0.3f), Offset(cx, cy - r), Offset(cx, cy + r))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.zoomOut, style = MaterialTheme.typography.bodySmall)
                    Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 30f..300f, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color.Black, activeTrackColor = Color.Black))
                    Text(s.zoomIn, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(s.preview, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFF0F0F0)).clipToBounds()) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        previewPixelSize = size.width
                        val scale = zoom / 100f
                        val fitScale = min(size.width / imgW, size.height / imgH) * scale
                        val drawW = imgW * fitScale; val drawH = imgH * fitScale
                        // Scale offset from main canvas to preview canvas
                        val offScale = if (canvasPixelSize > 0f) size.width / canvasPixelSize else 1f
                        val x = (size.width - drawW) / 2f + offsetX * offScale
                        val y = (size.height - drawH) / 2f + offsetY * offScale
                        drawImage(image = bitmap, dstOffset = IntOffset(x.roundToInt(), y.roundToInt()), dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Use actual canvas pixel size, not dp value
                val ref = canvasPixelSize
                if (ref <= 0f) return@TextButton
                val scale = zoom / 100f
                val fitScale = min(ref / imgW, ref / imgH) * scale
                val drawW = imgW * fitScale; val drawH = imgH * fitScale
                // Image top-left in canvas coordinates
                val imgX = (ref - drawW) / 2f + offsetX
                val imgY = (ref - drawH) / 2f + offsetY
                // Crop region = canvas center circle radius = ref/2
                val cropRadius = ref / 2f
                val cropCenter = ref / 2f
                // Map crop region back to bitmap coordinates
                val srcX = ((cropCenter - cropRadius - imgX) / drawW * imgW).roundToInt().coerceIn(0, bitmap.width - 1)
                val srcY = ((cropCenter - cropRadius - imgY) / drawH * imgH).roundToInt().coerceIn(0, bitmap.height - 1)
                val srcW = ((cropRadius * 2) / drawW * imgW).roundToInt().coerceIn(1, bitmap.width - srcX)
                val srcH = ((cropRadius * 2) / drawH * imgH).roundToInt().coerceIn(1, bitmap.height - srcY)
                val cropped = cropImageBitmap(bitmap, srcX, srcY, srcW, srcH)
                if (cropped != null) onConfirm(cropped)
            }) { Text(s.confirm) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

// ==================== Rectangular crop (background) ====================

@Composable
private fun RectCropDialog(
    bitmap: ImageBitmap,
    onConfirm: (ImageBitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = strings()
    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()
    val canvasW = 320f
    val canvasH = canvasW * (imgH / imgW)

    var cropLeft by remember { mutableStateOf(0.05f) }
    var cropTop by remember { mutableStateOf(0.05f) }
    var cropRight by remember { mutableStateOf(0.95f) }
    var cropBottom by remember { mutableStateOf(0.95f) }

    var dragType by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.cropImage) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(canvasW.dp)
                        .height(canvasH.dp)
                        .background(Color(0xFFF0F0F0))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    if (event.type == PointerEventType.Scroll) {
                                        val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                                        if (scrollDelta != null) {
                                            val delta = -scrollDelta.y * 0.01f
                                            val centerX = (cropLeft + cropRight) / 2f
                                            val centerY = (cropTop + cropBottom) / 2f
                                            val halfW = (cropRight - cropLeft) / 2f + delta
                                            val halfH = (cropBottom - cropTop) / 2f + delta
                                            val minSize = 0.05f
                                            val maxHalfW = min(centerX, 1f - centerX)
                                            val maxHalfH = min(centerY, 1f - centerY)
                                            val hw = halfW.coerceIn(minSize / 2, maxHalfW)
                                            val hh = halfH.coerceIn(minSize / 2, maxHalfH)
                                            cropLeft = (centerX - hw).coerceIn(0f, 1f)
                                            cropRight = (centerX + hw).coerceIn(0f, 1f)
                                            cropTop = (centerY - hh).coerceIn(0f, 1f)
                                            cropBottom = (centerY + hh).coerceIn(0f, 1f)
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                var lastDist = 0f
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val pressed = event.changes.filter { it.pressed }
                                    if (pressed.size >= 2) {
                                        val p0 = pressed[0].position
                                        val p1 = pressed[1].position
                                        val dist = (p1 - p0).getDistance()
                                        if (lastDist > 0f) {
                                            val delta = (dist - lastDist) * 0.0004f
                                            val centerX = (cropLeft + cropRight) / 2f
                                            val centerY = (cropTop + cropBottom) / 2f
                                            val halfW = (cropRight - cropLeft) / 2f + delta
                                            val halfH = (cropBottom - cropTop) / 2f + delta
                                            val minSize = 0.05f
                                            val maxHalfW = min(centerX, 1f - centerX)
                                            val maxHalfH = min(centerY, 1f - centerY)
                                            val hw = halfW.coerceIn(minSize / 2, maxHalfW)
                                            val hh = halfH.coerceIn(minSize / 2, maxHalfH)
                                            cropLeft = (centerX - hw).coerceIn(0f, 1f)
                                            cropRight = (centerX + hw).coerceIn(0f, 1f)
                                            cropTop = (centerY - hh).coerceIn(0f, 1f)
                                            cropBottom = (centerY + hh).coerceIn(0f, 1f)
                                        }
                                        lastDist = dist
                                        pressed.forEach { it.consume() }
                                    } else {
                                        lastDist = 0f
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val w = size.width; val h = size.height
                                    val lx = cropLeft * w; val ly = cropTop * h
                                    val rx = cropRight * w; val by = cropBottom * h
                                    val handle = 36f
                                    dragType = when {
                                        offset.x < lx + handle && offset.y < ly + handle -> "tl"
                                        offset.x > rx - handle && offset.y < ly + handle -> "tr"
                                        offset.x < lx + handle && offset.y > by - handle -> "bl"
                                        offset.x > rx - handle && offset.y > by - handle -> "br"
                                        offset.x in lx..rx && offset.y in ly..by -> "move"
                                        else -> null
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dx = dragAmount.x / size.width
                                    val dy = dragAmount.y / size.height
                                    val minSize = 0.05f

                                    when (dragType) {
                                        "tl" -> {
                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSize)
                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSize)
                                        }
                                        "tr" -> {
                                            cropRight = (cropRight + dx).coerceIn(cropLeft + minSize, 1f)
                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSize)
                                        }
                                        "bl" -> {
                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSize)
                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + minSize, 1f)
                                        }
                                        "br" -> {
                                            cropRight = (cropRight + dx).coerceIn(cropLeft + minSize, 1f)
                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + minSize, 1f)
                                        }
                                        "move" -> {
                                            val rw = cropRight - cropLeft; val rh = cropBottom - cropTop
                                            val nl = (cropLeft + dx).coerceIn(0f, 1f - rw)
                                            val nt = (cropTop + dy).coerceIn(0f, 1f - rh)
                                            cropLeft = nl; cropTop = nt; cropRight = nl + rw; cropBottom = nt + rh
                                        }
                                    }
                                },
                                onDragEnd = { dragType = null },
                            )
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawImage(image = bitmap, dstOffset = IntOffset(0, 0), dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
                    }
                    Canvas(
                        modifier = Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    ) {
                        val w = size.width; val h = size.height
                        val lx = cropLeft * w; val ly = cropTop * h
                        val rx = cropRight * w; val by = cropBottom * h

                        drawRect(Color.Black.copy(alpha = 0.5f))
                        drawRect(Color.Transparent, Offset(lx, ly), Size(rx - lx, by - ly), blendMode = BlendMode.Clear)
                        drawRect(Color.White, Offset(lx, ly), Size(rx - lx, by - ly), style = Stroke(2f))
                        val gw = rx - lx; val gh = by - ly
                        drawLine(Color.White.copy(0.3f), Offset(lx + gw / 3, ly), Offset(lx + gw / 3, by))
                        drawLine(Color.White.copy(0.3f), Offset(lx + gw * 2 / 3, ly), Offset(lx + gw * 2 / 3, by))
                        drawLine(Color.White.copy(0.3f), Offset(lx, ly + gh / 3), Offset(rx, ly + gh / 3))
                        drawLine(Color.White.copy(0.3f), Offset(lx, ly + gh * 2 / 3), Offset(rx, ly + gh * 2 / 3))
                        val hs = 10f
                        listOf(Offset(lx, ly), Offset(rx, ly), Offset(lx, by), Offset(rx, by)).forEach { c ->
                            drawRect(Color.White, Offset(c.x - hs / 2, c.y - hs / 2), Size(hs, hs))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Rect crop uses normalized coordinates, directly map to bitmap pixels - this is correct
                val srcX = (cropLeft * imgW).roundToInt().coerceIn(0, bitmap.width - 1)
                val srcY = (cropTop * imgH).roundToInt().coerceIn(0, bitmap.height - 1)
                val srcW = ((cropRight - cropLeft) * imgW).roundToInt().coerceIn(1, bitmap.width - srcX)
                val srcH = ((cropBottom - cropTop) * imgH).roundToInt().coerceIn(1, bitmap.height - srcY)
                val cropped = cropImageBitmap(bitmap, srcX, srcY, srcW, srcH)
                if (cropped != null) onConfirm(cropped)
            }) { Text(s.confirm) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}
