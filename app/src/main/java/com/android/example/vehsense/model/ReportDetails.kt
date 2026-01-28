package com.android.example.vehsense.model

import com.android.example.vehsense.local.ObdFrameEntity

data class ReportDetails(
    val report: Report,
    val rideEvents: List<RideEvent>,
    val obdFrames: List<ObdFrameEntity>,
    val vehicle: Vehicle
)
