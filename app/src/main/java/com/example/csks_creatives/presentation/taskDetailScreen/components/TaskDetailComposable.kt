package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.TaskDetailViewModel
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCommentsEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent

@Composable
fun TaskDetailComposable(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    isTaskCreation: Boolean,
    userRole: UserRole,
    taskId: String,
    employeeId: String,
    paddingValue: PaddingValues,

    ) {
    val taskState = viewModel.taskDetailState.collectAsState()
    val commentState = viewModel.taskCommentState.collectAsState()
    val visibilityState = viewModel.visibilityState.collectAsState()
    val dropDownListState = viewModel.dropDownListState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(paddingValue)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Task Details", style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = taskState.value.taskTitle,
            onValueChange = {
                if (userRole == UserRole.Admin) viewModel.onEvent(
                    TaskDetailEvent.TaskTitleTextFieldChanged(
                        it
                    )
                )
            },
            label = { Text("Task Name") },
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth()
        )

        // Task Description
        OutlinedTextField(
            value = taskState.value.taskDescription,
            onValueChange = {
                if (userRole == UserRole.Admin) viewModel.onEvent(
                    TaskDetailEvent.TaskDescriptionTextFieldChanged(
                        it
                    )
                )
            },
            label = { Text("Task Description") },
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth()
        )

        // Assigned Employee (Dropdown)
        DropdownMenuWithSelection(
            label = "Assigned Employee",
            selectedItem = dropDownListState.value.employeeList.find { it.employeeId == taskState.value.taskAssignedTo }?.employeeName
                ?: "Select",
            items = dropDownListState.value.employeeList.map { it.employeeName },
            onItemSelected = { selectedEmployee ->
                val employee =
                    dropDownListState.value.employeeList.find { it.employeeName == selectedEmployee }
                        ?: Employee()
                employee.let { viewModel.onEvent(TaskDetailEvent.TaskAssignedToEmployeeChanged(it.employeeId)) }
            },
            enabled = userRole == UserRole.Admin,
            isVisible = userRole != UserRole.Employee
        )

        // Assign Task to Client
        DropdownMenuWithSelection(
            label = "Assigned Task To Client",
            selectedItem = dropDownListState.value.clientsList.find { it.clientId == taskState.value.taskClientId }?.clientName
                ?: "Select",
            items = dropDownListState.value.clientsList.map { it.clientName },
            onItemSelected = { selectedClient ->
                val client =
                    dropDownListState.value.clientsList.find { it.clientName == selectedClient }
                client?.let { viewModel.onEvent(TaskDetailEvent.TaskClientIdChanged(it.clientId)) }
            },
            enabled = userRole == UserRole.Admin,
            isVisible = userRole != UserRole.Employee
        )

        // Story Points
        OutlinedTextField(
            value = taskState.value.taskStoryPoints.toString(),
            onValueChange = {
                if (userRole == UserRole.Admin) viewModel.onEvent(
                    TaskDetailEvent.TaskStoryPointsChanged(it.toIntOrNull() ?: 0)
                )
            },
            label = { Text("Story Points") },
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth()
        )

        // Task Status (Dropdown)
        if (isTaskCreation.not()) {
            DropdownMenuWithSelection(
                label = "Task Status",
                selectedItem = taskState.value.taskCurrentStatus.name,
                items = viewModel.getAvailableStatusOptions(),
                onItemSelected = { selectedStatus ->
                    viewModel.onEvent(
                        TaskDetailEvent.TaskStatusTypeChanged(
                            TaskStatusType.valueOf(
                                selectedStatus
                            )
                        )
                    )
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text("Task Status History", style = MaterialTheme.typography.titleMedium)

        taskState.value.taskStatusHistory.forEach { statusEntry ->
            Text("${statusEntry.taskStatusType.name} - ${statusEntry.getDurationString()}")
        }

        // Toggle Comments Section
        // TODO ALLOW COMMENTS DURING TASKS CREATION - Use a Queuing Mechanism to post tasks once Task Created
        Button(
            onClick = {
                viewModel.onEvent(TaskDetailEvent.ToggleCommentsSection)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isTaskCreation.not()
        ) {
            Text(if (visibilityState.value.isCommentsSectionVisible) "Hide Comments" else "Show Comments")
        }

        if (visibilityState.value.isCommentsSectionVisible and isTaskCreation.not()) {
            Column {
                taskState.value.taskComments.forEach { comment ->
                    Text(
                        "${comment.commentedBy}: ${comment.commentString} : ${comment.commentTimeStamp}",
                        modifier = Modifier.padding(4.dp)
                    )
                }

                OutlinedTextField(
                    value = commentState.value.commentString,
                    onValueChange = {
                        viewModel.onCommentEvent(TaskCommentsEvent.commentStringChanged(it))
                    },
                    label = { Text("Add a comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.onCommentEvent(TaskCommentsEvent.CreateComment) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post Comment")
                }
            }
        }
    }
}