package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority
import com.example.csks_creatives.presentation.components.helper.FutureSelectableDates
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = "Welcome, $employeeId",
                canShowLogo = true,
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
        },
        bottomBar = {
            NavigationBar(containerColor = darkSlateBlue) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Active Tasks"
                        )
                    },
                    label = { Text("Active") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Done, contentDescription = "Completed Tasks") },
                    label = { Text("Done") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Leaves") },
                    label = { Text("Leaves") }
                )
            }
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
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                0 -> { // Active Tasks
                    if (state.isActiveTasksLoading.not()) {
                        item {
                            SectionHeaderWithSort(
                                title = "Active Tasks",
                                isAscending = state.tasksOrder == DateOrder.Ascending,
                                onSortClick = {
                                    viewModel.onEvent(
                                        EmployeeHomeScreenEvent.Order(
                                            if (state.tasksOrder == DateOrder.Ascending) DateOrder.Descending else DateOrder.Ascending
                                        )
                                    )
                                }
                            )
                        }

                        if (state.activeTasks.isEmpty()) {
                            item {
                                Text(
                                    text = "No active tasks found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                                )
                            }
                        } else {
                            items(state.activeTasks.size) { index ->
                                TaskItemCard(
                                    task = state.activeTasks[index],
                                    onItemClick = {
                                        navController.navigate("task_detail/${state.activeTasks[index].taskId}/$employeeId")
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                1 -> { // Completed Tasks
                    if (state.isCompletedTasksLoading.not()) {
                        viewModel.getEmployeeCompletedTasks(employeeId)
                        item {
                            SectionHeaderWithSort(
                                title = "Completed Tasks",
                                isAscending = state.tasksOrder == DateOrder.Ascending,
                                onSortClick = {
                                    viewModel.onEvent(
                                        EmployeeHomeScreenEvent.Order(
                                            if (state.tasksOrder == DateOrder.Ascending) DateOrder.Descending else DateOrder.Ascending
                                        )
                                    )
                                }
                            )
                        }

                        if (state.completedTasks.isEmpty()) {
                            item {
                                Text(
                                    text = "No completed tasks found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                                )
                            }
                        } else {
                            items(state.completedTasks.size) { index ->
                                TaskItemCard(
                                    task = state.completedTasks[index],
                                    isCompletedTask = true,
                                    onItemClick = {
                                        navController.navigate("task_detail/${state.completedTasks[index].taskId}/$employeeId")
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                2 -> { // Leave Requests
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.onAddLeaveDialogEvent(LeaveRequestDialogEvent.OpenDialog)
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Add Leave",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Request Leave",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                    if (state.rejectedLeaves.isNotEmpty()) {
                        item {
                            Text(
                                "Rejected Leaves",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(state.rejectedLeaves.size) { index ->
                            LeaveRequestCard(
                                state.rejectedLeaves[index],
                                onReRequest = { viewModel.reRequestLeaveRequest(state.rejectedLeaves[index]) }
                            )
                        }
                    }

                    if (state.unApprovedLeaves.isNotEmpty()) {
                        item {
                            Text(
                                "Leaves Pending Approval",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(state.unApprovedLeaves.size) { index ->
                            LeaveRequestCard(
                                state.unApprovedLeaves[index],
                                onWidthDrawRequest = {
                                    viewModel.withDrawLeaveRequest(state.unApprovedLeaves[index])
                                }
                            )
                        }
                    }

                    if (state.approvedLeaves.isNotEmpty()) {
                        item {
                            Text(
                                "Approved Leaves",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(state.approvedLeaves.size) { index ->
                            LeaveRequestCard(state.approvedLeaves[index])
                        }
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
        border = BorderStroke(
            width = 2.dp,
            color = getBorderColorBasedOnTaskPriority(
                task.taskPriority,
                isTaskCompleted = task.currentStatus == TaskStatusType.COMPLETED
            )
        ),
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
fun LeaveRequestCard(
    leave: LeaveRequest,
    onWidthDrawRequest: () -> Unit = {},
    onReRequest: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (leave.approvedStatus == LeaveApprovalStatus.APPROVED) Color.Green else if (leave.approvedStatus == LeaveApprovalStatus.UN_APPROVED) Color.Blue else Color.Red
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Date: ${formatTimeStampToGetJustDate(leave.leaveDate.toDate().time.toString())}")
                Text("Reason: ${leave.leaveReason}")
                Text(
                    "Status: ${if (leave.approvedStatus == LeaveApprovalStatus.APPROVED) "Approved" else if (leave.approvedStatus == LeaveApprovalStatus.UN_APPROVED) "Not Approved" else "Rejected"}",
                )
            }
            if (leave.approvedStatus == LeaveApprovalStatus.UN_APPROVED) {
                Button(
                    onClick = onWidthDrawRequest,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .height(40.dp)
                ) {
                    Text("Withdraw")
                }
            }
            if (leave.approvedStatus == LeaveApprovalStatus.REJECTED) {
                Button(
                    onClick = onReRequest,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .height(40.dp)
                ) {
                    Text("Re-Request")
                }
            }
        }

    }
}

@Composable
fun SectionHeaderWithSort(
    title: String,
    isAscending: Boolean,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        IconButton(onClick = onSortClick) {
            Icon(
                imageVector = if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Sort"
            )
        }
    }
}