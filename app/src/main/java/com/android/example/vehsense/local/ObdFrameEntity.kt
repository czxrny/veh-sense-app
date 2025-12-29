package com.android.example.vehsense.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "obd_frames")
data class ObdFrameEntity(
    @PrimaryKey
    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("rpm")
    val rpm: Int,

    @SerializedName("engine_load")
    val engineLoad: Int,

    @SerializedName("vehicle_speed")
    val vehicleSpeed: Int,
)
