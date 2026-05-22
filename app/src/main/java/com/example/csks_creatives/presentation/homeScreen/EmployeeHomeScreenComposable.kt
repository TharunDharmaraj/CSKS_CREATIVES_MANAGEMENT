package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveDuration
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.presentation.components.charCoal
import com.example.csks_creatives.presentation.components.charCoalPurple
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.goldenRod
import com.example.csks_creatives.presentation.components.grey
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority
import com.example.csks_creatives.presentation.components.helper.FutureSelectableDates
import com.example.csks_creatives.presentation.components.limeGreen
import com.example.csks_creatives.presentation.components.red
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.transparent
import com.example.csks_creatives.presentation.components.displayName
import com.example.csks_creatives.presentation.components.icon
import com.example.csks_creatives.presentation.components.EmployeeProfileComponent
import com.example.csks_creatives.presentation.components.ui.ModernDateView
import com.example.csks_creatives.presentation.components.ui.PaginationLoader
import com.example.csks_creatives.presentation.components.ui.isAtBottom
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.EmployeeHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.LeaveRequestDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.LeaveRequestDialogState
import com.example.csks_creatives.presentation.taskDetailScreen.components.ModernTaskTextField
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        containerColor = darkSlateBlue,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = employeeId,
                canShowLogo = true,
                canShowSearch = false,
                canShowMenu = true,
                menuItems = buildList {
                    if (selectedTab != 3) {
                        add(ToolbarOverFlowMenuItem("force_fetch_tasks", "Force Fetch"))
                    }
                    add(ToolbarOverFlowMenuItem("logout", "Logout"))
                },
                onSearchClicked = { /* Ignore */ },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
                        "force_fetch_tasks" -> {
                            viewModel.onEvent(EmployeeHomeScreenEvent.ForceFetchTasks)
                        }

                        "force_fetch_leaves" -> {
                            viewModel.onEvent(EmployeeHomeScreenEvent.ForceFetchLeaves)
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = darkSlateBlue,
                tonalElevation = 8.dp
            ) {
                val navBarColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = vividCerulean,
                    selectedTextColor = vividCerulean,
                    unselectedIconColor = silverGrey,
                    unselectedTextColor = silverGrey,
                    indicatorColor = vividCerulean.copy(alpha = 0.1f)
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = navBarColors,
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Backlog Tasks") },
                    label = { Text("Backlog") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = navBarColors,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Active Tasks"
                        )
                    },
                    label = { Text("Active") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = navBarColors,
                    icon = { Icon(Icons.Default.Done, contentDescription = "Completed Tasks") },
                    label = { Text("Done") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = navBarColors,
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Leaves") },
                    label = { Text("Leaves") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    colors = navBarColors,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        val context = LocalContext.current
        val state by viewModel.employeeHomeScreenState.collectAsState()
        val leaveRequestDialogState by viewModel.leaveRequestDialogState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
  
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

        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> { // Backlog Tasks
                    EmployeeBacklogTasksTab(viewModel, navController, employeeId)
                }

                1 -> { // Active Tasks
                    EmployeeActiveTasksTab(viewModel, navController, employeeId)
                }

                2 -> { // Completed Tasks
                    EmployeeCompletedTasksTab(viewModel, navController, employeeId)
                }

                3 -> { // Leave Requests
                    EmployeeLeavesTab(viewModel)
                }

                4 -> { // Profile
                    EmployeeProfileComponent(
                        employeeName = state.employeeName,
                        employeeJoinedTime = state.employeeJoinedTime,
                        employeePassword = state.employeePassword,
                        totalNumberOfTasksCompleted = state.totalNumberOfTasksCompleted,
                        isCompletedCountLoading = state.isCompletedCountLoading,
                        approvedLeaves = state.approvedLeaves,
                        unApprovedLeaves = state.unApprovedLeaves,
                        rejectedLeaves = state.rejectedLeaves,
                        onFetchCompletedCount = {
                            viewModel.onEvent(EmployeeHomeScreenEvent.FetchCompletedTasksCount)
                        },
                        coroutineScope = coroutineScope
                    )
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
fun EmployeeBacklogTasksTab(
    viewModel: EmployeeHomeScreenViewModel,
    navController: NavHostController,
    employeeId: String
) {
    val state by viewModel.employeeHomeScreenState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf { listState.isAtBottom() }
    }

    LaunchedEffect(shouldLoadMore.value, state.backlogTasks.size, state.isBacklogTasksLoading, state.isPaginationLoading, state.isBacklogTasksEndReached) {
        if (shouldLoadMore.value && !state.isBacklogTasksLoading && !state.isPaginationLoading && !state.isBacklogTasksEndReached && state.backlogTasks.isNotEmpty()) {
            viewModel.onEvent(EmployeeHomeScreenEvent.LoadMoreBacklogTasks)
        }
    }

    if (state.isBacklogTasksLoading && state.backlogTasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                SectionHeaderWithSort(
                    title = "Backlog Tasks",
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

            if (state.backlogTasks.isEmpty() && !state.isBacklogTasksLoading) {
                item {
                    Text(
                        text = "No backlog tasks found",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(color = silverGrey)
                    )
                }
            } else {
                items(state.backlogTasks.size) { index ->
                    TaskItemCard(
                        task = state.backlogTasks[index],
                        onItemClick = {
                            navController.navigate("task_detail/${state.backlogTasks[index].taskId}/$employeeId")
                        },
                        viewModel = viewModel
                    )
                }
                if (state.isPaginationLoading) {
                    item { PaginationLoader() }
                }
            }
        }
    }
}

@Composable
fun EmployeeActiveTasksTab(
    viewModel: EmployeeHomeScreenViewModel,
    navController: NavHostController,
    employeeId: String
) {
    val state by viewModel.employeeHomeScreenState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf { listState.isAtBottom() }
    }

    LaunchedEffect(shouldLoadMore.value, state.activeTasks.size, state.isActiveTasksLoading, state.isPaginationLoading, state.isActiveTasksEndReached) {
        if (shouldLoadMore.value && !state.isActiveTasksLoading && !state.isPaginationLoading && !state.isActiveTasksEndReached && state.activeTasks.isNotEmpty()) {
            viewModel.onEvent(EmployeeHomeScreenEvent.LoadMoreActiveTasks)
        }
    }

    if (state.isActiveTasksLoading && state.activeTasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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

            if (state.activeTasks.isEmpty() && !state.isActiveTasksLoading) {
                item {
                    Text(
                        text = "No active tasks found",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(color = silverGrey)
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
                if (state.isPaginationLoading) {
                    item { PaginationLoader() }
                }
            }
        }
    }
}

@Composable
fun EmployeeCompletedTasksTab(
    viewModel: EmployeeHomeScreenViewModel,
    navController: NavHostController,
    employeeId: String
) {
    val state by viewModel.employeeHomeScreenState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf { listState.isAtBottom() }
    }

    LaunchedEffect(Unit) {
        viewModel.getEmployeeCompletedTasks(employeeId)
    }

    LaunchedEffect(shouldLoadMore.value, state.completedTasks.size, state.isCompletedTasksLoading, state.isPaginationLoading, state.isCompletedTasksEndReached) {
        if (shouldLoadMore.value && !state.isCompletedTasksLoading && !state.isPaginationLoading && !state.isCompletedTasksEndReached && state.completedTasks.isNotEmpty()) {
            viewModel.onEvent(EmployeeHomeScreenEvent.LoadMoreCompletedTasks)
        }
    }

    if (state.isCompletedTasksLoading && state.completedTasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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

            if (state.completedTasks.isEmpty() && !state.isCompletedTasksLoading) {
                item {
                    Text(
                        text = "No completed tasks found",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(color = silverGrey)
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
                if (state.isPaginationLoading) {
                    item { PaginationLoader() }
                }
            }
        }
    }
}

@Composable
fun EmployeeLeavesTab(viewModel: EmployeeHomeScreenViewModel) {
    val state by viewModel.employeeHomeScreenState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            if (state.rejectedLeaves.isNotEmpty()) {
                item { SectionHeader("Rejected Leaves") }
                items(state.rejectedLeaves.size) { index ->
                    LeaveRequestCard(
                        state.rejectedLeaves[index],
                        onReRequest = { viewModel.reRequestLeaveRequest(state.rejectedLeaves[index]) }
                    )
                }
            }

            if (state.unApprovedLeaves.isNotEmpty()) {
                item { SectionHeader("Pending Approval") }
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
                item { SectionHeader("Approved Leaves") }
                items(state.approvedLeaves.size) { index ->
                    LeaveRequestCard(state.approvedLeaves[index])
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
        }

        // Modern Floating Action Button for Adding Leave
        LargeFloatingActionButton(
            onClick = { viewModel.onAddLeaveDialogEvent(LeaveRequestDialogEvent.OpenDialog) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = vividCerulean,
            contentColor = white,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Leave")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Leave", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = white.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp),
        fontWeight = FontWeight.SemiBold
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: ClientTask,
    onItemClick: () -> Unit,
    isCompletedTask: Boolean = false,
    viewModel: EmployeeHomeScreenViewModel
) {
    val priorityColor = getBorderColorBasedOnTaskPriority(task.taskPriority)
    val statusColor = when (task.currentStatus) {
        TaskStatusType.BACKLOG -> red
        TaskStatusType.IN_PROGRESS -> vividCerulean
        TaskStatusType.COMPLETED -> limeGreen
        TaskStatusType.IN_REVIEW -> Color.Magenta
        TaskStatusType.PAUSED -> goldenRod
        else -> grey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(2.dp, priorityColor.copy(alpha = 0.5f)),
        onClick = onItemClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = priorityColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.5.dp, priorityColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = task.taskPriority.name,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = task.taskType.icon,
                        contentDescription = null,
                        tint = silverGrey.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.taskType.displayName,
                        fontSize = 13.sp,
                        color = silverGrey
                    )
                }
                
                // Effort Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = vividCerulean.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, vividCerulean.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = vividCerulean,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.taskEstimate}h",
                            color = vividCerulean,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = task.currentStatus.name,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                ModernDateView(task.taskCreationTime)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Client: ${task.clientId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = silverGrey
                )

                if (isCompletedTask) {
                    Text(
                        text = "Time: ${viewModel.getCompletedTaskTime(task)}",
                        color = vividCerulean,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
        val showDatePicker = remember { mutableStateOf(false) }
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

        if (showDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                confirmButton = {
                    Button(onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val newDate = Date(selectedDateMillis)
                            onEvent(LeaveRequestDialogEvent.OnLeaveRequestDateChanged(newDate))
                        }
                        showDatePicker.value = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker.value = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { onEvent(LeaveRequestDialogEvent.CloseDialog) },
            containerColor = charCoal,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(vividCerulean.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = vividCerulean)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Request Leave",
                        style = MaterialTheme.typography.headlineSmall,
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val focusManager = LocalFocusManager.current
                    Column {
                        Text("When do you need it?", style = MaterialTheme.typography.labelMedium, color = silverGrey)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = { showDatePicker.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            color = charCoalPurple,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = vividCerulean, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(state.leaveRequestDate),
                                    color = white,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Column {
                        Text("Select Duration", style = MaterialTheme.typography.labelMedium, color = silverGrey)
                        LeaveDurationToggle(
                            isHalfDay = state.leaveDuration == LeaveDuration.HALF_DAY,
                            onToggleChange = { onEvent(LeaveRequestDialogEvent.OnLeaveDurationChanged(it)) }
                        )
                    }

                    ModernTaskTextField(
                        value = state.leaveRequestReason,
                        onValueChange = { onEvent(LeaveRequestDialogEvent.OnLeaveRequestReasonChanged(it)) },
                        label = "Reason for absence",
                        icon = Icons.Default.Info,
                        focusManager = focusManager
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(LeaveRequestDialogEvent.SubmitLeaveRequest) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = vividCerulean),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Submit Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { onEvent(LeaveRequestDialogEvent.CloseDialog) }) {
                        Text("Maybe Later", color = silverGrey)
                    }
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
fun LeaveDurationToggle(
    isHalfDay: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(charCoalPurple, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf("Full Day", "Half Day")
        options.forEachIndexed { index, text ->
            val isSelected = (index == 1) == isHalfDay
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { onToggleChange(index == 1) },
                color = if (isSelected) vividCerulean else transparent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) white else silverGrey,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveRequestCard(
    leave: LeaveRequest,
    onWidthDrawRequest: () -> Unit = {},
    onReRequest: () -> Unit = {}
) {
    val statusColor = when (leave.approvedStatus) {
        LeaveApprovalStatus.APPROVED -> limeGreen
        LeaveApprovalStatus.UN_APPROVED -> vividCerulean
        LeaveApprovalStatus.REJECTED -> red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    ModernDateView(
                        timeStamp = leave.leaveDate.toDate().time.toString(),
                        useRelativeTime = false,
                        showTime = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = leave.leaveDuration.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = silverGrey.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    val statusText = when (leave.approvedStatus) {
                        LeaveApprovalStatus.APPROVED -> "Approved"
                        LeaveApprovalStatus.UN_APPROVED -> "Pending"
                        LeaveApprovalStatus.REJECTED -> "Rejected"
                    }
                    Text(
                        text = statusText,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = leave.leaveReason,
                style = MaterialTheme.typography.bodyMedium,
                color = silverGrey
            )

            if (leave.approvedStatus == LeaveApprovalStatus.UN_APPROVED || leave.approvedStatus == LeaveApprovalStatus.REJECTED) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (leave.approvedStatus == LeaveApprovalStatus.UN_APPROVED) {
                        OutlinedButton(
                            onClick = onWidthDrawRequest,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = silverGrey)
                        ) {
                            Text("Withdraw")
                        }
                    }
                    if (leave.approvedStatus == LeaveApprovalStatus.REJECTED) {
                        Button(
                            onClick = onReRequest,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = vividCerulean)
                        ) {
                            Text("Re-Request", color = white)
                        }
                    }
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = white,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onSortClick) {
            Icon(
                imageVector = if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Sort",
                tint = vividCerulean
            )
        }
    }
}
