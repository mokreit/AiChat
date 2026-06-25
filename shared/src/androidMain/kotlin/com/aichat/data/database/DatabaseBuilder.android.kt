package com.aichat.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = applicationContext ?: error("Application context not initialized")
    val dbFile = appContext.getDatabasePath("aichat.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
}

internal var applicationContext: Context? = null

fun initDatabase(context: Context) {
    applicationContext = context.applicationContext
}
