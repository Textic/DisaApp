package com.textic.disaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

sealed class LoginResult {
    object Success : LoginResult()
    object InactiveUser : LoginResult()
    object WrongCredentials : LoginResult()
}

sealed class RegistrationResult {
    object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
    object EmailAlreadyExists : RegistrationResult()
}

data class User(
    val name: String = "",
    val email: String = ""
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    suspend fun register(name: String, email: String, pass: String): RegistrationResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(name = name, email = email)
                db.collection("users").document(firebaseUser.uid).set(user).await()
            }
            RegistrationResult.Success
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthUserCollisionException -> {
                    Log.e("AuthViewModel", "Registration failed: Email already in use.", e)
                    RegistrationResult.EmailAlreadyExists
                }
                else -> {
                    Log.e("AuthViewModel", "Registration failed: ${e.message}", e)
                    RegistrationResult.Error(e.message ?: "An unknown error occurred.")
                }
            }
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

    suspend fun signInWithGoogle(idToken: String): LoginResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!

            // Check if the user is new and create a document in Firestore
            if (authResult.additionalUserInfo?.isNewUser == true) {
                val user = User(name = firebaseUser.displayName ?: "", email = firebaseUser.email ?: "")
                db.collection("users").document(firebaseUser.uid).set(user).await()
            }

            LoginResult.Success
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google Sign-In failed", e)
            LoginResult.WrongCredentials // Or a new specific error type
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
