package com.example.tripshare.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tripshare.models.RideModel
import com.google.firebase.database.*

class RideAuthViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("rides")

    private val _rides = MutableLiveData<List<RideModel>>()
    val rides: LiveData<List<RideModel>> = _rides

    private val _myBookings = MutableLiveData<List<RideModel>>()
    val myBookings: LiveData<List<RideModel>> = _myBookings

    // Helper: safely parse a "list-ish" value into List<String>
    private fun parseStringList(value: Any?): List<String> {
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            is String -> listOf(value)
            is Map<*, *> -> value.values.mapNotNull { it as? String } // firebase sometimes stores lists as {0: "a", 1: "b"}
            else -> emptyList()
        }
    }

    // Helper: construct RideModel from a snapshot defensively
    private fun parseRide(snapshot: DataSnapshot): RideModel {
        val id = snapshot.key ?: ""
        val origin = snapshot.child("origin").getValue(String::class.java) ?: ""
        val destination = snapshot.child("destination").getValue(String::class.java) ?: ""
        val date = snapshot.child("date").getValue(String::class.java) ?: ""
        val time = snapshot.child("time").getValue(String::class.java) ?: ""
        val seats = snapshot.child("seats").getValue(Long::class.java)?.toInt()
            ?: snapshot.child("seats").getValue(Int::class.java) ?: 0
        val status = snapshot.child("status").getValue(String::class.java) ?: "Available"
        val driverName = snapshot.child("driverName").getValue(String::class.java) ?: ""
        val driverPhone = snapshot.child("driverPhone").getValue(String::class.java) ?: ""

        // bookedBy may be stored as List, String, Map, or missing — handle all.
        val bookedByRaw = snapshot.child("bookedBy").value
        val bookedBy = parseStringList(bookedByRaw)

        return RideModel(
            id = id,
            origin = origin,
            destination = destination,
            date = date,
            time = time,
            seats = seats,
            status = status,
            driverName = driverName,
            driverPhone = driverPhone,
            bookedBy = bookedBy
        )
    }

    // Fetch all rides (safe parsing)
    fun fetchRides() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rideList = snapshot.children.map { snap ->
                    parseRide(snap)
                }
                _rides.postValue(rideList)
            }
            override fun onCancelled(error: DatabaseError) {
                _rides.postValue(emptyList())
            }
        })
    }

    // Add new ride (unchanged behavior)
    fun addRide(
        origin: String,
        destination: String,
        date: String,
        time: String,
        seats: Int,
        status: String,
        driverName: String,
        driverPhone: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (origin.isBlank() || destination.isBlank() || seats <= 0) {
            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val rideId = database.push().key ?: return
        val newRide = RideModel(
            id = rideId,
            origin = origin,
            destination = destination,
            date = date,
            time = time,
            seats = seats,
            status = status,
            driverName = driverName,
            driverPhone = driverPhone,
            bookedBy = emptyList()
        )

        database.child(rideId).setValue(newRide)
            .addOnSuccessListener {
                fetchRides()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Update ride (unchanged)
    fun updateRide(
        rideId: String,
        origin: String,
        destination: String,
        date: String,
        time: String,
        seats: Int,
        status: String,
        driverName: String,
        driverPhone: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val updatedRide = RideModel(
            id = rideId,
            origin = origin,
            destination = destination,
            date = date,
            time = time,
            seats = seats,
            status = status,
            driverName = driverName,
            driverPhone = driverPhone,
            bookedBy = emptyList()
        )

        database.child(rideId).setValue(updatedRide)
            .addOnSuccessListener {
                fetchRides()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete ride
    fun deleteRide(rideId: String, context: Context, onSuccess: () -> Unit) {
        database.child(rideId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Ride deleted", Toast.LENGTH_SHORT).show()
                fetchRides()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Get ride by ID (safe parse)
    fun getRideById(rideId: String): LiveData<RideModel?> {
        val rideLiveData = MutableLiveData<RideModel?>()
        database.child(rideId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    rideLiveData.postValue(parseRide(snapshot))
                } else {
                    rideLiveData.postValue(null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                rideLiveData.postValue(null)
            }
        })
        return rideLiveData
    }

    // Book ride (carpool: multiple users) — uses safe parsing from snapshot
    fun bookRide(rideId: String, userId: String, context: Context, onSuccess: () -> Unit) {
        val rideRef = database.child(rideId)
        rideRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(context, "Ride not found", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val ride = parseRide(snapshot)
            if (ride.seats > 0) {
                val updatedRide = ride.copy(
                    seats = ride.seats - 1,
                    status = if (ride.seats - 1 == 0) "Booked" else "Available",
                    bookedBy = (ride.bookedBy ?: emptyList()) + userId
                )
                rideRef.setValue(updatedRide).addOnSuccessListener {
                    Toast.makeText(context, "Ride booked!", Toast.LENGTH_SHORT).show()
                    fetchMyBookings(userId)
                    fetchRides()
                    onSuccess()
                }.addOnFailureListener {
                    Toast.makeText(context, "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No seats available", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch my bookings (safe parsing)
    fun fetchMyBookings(userId: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookings = snapshot.children.mapNotNull { snap ->
                    val ride = parseRide(snap)
                    if (ride.bookedBy?.contains(userId) == true) ride else null
                }
                _myBookings.postValue(bookings)
            }
            override fun onCancelled(error: DatabaseError) {
                _myBookings.postValue(emptyList())
            }
        })
    }

    // Cancel booking (safe parsing)
    fun cancelBooking(rideId: String, userId: String, context: Context, onSuccess: () -> Unit) {
        val rideRef = database.child(rideId)
        rideRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(context, "Ride not found", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val ride = parseRide(snapshot)
            if (ride.bookedBy?.contains(userId) == true) {
                val updatedRide = ride.copy(
                    seats = ride.seats + 1,
                    status = "Available",
                    bookedBy = ride.bookedBy.filter { it != userId }
                )
                rideRef.setValue(updatedRide).addOnSuccessListener {
                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    fetchMyBookings(userId)
                    fetchRides()
                    onSuccess()
                }.addOnFailureListener {
                    Toast.makeText(context, "Cancel failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "You haven’t booked this ride", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
