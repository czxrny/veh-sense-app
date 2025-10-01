package com.android.example.vehsense.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*

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
