package com.example.disaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.disaapp.ui.screens.LoginScreen
import com.example.disaapp.ui.screens.RecoverPasswordScreen
import com.example.disaapp.ui.screens.RegisterScreen
import com.example.disaapp.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("recover_password") {
            RecoverPasswordScreen(navController = navController)
        }
    }
}
