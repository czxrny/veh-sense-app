package com.android.example.vehsense.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ObdFrameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun obdFrameDao(): ObdFrameDao
}
