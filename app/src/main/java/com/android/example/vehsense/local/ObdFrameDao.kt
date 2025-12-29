package com.android.example.vehsense.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ObdFrameDao {
    @Insert
    suspend fun insert(frame: ObdFrameEntity)

    @Insert
    suspend fun insertAll(frames: List<ObdFrameEntity>)

    @Query("SELECT * FROM obd_frames ORDER BY timestamp ASC")
    suspend fun getAll(): List<ObdFrameEntity>


    @Query("DELETE FROM obd_frames")
    suspend fun deleteAll()
}
