package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPriority
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.financeScreen.FinanceScreenComposable
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.AdminHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.*
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.navigation.AdminBottomNavigation
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeScreenViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val adminToolbarTitle = viewModel.homeScreenTitle.collectAsState()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val navigationItems = listOf(
        AdminBottomNavigation.Tasks,
        AdminBottomNavigation.Employees,
        AdminBottomNavigation.Clients,
        AdminBottomNavigation.Finance,
        AdminBottomNavigation.LeaveRequests
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = adminToolbarTitle.value,
                canShowLogo = true,
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
        },
        bottomBar = {
            NavigationBar(containerColor = darkSlateBlue) {
                val currentPage = pagerState.currentPage

                navigationItems.forEach { item ->
                    val showBadge =
                        item is AdminBottomNavigation.LeaveRequests && viewModel.hasUnapprovedLeaves.value

                    NavigationBarItem(
                        selected = navigationItems[currentPage] == item,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(navigationItems.indexOf(item))
                            }
                        },
                        icon = {
                            if (showBadge) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .offset(x = 6.dp, y = (-12).dp),
                                            containerColor = Color.Red,
                                            content = {})
                                    }
                                ) {
                                    Icon(item.icon, contentDescription = item.title)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.title)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        val context = LocalContext.current
        val visibilityState = viewModel.adminHomeScreenVisibilityState.collectAsState()

        LaunchedEffect(pagerState.currentPage) {
            when (navigationItems[pagerState.currentPage]) {
                AdminBottomNavigation.Employees -> viewModel.setHomeScreenTitle("Employees")
                AdminBottomNavigation.Clients -> viewModel.setHomeScreenTitle("Clients")
                AdminBottomNavigation.Tasks -> viewModel.setHomeScreenTitle("My Tasks")
                AdminBottomNavigation.Finance -> viewModel.setHomeScreenTitle("Finance")
                AdminBottomNavigation.LeaveRequests -> viewModel.setHomeScreenTitle("Leave Requests")
            }
        }

        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is ToastUiEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        HorizontalPager(
            count = navigationItems.size,
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (navigationItems[page]) {
                AdminBottomNavigation.Employees -> {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleEmployeeSection)
                    EmployeeListScreen(navController, viewModel)
                }

                AdminBottomNavigation.Clients -> {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleClientSection)
                    ClientListScreen(navController, viewModel)
                }

                AdminBottomNavigation.Tasks -> {
                    TaskListScreen(navController, viewModel)
                }

                AdminBottomNavigation.Finance -> {
                    FinanceScreenComposable()
                }

                AdminBottomNavigation.LeaveRequests -> {
                    LeaveRequestListScreen(viewModel)
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
                    singleLine = true,
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
                    singleLine = true,
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
                singleLine = true,
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
fun LeaveRequestTaskItem(
    leaveRequest: LeaveRequest,
    onApproval: () -> Unit,
    onReject: () -> Unit
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

            Column(
                modifier = Modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onApproval,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun CardItem(
    title: String,
    cardBorder: BorderStroke? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = cardBorder ?: BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
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

@Composable
fun TaskCardItem(
    title: String,
    cardBorder: BorderStroke? = null,
    subtitle: String? = null,
    priority: TaskPriority,
    onClick: () -> Unit,
    creationTime: String,
    currentState: TaskStatusType
) {
    val statusColor = when (currentState) {
        TaskStatusType.IN_PROGRESS -> Color.Yellow
        TaskStatusType.IN_REVIEW -> Color.Magenta
        else -> Color.Gray
    }

    val showStatusChip =
        currentState != TaskStatusType.COMPLETED && currentState != TaskStatusType.BACKLOG

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = cardBorder ?: BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1: Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (showStatusChip) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = currentState.name,
                            fontSize = 14.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Subtitle + Date (left), Priority (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = getBorderColorBasedOnTaskPriority(priority).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, getBorderColorBasedOnTaskPriority(priority)),
                    ) {
                        Text(
                            text = priority.name,
                            color = getBorderColorBasedOnTaskPriority(priority),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(50)) {
                    Text(
                        text = formatTimeStampToGetJustDate(creationTime),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


@Composable
fun LeaveRequestListScreen(viewModel: AdminHomeScreenViewModel) {
    val state by viewModel.adminHomeScreenState.collectAsState()
    val isLoading by viewModel.adminHomeScreenLoadingState.collectAsState()
    if (isLoading.isLeaveRequestsLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        if (state.activeLeaveRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No leave requests from Employees!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(state.activeLeaveRequests.size) { index ->
                    LeaveRequestTaskItem(
                        leaveRequest = state.activeLeaveRequests[index],
                        onApproval = {
                            viewModel.onLeaveRequestApproved(state.activeLeaveRequests[index])
                        },
                        onReject = {
                            viewModel.onLeaveRequestRejected(state.activeLeaveRequests[index])
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ClientListScreen(navController: NavHostController, viewModel: AdminHomeScreenViewModel) {
    val state = viewModel.adminHomeScreenState.collectAsState()
    val isLoading = viewModel.adminHomeScreenLoadingState.collectAsState().value.isClientsLoading

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        if (state.value.clientList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Clients found, tap on Add clients",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(state.value.clientList.size) { index ->
                    CardItem(
                        title = state.value.clientList[index].clientName,
                        onClick = { navController.navigate("client_detail/${state.value.clientList[index].clientId}") },
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeListScreen(navController: NavHostController, viewModel: AdminHomeScreenViewModel) {
    val state = viewModel.adminHomeScreenState.collectAsState()
    val isLoading = viewModel.adminHomeScreenLoadingState.collectAsState().value.isEmployeesLoading

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        if (state.value.employeeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Employees found, tap on Add Employees",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(state.value.employeeList.size) { index ->
                    CardItem(
                        title = state.value.employeeList[index].employeeName,
                        onClick = { navController.navigate("employee_detail/${state.value.employeeList[index].employeeId}") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TaskListScreen(navController: NavHostController, viewModel: AdminHomeScreenViewModel) {
    val state by viewModel.adminHomeScreenState.collectAsState()
    val loadingState by viewModel.adminHomeScreenLoadingState.collectAsState()

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val tabTitles = listOf("Active", "Backlog", "Completed")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = darkSlateBlue,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleActiveTaskSection)
                    TaskListContent(
                        tasks = state.activeTaskList,
                        tasksListName = "Active Tasks",
                        isLoading = loadingState.isActiveTasksLoading,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        }
                    )
                }

                1 -> {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleBacklogTaskSection)
                    TaskListContent(
                        tasks = state.backlogTaskList,
                        tasksListName = "Backlog Tasks",
                        isLoading = loadingState.isBacklogTasksLoading,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        }
                    )
                }

                2 -> {
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleCompletedTaskSection)
                    TaskListContent(
                        tasks = state.completedTasksList,
                        tasksListName = "Completed Tasks",
                        isLoading = loadingState.isCompletedTasksLoading,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskListContent(
    tasks: List<ClientTask>,
    tasksListName: String = "tasks",
    isLoading: Boolean,
    order: DateOrder,
    navController: NavHostController,
    orderChangeEvent: (DateOrder) -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        tasks.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No $tasksListName found", style = MaterialTheme.typography.bodyLarge)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    DateOrderComposable(order, orderChangeEvent)
                }
                items(tasks.size) { index ->
                    TaskCardItem(
                        title = tasks[index].taskName,
                        subtitle = tasks[index].taskType.name,
                        creationTime = tasks[index].taskCreationTime,
                        currentState = tasks[index].currentStatus,
                        cardBorder = BorderStroke(
                            width = 2.dp,
                            color = getBorderColorBasedOnTaskPriority(tasks[index].taskPriority)
                        ),
                        priority = tasks[index].taskPriority,
                        onClick = {
                            navController.navigate("task_detail/${tasks[index].taskId}/$ADMIN_NAME")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateOrderComposable(taskDateOrder: DateOrder, onTaskDateOrderChange: (DateOrder) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by Date Created",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = {
                val newOrder =
                    if (taskDateOrder is DateOrder.Ascending)
                        DateOrder.Descending else DateOrder.Ascending
                onTaskDateOrderChange(newOrder)
            }
        ) {
            Icon(
                imageVector = if (taskDateOrder is DateOrder.Ascending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Sort Order"
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}