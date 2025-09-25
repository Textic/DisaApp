package com.textic.disaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.textic.disaapp.ui.navigation.AppNavigation
import com.textic.disaapp.ui.theme.DisaAppTheme

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
