package com.android.example.vehsense.model

data class AuthResponse(
    val localId: Int,
    val refreshKey: String,
    val token: String,
)
