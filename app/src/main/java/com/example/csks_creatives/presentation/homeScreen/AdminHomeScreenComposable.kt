package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.presentation.components.LoadingProgress
import com.example.csks_creatives.presentation.components.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.AdminHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AddClientDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AddEmployeeDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AdminHomeScreenEvent
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import kotlinx.coroutines.launch

@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeScreenViewModel = hiltViewModel(),
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Welcome, Admin",
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("add_employee", "Add Employee"),
                    ToolbarOverFlowMenuItem("add_client", "Add Client"),
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                canShowAddTaskButton = true,
                onAddTaskIconClicked = {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.CreateTaskButtonClick)
                    navController.navigate("create_task")
                },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
                        "add_employee" -> {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.CreateEmployeeButtonClick)
                        }

                        "add_client" -> {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.CreateClientButtonClick)
                        }

                        "logout" -> {

                            viewModel.emitLogoutEvent(true)
                            navController.navigate("login")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        val state = viewModel.adminHomeScreenState.collectAsState()
        val visibilityState = viewModel.adminHomeScreenVisibilityState.collectAsState()
        val loadingState = viewModel.adminHomeScreenLoadingState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is ToastUiEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.CreateTaskButtonClick)
                            navController.navigate("create_task")
                        }
                    ) {
                        Text("Add Task")
                    }
                }
            }

            item {
                SectionToggleButton("Employees", visibilityState.value.isEmployeeSectionVisible) {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleEmployeeSection)
                }
            }
            if (visibilityState.value.isEmployeeSectionVisible) {
                items(state.value.employeeList.size) { index ->
                    if (loadingState.value.isEmployeesLoading) {
                        LoadingProgress()
                    }
                    Text(
                        text = state.value.employeeList[index].employeeName,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val employeeId = state.value.employeeList[index].employeeId
                                navController.navigate("employee_detail/$employeeId")
                            }
                    )
                }
            }

            item {
                SectionToggleButton("Clients", visibilityState.value.isClientSectionVisible) {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleClientSection)
                }
            }
            if (visibilityState.value.isClientSectionVisible) {
                items(state.value.clientList.size) { index ->
                    if (loadingState.value.isClientsLoading) {
                        LoadingProgress()
                    }
                    Text(
                        text = state.value.clientList[index].clientName,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val clientId = state.value.clientList[index].clientId
                                navController.navigate("client_detail/$clientId")
                            }
                    )
                }
            }

            item {
                SectionToggleButton(
                    "Active Tasks",
                    visibilityState.value.isActiveTaskSectionVisible
                ) {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleActiveTaskSection)
                }
            }
            if (visibilityState.value.isActiveTaskSectionVisible) {
                items(state.value.activeTaskList.size) { index ->
                    if (loadingState.value.isActiveTasksLoading) {
                        LoadingProgress()
                    }
                    Text(
                        text = state.value.activeTaskList[index].taskName,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val taskId = state.value.activeTaskList[index].taskId
                                navController.navigate("task_detail/$taskId/$ADMIN_NAME")
                            }
                    )
                }
            }

            item {
                SectionToggleButton(
                    "Backlog Tasks",
                    visibilityState.value.isBacklogTaskSectionVisible
                ) {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleBacklogTaskSection)
                }
            }
            if (visibilityState.value.isBacklogTaskSectionVisible) {
                items(state.value.backlogTaskList.size) { index ->
                    if (loadingState.value.isBacklogTasksLoading) {
                        LoadingProgress()
                    }
                    Text(
                        text = state.value.backlogTaskList[index].taskName,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val taskId = state.value.backlogTaskList[index].taskId
                                navController.navigate("task_detail/$taskId/$ADMIN_NAME")
                            }
                    )
                }
            }

            item {
                SectionToggleButton(
                    "Completed Tasks",
                    visibilityState.value.isCompletedTaskSectionVisible
                ) {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleCompletedTaskSection)
                }
            }
            if (visibilityState.value.isCompletedTaskSectionVisible) {
                items(state.value.completedTasksList.size) { index ->
                    if (loadingState.value.isCompletedTasksLoading) {
                        LoadingProgress()
                    }
                    Text(
                        text = state.value.completedTasksList[index].taskName,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val taskId = state.value.completedTasksList[index].taskId
                                navController.navigate("task_detail/$taskId/$ADMIN_NAME")
                            }
                    )
                }
            }
        }
        if (visibilityState.value.isAddClientDialogVisible) {
            AddClientDialog(viewModel)
        }

        if (visibilityState.value.isAddEmployeeDialogVisible) {
            AddEmployeeDialog(viewModel)
        }
    }
}

@Composable
fun AddEmployeeDialog(viewModel: AdminHomeScreenViewModel) {
    val state = viewModel.addEmployeeDialogState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.CloseDialogButtonClicked)
        },
        title = { Text("Add Employee") },
        text = {
            Column {
                TextField(
                    value = state.value.employeeName,
                    onValueChange = {
                        viewModel.onEmployeeDialogEvent(
                            AddEmployeeDialogEvent.EmployeeNameTextFieldChanged(
                                it
                            )
                        )
                    },
                    label = { Text("Employee Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = state.value.employeePassword,
                    onValueChange = {
                        viewModel.onEmployeeDialogEvent(
                            AddEmployeeDialogEvent.EmployeeNamePasswordFieldChanged(
                                it
                            )
                        )
                    },
                    label = { Text("Password") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.AddEmployeeButtonClicked)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = {
                viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.CloseDialogButtonClicked)
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddClientDialog(viewModel: AdminHomeScreenViewModel) {
    val state = viewModel.addClientDialogState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { viewModel.onClientDialogEvent(AddClientDialogEvent.CloseDialogButtonClicked) },
        title = { Text("Add Client") },
        text = {
            TextField(
                value = state.value.clientName,
                onValueChange = {
                    viewModel.onClientDialogEvent(
                        AddClientDialogEvent.ClientNameTextFieldChanged(
                            it
                        )
                    )
                },
                label = { Text("Client Name") }
            )
        },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.onClientDialogEvent(AddClientDialogEvent.AddClientButtonClicked)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.onClientDialogEvent(AddClientDialogEvent.CloseDialogButtonClicked) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SectionToggleButton(text: String, isVisible: Boolean, onToggle: () -> Unit) {
    Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
        Text(if (isVisible) "Hide $text" else "Show $text")
    }
    Spacer(modifier = Modifier.height(8.dp))
}