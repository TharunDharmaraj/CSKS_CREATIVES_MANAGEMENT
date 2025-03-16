package com.example.csks_creatives

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.clientTasksListScreen.ClientTasksListComposable
import com.example.csks_creatives.presentation.employeeDetailsScreen.EmployeeDetailsScreen
import com.example.csks_creatives.presentation.homeScreen.AdminHomeScreen
import com.example.csks_creatives.presentation.homeScreen.EmployeeHomeScreenComposable
import com.example.csks_creatives.presentation.loginScreen.LoginScreen
import com.example.csks_creatives.presentation.taskDetailScreen.TaskDetailsComposable

@Composable
fun AppNavigation(startDestination: String = "login") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(viewModel = hiltViewModel(), navController) }

        composable("employee_home/{employeeId}") { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EmployeeHomeScreenComposable(
                viewModel = hiltViewModel(),
                navController = navController,
                employeeId = employeeId
            )
        }

        composable("task_detail/{taskId}/{employeeId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            var userRole: UserRole = UserRole.Employee
            if (employeeId == ADMIN_NAME) {
                userRole = UserRole.Admin
            }
            TaskDetailsComposable(
                navController = navController,
                viewModel = hiltViewModel(),
                userRole = userRole,
                isTaskCreation = false,
                taskId = taskId,
                employeeId = employeeId
            )
        }

        composable("admin_home") {
            AdminHomeScreen(viewModel = hiltViewModel(), navController)
        }

        composable("create_task") {
            TaskDetailsComposable(
                navController = navController,
                viewModel = hiltViewModel(),
                userRole = UserRole.Admin,
                isTaskCreation = true
            )
        }

        composable("employee_detail/{employeeId}") { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EmployeeDetailsScreen(
                employeeId = employeeId,
                navController,
                viewModel = hiltViewModel()
            )
        }

        composable("client_detail/{clientId}") { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            ClientTasksListComposable(
                clientId = clientId,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}