package com.android.example.vehsense.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.android.example.vehsense.model.ObdFrame

data class RideUiState(
    val obdFrame: ObdFrame,
    val connectionWasInterrupted: Boolean
)

@Composable
fun RideScreen(
    uiState: RideUiState,
    onStopTheRide: () -> Unit,
) {
    var showExitPopup by remember { mutableStateOf(false) }

    if(uiState.connectionWasInterrupted) {
        onStopTheRide()
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("This is the ride screen", style = MaterialTheme.typography.titleLarge)

        Text("RPM:${uiState.obdFrame.rpm}")
        Text("Engine Load:${uiState.obdFrame.engineLoad}")
        Text("Speed:${uiState.obdFrame.vehicleSpeed}")

        Button(
            onClick = {
                showExitPopup = true
            }
        ) {
            Text("End the ride", style = MaterialTheme.typography.bodyLarge)
        }
    }

    BackHandler() {
        showExitPopup = true
    }

    if(showExitPopup) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showExitPopup = false }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(color = Color.White, shape = RectangleShape)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Leaving this screen will end the ride", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Are you sure you want to leave?", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showExitPopup = false
                                onStopTheRide()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("END", style = MaterialTheme.typography.bodyLarge)
                        }
                        Button(
                            onClick = {
                                showExitPopup = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            Text("STAY", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}