package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val id: Int,
    @SerializedName("user_name")val userName: String,
    @SerializedName("organization_id")val organizationId: Int?,
    @SerializedName("total_kilometers")val totalKilometers: Int,
    @SerializedName("number_of_rides")val numberOfRides: Int,
    val rating: String
)
