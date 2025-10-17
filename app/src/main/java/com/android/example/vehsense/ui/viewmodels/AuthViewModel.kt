package com.android.example.vehsense.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.storage.UserStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application): AndroidViewModel(application) {
    data class Session (
        var userId: Int,
        var token: String
    )
    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private val communicator = BackendCommunicator()
    private val sessionManager = UserStorage(getApplication<Application>())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = communicator.login(email, password)
                response.onSuccess { loginResponse ->
                    sessionManager.saveSession(loginResponse.localId, loginResponse.refreshKey)
                    _errorMessage.value = null
                    _currentSession.value = Session(loginResponse.localId, loginResponse.refreshKey)
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = communicator.signup(name, email, password)
                response.onSuccess { signUpResponse ->
                    sessionManager.saveSession(signUpResponse.localId, signUpResponse.refreshKey)
                    _errorMessage.value = null
                    _currentSession.value = Session(signUpResponse.localId, signUpResponse.refreshKey)
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }
}
