package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminBottomNavigation(val title: String, val icon: ImageVector) {
    object Employees : AdminBottomNavigation("Employees", Icons.Default.Person)
    object Clients : AdminBottomNavigation("Clients", Icons.Default.Star)
    object Tasks : AdminBottomNavigation("Tasks", Icons.Default.Email)
    object LeaveRequests : AdminBottomNavigation("Leaves", icon = Icons.Default.Info)
}