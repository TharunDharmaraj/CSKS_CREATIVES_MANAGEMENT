package com.example.csks_creatives.presentation.loginScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
import com.example.csks_creatives.presentation.mainActivity.viewModel.MainViewModel

@Composable
fun AuthGate(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val mainState by viewModel.mainState.collectAsState()
    
    LaunchedEffect(mainState.isUserFetched) {
        if (mainState.isUserFetched) {
            if (mainState.employeeId.isNotEmpty()) {
                navController.navigate("employee_home/${mainState.employeeId}") {
                    popUpTo("gate") { inclusive = true }
                }
            } else if (mainState.adminName.isNotEmpty()) {
                navController.navigate("admin_home") {
                    popUpTo("gate") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("gate") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkSlateBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = vividCerulean)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fetching details...",
                color = white,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
