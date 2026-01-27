package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.android.example.vehsense.model.Report
import com.android.example.vehsense.ui.components.CircularProgressionScreen
import com.android.example.vehsense.ui.components.FadePopup
import com.android.example.vehsense.ui.components.StandardScreen
import com.android.example.vehsense.ui.viewmodels.ReportViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ReportsUiState(
    val reportsState: ReportViewModel.ReportState
)

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
) {
    var selectedReport by remember { mutableStateOf<Report?>(null) }

    StandardScreen(
        topText = "Driver's Reports"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                    uiState.reportsState.reports.forEach { report ->

                        val startTimeFormatted = Instant.ofEpochMilli(report.startTime)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                        val stopTimeFormatted = Instant.ofEpochMilli(report.stopTime)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                        val durationMillis = report.stopTime - report.startTime
                        val duration = Duration.ofMillis(durationMillis)
                        val hours = duration.toHours()
                        val minutes = (duration.toMinutes() % 60)
                        val durationText = "${hours}h ${minutes}m"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("Start Time: $startTimeFormatted")
                                Text("Stop Time: $stopTimeFormatted")
                                Text("Duration: $durationText")
                                Text("Kilometers Travelled: ${report.kilometersTravelled}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(onClick = { selectedReport = report }) {
                                    Text("See Details")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    selectedReport?.let {
        ShowReportDetails(
            report = selectedReport,
            onDismiss = { selectedReport = null }
        )
    }
}

@Composable
fun ShowReportDetails(
    report: Report?,
    onDismiss: () -> Unit
) {
    var isActive by remember { mutableStateOf(true) }

    report?.let { r ->
        FadePopup(
            isActive = isActive,
            onBack = { isActive = false },
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                @Composable
                fun DetailRow(label: String, value: String) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(label, style = MaterialTheme.typography.titleLarge)
                        Text(value, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
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

                DetailRow("Report ID", r.id.toString())
                DetailRow("User ID", r.userId.toString())
                if (r.organizationId != 0) {
                    DetailRow("Organization ID", r.organizationId.toString())
                }

                DetailRow("Vehicle ID", r.vehicleId.toString())
                DetailRow("Start Time", startTime)
                DetailRow("Stop Time", stopTime)
                DetailRow("Duration", durationText)
                DetailRow("Acceleration Style", r.accelerationStyle)
                DetailRow("Braking Style", r.brakingStyle)
                DetailRow("Average Speed", "${r.averageSpeed} km/h")
                DetailRow("Max Speed", "${r.maxSpeed} km/h")
                DetailRow("Kilometers Travelled", "${r.kilometersTravelled} km")

                Spacer(Modifier.height(24.dp))
                Button(onClick = { isActive = false }) {
                    Text("Close", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
