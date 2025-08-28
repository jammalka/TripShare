package com.example.tripshare.models

data class RideModel(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val seats: Int = 0,
    val status: String? = "Available",
    val driverName: String? = "",
    val driverPhone: String? = "",
    val bookedBy: String? = null
)
