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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.fetchRides()
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            rides.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No rides available. Add a ride to get started.")
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = padding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(rides) { ride ->
                        RideCard(ride, viewModel, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun RideCard(
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
            Text("From: ${ride.origin ?: "N/A"}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("To: ${ride.destination ?: "N/A"}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("Date: ${ride.date ?: "N/A"}")
            Text("Time: ${ride.time ?: "N/A"}")
            Text("Seats: ${ride.seats}")

            Spacer(modifier = Modifier.height(8.dp))

            // Status badge
            val rideStatus = ride.status ?: "Available"
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

            // Driver info with call intent
            val driverName = ride.driverName ?: "N/A"
            val driverPhone = ride.driverPhone ?: ""
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Driver: $driverName")
                Spacer(modifier = Modifier.width(12.dp))
                if (driverPhone.isNotBlank()) {
                    Text(
                        text = driverPhone,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone"))
                            context.startActivity(intent)
                        }
                    )
                } else {
                    Text("No phone", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(onClick = { navController.navigate("${ROUTE_UPDATE_RIDE}/${ride.id}") }) {
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
                    viewModel.deleteRide(ride.id, context)
                    showDeleteDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            },
            title = { Text("Cancel Ride") },
            text = { Text("Are you sure you want to cancel this ride?") }
        )
    }
}
