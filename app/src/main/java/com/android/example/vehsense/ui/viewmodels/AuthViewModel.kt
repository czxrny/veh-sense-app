package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.BuildConfig
import com.android.example.vehsense.model.AuthResponse
import com.android.example.vehsense.network.BackendCommunicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _currentSession = MutableStateFlow<AuthResponse?>(null)
    val currentSession: StateFlow<AuthResponse?> = _currentSession

    private val communicator = BackendCommunicator()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = communicator.login(email, password)
                response.onSuccess { loginResponse ->
                    _errorMessage.value = null
                    _currentSession.value = loginResponse
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
                    _errorMessage.value = null
                    _currentSession.value = signUpResponse
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }
}
