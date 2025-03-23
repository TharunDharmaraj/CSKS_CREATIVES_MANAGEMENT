package com.example.csks_creatives.presentation.employeeDetailsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.navigation.NavHostController
import com.example.csks_creatives.data.utils.Constants.ADMIN_NAME
import com.example.csks_creatives.presentation.components.DateOrder
import com.example.csks_creatives.presentation.employeeDetailsScreen.components.EmployeeTaskCard
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.EmployeeDetailsScreenViewModel
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem

@Composable
fun EmployeeDetailsScreen(
    employeeId: String,
    navController: NavHostController,
    viewModel: EmployeeDetailsScreenViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            AppToolbar(
                title = employeeId,
                canShowMenu = true,
                canShowSearch = true,
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
        }
    ) { paddingValue ->
        val state = viewModel.employeeDetailsScreenState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.initialize(employeeId)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .padding(paddingValue)
        ) {
            item {
                if (state.value.isSearchBarVisible) {
                    OutlinedTextField(value = state.value.searchText,
                        onValueChange = {
                            viewModel.onEvent(
                                EmployeeDetailsScreenEvent.OnSearchTextChanged(
                                    it
                                )
                            )
                        },
                        label = { Text("Search Tasks") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Icon"
                            )
                        })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Employee Name: ${state.value.employeeName}", fontWeight = FontWeight.Bold)
                Text("Employee Password: ${state.value.employeePassword}")
                Text("Joined On: ${state.value.employeeJoinedTime}")
                Text("Total Tasks Completed: ${state.value.totalNumberOfTasksCompleted}")

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort by Date: ", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Ascending)) }) {
                        Text("Ascending")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.onEvent(EmployeeDetailsScreenEvent.Order(DateOrder.Descending)) }) {
                        Text("Descending")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    viewModel.onEvent(EmployeeDetailsScreenEvent.ToggleActiveTasksSectionVisibility)
                }) {
                    Text("Active Tasks", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Icon(
                        imageVector = if (state.value.isActiveTasksSectionVisible) Icons.Default.Clear else Icons.Default.Add,
                        contentDescription = "Expand"
                    )
                }
            }
            if (state.value.isActiveTasksSectionVisible) {
                items(state.value.tasksInProgress.size) { index ->
                    val task = state.value.tasksInProgress[index]
                    EmployeeTaskCard(task = task, onClick = {
                        navController.navigate("task_detail/${task.taskId}/$ADMIN_NAME")
                    }, timeTaken = viewModel.getTimeTakenForActiveTask(task.taskId))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    viewModel.onEvent(EmployeeDetailsScreenEvent.ToggleCompletedTasksSectionVisibility)
                }) {
                    Text("Completed Tasks", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Icon(
                        imageVector = if (state.value.isCompletedTasksSectionVisible) Icons.Default.Clear else Icons.Default.Add,
                        contentDescription = "Expand"
                    )
                }
            }
            if (state.value.isCompletedTasksSectionVisible) {
                items(state.value.tasksCompleted.size) { index ->
                    val task = state.value.tasksCompleted[index]
                    EmployeeTaskCard(
                        task = task,
                        onClick = {
                            navController.navigate("task_detail/${task.taskId}/$ADMIN_NAME")
                        },
                        timeTaken = viewModel.getTimeTakenForCompletion(
                            task.taskId
                        )
                    )
                }
            }
        }
    }
}