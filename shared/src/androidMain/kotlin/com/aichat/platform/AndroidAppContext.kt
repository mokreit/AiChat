package com.aichat.platform

import android.content.Context

/**
 * Centralized Android application context and activity helpers
 * for platform implementations (FilePicker, ImageLoader, etc.)
 */
object AndroidAppContext {
    private var _context: Context? = null

    val context: Context
        get() = _context ?: error("AndroidAppContext not initialized. Call init() in Activity.onCreate.")

    fun init(context: Context) {
        _context = context.applicationContext
    }
}

/**
 * Activity bridge for file picking on Android.
 * The Activity sets [launchImagePicker] in onCreate,
 * and the shared FilePicker invokes it.
 */
object AndroidActivityHelper {
    // Lambda that launches image picker (set by Activity)
    var launchImagePicker: (() -> Unit)? = null
    var pendingCallback: ((String?) -> Unit)? = null

    // Lambda that requests audio permission (set by Activity)
    var requestAudioPermission: ((callback: (Boolean) -> Unit) -> Unit)? = null
    var audioPermissionCallback: ((Boolean) -> Unit)? = null
}
