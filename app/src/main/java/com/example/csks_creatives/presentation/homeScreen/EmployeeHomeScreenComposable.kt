package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.helper.FutureSelectableDates
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.EmployeeHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.LeaveRequestDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.LeaveRequestDialogState
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmployeeHomeScreenComposable(
    viewModel: EmployeeHomeScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    employeeId: String
) {
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Welcome, $employeeId",
                canShowSearch = false,
                canShowMenu = true,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                onSearchClicked = { /* Ignore */ },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
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
        val state by viewModel.employeeHomeScreenState.collectAsState()
        val leaveRequestDialogState by viewModel.leaveRequestDialogState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is ToastUiEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.initialize(employeeId)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order By:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleOrderSection) }) {
                        Text(if (state.isOrderByToggleVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.isOrderByToggleVisible) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioButton(
                            onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.Order(DateOrder.Ascending)) },
                            selected = state.tasksOrder == DateOrder.Ascending
                        )
                        Text("Ascending", modifier = Modifier.padding(start = 4.dp))

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.Order(DateOrder.Descending)) },
                            selected = state.tasksOrder == DateOrder.Descending
                        )
                        Text("Descending", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Tasks:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleActiveTasksSection) }) {
                        Text(if (state.isActiveTasksSectionVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.isActiveTasksSectionVisible) {
                items(state.activeTasks.size) { index ->
                    val clientTask = state.activeTasks[index]
                    if (state.isLoading) {
                        LoadingProgress()
                    }
                    TaskItemCard(
                        state.activeTasks[index],
                        onItemClick = {
                            navController.navigate("task_detail/${clientTask.taskId}/$employeeId")
                        },
                        viewModel = viewModel
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Completed Tasks:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleCompletedTasksSection) }) {
                        Text(if (state.isCompletedTasksSectionVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.isCompletedTasksSectionVisible) {
                items(state.completedTasks.size) { index ->
                    val clientTask = state.completedTasks[index]
                    if (state.isLoading) {
                        LoadingProgress()
                    }
                    TaskItemCard(
                        clientTask,
                        onItemClick = {
                            navController.navigate("task_detail/${clientTask.taskId}/$employeeId")
                        },
                        isCompletedTask = true,
                        viewModel = viewModel
                    )
                }
            }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleLeavesSection) }
                ) {
                    Text(if (state.isLeavesSectionVisible) "Hide Leaves" else "Show Leaves")
                }
            }
            if (state.isLeavesSectionVisible) {
                if (state.rejectedLeaves.isNotEmpty()) {
                    item {
                        Text(
                            "Leaves Pending Approval",
                            style = MaterialTheme.typography.titleMedium
                        )
                        state.rejectedLeaves.forEach { leave ->
                            LeaveRequestCard(leave)
                        }
                    }
                }

                if (state.approvedLeaves.isNotEmpty()) {
                    item {
                        Text("Approved Leaves", style = MaterialTheme.typography.titleMedium)
                        state.approvedLeaves.forEach { leave ->
                            LeaveRequestCard(leave)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.onAddLeaveDialogEvent(LeaveRequestDialogEvent.OpenDialog) }) {
                        Text("Request Leave")
                    }
                }
            }
        }

        LeaveRequestDialog(
            isVisible = state.isAddLeaveDialogVisible,
            state = leaveRequestDialogState,
            onEvent = { viewModel.onAddLeaveDialogEvent(it) }
        )
    }
}

@Composable
fun TaskItemCard(
    task: ClientTask,
    onItemClick: () -> Unit,
    isCompletedTask: Boolean = false,
    viewModel: EmployeeHomeScreenViewModel
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onItemClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.taskName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: ${task.currentStatus}")
            if (isCompletedTask) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Time Taken: ${viewModel.getCompletedTaskTime(task)}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestDialog(
    isVisible: Boolean,
    state: LeaveRequestDialogState,
    onEvent: (LeaveRequestDialogEvent) -> Unit
) {
    if (isVisible) {
        var showDatePicker by remember { mutableStateOf(false) }
        val tomorrowMillis = remember {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tomorrowMillis,
            selectableDates = FutureSelectableDates
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val newDate = Date(selectedDateMillis)
                            onEvent(LeaveRequestDialogEvent.OnLeaveRequestDateChanged(newDate))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { onEvent(LeaveRequestDialogEvent.CloseDialog) },
            title = { Text("Leave Request") },
            text = {
                Column {
                    Text("Select Date")
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showDatePicker = true }) {
                        Text(
                            "Pick Date: ${
                                SimpleDateFormat(
                                    "dd-MM-yyyy",
                                    Locale.getDefault()
                                ).format(state.leaveRequestDate)
                            }"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reason for Leave")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = state.leaveRequestReason,
                        onValueChange = {
                            onEvent(
                                LeaveRequestDialogEvent.OnLeaveRequestReasonChanged(
                                    it
                                )
                            )
                        },
                        placeholder = { Text("Enter reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onEvent(LeaveRequestDialogEvent.SubmitLeaveRequest) }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                Button(onClick = { onEvent(LeaveRequestDialogEvent.CloseDialog) }) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            )
        )
    }
}

@Composable
fun LeaveRequestCard(leave: LeaveRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 4.dp,
            color = if (leave.approvedStatus) Color.Green else Color.Red
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Date: ${formatTimeStampToGetJustDate(leave.leaveDate.toDate().time.toString())}")
            Text("Reason: ${leave.leaveReason}")
            Text(
                "Status: ${if (leave.approvedStatus) "Approved" else "Not Approved"}",
            )
        }
    }
}
