package com.android.example.vehsense.network

import android.util.Log
import com.android.example.vehsense.BuildConfig
import com.android.example.vehsense.model.AuthResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BackendCommunicator {
    private val client = OkHttpClient()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun getFreshToken(userId: Int, key: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("user_id", userId)
                    put("refresh_key", key)
                }

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${baseUrl}/auth/refresh")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Refresh error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed = try {
                        Gson().fromJson(bodyString, AuthResponse::class.java)
                    } catch (e: Exception) {
                        return@withContext Result.failure(Exception("Failed to parse response: ${e.message}"))
                    }

                    Result.success(parsed)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${baseUrl}/auth/login")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Login error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed = try {
                        Gson().fromJson(bodyString, AuthResponse::class.java)
                    } catch (e: Exception) {
                        return@withContext Result.failure(Exception("Failed to parse response: ${e.message}"))
                    }

                    Result.success(parsed)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    suspend fun signup(username: String, email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("user_name", username)
                    put("email", email)
                    put("password", password)
                }

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${baseUrl}/auth/signup")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Signup error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed = try {
                        Gson().fromJson(bodyString, AuthResponse::class.java)
                    } catch (e: Exception) {
                        return@withContext Result.failure(Exception("Failed to parse response: ${e.message}"))
                    }

                    Result.success(parsed)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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