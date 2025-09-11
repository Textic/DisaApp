package com.example.disaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.disaapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class LoginResult {
    object Success : LoginResult()
    object InactiveUser : LoginResult()
    object WrongCredentials : LoginResult()
}

class AuthViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(listOf(
        User("admin", "benja.rojas@email.com", "hola123", "active"),
        User("usuario", "sofi.gonzalez@email.com", "hola123123", "active"),
        User("hola", "mati.soto@email.com", "hola123123", "inactive"),
        User("Isidora Morales", "isi.morales@email.com", "hola123123", "active"),
        User("Agustin Muñoz", "agus.munoz@email.com", "hola123123", "inactive")
    ))
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun register(user: User): Boolean {
        if (_users.value.any { it.email == user.email }) {
            return false // si ya existe la cuenta
        }
        _users.value = _users.value + user.copy(status = "active")
        return true
    }

    fun login(email: String, password: String): LoginResult {
        val user = _users.value.find { it.email == email }

        if (user == null) {
            return LoginResult.WrongCredentials
        }

        if (user.password != password) {
            return LoginResult.WrongCredentials
        }

        if (user.status == "inactive") {
            return LoginResult.InactiveUser
        }

        return LoginResult.Success
    }

    fun checkEmailExists(email: String): Boolean {
        return _users.value.any { it.email == email }
    }

    fun resetPassword(email: String, newPassword: String): Boolean {
        val user = _users.value.find { it.email == email }
        if (user != null) {
            val updatedUser = user.copy(password = newPassword)
            _users.value = _users.value.map { if (it.email == email) updatedUser else it }
            return true
        }
        return false
    }

    /**
     * Filtra la lista de usuarios y devuelve solo los que tienen el estado "active".
     */
    fun getActiveUsers(): List<User> {
        return _users.value.filter { it.status == "active" }
    }

    /**
     * Filtra por usuarios inactivos, mapea la lista a sus correos,
     * y transforma cada correo a mayusculas.
     */
    fun getInactiveUserEmails(): List<String> {
        return _users.value
            .filter { it.status == "inactive" }
            .map { it.email.uppercase() }
    }

    /**
     * Encuentra un usuario por su email y actualiza su estado a "inactive".
     * Demuestra el uso de map para actualizar un objeto inmutable en una lista.
     */
    fun deactivateUser(email: String) {
        _users.value = _users.value.map {
            if (it.email == email) {
                it.copy(status = "inactive")
            } else {
                it
            }
        }
    }

    /**
     * Agrupa los usuarios por su estado y cuenta cuantos hay en cada grupo.
     * Demuestra el uso de groupBy y mapValues.
     */
    fun countUsersByStatus(): Map<String, Int> {
        return _users.value
            .groupBy { it.status }
            .mapValues { it.value.size }
    }
}
