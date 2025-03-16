package com.example.csks_creatives.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.csks_creatives.presentation.AppNavigation
import com.example.csks_creatives.presentation.mainActivity.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val mainState by viewModel.mainState.collectAsState()

            LaunchedEffect(mainState) {
                when {
                    mainState.employeeId.isNotEmpty() -> {
                        navController.navigate("employee_home/${mainState.employeeId}") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                    mainState.adminName.isNotEmpty() -> {
                        navController.navigate("admin_home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                    else -> {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }

            AppNavigation(navController)
        }
    }
}
