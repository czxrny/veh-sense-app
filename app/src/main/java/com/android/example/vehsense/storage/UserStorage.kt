package com.android.example.vehsense.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class UserStorage(context: Context) {
    private val PREFS_NAME = "secure_prefs"
    private val KEY_USER_ID = "user_id"
    private val KEY_REFRESH = "refresh_key"

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveRefreshKey(key: String) {
        prefs.edit().apply {
            putString(KEY_REFRESH, key)
            apply()
        }
    }

    fun getRefreshKey(): String? {
        val key = prefs.getString(KEY_REFRESH, null)
        return key
    }

    fun saveUserId(userId: Int) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId.toString())
            apply()
        }
    }

    fun getUserId(): Int? {
        return prefs.getString(KEY_USER_ID, null)?.toInt()
    }

    fun clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun clearRefreshKey() {
        prefs.edit().remove(KEY_REFRESH).apply()
    }

    fun wasPreviouslyLoggedIn(): Boolean = getRefreshKey() != null
}

