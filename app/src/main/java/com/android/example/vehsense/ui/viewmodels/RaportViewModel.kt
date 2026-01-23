package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.Report
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator = BackendCommunicator(),
): ViewModel() {
    sealed class ReportState {
        data object Loading : ReportState()
        data class Success(
            val reports: List<Report>
        ) : ReportState()
        data class Error(val message: String) : ReportState()
    }

    private val _reportsInfo = MutableStateFlow<ReportState>(ReportState.Loading)
    val reportsInfo: StateFlow<ReportState> = _reportsInfo

    init {
        getReports()
    }

    fun getReports() {
        _reportsInfo.value = ReportState.Loading
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _reportsInfo.value = ReportState.Error("Could not authorize")
                    return@launch
                }

                val response = communicator.getReports(token)
                response.onSuccess { res ->
                    _reportsInfo.value = ReportState.Success(res)
                }.onFailure { e ->
                    _reportsInfo.value = ReportState.Error(e.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _reportsInfo.value = ReportState.Error(e.message ?: "Unknown error")
            }
        }
    }
}