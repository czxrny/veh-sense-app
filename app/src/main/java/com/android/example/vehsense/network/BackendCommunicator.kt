package com.android.example.vehsense.network

import android.util.Base64
import android.util.Log
import com.android.example.vehsense.BuildConfig
import com.android.example.vehsense.local.ObdFrameEntity
import com.android.example.vehsense.model.AuthResponse
import com.android.example.vehsense.model.OrganizationInfo
import com.android.example.vehsense.model.Report
import com.android.example.vehsense.model.RideRecord
import com.android.example.vehsense.model.UploadRideRequest
import com.android.example.vehsense.model.UserInfo
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.model.VehicleAddRequest
import com.android.example.vehsense.model.VehicleUpdateRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class BackendCommunicator {
    private val client = OkHttpClient()
    private val baseUrl = BuildConfig.BACKEND_URL
    private val batchUrl = BuildConfig.BATCH_RECEIVER_URL

    suspend fun getFreshToken(userId: Int, key: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("user_id", userId)
                    put("refresh_key", key)
                }

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val apiKey = BuildConfig.API_KEY
                val request = Request.Builder()
                    .url("${baseUrl}/auth/refresh")
                    .addHeader("Authorization", "ApiKey $apiKey")
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

                val apiKey = BuildConfig.API_KEY
                val request = Request.Builder()
                    .url("${baseUrl}/auth/login")
                    .addHeader("Authorization", "ApiKey $apiKey")
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

                val apiKey = BuildConfig.API_KEY
                val request = Request.Builder()
                    .url("${baseUrl}/auth/signup")
                    .addHeader("Authorization", "ApiKey $apiKey")
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

    suspend fun getVehicles(token: String): Result<List<Vehicle>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/vehicles")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Vehicle Get error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: List<Vehicle> = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<List<Vehicle>>() {}.type
                        Gson().fromJson(bodyString, listType)
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

    suspend fun getVehicleById(token: String, id: Int): Result<Vehicle> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/vehicles/${id}")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Vehicle Get error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: Vehicle = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<Vehicle>() {}.type
                        Gson().fromJson(bodyString, listType)
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

    suspend fun addVehicle(vehicleAddRequest: VehicleAddRequest, token: String): Result<Vehicle> {
        return withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(vehicleAddRequest)

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${baseUrl}/vehicles")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Vehicle add error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed = try {
                        Gson().fromJson(bodyString, Vehicle::class.java)
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

    suspend fun updateVehicle(
        vehicleAddRequest: VehicleUpdateRequest,
        id: Int,
        token: String
    ): Result<Vehicle> {
        return withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(vehicleAddRequest)

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${baseUrl}/vehicles/${id}")
                    .addHeader("Authorization", "Bearer $token")
                    .patch(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Vehicle update error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed = try {
                        Gson().fromJson(bodyString, Vehicle::class.java)
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

    suspend fun deleteVehicle(vehicleId: Int, token: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/vehicles/$vehicleId")
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Delete error: ${response.code}"))
                    }

                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserInfo(token: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/me")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("User Info Get error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: UserInfo = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<UserInfo>() {}.type
                        Gson().fromJson(bodyString, listType)
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

    suspend fun getUserOrganizationInfo(token: String): Result<OrganizationInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/me/organization")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("User Info Get error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: OrganizationInfo = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<OrganizationInfo>() {}.type
                        Gson().fromJson(bodyString, listType)
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

    // Sending rideData using bytearray containing ObdFrameEntities
    data class UploadResponse(val success: Boolean)

    suspend fun sendRideData(
        vehicleId: Int,
        rideFrames: List<ObdFrameEntity>,
        token: String
    ): Result<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. To json
                val jsonBytes = Gson().toJson(rideFrames).toByteArray(Charsets.UTF_8)

                // 2. Gzip
                val byteArr = ByteArrayOutputStream()
                GZIPOutputStream(byteArr).use { it.write(jsonBytes) }
                val gzipBytes = byteArr.toByteArray()

                // 3. Base64
                val base64Gzip = Base64.encodeToString(gzipBytes, Base64.NO_WRAP)

                // 4. Body json
                val bodyObj = UploadRideRequest(
                    vehicleId = vehicleId,
                    data = base64Gzip
                )
                val jsonBody = Gson().toJson(bodyObj)
                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

                // 5. Request
                val request = Request.Builder()
                    .url("$batchUrl/upload")
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->

                    val bodyString = response.body?.string() ?: ""

                    Log.d(
                        "UPLOAD_RESPONSE",
                        "HTTP ${response.code} ${response.message}, body=${bodyString.take(2000)}"
                    )

                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception(
                                "Upload failed: HTTP ${response.code} ${response.message}, body=${bodyString.take(2000)}"
                            )
                        )
                    }

                    Result.success(UploadResponse(success = true))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getReports(token: String): Result<List<Report>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/reports")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()

                        Log.e(
                            "VEHSENSE_API",
                            "Error ${response.code} | ${response.message} | body=$errorBody"
                        )

                        return@withContext Result.failure(
                            Exception("Report Get error: ${response.code} | $errorBody")
                        )
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: List<Report> = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<List<Report>>() {}.type
                        Gson().fromJson(bodyString, listType)
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

    suspend fun getReportDataById(token: String, id: Int): Result<RideRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}/reports/${id}/data")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Ride record Get error: ${response.code}"))
                    }

                    val bodyString = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response body"))

                    val parsed: RideRecord = try {
                        val listType =
                            object : com.google.gson.reflect.TypeToken<RideRecord>() {}.type
                        Gson().fromJson(bodyString, listType)
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
}