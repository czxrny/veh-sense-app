package com.android.example.vehsense.network

import android.util.Log
import com.android.example.vehsense.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BackendCommunicator {
    private val client = OkHttpClient()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("${baseUrl}auth/login")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(Exception("Login error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(username: String, email: String, password: String): Result<String> {
        return try {
            val json = JSONObject().apply {
                put("user_name", username)
                put("email", email)
                put("password", password)
            }

            val body = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("${baseUrl}auth/signup")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(Exception("Signup error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun test() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$baseUrl/ping")
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