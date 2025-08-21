package com.example.disaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.disaapp.ui.navigation.AppNavigation
import com.example.disaapp.ui.theme.DisaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisaAppTheme {
                AppNavigation()
            }
        }
    }
}
