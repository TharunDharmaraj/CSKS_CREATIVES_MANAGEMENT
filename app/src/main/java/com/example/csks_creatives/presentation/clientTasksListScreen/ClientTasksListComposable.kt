package com.example.csks_creatives.presentation.clientTasksListScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.presentation.clientTasksListScreen.components.ClientCostBreakDown
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.ClientTasksListViewModel
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.components.ui.TaskItem
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalPagerApi::class)
@Composable
fun ClientTasksListComposable(
    clientId: String,
    navController: NavController,
    viewModel: ClientTasksListViewModel = hiltViewModel()
) {
    val state = viewModel.clientsTasksListState.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    val tabTitles = listOf("Tasks", "Amounts")

    LaunchedEffect(Unit) {
        viewModel.initialize(clientId)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setFilterAndSearchIconVisibility(pagerState.currentPage == 0)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppToolbar(
                title = "Client $clientId",
                canShowMenu = true,
                canShowSearch = state.value.canShowSearchIcon,
                canShowFilterTasks = state.value.isFilterTasksIconVisible,
                canShowBackIcon = true,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                onFilterTasksIconClicked = {
                    viewModel.onEvent(ClientTasksListScreenEvent.ToggleFilterTasksClicked)
                },
                onSearchClicked = {
                    viewModel.onEvent(ClientTasksListScreenEvent.ToggleSearchBarClicked)
                },
                onBackClicked = { navController.popBackStack() },
                onMenuItemClicked = { itemId ->
                    if (itemId == "logout") {
                        viewModel.emitLogoutEvent(true)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .padding(paddingValue)
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(count = tabTitles.size, state = pagerState) { page ->
                when (page) {
                    0 -> {
                        if (state.value.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingProgress()
                            }
                        } else {
                            Column {
                                if (state.value.isFilterSectionVisible) {
                                    Column {
                                        if (state.value.isSearchBarVisible) {
                                            OutlinedTextField(
                                                value = state.value.searchText,
                                                onValueChange = {
                                                    viewModel.onEvent(
                                                        ClientTasksListScreenEvent.OnSearchTextChanged(
                                                            it
                                                        )
                                                    )
                                                },
                                                label = { Text("Search Task") },
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = "Search"
                                                    )
                                                },
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }

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
                                                        if (state.value.tasksOrder is DateOrder.Ascending)
                                                            DateOrder.Descending else DateOrder.Ascending
                                                    viewModel.onEvent(
                                                        ClientTasksListScreenEvent.Order(
                                                            newOrder
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (state.value.tasksOrder is DateOrder.Ascending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Sort Order"
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            item {
                                                Button(
                                                    onClick = {
                                                        viewModel.onEvent(
                                                            ClientTasksListScreenEvent.ShowOnlyPaidTasksFilter
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (state.value.isPaidTasksVisible) Color.Green else Color.Gray
                                                    )
                                                ) {
                                                    Text(text = "Show Paid")
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.onEvent(
                                                            ClientTasksListScreenEvent.ShowOnlyPartiallyPaidTasksFilter
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (state.value.isPartiallyPaidTasksVisible) Color.Yellow else Color.Gray
                                                    )
                                                ) {
                                                    Text(text = "Show Partial")
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.onEvent(
                                                            ClientTasksListScreenEvent.ShowOnlyUnPaidTasksFilter
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (state.value.isUnpaidTasksVisible) Color.Red else Color.Gray
                                                    )
                                                ) {
                                                    Text(text = "Show Unpaid")
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Filter by Status",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        LazyRow {
                                            items(TaskStatusType.entries.size) { index ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .clickable {
                                                            viewModel.onEvent(
                                                                ClientTasksListScreenEvent.ToggleStatusFilter(
                                                                    TaskStatusType.entries[index]
                                                                )
                                                            )
                                                        }
                                                ) {
                                                    Checkbox(
                                                        checked = state.value.selectedStatuses.contains(
                                                            TaskStatusType.entries[index]
                                                        ),
                                                        onCheckedChange = {
                                                            viewModel.onEvent(
                                                                ClientTasksListScreenEvent.ToggleStatusFilter(
                                                                    TaskStatusType.entries[index]
                                                                )
                                                            )
                                                        }
                                                    )
                                                    Text(text = TaskStatusType.entries[index].name)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                if (state.value.tasksList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No tasks found for client",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(state.value.tasksList.size) { index ->
                                            TaskItem(
                                                task = state.value.tasksList[index],
                                                onTaskClick = {
                                                    navController.navigate("task_detail/${state.value.tasksList[index].taskId}/$ADMIN_NAME")
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        ClientCostBreakDown(
                            { viewModel.getYearlyAndMonthlyCostBreakdown() },
                            { viewModel.getTotalUnPaidCostForClient() }
                        )
                    }
                }
            }
        }
    }
}