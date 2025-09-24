package com.example.disaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

sealed class LoginResult {
    object Success : LoginResult()
    object InactiveUser : LoginResult()
    object WrongCredentials : LoginResult()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    suspend fun register(email: String, pass: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            LoginResult.Success
        } catch (e: Exception) {
            LoginResult.WrongCredentials
        }
    }

    //TODO: Implement checkEmailExists
    fun checkEmailExists(email: String): Boolean {
        return false
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
