package com.aichat.data.database

import androidx.room.RoomDatabase

fun getRoomDatabase(): RoomDatabase.Builder<AppDatabase> {
    return getDatabaseBuilder()
}
