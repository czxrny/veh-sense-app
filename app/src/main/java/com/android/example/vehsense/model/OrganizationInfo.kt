package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class OrganizationInfo(
    val id: Int,
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    @SerializedName("zip_code")val zipCode: String,
    @SerializedName("country_code")val countryCode: String,
    @SerializedName("contact_number")val contactNumber: String,
    val email: String,
)
