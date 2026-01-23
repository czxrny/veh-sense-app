package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.ui.components.CircularProgressionScreen
import com.android.example.vehsense.ui.components.StandardScreen
import com.android.example.vehsense.ui.viewmodels.ReportViewModel

data class ReportsUiState(
    val reportsState: ReportViewModel.ReportState
)

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
) {
    StandardScreen(
        topText = "Driver's Reports"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        Text("${report.id}, ${report.startTime}, ${report.stopTime}, ${report.accelerationStyle}, ${report.brakingStyle}, ${report.averageSpeed}, ${report.maxSpeed}, ${report.kilometersTravelled}")
                    }
                }
            }
        }
    }
}