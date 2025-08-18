package com.example.tripshare.models

data class RideModel(
    val rideId: String = "",
    val driverId: String = "",
    val eventId: String = "",      // links ride to event
    val seatsAvailable: Int = 0,
    val pickupPoint: String = ""
)
