package com.example.csks_creatives.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

                LaunchedEffect(mainState) {
                    if (!isNavigated.value && (mainState.employeeId.isNotEmpty() ||
                                mainState.adminName.isNotEmpty())
                    ) {
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
