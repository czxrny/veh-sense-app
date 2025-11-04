package com.android.example.vehsense.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.android.example.vehsense.model.ObdFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ELMPoller(
    private val onFrameUpdate: (ObdFrame) -> Unit,
    private val socket: BluetoothSocket
): ELMCommander(socket) {
    suspend fun pollDevice() = coroutineScope {
        try {
            while (isActive && socket.isConnected) {
                val obdValues = mutableMapOf<String, Int>()
                for (command in ObdCommand.entries) {
                    val response = sendAndRead(command.code)
                    Log.d("OBD", "Resp: $response")
                    obdValues[command.name] = command.parse(response)
                }
                onFrameUpdate(ObdFrame(obdValues))
            }
        } catch (e: Exception) {
            throw e
        }
    }
}