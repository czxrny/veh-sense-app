package com.android.example.vehsense.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class UserSession(
    val userId: String,
    val token: String
)

class UserStorage(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSession(userId: Int, token: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId.toString())
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun getSession(): UserSession? {
        val id = prefs.getString(KEY_USER_ID, null)
        val token = prefs.getString(KEY_TOKEN, null)

        return if (id != null && token != null) {
            UserSession(id, token)
        } else {
            null
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getSession() != null
}
