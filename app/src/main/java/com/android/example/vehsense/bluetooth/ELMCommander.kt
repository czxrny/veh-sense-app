package com.android.example.vehsense.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

open class ELMCommander(
    private val socket: BluetoothSocket,
) {
    private val input = socket.inputStream
    private val output = socket.outputStream

    // Run ELM327 'ATI' Command to ensure the device is correct
    suspend fun isELM(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendAndRead("ATI")
                Log.d("ELM", "ATI: $response")
                return@withContext response.contains("ELM", ignoreCase = true)
            } catch (e: IOException) {
                Log.e("ELM", "Error checking ELM327", e)
                throw e
            }
        }
    }

    // Run all of the config commands located in ./bluetooth/ObdCommands.kt
    suspend fun runConfig() {
        return withContext(Dispatchers.IO) {
            Log.d("ELM", "Running the ELM327 Config...")
            try {
                for (cfg in ObdConfig.entries) {
                    sendAndRead(cfg.command)
                }
            } catch (e: IOException) {
                Log.e("ELM", "Error during configuration", e)
                throw e
            }
        }
    }

    // Reset the device
    suspend fun reset() {
        return withContext(Dispatchers.IO) {
            try {
                sendAndRead(ObdConfig.RESET.command)
                Log.d("ELM", "Device resetted")
                return@withContext
            } catch (e: IOException) {
                Log.e("ELM", "Error while resetting the device", e)
                throw e
            }
        }
    }

    protected suspend fun sendAndRead(cmd: String, timeoutMs: Long = 3000): String {
        sendCommand(cmd)
        return readUntilPrompt(timeoutMs)
    }

    private suspend fun sendCommand(cmd: String) = withContext(Dispatchers.IO) {
        try {
            val data = (cmd + "\r").toByteArray()
            output.write(data)
            output.flush()
            Log.d("ELM", "$cmd Command Sent")
        } catch (e: IOException) {
            Log.e("ELM", "Send error", e)
            throw e
        }
    }

    // Each of the ELM responses ends with '>' sign
    // This function reads all of the data in the Socket stream until reading the '>' sign...
    private suspend fun readUntilPrompt(timeoutMs: Long): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val buffer = ByteArray(256)

        try {
            withTimeout(timeoutMs) {
                while (isActive && socket.isConnected) {
                    val available = input.available()
                    if (available > 0) {
                        val bytesRead = input.read(buffer, 0, minOf(available, buffer.size))
                        if (bytesRead > 0) {
                            val chunk = String(buffer, 0, bytesRead)
                            sb.append(chunk)
                            if (sb.contains(">")) break
                        }
                    } else {
                        delay(10)
                    }
                }
            }
        } catch (e: CancellationException) {
            Log.d("ELM", "Reading stopped")
            throw e
        } catch (e: Exception) {
            Log.e("ELM", "Read timeout/error", e)
            throw IOException("ELM read timeout", e)
        }

        val raw = sb.toString()
        Log.d("ELM", "<< $raw")

        return@withContext raw
            .replace("\r", "")
            .replace("\n", "")
            .trim()
            .removeSuffix(">")
    }
}
