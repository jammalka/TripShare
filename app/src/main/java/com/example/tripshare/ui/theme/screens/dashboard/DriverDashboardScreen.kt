package com.example.tripshare.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tripshare.data.RideAuthViewModel
import com.example.tripshare.models.RideModel
import com.example.tripshare.navigation.ROUTE_ADD_RIDE
import com.example.tripshare.navigation.ROUTE_UPDATE_RIDE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    navController: NavController,
    viewModel: RideAuthViewModel = viewModel()
) {
    val rides by viewModel.rides.observeAsState(emptyList())
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // Fetch rides on first composition (fetchRides has no callback in your current ViewModel)
    LaunchedEffect(Unit) {
        viewModel.fetchRides()
        // keep behavior same as earlier: hide spinner once fetch triggered
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Driver Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(ROUTE_ADD_RIDE) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Ride")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                rides.isEmpty() -> Text(
                    "No rides available. Add a ride to get started.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(rides) { ride ->
                        RideCardDriver(
                            ride = ride,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideCardDriver(
    ride: RideModel,
    viewModel: RideAuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("From: ${ride.origin.ifBlank { "N/A" }}", fontWeight = FontWeight.Bold)
            Text("To: ${ride.destination.ifBlank { "N/A" }}", fontWeight = FontWeight.Bold)
            Text("Date: ${ride.date.ifBlank { "N/A" }}")
            Text("Time: ${ride.time.ifBlank { "N/A" }}")
            Text("Seats: ${ride.seats}")

            Spacer(modifier = Modifier.height(8.dp))

            val rideStatus = ride.status.ifBlank { "Available" }
            Text(
                text = rideStatus,
                color = Color.White,
                modifier = Modifier
                    .background(
                        if (rideStatus == "Booked") Color.Red else Color(0xFF4CAF50),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Driver: ${ride.driverName.ifBlank { "N/A" }}")
                Spacer(modifier = Modifier.width(12.dp))
                val phone = ride.driverPhone.ifBlank { "" }
                if (phone.isNotBlank()) {
                    Text(
                        text = phone,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    )
                } else {
                    Text("No phone", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                // Safe navigation: only navigate if ride.id is present
                Button(onClick = { ride.id.takeIf { it.isNotBlank() }?.let { id -> navController.navigate("$ROUTE_UPDATE_RIDE/$id") } }) {
                    Text("Update")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { showDeleteDialog = true }
                ) {
                    Text("Delete")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    // call delete with onSuccess callback (matches ViewModel signature that expects onSuccess)
                    ride.id.takeIf { it.isNotBlank() }?.let { id ->
                        viewModel.deleteRide(id, context) {
                            // refresh and notify
                            viewModel.fetchRides()
                        }
                    }
                    showDeleteDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("No") }
            },
            title = { Text("Cancel Ride") },
            text = { Text("Are you sure you want to cancel this ride?") }
        )
    }
}
