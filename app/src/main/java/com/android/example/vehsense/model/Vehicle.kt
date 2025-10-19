package com.android.example.vehsense.model

data class Vehicle(
    val id: Int,
    val ownerId: Int?,
    val organizationId: Int?,
    val brand: String,
    val model: String,
    val year: Int,
    val engineCapacity: Int,
    val enginePower: Int,
    val plates: String?,
    val expectedFuel: Double
)

