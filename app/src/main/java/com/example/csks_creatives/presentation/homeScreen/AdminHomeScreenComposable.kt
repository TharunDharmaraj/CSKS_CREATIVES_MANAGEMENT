package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveDuration
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPriority
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskType
import com.example.csks_creatives.presentation.components.charCoal
import com.example.csks_creatives.presentation.components.charCoalPurple
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.displayName
import com.example.csks_creatives.presentation.components.goldenRod
import com.example.csks_creatives.presentation.components.grey
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority
import com.example.csks_creatives.presentation.components.icon
import com.example.csks_creatives.presentation.components.limeGreen
import com.example.csks_creatives.presentation.components.red
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.transparent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.components.ui.ModernDateView
import com.example.csks_creatives.presentation.components.ui.PaginationLoader
import com.example.csks_creatives.presentation.components.ui.isAtBottom
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.AdminHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AddClientDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AddEmployeeDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.AdminHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.navigation.AdminBottomNavigation
import com.example.csks_creatives.presentation.taskDetailScreen.components.ModernTaskTextField
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
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
//        AdminBottomNavigation.Finance,
        AdminBottomNavigation.LeaveRequests
    )

    Scaffold(
        containerColor = darkSlateBlue,
        topBar = {
            AppToolbar(
                title = adminToolbarTitle.value,
                canShowLogo = true,
                canShowMenu = true,
                menuItems = buildList {
                    val currentTab = navigationItems[pagerState.currentPage]
                    if (currentTab != AdminBottomNavigation.LeaveRequests 
//                        && currentTab != AdminBottomNavigation.Finance
                        ) {
                        add(ToolbarOverFlowMenuItem("force_fetch", "Force Fetch"))
                    }
                    add(ToolbarOverFlowMenuItem("add_employee", "Add Employee"))
                    add(ToolbarOverFlowMenuItem("add_client", "Add Client"))
                    add(ToolbarOverFlowMenuItem("logout", "Logout"))
                },
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

                        "force_fetch" -> {
                            when (navigationItems[pagerState.currentPage]) {
                                AdminBottomNavigation.Tasks -> viewModel.onHomeScreenEvent(
                                    AdminHomeScreenEvent.ForceFetchTasks
                                )

                                AdminBottomNavigation.Employees -> viewModel.onHomeScreenEvent(
                                    AdminHomeScreenEvent.ForceFetchEmployees
                                )

                                AdminBottomNavigation.Clients -> viewModel.onHomeScreenEvent(
                                    AdminHomeScreenEvent.ForceFetchClients
                                )

                                AdminBottomNavigation.LeaveRequests -> viewModel.onHomeScreenEvent(
                                    AdminHomeScreenEvent.ForceFetchLeaveRequests
                                )
                            }
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
            val currentTab = navigationItems[pagerState.currentPage]
            when (currentTab) {
                AdminBottomNavigation.Employees -> {
                    viewModel.setHomeScreenTitle("Employees")
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleEmployeeSection)
                }

                AdminBottomNavigation.Clients -> {
                    viewModel.setHomeScreenTitle("Clients")
                    viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleClientSection)
                }

                AdminBottomNavigation.Tasks -> {
                    viewModel.setHomeScreenTitle("My Tasks")
                }

//                AdminBottomNavigation.Finance -> viewModelewModel.setHomeScreenTitle("Finance")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (navigationItems[page]) {
                AdminBottomNavigation.Employees -> {
                    EmployeeListScreen(navController, viewModel)
                }

                AdminBottomNavigation.Clients -> {
                    ClientListScreen(navController, viewModel)
                }

                AdminBottomNavigation.Tasks -> {
                    TaskListScreen(navController, viewModel)
                }

//                AdminBottomNavigation.Finance -> {
//                    FinanceScreenComposable()
//                }

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
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = {
            viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.CloseDialogButtonClicked)
        },
        containerColor = charCoal,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "New Team Member",
                style = MaterialTheme.typography.headlineSmall,
                color = white,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModernTaskTextField(
                    value = state.value.employeeName,
                    onValueChange = {
                        viewModel.onEmployeeDialogEvent(
                            AddEmployeeDialogEvent.EmployeeNameTextFieldChanged(it)
                        )
                    },
                    label = "Employee Name",
                    icon = Icons.Default.Person,
                    focusManager = focusManager
                )
                ModernTaskTextField(
                    value = state.value.employeePassword,
                    onValueChange = {
                        viewModel.onEmployeeDialogEvent(
                            AddEmployeeDialogEvent.EmployeeNamePasswordFieldChanged(it)
                        )
                    },
                    label = "Secure Password",
                    icon = Icons.Default.Lock,
                    focusManager = focusManager
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.AddEmployeeButtonClicked)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = vividCerulean),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Member", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.onEmployeeDialogEvent(AddEmployeeDialogEvent.CloseDialogButtonClicked)
            }) {
                Text("Cancel", color = silverGrey)
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
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { viewModel.onClientDialogEvent(AddClientDialogEvent.CloseDialogButtonClicked) },
        containerColor = charCoal,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Register Client",
                style = MaterialTheme.typography.headlineSmall,
                color = white,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            ModernTaskTextField(
                value = state.value.clientName,
                onValueChange = {
                    viewModel.onClientDialogEvent(
                        AddClientDialogEvent.ClientNameTextFieldChanged(it)
                    )
                },
                label = "Client Name",
                icon = Icons.Default.Person,
                focusManager = focusManager
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.onClientDialogEvent(AddClientDialogEvent.AddClientButtonClicked)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = goldenRod),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Client", color = white, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onClientDialogEvent(AddClientDialogEvent.CloseDialogButtonClicked) }) {
                Text("Cancel", color = silverGrey)
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
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(vividCerulean.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = vividCerulean,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = leaveRequest.postedBy,
                        style = MaterialTheme.typography.titleMedium,
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    ModernDateView(leaveRequest.leaveDate.toDate().time.toString(), useRelativeTime = false)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (leaveRequest.leaveDuration == LeaveDuration.HALF_DAY) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = vividCerulean.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, vividCerulean.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "HALF DAY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = vividCerulean
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = leaveRequest.leaveReason,
                style = MaterialTheme.typography.bodyMedium,
                color = silverGrey,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, red.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = red)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onApproval,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = limeGreen)
                ) {
                    Text("Approve", color = white)
                }
            }
        }
    }
}
//
//@Composable
//fun CardItem(
//    title: String,
//    cardBorder: BorderStroke? = null,
//    subtitle: String? = null,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(12.dp),
//        border = cardBorder ?: BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            subtitle?.let {
//                Text(
//                    text = it,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCardItem(
    title: String,
    cardBorder: BorderStroke? = null,
    taskType: TaskType,
    priority: TaskPriority,
    onClick: () -> Unit,
    creationTime: String,
    currentState: TaskStatusType,
    estimate: Int
) {
    val statusColor = when (currentState) {
        TaskStatusType.IN_PROGRESS -> vividCerulean
        TaskStatusType.IN_REVIEW -> Color.Magenta
        TaskStatusType.PAUSED -> Color.Yellow
        TaskStatusType.BACKLOG -> Color.Red
        TaskStatusType.COMPLETED -> limeGreen
        else -> grey
    }

    val showStatusChip =
        currentState != TaskStatusType.COMPLETED && currentState != TaskStatusType.BACKLOG
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        border = cardBorder ?: BorderStroke(1.dp, charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    color = white,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (showStatusChip) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = currentState.name,
                            fontSize = 10.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Metadata chips with wrapping
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = taskType.icon,
                        contentDescription = null,
                        tint = silverGrey.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = taskType.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = silverGrey
                    )
                }

                val priorityColor = getBorderColorBasedOnTaskPriority(priority)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = priorityColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.5.dp, priorityColor.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = priority.name,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = vividCerulean,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${estimate}h",
                            color = vividCerulean,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ModernDateView(creationTime)
            }
        }
    }
}

