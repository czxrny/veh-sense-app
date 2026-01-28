package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class Report(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("organization_id") val organizationId: Int?,
    @SerializedName("vehicle_id") val vehicleId: Int,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("stop_time") val stopTime: Long,
    @SerializedName("acceleration_style") val accelerationStyle: String,
    @SerializedName("braking_style") val brakingStyle: String,
    @SerializedName("average_speed") val averageSpeed: Float,
    @SerializedName("max_speed") val maxSpeed: Float,
    @SerializedName("kilometers_travelled") val kilometersTravelled: Float,
)
