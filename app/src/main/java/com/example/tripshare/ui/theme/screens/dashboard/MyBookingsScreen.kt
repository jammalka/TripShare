package com.example.tripshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    userId: String,
    viewModel: RideAuthViewModel = viewModel()
) {
    val bookings by viewModel.myBookings.observeAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.fetchMyBookings(userId) }

    Scaffold(topBar = { TopAppBar(title = { Text("My Bookings") }) }) { padding ->
        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No bookings yet") }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(bookings) { ride ->
                    BookingCard(ride, userId, viewModel)
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    ride: RideModel,
    userId: String,
    viewModel: RideAuthViewModel
) {
    val context = LocalContext.current

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
            Text("From: ${ride.origin ?: "N/A"}")
            Text("To: ${ride.destination ?: "N/A"}")
            Text("Date: ${ride.date ?: "N/A"}")
            Text("Time: ${ride.time ?: "N/A"}")
            Text("Seats remaining: ${ride.seats}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    viewModel.cancelBooking(ride.id, userId, context) {
                        Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                        viewModel.fetchMyBookings(userId)
                        viewModel.fetchRides()
                    }
                }
            ) { Text("Cancel Booking") }
        }
    }
}
