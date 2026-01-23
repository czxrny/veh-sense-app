package com.android.example.vehsense.model

// Read from RideRecord backend response
data class RideEvent(
    val timestamp: Long,
    val type: String,
    val value: Float
)
