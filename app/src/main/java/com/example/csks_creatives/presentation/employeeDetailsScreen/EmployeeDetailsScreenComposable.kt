package com.example.csks_creatives.presentation.employeeDetailsScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.presentation.components.EmployeeProfileComponent
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.components.ui.PaginationLoader
import com.example.csks_creatives.presentation.components.ui.isAtBottom
import com.example.csks_creatives.presentation.components.white
import com.example.csks_creatives.presentation.employeeDetailsScreen.components.EmployeeTaskCard
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.EmployeeDetailsScreenViewModel
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.navigation.EmployeeDetailsScreenBottomNavigation
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            listState.isAtBottom()
        }
    }

    LaunchedEffect(shouldLoadMore.value, state.value.isActiveTasksLoading, state.value.isCompletedTasksLoading, state.value.isPaginationLoading, state.value.isEndReached, pagerState.currentPage) {
        if (shouldLoadMore.value && !state.value.isActiveTasksLoading && !state.value.isCompletedTasksLoading && !state.value.isPaginationLoading && !state.value.isEndReached && (pagerState.currentPage == 0 || pagerState.currentPage == 1)) {
            viewModel.onEvent(EmployeeDetailsScreenEvent.LoadMoreTasks)
        }
    }

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
        containerColor = darkSlateBlue,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = adminToolbarTitle.value,
                canShowMenu = true,
                canShowSearch = pagerState.currentPage == 0 || pagerState.currentPage == 1,
                canShowBackIcon = true,
                menuItems = buildList {
                    add(ToolbarOverFlowMenuItem("force_fetch", "Force Fetch"))
                    add(ToolbarOverFlowMenuItem("logout", "Logout"))
                },
                onSearchClicked = {
                    viewModel.onEvent(EmployeeDetailsScreenEvent.ToggleSearchBarVisibility)
                },
                onBackClicked = { navController.popBackStack() },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
                        "force_fetch" -> {
                            viewModel.onEvent(EmployeeDetailsScreenEvent.ForceFetchTasks)
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
                    if (state.value.isActiveTasksLoading && state.value.tasksInProgress.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingProgress()
                        }
                    } else {
                        ActiveTasksScreen(viewModel, navController, listState)
                    }
                }

                1 -> {
                    if (state.value.isCompletedTasksLoading && state.value.tasksCompleted.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingProgress()
                        }
                    } else {
                        CompletedTasksScreen(viewModel, navController, listState)
                    }
                }

                2 -> EmployeeProfileComponent(
                    employeeName = state.value.employeeName,
                    employeeJoinedTime = state.value.employeeJoinedTime,
                    employeePassword = state.value.employeePassword,
                    totalNumberOfTasksCompleted = state.value.totalNumberOfTasksCompleted,
                    isCompletedCountLoading = state.value.isCompletedCountLoading,
                    approvedLeaves = state.value.approvedLeavesList,
                    unApprovedLeaves = state.value.unApprovedLeavesList,
                    rejectedLeaves = state.value.rejectedLeavesList,
                    onApproveLeave = { leaveRequest ->
                        viewModel.approveEmployeeLeave(leaveRequest = leaveRequest)
                    },
                    onRejectLeave = { leaveRequest ->
                        viewModel.rejectEmployeeLeave(leaveRequest = leaveRequest)
                    },
                    onFetchCompletedCount = {
                        viewModel.onEvent(EmployeeDetailsScreenEvent.FetchCompletedTasksCount)
                    },
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}

@Composable
fun ActiveTasksScreen(viewModel: EmployeeDetailsScreenViewModel, navController: NavHostController, listState: androidx.compose.foundation.lazy.LazyListState) {
    val state by viewModel.employeeDetailsScreenState.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (state.tasksInProgress.isEmpty() && !state.isActiveTasksLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active tasks found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = white,
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
            LazyColumn(state = listState) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sort by Date:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = white,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AssistChip(
                            onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Ascending)) },
                            label = { Text("Oldest First") },
                            colors = AssistChipDefaults.assistChipColors(labelColor = silverGrey),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Descending)) },
                            label = { Text("Newest First") },
                            colors = AssistChipDefaults.assistChipColors(labelColor = silverGrey),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.2f))
                        )
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
                if (state.isPaginationLoading) {
                    item { PaginationLoader() }
                }
            }
        }
    }
}

@Composable
fun CompletedTasksScreen(
    viewModel: EmployeeDetailsScreenViewModel,
    navController: NavHostController,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val state by viewModel.employeeDetailsScreenState.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (state.tasksCompleted.isEmpty() && !state.isCompletedTasksLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Completed tasks found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = white,
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
            LazyColumn(state = listState) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sort by Date:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = white,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AssistChip(
                            onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Ascending)) },
                            label = { Text("Oldest First") },
                            colors = AssistChipDefaults.assistChipColors(labelColor = silverGrey),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Descending)) },
                            label = { Text("Newest First") },
                            colors = AssistChipDefaults.assistChipColors(labelColor = silverGrey),
                            border = BorderStroke(1.dp, silverGrey.copy(alpha = 0.2f))
                        )
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
                if (state.isPaginationLoading) {
                    item { PaginationLoader() }
                }
            }
        }
    }
}


