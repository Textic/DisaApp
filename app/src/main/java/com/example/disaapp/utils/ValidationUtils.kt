package com.example.disaapp.utils

import android.util.Patterns

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    // Ejemplo: la contrasena debe tener al menos 6 caracteres
    return password.length >= 6
}
