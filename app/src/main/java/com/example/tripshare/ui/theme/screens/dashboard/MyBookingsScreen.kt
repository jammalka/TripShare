package com.example.tripshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState   // 👈 IMPORTANT
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.tripshare.data.RideAuthViewModel
import com.example.tripshare.models.RideModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavHostController,
    userId: String,
    rideAuthViewModel: RideAuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val rides by rideAuthViewModel.rides.observeAsState(emptyList())  // 👈 Safe

    LaunchedEffect(Unit) {
        rideAuthViewModel.fetchRides()
    }

    // filter only this passenger's bookings
    val myBookings = rides.filter { it.bookedBy == userId }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Bookings") })
        }
    ) { padding ->
        if (myBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No bookings yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // ✅ items() gets RideModel directly
                items(myBookings) { ride: RideModel ->
                    BookingCard(ride = ride, onCancel = {
                        rideAuthViewModel.cancelBooking(
                            rideId = ride.id,
                            context = context
                        ) {
                            Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    ride: RideModel,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("From: ${ride.origin}")
            Text("To: ${ride.destination}")
            Text("Date: ${ride.date}  Time: ${ride.time}")
            Text("Driver: ${ride.driverName} (${ride.driverPhone})")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCancel) {
                Text("Cancel Booking")
            }
        }
    }
}
