package com.example.tripshare.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.example.tripshare.models.RideModel
import com.google.firebase.database.*

class RideAuthViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("rides")

    private val _rides = MutableLiveData<List<RideModel>>()
    val rides: LiveData<List<RideModel>> = _rides

    private var ridesListener: ValueEventListener? = null

    fun fetchRides() {
        ridesListener?.let { database.removeEventListener(it) }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rideList = snapshot.children.mapNotNull { snap ->
                    snap.getValue(RideModel::class.java)
                }
                _rides.postValue(rideList)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)
        ridesListener = listener
    }

    fun getRideById(rideId: String): LiveData<RideModel?> {
        val rideLiveData = MutableLiveData<RideModel?>()
        database.child(rideId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ride = snapshot.getValue(RideModel::class.java)
                rideLiveData.postValue(ride)
            }

            override fun onCancelled(error: DatabaseError) {
                rideLiveData.postValue(null)
            }
        })
        return rideLiveData
    }

    fun addRide(
        origin: String,
        destination: String,
        date: String = "N/A",
        time: String = "N/A",
        seats: Int,
        status: String = "Available",
        driverName: String = "Driver Name",
        driverPhone: String = "0000000000",
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (origin.isBlank() || destination.isBlank() || seats <= 0) {
            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val rideId = database.push().key ?: run {
            Toast.makeText(context, "Failed to generate ride ID", Toast.LENGTH_SHORT).show()
            return
        }

        val newRide = RideModel(
            id = rideId,
            origin = origin.trim(),
            destination = destination.trim(),
            date = if (date.isBlank()) "N/A" else date.trim(),
            time = if (time.isBlank()) "N/A" else time.trim(),
            seats = seats,
            status = status,
            driverName = driverName,
            driverPhone = driverPhone,
            bookedBy = null
        )

        database.child(rideId).setValue(newRide)
            .addOnSuccessListener {
                Toast.makeText(context, "Ride added successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add ride: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateRide(
        rideId: String,
        origin: String,
        destination: String,
        date: String = "N/A",
        time: String = "N/A",
        seats: Int,
        status: String = "Available",
        driverName: String = "Driver Name",
        driverPhone: String = "0000000000",
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (origin.isBlank() || destination.isBlank() || seats <= 0) {
            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedRide = RideModel(
            id = rideId,
            origin = origin.trim(),
            destination = destination.trim(),
            date = if (date.isBlank()) "N/A" else date.trim(),
            time = if (time.isBlank()) "N/A" else time.trim(),
            seats = seats,
            status = status,
            driverName = driverName,
            driverPhone = driverPhone,
            bookedBy = null // reset booking if ride is updated
        )

        database.child(rideId).setValue(updatedRide)
            .addOnSuccessListener {
                Toast.makeText(context, "Ride updated successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteRide(rideId: String, context: Context) {
        database.child(rideId).removeValue()
            .addOnSuccessListener { Toast.makeText(context, "Ride deleted", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(context, "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show() }
    }

    // ✅ New: Book a ride
    fun bookRide(rideId: String, userId: String, context: Context) {
        database.child(rideId).get().addOnSuccessListener { snapshot ->
            val ride = snapshot.getValue(RideModel::class.java)
            if (ride != null && ride.status == "Available") {
                val updatedRide = ride.copy(
                    status = "Booked",
                    bookedBy = userId
                )
                database.child(rideId).setValue(updatedRide)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Ride booked successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Ride is no longer available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchMyBookings(userId: String): LiveData<List<RideModel>> {
        val myBookings = MutableLiveData<List<RideModel>>()
        database.orderByChild("bookedBy").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookings = snapshot.children.mapNotNull { snap ->
                        snap.getValue(RideModel::class.java)
                    }
                    myBookings.postValue(bookings)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        return myBookings
    }

    override fun onCleared() {
        super.onCleared()
        ridesListener?.let { database.removeEventListener(it) }
    }
    fun cancelBooking(
        rideId: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val rideRef = database.child(rideId)

        rideRef.get().addOnSuccessListener { snapshot ->
            val ride = snapshot.getValue(RideModel::class.java)
            if (ride != null) {
                val updatedRide = ride.copy(
                    status = "Available",
                    bookedBy = null
                )
                rideRef.setValue(updatedRide)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to cancel: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}
