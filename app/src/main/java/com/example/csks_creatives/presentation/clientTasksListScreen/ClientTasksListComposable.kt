package com.example.csks_creatives.presentation.clientTasksListScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.ClientTasksListViewModel
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.components.DateOrder
import com.example.csks_creatives.presentation.components.TaskItem
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem

@Composable
fun ClientTasksListComposable(
    clientId: String,
    navController: NavController,
    viewModel: ClientTasksListViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Client $clientId",
                canShowSearch = true,
                canShowBackIcon = true,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                onSearchClicked = {
                    viewModel.onEvent(ClientTasksListScreenEvent.ToggleSearchBarClicked)
                },
                onBackClicked = { navController.popBackStack() },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
                        "logout" -> {
                            viewModel.emitLogoutEvent(true)
                            navController.navigate("login")
                        }
                    }
                }
            )
        }
    ) { paddingValue ->
        val state = viewModel.clientsTasksListState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.initialize(clientId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
                .padding(16.dp)
        ) {
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort by Date Created", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = {
                        val newOrder = if (state.value.tasksOrder is DateOrder.Ascending)
                            DateOrder.Descending else DateOrder.Ascending
                        viewModel.onEvent(ClientTasksListScreenEvent.Order(newOrder))
                    }
                ) {
                    Icon(
                        imageVector = if (state.value.tasksOrder is DateOrder.Ascending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Sort Order"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Filter by Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            checked = state.value.selectedStatuses.contains(TaskStatusType.entries[index]),
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

            LazyColumn {
                items(state.value.tasksList.size) { index ->
                    val clientTask = state.value.tasksList[index]
                    TaskItem(task = clientTask, onTaskClick = {
                        navController.navigate("task_detail/${clientTask.taskId}/$ADMIN_NAME")
                    })
                }
            }
        }
    }
}
