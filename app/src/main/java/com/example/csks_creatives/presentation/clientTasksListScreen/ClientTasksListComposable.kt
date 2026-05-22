package com.example.csks_creatives.presentation.clientTasksListScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.presentation.clientTasksListScreen.components.ClientCostBreakDown
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.ClientTasksListViewModel
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.EditClientNameDialogEvent
import com.example.csks_creatives.presentation.components.charCoal
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.components.goldenRod
import com.example.csks_creatives.presentation.components.limeGreen
import com.example.csks_creatives.presentation.components.red
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.transparent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.components.ui.PaginationLoader
import com.example.csks_creatives.presentation.components.ui.TaskItem
import com.example.csks_creatives.presentation.components.ui.isAtBottom
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
import com.example.csks_creatives.presentation.taskDetailScreen.components.ModernTaskTextField
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ClientTasksListComposable(
    clientId: String,
    navController: NavController,
    viewModel: ClientTasksListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.clientsTasksListState.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    val toolbarTitle = viewModel.clientName.collectAsState().value

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            listState.isAtBottom()
        }
    }

    LaunchedEffect(shouldLoadMore.value, state.value.isLoading, state.value.isPaginationLoading, state.value.isEndReached, pagerState.currentPage) {
        if (shouldLoadMore.value && !state.value.isLoading && !state.value.isPaginationLoading && !state.value.isEndReached && pagerState.currentPage == 0) {
            viewModel.onEvent(ClientTasksListScreenEvent.LoadMoreTasks)
        }
    }

    val tabTitles = listOf("Tasks", "Amounts")

    LaunchedEffect(Unit) {
        viewModel.initialize(clientId)
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

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setFilterAndSearchIconVisibility(pagerState.currentPage == 0)
    }

    Scaffold(
        containerColor = darkSlateBlue,
        topBar = {
            AppToolbar(
                title = "Client $toolbarTitle",
                canShowMenu = true,
                canShowSearch = state.value.canShowSearchIcon,
                canShowFilterTasks = state.value.isFilterTasksIconVisible,
                canShowBackIcon = true,
                menuItems = buildList {
                    add(ToolbarOverFlowMenuItem("editClient", "Edit Client Name"))
                    if (pagerState.currentPage == 0) {
                        add(ToolbarOverFlowMenuItem("force_fetch", "Force Fetch"))
                    }
                    add(ToolbarOverFlowMenuItem("logout", "Logout"))
                },
                onFilterTasksIconClicked = {
                    viewModel.onEvent(ClientTasksListScreenEvent.ToggleFilterTasksClicked)
                },
                onSearchClicked = {
                    viewModel.onEvent(ClientTasksListScreenEvent.ToggleSearchBarClicked)
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

                        "editClient" -> {
                            viewModel.makeEmployeeEditDialogVisible()
                        }

                        "force_fetch" -> {
                            viewModel.onEvent(ClientTasksListScreenEvent.ForceFetchTasks)
                        }
                    }
                }
            )
        }
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = transparent,
                contentColor = vividCerulean,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) }
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
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint = vividCerulean
                                                    )
                                                },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = white,
                                                    unfocusedTextColor = white,
                                                    focusedBorderColor = vividCerulean,
                                                    unfocusedBorderColor = silverGrey.copy(alpha = 0.3f)
                                                ),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Sort by Date",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = white
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
                                                    contentDescription = "Sort Order",
                                                    tint = vividCerulean
                                                )
                                            }
                                        }

                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            item {
                                                FilterChip(
                                                    selected = state.value.isPaidTasksVisible,
                                                    onClick = { viewModel.onEvent(ClientTasksListScreenEvent.ShowOnlyPaidTasksFilter) },
                                                    label = { Text("Paid") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = limeGreen.copy(alpha = 0.2f),
                                                        selectedLabelColor = limeGreen
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                FilterChip(
                                                    selected = state.value.isPartiallyPaidTasksVisible,
                                                    onClick = { viewModel.onEvent(ClientTasksListScreenEvent.ShowOnlyPartiallyPaidTasksFilter) },
                                                    label = { Text("Partial") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = goldenRod.copy(alpha = 0.2f),
                                                        selectedLabelColor = goldenRod
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                FilterChip(
                                                    selected = state.value.isUnpaidTasksVisible,
                                                    onClick = { viewModel.onEvent(ClientTasksListScreenEvent.ShowOnlyUnPaidTasksFilter) },
                                                    label = { Text("Unpaid") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = red.copy(alpha = 0.2f),
                                                        selectedLabelColor = red
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Filter by Status",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = silverGrey,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                        LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                                            items(TaskStatusType.entries.size) { index ->
                                                val status = TaskStatusType.entries[index]
                                                val isSelected = state.value.selectedStatuses.contains(status)
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.onEvent(ClientTasksListScreenEvent.ToggleStatusFilter(status)) },
                                                    label = { Text(status.name) },
                                                    modifier = Modifier.padding(end = 8.dp),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = vividCerulean.copy(alpha = 0.2f),
                                                        selectedLabelColor = vividCerulean
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                if (state.value.tasksList.isEmpty() && !state.value.isLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No tasks found for client",
                                            fontSize = 16.sp,
                                            color = silverGrey
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(state.value.tasksList.size) { index ->
                                            TaskItem(
                                                task = state.value.tasksList[index],
                                                onTaskClick = {
                                                    navController.navigate("task_detail/${state.value.tasksList[index].taskId}/$ADMIN_NAME")
                                                },
                                                taskElapsedTime = viewModel.getTaskElapsedTime(state.value.tasksList[index])
                                            )
                                        }
                                        if (state.value.isPaginationLoading) {
                                            item { PaginationLoader() }
                                        }
                                        item { Spacer(modifier = Modifier.height(16.dp)) }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ClientCostBreakDown(
                                { viewModel.getYearlyAndMonthlyCostBreakdown() },
                                { viewModel.getTotalUnPaidCostForClient() }
                            )
                        }
                    }

                }
            }
        }
        if (state.value.isEditClientNameDialogVisible) {
            RenameClientDialog(viewModel)
        }
    }
}

@Composable
fun RenameClientDialog(viewModel: ClientTasksListViewModel) {
    val state = viewModel.editClientNameDialogState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { viewModel.onEditDialogEvent(EditClientNameDialogEvent.CancelClicked) },
        containerColor = charCoal,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Rename Client",
                style = MaterialTheme.typography.headlineSmall,
                color = white,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            ModernTaskTextField(
                value = state.value.clientName,
                onValueChange = {
                    viewModel.onEditDialogEvent(
                        EditClientNameDialogEvent.OnClientNameTextEdit(
                            it
                        )
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
                        viewModel.onEditDialogEvent(EditClientNameDialogEvent.SaveClicked)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = vividCerulean),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onEditDialogEvent(EditClientNameDialogEvent.CancelClicked) }) {
                Text("Cancel", color = silverGrey)
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    )
}