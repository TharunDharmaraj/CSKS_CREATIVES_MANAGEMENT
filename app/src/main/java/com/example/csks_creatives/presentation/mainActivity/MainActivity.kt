package com.example.csks_creatives.presentation.mainActivity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.csks_creatives.presentation.AppNavigation
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.ui.theme.CSKS_CREATIVESTheme
import com.example.csks_creatives.ui.theme.SetStatusAndNavigationBarColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CSKS_CREATIVESTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    NotificationPermissionRequester()
                }
                SetStatusAndNavigationBarColor(darkSlateBlue)

                val navController = rememberNavController()

                var backPressedTime by remember { mutableLongStateOf(0L) }
                val context = LocalContext.current

                val currentRoute = navController.currentBackStackEntry?.destination?.route
                val isAtHomeScreen =
                    currentRoute?.startsWith("employee_home") == true || currentRoute == "admin_home" || currentRoute == "login" || currentRoute == "gate"

                BackHandler(enabled = isAtHomeScreen) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime > 2000) {
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT)
                            .show()
                        backPressedTime = currentTime
                    } else {
                        (context as? Activity)?.finish()
                    }
                }

                AppNavigation(navController)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NotificationPermissionRequester() {
    LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    // Optional: Block screen or show dialog until permission is granted
    if (!permissionState.status.isGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text(
                    "Notification Permission Required",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text("To continue using the app, please allow notification access.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    permissionState.launchPermissionRequest()
                }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
