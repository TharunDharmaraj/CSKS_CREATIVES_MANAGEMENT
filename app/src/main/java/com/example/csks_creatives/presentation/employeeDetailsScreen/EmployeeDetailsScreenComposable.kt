package com.example.csks_creatives.presentation.employeeDetailsScreen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.employeeDetailsScreen.components.EmployeeTaskCard
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.EmployeeDetailsScreenViewModel
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.navigation.EmployeeDetailsScreenBottomNavigation
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state.EmployeeDetailsScreenState
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun EmployeeDetailsScreen(
    employeeId: String,
    navController: NavHostController,
    viewModel: EmployeeDetailsScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val adminToolbarTitle = viewModel.employeeDetailsScreenTitle.collectAsState()
    val navigationItems = listOf(
        EmployeeDetailsScreenBottomNavigation.ActiveTasks,
        EmployeeDetailsScreenBottomNavigation.CompletedTasks,
        EmployeeDetailsScreenBottomNavigation.Profile
    )

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ToastUiEvent.ShowToast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val state = viewModel.employeeDetailsScreenState.collectAsState()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initialize(employeeId)
    }

    LaunchedEffect(pagerState.currentPage) {
        when (navigationItems[pagerState.currentPage]) {
            EmployeeDetailsScreenBottomNavigation.Profile -> viewModel.setEmployeeDetailsScreenToolbarTitle(
                "Profile - $employeeId"
            )

            EmployeeDetailsScreenBottomNavigation.ActiveTasks -> viewModel.setEmployeeDetailsScreenToolbarTitle(
                "Active - $employeeId"
            )

            EmployeeDetailsScreenBottomNavigation.CompletedTasks -> viewModel.setEmployeeDetailsScreenToolbarTitle(
                "Completed - $employeeId"
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = adminToolbarTitle.value,
                canShowMenu = true,
                canShowSearch = pagerState.currentPage == 0 || pagerState.currentPage == 1,
                canShowBackIcon = true,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                onSearchClicked = {
                    viewModel.onEvent(EmployeeDetailsScreenEvent.ToggleSearchBarVisibility)
                },
                onBackClicked = { navController.popBackStack() },
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
                val tabs = listOf("Active", "Completed", "Details")
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    "Active" -> Icons.AutoMirrored.Filled.ArrowForward
                                    "Completed" -> Icons.Rounded.CheckCircle
                                    else -> Icons.Default.Person
                                },
                                contentDescription = tab
                            )
                        },
                        label = { Text(tab) }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (page) {
                0 -> {
                    if (state.value.isActiveTasksLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingProgress()
                        }
                    } else {
                        ActiveTasksScreen(viewModel, navController)
                    }
                }

                1 -> {
                    if (state.value.isCompletedTasksLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingProgress()
                        }
                    } else {
                        CompletedTasksScreen(viewModel, navController)
                    }
                }

                2 -> EmployeeDetailSection(
                    state.value,
                    onApproveLeave = { leaveRequest ->
                        viewModel.approveEmployeeLeave(leaveRequest = leaveRequest)
                    },
                    onRejectLeave = { leaveRequest ->
                        viewModel.rejectEmployeeLeave(leaveRequest = leaveRequest)
                    },
                    coroutineScope
                )
            }
        }
    }
}

