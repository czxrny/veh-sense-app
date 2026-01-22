package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.ui.components.CircularProgressionScreen
import com.android.example.vehsense.ui.components.FadePopup
import com.android.example.vehsense.ui.components.StandardScreen
import com.android.example.vehsense.ui.viewmodels.UserViewModel

data class UserUiState(
    val userInfo: UserViewModel.UserState,
    val isPrivate: Boolean
)

@Composable
fun UserScreen(
    uiState: UserUiState,
    onGoBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutPopup by remember { mutableStateOf(false) }

    StandardScreen(
        topText = "Driver's Hub"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = uiState.userInfo) {
                is UserViewModel.UserState.Loading -> {
                    CircularProgressionScreen()
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
            Button(
                onClick = { showLogoutPopup = true }
            ) {
                Text("Logout", style = MaterialTheme.typography.bodyLarge)
            }
            Button(
                onClick = { onGoBack() }
            ) {
                Text("Go back", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    FadePopup(
        isActive = showLogoutPopup,
        onBack = {
            showLogoutPopup = false
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Are you sure you want to logout?")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showLogoutPopup = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("YES", style = MaterialTheme.typography.bodyLarge)
                }
                Button(
                    onClick = {
                        showLogoutPopup = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("NO", style = MaterialTheme.typography.bodyLarge)
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
