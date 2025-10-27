package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val sessionManager: SessionManager
): ViewModel() {
    private val _isSessionValid = MutableStateFlow<Boolean?>(null)
    val isSessionValid: StateFlow<Boolean?> = _isSessionValid

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val ok = sessionManager.loadSession()
            _isSessionValid.value = ok
        }
    }
}