package com.android.example.vehsense.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*

data class ObdFrame(
    var rpm: Int = 0,
)

fun mapToObdFrame(obdValues: Map<String, Int>): ObdFrame {
    val json = Json.encodeToString(obdValues)
    return Json.decodeFromString(json)
}