@Composable
fun LeaveRequestListScreen(viewModel: AdminHomeScreenViewModel) {
    val state by viewModel.adminHomeScreenState.collectAsState()
    val isLoading by viewModel.adminHomeScreenLoadingState.collectAsState()

    if (isLoading.isLeaveRequestsLoading && state.activeLeaveRequests.isEmpty()) {
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
fun EmployeeCard(
    employee: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = vividCerulean.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = vividCerulean.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.employeeName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = vividCerulean,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.employeeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Task Count Badge - Modern and Professional
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = limeGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, limeGreen.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = limeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${employee.numberOfTasksCompleted.ifEmpty { "0" }} Tasks",
                                style = MaterialTheme.typography.labelSmall,
                                color = white.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (employee.joinedTime.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Joined ",
                                style = MaterialTheme.typography.labelSmall,
                                color = silverGrey.copy(alpha = 0.5f)
                            )
                            ModernDateView(
                                timeStamp = employee.joinedTime,
                                useRelativeTime = false,
                                showTime = false
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = silverGrey.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ClientCard(
    client: Client,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = goldenRod.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = goldenRod.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = goldenRod,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Client ID: ${client.clientId.take(6).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = silverGrey.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = silverGrey.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ClientListScreen(navController: NavHostController, viewModel: AdminHomeScreenViewModel) {
    val state by viewModel.adminHomeScreenState.collectAsState()
    val isLoadingState by viewModel.adminHomeScreenLoadingState.collectAsState()
    val isLoading = isLoadingState.isClientsLoading

    if (isLoading && state.clientList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        if (state.clientList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Clients found, tap on Add clients",
                    style = MaterialTheme.typography.bodyLarge,
                    color = white.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(state.clientList.size) { index ->
                    ClientCard(
                        client = state.clientList[index],
                        onClick = { navController.navigate("client_detail/${state.clientList[index].clientId}") },
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun EmployeeListScreen(navController: NavHostController, viewModel: AdminHomeScreenViewModel) {
    val state by viewModel.adminHomeScreenState.collectAsState()
    val isLoadingState by viewModel.adminHomeScreenLoadingState.collectAsState()
    val isLoading = isLoadingState.isEmployeesLoading

    if (isLoading && state.employeeList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        if (state.employeeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Employees found, tap on Add Employees",
                    style = MaterialTheme.typography.bodyLarge,
                    color = white.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(state.employeeList.size) { index ->
                    EmployeeCard(
                        employee = state.employeeList[index],
                        onClick = { navController.navigate("employee_detail/${state.employeeList[index].employeeId}") }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
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

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleActiveTaskSection)
            1 -> viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleBacklogTaskSection)
            2 -> viewModel.onHomeScreenEvent(AdminHomeScreenEvent.ToggleCompletedTaskSection)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = transparent,
            contentColor = vividCerulean,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    TaskListContent(
                        tasks = state.activeTaskList,
                        tasksListName = "Active Tasks",
                        isLoading = loadingState.isActiveTasksLoading,
                        isPaginationLoading = state.isPaginationLoading,
                        isEndReached = state.isActiveTasksEndReached,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        },
                        onLoadMore = {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.LoadMoreActiveTasks)
                        }
                    )
                }

                1 -> {
                    TaskListContent(
                        tasks = state.backlogTaskList,
                        tasksListName = "Backlog Tasks",
                        isLoading = loadingState.isBacklogTasksLoading,
                        isPaginationLoading = state.isPaginationLoading,
                        isEndReached = state.isBacklogTasksEndReached,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        },
                        onLoadMore = {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.LoadMoreBacklogTasks)
                        }
                    )
                }

                2 -> {
                    TaskListContent(
                        tasks = state.completedTasksList,
                        tasksListName = "Completed Tasks",
                        isLoading = loadingState.isCompletedTasksLoading,
                        isPaginationLoading = state.isPaginationLoading,
                        isEndReached = state.isCompletedTasksEndReached,
                        navController = navController,
                        order = state.tasksOrder,
                        orderChangeEvent = { dateOrder ->
                            viewModel.onHomeScreenEvent(
                                AdminHomeScreenEvent.ToggleOrderDate(dateOrder)
                            )
                        },
                        onLoadMore = {
                            viewModel.onHomeScreenEvent(AdminHomeScreenEvent.LoadMoreCompletedTasks)
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
    isPaginationLoading: Boolean = false,
    isEndReached: Boolean = false,
    order: DateOrder,
    navController: NavHostController,
    orderChangeEvent: (DateOrder) -> Unit,
    onLoadMore: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            listState.isAtBottom()
        }
    }

    LaunchedEffect(shouldLoadMore.value, isLoading, isPaginationLoading, isEndReached) {
        if (shouldLoadMore.value && !isLoading && !isPaginationLoading && !isEndReached) {
            onLoadMore()
        }
    }

    when {
        isLoading && tasks.isEmpty() -> {
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
                Text(
                    text = "No $tasksListName found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = white
                )
            }
        }

        else -> {
            LazyColumn(
                state = listState,
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
                        taskType = tasks[index].taskType,
                        creationTime = tasks[index].taskCreationTime,
                        currentState = tasks[index].currentStatus,
                        cardBorder = BorderStroke(
                            width = 2.dp,
                            color = getBorderColorBasedOnTaskPriority(
                                tasks[index].taskPriority,
                                isTaskCompleted = tasks[index].currentStatus == TaskStatusType.COMPLETED
                            ).copy(alpha = 0.5f)
                        ),
                        priority = tasks[index].taskPriority,
                        estimate = tasks[index].taskEstimate,
                        onClick = {
                            navController.navigate("task_detail/${tasks[index].taskId}/$ADMIN_NAME")
                        }
                    )
                }

                if (isPaginationLoading) {
                    item {
                        PaginationLoader()
                    }
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
            fontWeight = FontWeight.Bold,
            color = white
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
                contentDescription = "Sort Order",
                tint = vividCerulean
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
