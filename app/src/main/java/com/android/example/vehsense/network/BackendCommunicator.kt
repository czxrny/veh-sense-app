package com.android.example.vehsense.network

import android.util.Log
import com.android.example.vehsense.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request

class BackendCommunicator {
    private val url = BuildConfig.BACKEND_URL

    fun test() {
        val client = OkHttpClient()
        Log.d("HTTP", "Success: $url")

        val request = Request.Builder()
            .url("$url/ping")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d("HTTP", "Success: ${response.body?.string()}")
                } else {
                    Log.d("HTTP", "Error: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}