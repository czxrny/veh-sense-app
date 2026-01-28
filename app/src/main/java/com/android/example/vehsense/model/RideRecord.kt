package com.android.example.vehsense.model

import com.google.gson.annotations.SerializedName

data class RideRecord(
    @SerializedName("report_id")val reportId: Int,
    val data: String,
    @SerializedName("event_data")val eventData: String
)
