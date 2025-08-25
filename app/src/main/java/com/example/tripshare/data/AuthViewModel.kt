package com.example.tripshare.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.tripshare.models.UserModel
import com.example.tripshare.navigation.ROUTE_LOGIN
import com.example.tripshare.navigation.ROUTE_ROLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase




class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signup(
        name: String,
        email: String,
        password: String,
        confirmpassword: String,
        role: String,
        phone: String,
        navController: NavController,
        context: Context
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() ||
            confirmpassword.isBlank() || role.isBlank() || phone.isBlank()
        ) {
            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()
            return
        }
        if (password != confirmpassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val user = UserModel(
                        uid = userId,
                        name = name,
                        email = email,
                        phone = phone,
                        role = role
                    )
                    saveUserToDatabase(user, navController, context)
                } else {
                    Toast.makeText(
                        context,
                        task.exception?.message ?: "Registration Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(
        user: UserModel,
        navController: NavController,
        context: Context
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
        dbRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    context, "User Registered Successfully",
                    Toast.LENGTH_LONG
                ).show()
                navController.navigate(ROUTE_LOGIN) {
                    popUpTo(0)
                }
            } else {
                Toast.makeText(
                    context,
                    task.exception?.message ?: "Failed to save user",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun login(
        email: String,
        password: String,
        navController: NavController,
        context: Context
    ) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(
                context, "Email and Password required",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context, "Login Successful",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(ROUTE_ROLE) {
                        popUpTo(0)
                    }
                } else {
                    Toast.makeText(
                        context,
                        task.exception?.message ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun logout(navController: NavController, context: Context) {
        auth.signOut()
        Toast.makeText(context, "Logged out", Toast.LENGTH_LONG).show()
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(0)
        }
    }
}
