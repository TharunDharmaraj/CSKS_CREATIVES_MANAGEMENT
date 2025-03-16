package com.example.csks_creatives.presentation.taskDetailScreen

import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.components.TaskDetailComposable
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.TaskDetailViewModel
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCreationUiEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.toolbar.AppToolbar

@Composable
fun TaskDetailsComposable(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    userRole: UserRole = UserRole.Employee,
    isTaskCreation: Boolean = false,
    taskId: String = "",
    employeeId: String = "",
    navController: NavHostController
) {
    val context = LocalContext.current
    val taskName by viewModel.taskName.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TaskCreationUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is TaskCreationUiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.initialize(userRole, isTaskCreation, taskId, employeeId)
    }
    Scaffold(
        topBar = {
            val actionButtonText: String
            val title: String
            val actionButtonEvent : () -> Unit
            if (userRole == UserRole.Employee || (userRole == UserRole.Admin && isTaskCreation.not())) {
                title = taskName
                actionButtonText = "Save"
                actionButtonEvent = { viewModel.onEvent(TaskDetailEvent.SaveTask) }
            } else {
                title = "Create Task"
                actionButtonText = "Create"
                actionButtonEvent = { viewModel.onEvent(TaskDetailEvent.CreateTask) }
            }
            AppToolbar(
                title = title,
                canShowSearch = false,
                canShowMenu = false,
                canShowBackIcon = true,
                onSearchClicked = { /* Ignore */ },
                onBackClicked = { navController.popBackStack() },
                canShowActionButton = true,
                actionButtonText = actionButtonText,
                onActionButtonClicked = actionButtonEvent,
                onMenuItemClicked = { }
            )
        }
    ) { paddingValue ->
        TaskDetailComposable(viewModel, isTaskCreation, userRole, taskId, employeeId, paddingValue)
    }
}