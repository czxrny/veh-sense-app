package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val localId: Int,
    @SerializedName("refresh_key") val refreshKey: String,
    val token: String,
)
