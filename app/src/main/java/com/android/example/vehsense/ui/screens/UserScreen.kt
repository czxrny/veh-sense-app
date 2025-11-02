package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.ui.viewmodels.UserViewModel

data class UserUiState(
    val userInfo: UserViewModel.UserState,
    val isPrivate: Boolean
)

@Composable
fun UserScreen(
    uiState: UserUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My info", style = MaterialTheme.typography.titleLarge)

        when (val state = uiState.userInfo) {
            is UserViewModel.UserState.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Fetching data...", style = MaterialTheme.typography.bodyLarge)
            }

            is UserViewModel.UserState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${state.message}",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is UserViewModel.UserState.Success -> {
                val user = state.user
                val organization = state.organization

                Spacer(modifier = Modifier.height(16.dp))
                Text("User Information", style = MaterialTheme.typography.titleMedium)

                InfoRow("Username", user.userName)
                InfoRow("Total kilometers", "${user.totalKilometers} km")
                InfoRow("Number of rides", user.numberOfRides.toString())
                InfoRow("Rating", user.rating)

                if (!uiState.isPrivate && organization != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Organization Information", style = MaterialTheme.typography.titleMedium)

                    InfoRow("Name", organization.name)
                    InfoRow("Address", organization.address)
                    InfoRow("City", organization.city)
                    InfoRow("Country", organization.country)
                    InfoRow("Zip Code", organization.zipCode)
                    InfoRow("Country Code", organization.countryCode)
                    InfoRow("Contact", organization.contactNumber)
                    InfoRow("Email", organization.email)
                } else if (!uiState.isPrivate && organization == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching organization info...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}
