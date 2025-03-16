package com.example.csks_creatives.presentation.homeScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.presentation.components.DateOrder
import com.example.csks_creatives.presentation.components.LoadingProgress
import com.example.csks_creatives.presentation.components.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.EmployeeHomeScreenViewModel
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.toolbar.AppToolbar
import com.example.csks_creatives.presentation.toolbar.ToolbarOverFlowMenuItem

@Composable
fun EmployeeHomeScreenComposable(
    viewModel: EmployeeHomeScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    employeeId: String
) {
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Welcome, $employeeId",
                canShowSearch = false,
                menuItems = listOf(
                    ToolbarOverFlowMenuItem("logout", "Logout")
                ),
                onSearchClicked = { /* Ignore */ },
                onMenuItemClicked = { itemId ->
                    when (itemId) {
                        "logout" -> {
                            viewModel.emitLogoutEvent(true)
                            navController.navigate("login"){
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        val state = viewModel.employeeHomeScreenState.collectAsState()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order By:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleOrderSection) }) {
                        Text(if (state.value.isOrderByToggleVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.value.isOrderByToggleVisible) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioButton(
                            onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.Order(DateOrder.Ascending)) },
                            selected = state.value.tasksOrder == DateOrder.Ascending
                        )
                        Text("Ascending", modifier = Modifier.padding(start = 4.dp))

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.Order(DateOrder.Descending)) },
                            selected = state.value.tasksOrder == DateOrder.Descending
                        )
                        Text("Descending", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Tasks:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleActiveTasksSection) }) {
                        Text(if (state.value.isActiveTasksSectionVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.value.isActiveTasksSectionVisible) {
                items(state.value.activeTasks.size) { index ->
                    val clientTask = state.value.activeTasks[index]
                    if (state.value.isLoading) {
                        LoadingProgress()
                    }
                    TaskItemCard(state.value.activeTasks[index], onItemClick = {
                        navController.navigate("task_detail/${clientTask.taskId}/$employeeId")
                    })
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Completed Tasks:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.onEvent(EmployeeHomeScreenEvent.ToggleCompletedTasksSection) }) {
                        Text(if (state.value.isCompletedTasksSectionVisible) "Hide" else "Show")
                    }
                }
            }

            if (state.value.isCompletedTasksSectionVisible) {
                items(state.value.completedTasks.size) { index ->
                    val clientTask = state.value.completedTasks[index]
                    if (state.value.isLoading) {
                        LoadingProgress()
                    }
                    TaskItemCard(clientTask, onItemClick = {
                        navController.navigate("task_detail/${clientTask.taskId}/$employeeId")
                    })
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(task: ClientTask, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        onClick = onItemClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.taskName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: ${task.currentStatus}")
        }
    }
}