package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.AdminHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.*
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
                canShowMenu = true,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("add_employee", "Add Employee"),
                    ToolbarOverFlowMenuItem("add_client", "Add Client"),
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                canShowAddTaskButton = true,
                onAddTaskIconClicked = {
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
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
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
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    CardItem(
                        title = state.value.employeeList[index].employeeName,
                        onClick = {
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
                    CardItem(
                        title = state.value.clientList[index].clientName,
                        onClick = {
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
                    CardItem(
                        title = state.value.activeTaskList[index].taskName,
                        subtitle = state.value.activeTaskList[index].taskType.name,
                        onClick = {
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
                    CardItem(
                        title = state.value.backlogTaskList[index].taskName,
                        subtitle = state.value.backlogTaskList[index].taskType.name,
                        onClick = {
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
                    CardItem(
                        title = state.value.completedTasksList[index].taskName,
                        subtitle = state.value.completedTasksList[index].taskType.name,
                        onClick = {
                            val taskId = state.value.completedTasksList[index].taskId
                            navController.navigate("task_detail/$taskId/$ADMIN_NAME")
                        }
                    )
                }
            }

            item {
                SectionToggleButton(
                    "Leave Requests",
                    visibilityState.value.isLeaveRequestsSectionVisible,
                    onToggle = {
                        viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleActiveLeavesSection)
                    }
                )
            }
            if (visibilityState.value.isLeaveRequestsSectionVisible) {
                items(state.value.activeLeaveRequests.size) { index ->
                    // Show card for leave requests
                    LeaveRequestTaskItem(
                        leaveRequest = state.value.activeLeaveRequests[index],
                        onApproval = { viewModel.onLeaveRequestApproved(state.value.activeLeaveRequests[index]) }
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
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
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
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    )
}

@Composable
fun SectionToggleButton(text: String, isVisible: Boolean, onToggle: () -> Unit) {
    Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
        Text(if (isVisible) "Hide $text" else "Show $text")
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun LeaveRequestTaskItem(
    leaveRequest: LeaveRequest,
    onApproval: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Posted by: ${leaveRequest.postedBy}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Date: ${
                        formatTimeStampToGetJustDate(leaveRequest.leaveDate.toDate().time.toString())
                    }",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reason: ${leaveRequest.leaveReason}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onApproval,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .height(40.dp)
            ) {
                Text("Approve")
            }
        }
    }
}

@Composable
fun CardItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}