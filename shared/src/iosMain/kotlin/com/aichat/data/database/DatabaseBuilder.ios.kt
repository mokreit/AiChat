package com.aichat.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.PackagedSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbFilePath = (documentDirectory?.path ?: "") + "/aichat.db"
    return androidx.room.Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    ).setDriver(PackagedSQLiteDriver())
        .fallbackToDestructiveMigration(true)
}
