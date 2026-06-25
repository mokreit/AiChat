package com.aichat.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val dir = System.getProperty("java.io.tmpdir")
            "$dir/aichat_settings.preferences_pb".toPath()
        },
    )
}
