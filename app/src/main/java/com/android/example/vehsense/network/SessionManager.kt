package com.android.example.vehsense.network

import com.android.example.vehsense.storage.UserStorage
import android.util.Base64
import android.util.Log
import org.json.JSONObject

class SessionManager(
    private val backendCommunicator: BackendCommunicator,
    private val userStorage: UserStorage
) {
    private var cachedToken: String? = null

    private var userId: Int?
        get() = userStorage.getUserId()
        set(value) {
            if (value != null) userStorage.saveUserId(value)
            else userStorage.clearUserId()
        }

    private var refreshKey: String?
        get() = userStorage.getRefreshKey()
        set(value) {
            if (value != null) userStorage.saveRefreshKey(value)
            else userStorage.clearRefreshKey()
        }

    // Returns active token. Will return null if there is no refresh key + user id in storage and current token is null/expired
    suspend fun getToken(): String? {
        cachedToken?.let {
            if (!isExpired(it)) return it
        }

        if (userId == null || refreshKey == null) return null

        val ok = loadSession()
        return if (ok) {
            cachedToken
        } else {
            null
        }
    }

    // Returns TRUE if Session was loaded correctly.
    suspend fun loadSession(): Boolean {
        return try {
            val authResponse = backendCommunicator.getFreshToken(userId!!, refreshKey!!).getOrThrow()
            cachedToken = authResponse.token
            refreshKey = authResponse.refreshKey
            userId = authResponse.localId
            true
        } catch (e: Exception) {
            Log.d("SessionManager", "Error while refreshing token: $e")
            false
        }
    }

    fun saveSession(newToken: String, newRefreshKey: String, newUserId: Int) {
        cachedToken = newToken
        refreshKey = newRefreshKey
        userId = newUserId
    }

    fun logout() {
        cachedToken = null
        refreshKey = null
        userId = null
    }

    private fun isExpired(token: String): Boolean {
        val parts = token.split(".")
        if (parts.size != 3) return true
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedString = String(decodedBytes)
        val json = JSONObject(decodedString)
        val exp = json.optLong("exp", 0)
        val currentTime = System.currentTimeMillis() / 1000
        return exp <= currentTime
    }
}
