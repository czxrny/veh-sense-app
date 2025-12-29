package com.android.example.vehsense.model

import com.android.example.vehsense.local.ObdFrameEntity

data class ObdFrame(
    var rpm: Int = 0,
    var engineLoad: Int = 0,
    var vehicleSpeed: Int = 0,
) {
    constructor(obdValues: Map<String, Int>) : this(
        rpm = obdValues["RPM"] ?: 0,
        engineLoad = obdValues["ENGINE_LOAD"] ?: 0,
        vehicleSpeed = obdValues["VEHICLE_SPEED"] ?: 0,
    )
}

fun ObdFrame.toEntity(): ObdFrameEntity =
    ObdFrameEntity(
        timestamp = System.currentTimeMillis(),
        rpm = rpm,
        engineLoad = engineLoad,
        vehicleSpeed = vehicleSpeed
    )
