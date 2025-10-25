package com.android.example.vehsense.core

import android.content.Context
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.storage.UserStorage

// Singleton containing all of the necessary objects for the backend communication part of the app
object AppContainer {
    lateinit var userStorage: UserStorage
    lateinit var backend: BackendCommunicator
    lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        userStorage = UserStorage(context)
        backend = BackendCommunicator()
        sessionManager = SessionManager(backend, userStorage)
    }
}