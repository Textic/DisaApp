package com.example.disaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.disaapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

        private val _users = MutableStateFlow<List<User>>(
        listOf(
            User("admin", "benja.rojas@email.com", "hola123"),
            User("usuario", "sofi.gonzalez@email.com", "hola123123"),
            User("hola", "mati.soto@email.com", "hola123123"),
            User("Isidora Morales", "isi.morales@email.com", "hola123123"),
            User("Agustin Muñoz", "agus.munoz@email.com", "hola123123")
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun register(user: User): Boolean {
        if (_users.value.any { it.email == user.email }) {
            return false // si ya existe la cuenta
        }
        _users.value = _users.value + user
        return true
    }

    fun login(email: String, password: String): Boolean {
        return _users.value.any { it.email == email && it.password == password }
    }
}
