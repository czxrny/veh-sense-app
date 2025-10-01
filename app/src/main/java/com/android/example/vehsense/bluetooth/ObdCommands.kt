package com.android.example.vehsense.bluetooth

interface ObdOperation {
    val code: String
    fun parse(response: String): Int
}

enum class ObdCommand(override val code: String) : ObdOperation {
    RPM("010C") {
        override fun parse(response: String): Int {
            return try {
                val parts = response.trim().split(" ")
                if (parts.size >= 4) {
                    val a = parts[2].toInt(16)
                    val b = parts[3].toInt(16)
                    (a * 256 + b) / 4
                } else 0
            } catch (e: Exception) {
                0
            }
        }
    },
    ENGINE_LOAD("0104") {
        override fun parse(response: String): Int {
            return try {
                val parts = response.trim().split(" ")
                if (parts.size >= 3) {
                    val a = parts[2].toInt(16)
                    a * 100 / 255
                } else 0
            } catch (e: Exception) {
                0
            }
        }
    },
    VEHICLE_SPEED("010D") {
        override fun parse(response: String): Int {
            return try {
                val parts = response.trim().split(" ")
                if (parts.size >= 3) {
                    parts[2].toInt(16)
                } else 0
            } catch (e: Exception) {
                0
            }
        }
    }
}


/// Used for initial configuration, run all one-by-one at once.
enum class ObdConfig(val command: String) {
    RESET("ATZ"),
    ECHO_OFF("ATE0"),
    LINEFEED_OFF("ATL0"),
}