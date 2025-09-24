package com.example.disaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.disaapp.utils.isValidEmail
import com.example.disaapp.utils.isValidPassword
import com.example.disaapp.viewmodel.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.disaapp.viewmodel.LoginResult
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Button(
                onClick = {
                    if (!isValidEmail(email)) {
                        Toast.makeText(context, "Formato de correo invalido.", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    if (!isValidPassword(password)) {
                        Toast.makeText(
                            context,
                            "La contraseña debe tener al menos 6 caracteres.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    scope.launch {
                        when (authViewModel.login(email, password)) {
                            is LoginResult.Success -> {
                                Toast.makeText(context, "Inicio de sesion exitoso", Toast.LENGTH_SHORT)
                                    .show()
                                navController.navigate("home")
                            }

                            is LoginResult.InactiveUser -> {
                                Toast.makeText(context, "El usuario esta inactivo.", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is LoginResult.WrongCredentials -> {
                                Toast.makeText(
                                    context,
                                    "Correo o contraseña incorrectos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Iniciar sesion")
            }

            TextButton(onClick = { navController.navigate("register") }) {
                Text("No tienes cuenta? Registrate")
            }

            TextButton(onClick = { navController.navigate("recover_password") }) {
                Text("Olvidaste tu contraseña?")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DisaAppTheme {
        LoginScreen(rememberNavController())
    }
}
