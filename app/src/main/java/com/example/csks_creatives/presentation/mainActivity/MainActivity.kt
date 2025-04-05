package com.example.csks_creatives.presentation.mainActivity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.csks_creatives.presentation.AppNavigation
import com.example.csks_creatives.presentation.components.charCoal
import com.example.csks_creatives.presentation.mainActivity.viewModel.MainViewModel
import com.example.csks_creatives.ui.theme.CSKS_CREATIVESTheme
import com.example.csks_creatives.ui.theme.SetStatusAndNavigationBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CSKS_CREATIVESTheme {
                SetStatusAndNavigationBarColor(charCoal)

                val navController = rememberNavController()
                val mainState by viewModel.mainState.collectAsState()
                val isNavigated = rememberSaveable { mutableStateOf(false) }

                var backPressedTime by remember { mutableLongStateOf(0L) }
                val context = LocalContext.current

                val currentRoute = navController.currentBackStackEntry?.destination?.route
                val isAtHomeScreen =
                    currentRoute?.startsWith("employee_home") == true || currentRoute == "admin_home" || currentRoute == "login"

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

                // Navigation logic
                LaunchedEffect(mainState) {
                    if (!isNavigated.value && (mainState.employeeId.isNotEmpty() || mainState.adminName.isNotEmpty())) {
                        when {
                            mainState.employeeId.isNotEmpty() -> {
                                navController.navigate("employee_home/${mainState.employeeId}") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                            mainState.adminName.isNotEmpty() -> {
                                navController.navigate("admin_home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                            else -> {
                                navController.navigate("login")
                            }
                        }
                        isNavigated.value = true
                    }
                }

                AppNavigation(navController)
            }
        }
    }
}
