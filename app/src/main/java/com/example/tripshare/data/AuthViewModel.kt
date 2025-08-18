package com.example.tripshare.data

import android.R.attr.name
import android.R.attr.password
import android.widget.Toast
import com.example.tripshare.models.UserModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel:viewmodel(){
  private val auth:FirebaseAuth=FirebaseAuth.getInstance()
  fun signup(
    uid:String,
    name:String,
    email: String,
    role: String,
    password: String,
    confirmpassword: String,
    phone: String,
    ){
    if (uid.isBlank() ||name.isBlank()|| email.isBlank() || role.isBlank() || password.isBlank()||confirmpassword.isBlank()||phone.isBlank()) {
      Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()
      return
    }
    if (password != confirmpassword) {
      Toast.makeText(context, "password does not match", Toast.LENGTH_LONG).show()
      return
    }
    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userId = auth.currentUser?.uid ?: ""
        val user =
          UserModel(uid = uid, name = name, email = email, role= role)

        saveUserToDatabase(user, navController, context)
      } else {
        Toast.makeText(
          context, task.exception?.message ?: "Registration Failed", Toast.LENGTH_LONG
        ).show()
      }
    }
  }
}


