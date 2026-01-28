package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.local.ObdFrameEntity
import com.android.example.vehsense.model.Report
import com.android.example.vehsense.model.ReportDetails
import com.android.example.vehsense.model.RideEvent
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.utils.decodeBase64Gzip
import com.android.example.vehsense.utils.fromJson
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator = BackendCommunicator(),
): ViewModel() {
    sealed class ReportState {
        data object Loading : ReportState()
        data class Success(val reports: List<Report>) : ReportState()
        data class Error(val message: String) : ReportState()
    }

    sealed class ReportDetailsState  {
        data object Idle : ReportDetailsState ()
        data object Loading : ReportDetailsState ()

        data class Success(
            val reportDetails: ReportDetails
        ) : ReportDetailsState ()

        data class Error(val message: String) : ReportDetailsState ()
    }

    private val _reportsInfo = MutableStateFlow<ReportState>(ReportState.Loading)
    val reportsInfo: StateFlow<ReportState> = _reportsInfo

    private val _reportDetailsState = MutableStateFlow<ReportDetailsState>(ReportDetailsState.Idle)
    val reportDetailsState: StateFlow<ReportDetailsState> = _reportDetailsState

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

                communicator.getReports(token)
                    .onSuccess { reports ->
                        _reportsInfo.value = ReportState.Success(reports)
                    }
                    .onFailure { e ->
                        _reportsInfo.value =
                            ReportState.Error(e.message ?: "Unknown error")
                    }

            } catch (e: Exception) {
                _reportsInfo.value =
                    ReportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadReportDetails(report: Report) {
        _reportDetailsState.value = ReportDetailsState.Loading

        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                    ?: return@launch run {
                        _reportDetailsState.value =
                            ReportDetailsState.Error("Unauthorized")
                    }

                val rideDataDeferred = async {
                    communicator.getReportDataById(token, report.id)
                }

                val vehicleDeferred = async {
                    communicator.getVehicleById(token, report.vehicleId)
                }

                val rideResult = rideDataDeferred.await()
                val vehicleResult = vehicleDeferred.await()

                if (rideResult.isSuccess && vehicleResult.isSuccess) {
                    val rideRecord = rideResult.getOrThrow()
                    val rideEventsJson = decodeBase64Gzip(rideRecord.eventData)
                    val obdFramesJson = decodeBase64Gzip(rideRecord.data)

                    val rideEvents: List<RideEvent> = fromJson(rideEventsJson)
                    val obdFrames: List<ObdFrameEntity> = fromJson(obdFramesJson)

                    _reportDetailsState.value =
                        ReportDetailsState.Success(ReportDetails(
                            report = report,
                            rideEvents = rideEvents,
                            obdFrames = obdFrames,
                            vehicle = vehicleResult.getOrThrow()
                        ))
                } else {
                    _reportDetailsState.value =
                        ReportDetailsState.Error("Failed to load report details")
                }

            } catch (e: Exception) {
                _reportDetailsState.value =
                    ReportDetailsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearReportDetails() {
        _reportDetailsState.value = ReportDetailsState.Idle
    }
}