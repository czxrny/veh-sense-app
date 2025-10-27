package com.android.example.vehsense.model

data class NewVehicleForm(
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val engineCapacity: String = "",
    val enginePower: String = "",
    val plates: String = "",
    val expectedFuel: String = ""
)
