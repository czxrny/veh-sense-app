package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class UploadRideRequest(
    @SerializedName("vehicle_id")
    val vehicleId: Int,

    val data: String
)