@Composable
fun ActiveTasksScreen(viewModel: EmployeeDetailsScreenViewModel, navController: NavHostController) {
    val state by viewModel.employeeDetailsScreenState.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (state.tasksInProgress.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active tasks found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        } else {
            if (state.isActiveTasksSectionVisible && state.isSearchBarVisible) {
                OutlinedTextField(
                    value = state.searchTextForActive,
                    onValueChange = {
                        viewModel.onEvent(EmployeeDetailsScreenEvent.OnSearchTextChangedForActive(it))
                    },
                    label = { Text("Search Active Tasks") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            LazyColumn {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort by Date:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Ascending))
                        }) {
                            Text("Ascending")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Descending))
                        }) {
                            Text("Descending")
                        }
                    }
                }
                items(state.tasksInProgress.size) { index ->
                    EmployeeTaskCard(
                        task = state.tasksInProgress[index],
                        onClick = {
                            navController.navigate("task_detail/${state.tasksInProgress[index].taskId}/$ADMIN_NAME")
                        },
                        timeTaken = viewModel.getTimeTakenForActiveTask(state.tasksInProgress[index].taskId)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedTasksScreen(
    viewModel: EmployeeDetailsScreenViewModel,
    navController: NavHostController
) {
    val state by viewModel.employeeDetailsScreenState.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (state.tasksCompleted.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Completed tasks found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        } else {
            if (state.isCompletedTasksSectionVisible && state.isSearchBarVisible) {
                OutlinedTextField(
                    value = state.searchTextForCompleted,
                    onValueChange = {
                        viewModel.onEvent(
                            EmployeeDetailsScreenEvent.OnSearchTextChangedForCompleted(
                                it
                            )
                        )
                    },
                    label = { Text("Search Completed Tasks") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            LazyColumn {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort by Date:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Ascending))
                        }) {
                            Text("Ascending")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Descending))
                        }) {
                            Text("Descending")
                        }
                    }
                }
                items(state.tasksCompleted.size) { index ->
                    EmployeeTaskCard(
                        task = state.tasksCompleted[index],
                        onClick = {
                            navController.navigate("task_detail/${state.tasksCompleted[index].taskId}/$ADMIN_NAME")
                        },
                        timeTaken = viewModel.getTimeTakenForCompletion(state.tasksCompleted[index].taskId)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun EmployeeDetailSection(
    state: EmployeeDetailsScreenState,
    onApproveLeave: (LeaveRequest) -> Unit,
    onRejectLeave: (LeaveRequest) -> Unit,
    coroutineScope: CoroutineScope
) {
    val pagerState = rememberPagerState(initialPage = 0)
    val tabTitles = listOf("Unapproved", "Approved", "Rejected", "Summary")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Name: ${state.employeeName}", fontWeight = FontWeight.Bold)
        Text("Password: ${state.employeePassword}")
        Text("Joined On: ${state.employeeJoinedTime}")
        Text("Total Tasks Completed: ${state.totalNumberOfTasksCompleted}")
        Text("Total Leaves Taken: ${state.approvedLeavesList.size}")


        Spacer(modifier = Modifier.height(24.dp))

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    if (state.unApprovedLeavesList.isEmpty()) {
                        Text("No unapproved leave requests.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.unApprovedLeavesList.size) { index ->
                                LeaveRequestTaskItem(
                                    leaveRequest = state.unApprovedLeavesList[index],
                                    onApproval = { onApproveLeave(state.unApprovedLeavesList[index]) },
                                    onReject = { onRejectLeave(state.unApprovedLeavesList[index]) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    if (state.approvedLeavesList.isEmpty()) {
                        Text("No approved leave requests.")
                    } else {
                        val now = Date()
                        val (futureLeaves, pastLeaves) = state.approvedLeavesList.partition {
                            it.leaveDate.toDate().after(now)
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (futureLeaves.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Future Leaves",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(futureLeaves.size) { index ->
                                    LeaveRequestTaskItem(leaveRequest = futureLeaves[index])
                                }
                            }

                            if (pastLeaves.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Past Leaves",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(pastLeaves.size) { index ->
                                    LeaveRequestTaskItem(leaveRequest = pastLeaves[index])
                                }
                            }

                            if (futureLeaves.isEmpty() && pastLeaves.isEmpty()) {
                                item {
                                    Text("No approved leave requests.")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (state.rejectedLeavesList.isEmpty()) {
                        Text("No rejected leave requests.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.rejectedLeavesList.size) { index ->
                                LeaveRequestTaskItem(
                                    leaveRequest = state.rejectedLeavesList[index],
                                    onApproval = { onApproveLeave(state.rejectedLeavesList[index]) }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    if (state.approvedLeavesList.isEmpty()) {
                        Text("No approved leaves to summarize.")
                    } else {
                        val groupedByYearMonth = state.approvedLeavesList
                            .sortedByDescending { it.leaveDate.toDate() }
                            .groupBy {
                                val date = it.leaveDate.toDate()
                                val month =
                                    SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
                                val year =
                                    SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                                "$month $year"
                            }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            groupedByYearMonth.forEach { (monthYear, leaves) ->
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$monthYear: ${leaves.size} leaves",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveRequestTaskItem(
    leaveRequest: LeaveRequest,
    onApproval: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
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
                if (onApproval != null) {
                    Button(
                        onClick = onApproval,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .height(40.dp)
                    ) {
                        Text("Approve")
                    }
                }
                if (onReject != null) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .height(40.dp)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}