package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.presentation.components.DateOrder
import com.example.csks_creatives.presentation.components.LoadingProgress
import com.example.csks_creatives.presentation.components.ToastUiEvent
import com.example.csks_creatives.presentation.components.helper.FutureSelectableDates
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.EmployeeHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.LeaveRequestDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.LeaveRequestDialogState
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.onAddLeaveDialogEvent(LeaveRequestDialogEvent.OpenDialog) }) {
                    Text("Request Leave")
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
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
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
            selectableDates =  FutureSelectableDates
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
            }
        )
    }
}
