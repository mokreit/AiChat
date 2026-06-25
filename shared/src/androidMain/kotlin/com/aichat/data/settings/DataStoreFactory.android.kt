package com.aichat.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> {
    val context = applicationContext ?: error("Application context not initialized")
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.absolutePath.toPath().resolve("aichat_settings.preferences_pb") },
    )
}

internal var applicationContext: Context? = null

fun initSettings(context: Context) {
    applicationContext = context.applicationContext
}
