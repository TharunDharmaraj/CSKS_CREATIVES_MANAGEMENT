package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminBottomNavigation(val title: String, val icon: ImageVector, val route: String) {
    object Employees : AdminBottomNavigation("Employees", Icons.Default.Person, "employees")
    object Clients : AdminBottomNavigation("Clients", Icons.Default.Star, "clients")
    object Tasks : AdminBottomNavigation("Tasks", Icons.Default.Email, "tasks")
    object LeaveRequests : AdminBottomNavigation("Leaves", icon = Icons.Default.Info, "leaves")
}