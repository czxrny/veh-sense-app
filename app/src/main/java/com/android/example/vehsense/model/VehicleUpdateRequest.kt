package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class VehicleUpdateRequest(
    @SerializedName("engine_power") val enginePower: Int,
    val plates: String?,
    @SerializedName("expected_fuel") val expectedFuel: Double
)