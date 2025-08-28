package com.example.tripshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tripshare.data.RideAuthViewModel
import com.example.tripshare.models.RideModel
import com.example.tripshare.navigation.ROUTE_BOOKINGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDashboardScreen(
    navController: NavController,
    userId: String,
    viewModel: RideAuthViewModel = viewModel()
) {
    val rides by viewModel.rides.observeAsState(emptyList())
    val context = LocalContext.current

    // ensure we fetch once when the screen composes
    LaunchedEffect(Unit) {
        viewModel.fetchRides()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Passenger Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("$ROUTE_BOOKINGS?userId=$userId")
            }) {
                Icon(Icons.Default.Book, contentDescription = "My Bookings")
            }
        }
    ) { padding ->
        if (rides.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No rides available currently") }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(rides) { ride ->
                    RideCardPassenger(ride, userId, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun RideCardPassenger(
    ride: RideModel,
    userId: String,
    viewModel: RideAuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var showBookDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("From: ${ride.origin.ifBlank { "N/A" }}")
            Text("To: ${ride.destination.ifBlank { "N/A" }}")
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

            Row {
                Button(
                    onClick = { showBookDialog = true },
                    enabled = (ride.seats > 0) && !(ride.bookedBy.contains(userId))
                ) {
                    Text(if (ride.bookedBy.contains(userId)) "Booked" else "Book")
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (ride.bookedBy.contains(userId)) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            ride.id.takeIf { it.isNotBlank() }?.let { id ->
                                viewModel.cancelBooking(id, userId, context) {
                                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                                    viewModel.fetchMyBookings(userId)
                                    viewModel.fetchRides()
                                }
                            }
                        }
                    ) { Text("Cancel") }
                }
            }
        }
    }

    if (showBookDialog) {
        AlertDialog(
            onDismissRequest = { showBookDialog = false },
            title = { Text("Confirm Booking") },
            text = { Text("Do you want to book this ride?") },
            confirmButton = {
                TextButton(onClick = {
                    // call bookRide with onSuccess callback (matches ViewModel expecting onSuccess)
                    ride.id.takeIf { it.isNotBlank() }?.let { id ->
                        viewModel.bookRide(id, userId, context) {
                            Toast.makeText(context, "Ride booked!", Toast.LENGTH_SHORT).show()
                            viewModel.fetchRides()
                        }
                    }
                    showBookDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showBookDialog = false }) { Text("No") }
            }
        )
    }
}
