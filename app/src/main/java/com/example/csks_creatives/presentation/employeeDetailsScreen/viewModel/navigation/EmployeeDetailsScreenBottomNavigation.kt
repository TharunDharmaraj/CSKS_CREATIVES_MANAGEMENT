package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.navigation

sealed class EmployeeDetailsScreenBottomNavigation {
    object ActiveTasks : EmployeeDetailsScreenBottomNavigation()
    object CompletedTasks : EmployeeDetailsScreenBottomNavigation()
    object Profile : EmployeeDetailsScreenBottomNavigation()
}