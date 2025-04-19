package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.csks_creatives.ui.theme.icons.*

sealed class AdminBottomNavigation(val title: String, val icon: ImageVector) {
    object Employees : AdminBottomNavigation("Employees", Icons.Outlined.Person)
    object Clients : AdminBottomNavigation("Clients", ClientIcon)
    object Tasks : AdminBottomNavigation("Tasks", AllTasksIcon)
    object Finance : AdminBottomNavigation("Money", bankIcon)
    object LeaveRequests : AdminBottomNavigation("Leaves", icon = LeaveRequestIcon)
}