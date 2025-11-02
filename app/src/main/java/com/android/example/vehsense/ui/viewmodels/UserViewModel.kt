package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.OrganizationInfo
import com.android.example.vehsense.model.UserInfo
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator = BackendCommunicator()
) : ViewModel() {
    sealed class UserState {
        data object Loading : UserState()
        data class Success(
            val user: UserInfo,
            val organization: OrganizationInfo? = null
        ) : UserState()
        data class Error(val message: String) : UserState()
    }
    val isPrivate: Boolean = requireNotNull(sessionManager.getPrivateStatus())

    private val _userInfo = MutableStateFlow<UserState>(UserState.Loading)
    val userInfo: StateFlow<UserState> = _userInfo

    fun loadUserInfo() {
        viewModelScope.launch {
            _userInfo.value = UserState.Loading
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _userInfo.value = UserState.Error("Could not authorize")
                    return@launch
                }

                val response = communicator.getUserInfo(token)
                response
                    .onSuccess { res ->
                        _userInfo.value = UserState.Success(res)
                        if (!isPrivate) {
                            loadOrganizationInfo()
                        }
                    }
                    .onFailure { e ->
                        _userInfo.value = UserState.Error(e.message ?: "Unknown error")
                    }

            } catch (e: Exception) {
                _userInfo.value = UserState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadOrganizationInfo() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _userInfo.value = UserState.Error("Could not authorize")
                    return@launch
                }

                val response = communicator.getUserOrganizationInfo(token)
                response
                    .onSuccess { res ->
                        val current = _userInfo.value
                        if (current is UserState.Success) {
                            _userInfo.value = current.copy(organization = res)
                        }
                    }
                    .onFailure { e ->
                        _userInfo.value = UserState.Error(e.message ?: "Unknown error")
                    }

            } catch (e: Exception) {
                _userInfo.value = UserState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
