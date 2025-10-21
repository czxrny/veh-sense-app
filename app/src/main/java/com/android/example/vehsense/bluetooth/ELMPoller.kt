package com.android.example.vehsense.bluetooth

import android.bluetooth.BluetoothSocket
import com.android.example.vehsense.model.ObdFrame
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class ELMPoller(
    private val onFrameUpdate: (ObdFrame) -> Unit,
    private val socket: BluetoothSocket
): ELMCommander(socket) {

    suspend fun pollDevice() = coroutineScope {
        while (isActive && socket.isConnected) {
            val obdValues = mutableMapOf<String, Int>()
            for (command in ObdCommand.entries) {
                super.sendCommand(command.code)
                val rawResponse = readResponse()
                obdValues[command.name] = command.parse(rawResponse)
                delay(200)
            }
            onFrameUpdate(ObdFrame(obdValues))
        }
    }
}