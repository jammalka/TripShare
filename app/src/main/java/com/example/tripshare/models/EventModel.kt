package com.example.tripshare.models

data class EventModel(
    val eventId: String = "",
    val title: String = "",
    val date: String = "",       // "2025-08-17"
    val location: String = "",
    val organizerId: String = ""
)
