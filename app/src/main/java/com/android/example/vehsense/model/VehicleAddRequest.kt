package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class VehicleAddRequest(
    val brand: String,
    val model: String,
    val year: Int,
    @SerializedName("engine_capacity") val engineCapacity: Int,
    @SerializedName("engine_power") val enginePower: Int,
    val plates: String?,
    @SerializedName("expected_fuel") val expectedFuel: Double
)
