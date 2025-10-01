package com.textic.disaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.textic.disaapp.ui.screens.HomeScreen
import com.textic.disaapp.ui.screens.LoginScreen
import com.textic.disaapp.ui.screens.RecoverPasswordScreen
import com.textic.disaapp.ui.screens.RegisterScreen
import com.textic.disaapp.ui.screens.ProfileScreen
import com.textic.disaapp.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val user by authViewModel.user.collectAsState()

    val startDestination = if (user != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("recover_password") {
            RecoverPasswordScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(authViewModel = authViewModel, navController = navController)
        }
    }
}
