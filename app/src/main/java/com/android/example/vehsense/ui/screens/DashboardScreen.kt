package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

}

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onGoToBT: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoToBT,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect to Bluetooth Device")
        }
    }
}
