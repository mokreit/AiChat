package com.aichat.platform

import androidx.compose.ui.graphics.ImageBitmap

/** Load an image from a file path, returns null if failed */
expect fun loadImageFromFile(path: String): ImageBitmap?

/**
 * Save an ImageBitmap to a file in PNG format.
 * Returns the saved file path, or null if failed.
 */
expect fun saveImageBitmap(bitmap: ImageBitmap, filename: String): String?

/**
 * Crop an ImageBitmap to the specified rectangle.
 */
expect fun cropImageBitmap(source: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap?
