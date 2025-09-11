package com.example.disaapp.model

data class User(
    val fullName: String,
    val email: String,
    val password: String,
    val status: String = "active"
)
