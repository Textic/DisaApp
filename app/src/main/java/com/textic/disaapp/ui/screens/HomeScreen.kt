package com.textic.disaapp.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.textic.disaapp.ui.navigation.AppDrawer
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.core.graphics.get
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(drawerState = drawerState, scope = scope, navController = navController)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DisaApp") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraPreview()
                } else {
                    PermissionDeniedContent {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

data class CIELAB(val l: Double, val a: Double, val b: Double)

fun rgbToCIELAB(color: Color): CIELAB {
    val rSrgb = color.red.toDouble()
    val gSrgb = color.green.toDouble()
    val bSrgb = color.blue.toDouble()

    val rLinear = if (rSrgb <= 0.04045) rSrgb / 12.92 else ((rSrgb + 0.055) / 1.055).pow(2.4)
    val gLinear = if (gSrgb <= 0.04045) gSrgb / 12.92 else ((gSrgb + 0.055) / 1.055).pow(2.4)
    val bLinear = if (bSrgb <= 0.04045) bSrgb / 12.92 else ((bSrgb + 0.055) / 1.055).pow(2.4)

    val r = rLinear * 100
    val g = gLinear * 100
    val b = bLinear * 100

    // sRGB to XYZ
    val x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375
    val y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750
    val z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041

    // XYZ to CIELAB
    var varX = x / 95.047 // Observer= 2°, Illuminant= D65
    var varY = y / 100.000
    var varZ = z / 108.883

    varX = if (varX > 0.008856) varX.pow(1.0 / 3.0) else (7.787 * varX) + (16.0 / 116.0)
    varY = if (varY > 0.008856) varY.pow(1.0 / 3.0) else (7.787 * varY) + (16.0 / 116.0)
    varZ = if (varZ > 0.008856) varZ.pow(1.0 / 3.0) else (7.787 * varZ) + (16.0 / 116.0)

    val l = (116.0 * varY) - 16.0
    val a = 500.0 * (varX - varY)
    val bLab = 200.0 * (varY - varZ)

    return CIELAB(l, a, bLab)
}

fun colorDistance(lab1: CIELAB, lab2: CIELAB): Double {
    return sqrt((lab1.l - lab2.l).pow(2) + (lab1.a - lab2.a).pow(2) + (lab1.b - lab2.b).pow(2))
}

fun getColorName(color: Color): String {
    val colorMap = mapOf(
        "Negro" to Color(0xFF000000),
        "Gris" to Color(0xFF808080),
        "Blanco" to Color(0xFFFFFFFF),
        "Rojo" to Color(0xFFFF0000),
        "Rojo oscuro" to Color(0xFF8B0000),
        "Rojo claro" to Color(0xFFF08080),
        "Verde" to Color(0xFF008000),
        "Verde oscuro" to Color(0xFF006400),
        "Azul" to Color(0xFF0000FF),
        "Amarillo" to Color(0xFFFFFF00),
        "Naranja" to Color(0xFFFFA500),
        "Marrón" to Color(0xFFA52A2A),
        "Rosa" to Color(0xFFFFC0CB),
        "Morado" to Color(0xFF800080),
        "Turquesa" to Color(0xFF40E0D0),
        "Violeta" to Color(0xFFEE82EE),
        "Oliva" to Color(0xFF808000),
        "Salmón" to Color(0xFFFA8072),
        "Dorado" to Color(0xFFFFD700),
        "Cian" to Color(0xFF00FFFF),
        "Magenta" to Color(0xFFFF00FF),
        "Lima" to Color(0xFF00FF00),
        "Marrón claro" to Color(0xFFD2B48C),
        "Azul marino" to Color(0xFF000080),
        "Verde azulado" to Color(0xFF008080)
    )

    val detectedLab = rgbToCIELAB(color)

    return colorMap.minByOrNull { (_, value) ->
        val valueLab = rgbToCIELAB(value)
        colorDistance(detectedLab, valueLab)
    }?.key ?: "Desconocido"
}

@Composable
fun CameraPreview() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    var detectedColor by remember { mutableStateOf(Color.White) }

    val executor = remember { Executors.newSingleThreadExecutor() }

    cameraController.setImageAnalysisAnalyzer(
        executor
    ) { image ->
        val bitmap = image.toBitmap()
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val pixel = bitmap[centerX, centerY]
        val red = android.graphics.Color.red(pixel)
        val green = android.graphics.Color.green(pixel)
        val blue = android.graphics.Color.blue(pixel)
        detectedColor = Color(red, green, blue)
        image.close()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pointer in the middle of the screen
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(10.dp)
                .background(Color.White, shape = CircleShape)
        )

        val colorName = getColorName(detectedColor)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.offset(y = 130.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(detectedColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Color: $colorName\n" +
                            "RGB: (${(detectedColor.red * 255).toInt()}, ${(detectedColor.green * 255).toInt()}, ${(detectedColor.blue * 255).toInt()})",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionDeniedContent(openSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Para utilizar esta función, necesitamos acceso a la cámara. Por favor, concede el permiso en la configuración de la aplicación.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = openSettings) {
            Text("Abrir configuración")
        }
    }
}