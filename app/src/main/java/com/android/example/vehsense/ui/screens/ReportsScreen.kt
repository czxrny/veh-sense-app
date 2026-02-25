package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.vehsense.model.Report
import com.android.example.vehsense.model.ReportDetails
import com.android.example.vehsense.ui.components.CircularProgressionScreen
import com.android.example.vehsense.ui.components.FadePopup
import com.android.example.vehsense.ui.components.ReportSummaryTile
import com.android.example.vehsense.ui.components.RideCharts
import com.android.example.vehsense.ui.components.StandardScreen
import com.android.example.vehsense.ui.viewmodels.ReportViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ReportsUiState(
    val reportsState: ReportViewModel.ReportState,
    val reportDetailsState: ReportViewModel.ReportDetailsState,
)

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
    onLoadReportDetails: (Report) -> Unit,
    onClearReportDetails: () -> Unit
) {
    StandardScreen(
        topText = "Driver's Reports"
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                when (uiState.reportsState) {
                    is ReportViewModel.ReportState.Loading -> CircularProgressionScreen()
                    is ReportViewModel.ReportState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.reportsState.message,
                            color = Color.Red,
                        )
                    }

                    is ReportViewModel.ReportState.Success -> {
                        var index = 1
                        uiState.reportsState.reports.forEach { report ->
                            Spacer(modifier = Modifier.height(16.dp))
                            ReportSummaryTile(
                                report,
                                index++,
                                onClick = onLoadReportDetails
                            )
                        }
                    }
                }
            }
        }
    }
    when (val state = uiState.reportDetailsState) {
        is ReportViewModel.ReportDetailsState.Success -> {
            ShowReportDetails(
                reportDetails = state.reportDetails,
                onDismiss = { onClearReportDetails() }
            )
        }

        is ReportViewModel.ReportDetailsState.Loading -> {
            CircularProgressionScreen()
        }

        is ReportViewModel.ReportDetailsState.Error -> {
            Text(state.message, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
        }

        is ReportViewModel.ReportDetailsState.Idle -> Unit
    }
}

@Composable
fun ShowReportDetails(
    reportDetails: ReportDetails,
    onDismiss: () -> Unit
) {
    var isActive by remember { mutableStateOf(true) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    FadePopup(
        isActive = isActive,
        onBack = { isActive = false },
        onDismiss = onDismiss
    ) {
        StandardScreen(
            topText = "Report Details"
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    @Composable
                    fun DetailRow(label: String, value: String) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.titleLarge, fontSize = 14.sp)
                            Text(value, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    val r = reportDetails.report
                    val startTime = Instant.ofEpochMilli(r.startTime)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                    val stopTime = Instant.ofEpochMilli(r.stopTime)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                    val durationMillis = r.stopTime - r.startTime
                    val duration = Duration.ofMillis(durationMillis)
                    val hours = duration.toHours()
                    val minutes = (duration.toMinutes() % 60)
                    val durationText = "${hours}h ${minutes}m"

                    DetailRow("Start Time", startTime)
                    DetailRow("Stop Time", stopTime)
                    DetailRow("Duration", durationText)
                    DetailRow("Acceleration Style", r.accelerationStyle)
                    DetailRow("Braking Style", r.brakingStyle)
                    DetailRow("Average Speed", "${r.averageSpeed} km/h")
                    DetailRow("Max Speed", "${r.maxSpeed} km/h")
                    DetailRow("Kilometers Travelled", "${r.kilometersTravelled} km")

                    Spacer(modifier = Modifier.height(16.dp))

                    val v = reportDetails.vehicle
                    DetailRow("Vehicle Brand", v.brand)
                    DetailRow("Model", v.model)
                    DetailRow("Year", v.year.toString())
                    DetailRow("Engine Capacity", "${v.engineCapacity} cc")
                    DetailRow("Engine Power", "${v.enginePower} hp")
                    v.plates?.let { DetailRow("Plates", it) }
                    DetailRow("Expected Fuel", "${v.expectedFuel} L")

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        RideCharts(
                            obdFrames = reportDetails.obdFrames,
                            rideEvents = reportDetails.rideEvents
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Close", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
