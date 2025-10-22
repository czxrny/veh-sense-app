package com.android.example.vehsense.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

open class ELMCommander(
    socket: BluetoothSocket,
) {
    private var reader: BufferedReader = BufferedReader(InputStreamReader(socket.inputStream))
    private var writer: BufferedWriter = BufferedWriter(OutputStreamWriter(socket.outputStream))

    // Run ELM327 'ATI' Command to ensure the device is correct
    suspend fun isELM(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sendCommand("ATI")
                val response = readResponseUntilEmpty()
                Log.d("ELM", response)
                return@withContext response.contains("ELM", ignoreCase = true)
            } catch (e: IOException) {
                Log.e("ELM", "Error checking ELM327", e)
                throw e
            }
        }
    }

    suspend fun runConfig() {
        return withContext(Dispatchers.IO) {
            try {
                for (cfg in ObdConfig.entries) {
                    sendCommand(cfg.command)
                    delay(500)
                    val response = readResponseUntilEmpty()
                    Log.d("ELM", response)
                }
            } catch (e: IOException) {
                Log.e("ELM", "Error during configuration", e)
                throw e
            }
        }
    }

    @Throws(IOException::class)
    protected suspend fun sendCommand(cmd: String) {
        return withContext(Dispatchers.IO) {
            try {
                writer.apply {
                    write(cmd + "\r")
                    flush()
                }
            } catch (e: IOException) {
                Log.e("ELM", "Command send error", e)
                throw e
            }
        }
    }

    // Reading the buffer until the stream is empty
    @Throws(IOException::class)
    protected suspend fun readResponseUntilEmpty(): String {
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            try {
                while(true) {
                    delay(50)
                    if (reader.ready()) {
                        val line = reader.readLine()
                        if (line != "" && line != ">") {
                            sb.append(line)
                            if (line.contains(">")) break
                        } else {
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("ELM", "Read error", e)
                throw e
            }
            return@withContext sb.toString().trim().removeSuffix(">")
        }
    }

    @Throws(IOException::class)
    suspend fun readResponse(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        try {
            while(true) {
                if (reader.ready()) {
                    val line = reader.readLine()
                    if (line != null) {
                        sb.append(line)
                        if (line.contains(">")) break
                    }
                } else {
                    delay(50)
                }
            }
        } catch(e: IOException) {
            Log.e("ELM", "Read error", e)
            throw e
        }
        return@withContext sb.toString().trim().removeSuffix(">")
    }

}