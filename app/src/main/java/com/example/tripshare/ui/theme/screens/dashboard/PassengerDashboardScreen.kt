package com.example.tripshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    rideAuthViewModel: RideAuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val rides: List<RideModel> by rideAuthViewModel.rides.observeAsState(emptyList())

    // fetch rides once when screen loads
    LaunchedEffect(Unit) {
        rideAuthViewModel.fetchRides()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Available Rides") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(ROUTE_BOOKINGS) }) {
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
            ) {
                Text("No rides available")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rides) { ride: RideModel ->
                    RideCard(ride = ride, onBook = {
                        rideAuthViewModel.bookRide(
                            rideId = ride.id,  // ✅ this works if fetchRides sets ride.id
                            userId = userId,
                            context = context
                        )
                        Toast.makeText(context, "Ride booked", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

@Composable
fun RideCard(
    ride: RideModel,
    onBook: () -> Unit
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
            Text("Seats: ${ride.seats}")
            Text("Driver: ${ride.driverName} (${ride.driverPhone})")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBook) {
                Text("Book Ride")
            }
        }
    }
}
