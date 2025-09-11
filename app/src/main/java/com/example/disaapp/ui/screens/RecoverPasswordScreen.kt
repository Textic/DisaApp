package com.example.disaapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.disaapp.ui.theme.DisaAppTheme
import com.example.disaapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordFieldVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Ingresa tu correo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !passwordFieldVisible
            )

            AnimatedVisibility(visible = !passwordFieldVisible) {
                Button(
                    onClick = {
                        if (authViewModel.checkEmailExists(email)) {
                            passwordFieldVisible = true
                            Toast.makeText(context, "Correo encontrado. Ingresa tu nueva contraseña.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Correo no encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Verificar correo")
                }
            }

            AnimatedVisibility(visible = passwordFieldVisible) {
                Column {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (authViewModel.resetPassword(email, newPassword)) {
                                Toast.makeText(context, "Contraseña cambiada exitosamente.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                // Esto no deberia pasar si el email ya fue verificado
                                Toast.makeText(context, "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Cambiar Contraseña")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoverPasswordScreenPreview() {
    DisaAppTheme {
        RecoverPasswordScreen(rememberNavController())
    }
}
