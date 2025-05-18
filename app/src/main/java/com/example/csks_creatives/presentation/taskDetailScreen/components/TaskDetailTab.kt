package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.utills.enums.tasks.*
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.DropDownListState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailState

@Composable
fun TaskDetailTabContent(
    taskState: TaskDetailState,
    dropDownListState: DropDownListState,
    userRole: UserRole,
    isTaskCreation: Boolean,
    onEvent: (TaskDetailEvent) -> Unit,
    getAvailableStatusOptions: () -> List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = taskState.taskTitle,
            onValueChange = {
                if (userRole == UserRole.Admin) onEvent(
                    TaskDetailEvent.TaskTitleTextFieldChanged(
                        it
                    )
                )
            },
            label = { Text("Task Name") },
            singleLine = true,
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth()
        )

        // Task Description
        ClickableLinkTextField(
            text = taskState.taskDescription, onTextChange = {
                onEvent(TaskDetailEvent.TaskDescriptionTextFieldChanged(it))
            }, readOnly = userRole != UserRole.Admin
        )

        // Estimate
        OutlinedTextField(
            value = taskState.taskEstimate.toString(),
            onValueChange = {
                if (userRole == UserRole.Admin) onEvent(
                    TaskDetailEvent.TaskEstimateChanged(it.toIntOrNull() ?: 0)
                )
            },
            singleLine = true,
            label = { Text("Estimate (Hours)") },
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Assigned Employee (Dropdown)
        GenericDropdownMenu(
            label = "Assigned Employee",
            selectedItem = dropDownListState.employeeList
                .find { it.employeeId == taskState.taskAssignedTo }?.employeeName ?: "Select",
            items = dropDownListState.employeeList.map { it.employeeName },
            onItemSelected = { selected ->
                dropDownListState.employeeList
                    .find { it.employeeName == selected }
                    ?.employeeId
                    ?.let { onEvent(TaskDetailEvent.TaskAssignedToEmployeeChanged(it)) }
            },
            enabled = userRole == UserRole.Admin,
            isVisible = userRole != UserRole.Employee
        )

        // Assign Task to Client
        GenericDropdownMenu(
            label = "Assigned Task To Client",
            selectedItem = dropDownListState.clientsList
                .find { it.clientId == taskState.taskClientId }?.clientName ?: "Select",
            items = dropDownListState.clientsList.map { it.clientName },
            onItemSelected = { selected ->
                dropDownListState.clientsList
                    .find { it.clientName == selected }
                    ?.clientId
                    ?.let { onEvent(TaskDetailEvent.TaskClientIdChanged(it)) }
            },
            enabled = userRole == UserRole.Admin,
            isVisible = userRole != UserRole.Employee
        )

        // Assign task type
        GenericDropdownMenu(
            label = "Task Type",
            selectedItem = taskState.taskType.name,
            items = dropDownListState.taskTypeList.map { it.name },
            onItemSelected = { selected ->
                dropDownListState.taskTypeList
                    .find { it.name == selected }
                    ?.let { onEvent(TaskDetailEvent.TaskTypeChanged(it)) }
            },
            enabled = userRole == UserRole.Admin
        )

        // Task Priority (Dropdown)
        GenericDropdownMenu(
            label = "Task Priority",
            selectedItem = taskState.taskPriority.name,
            items = dropDownListState.taskPriority.map { it.name },
            onItemSelected = { selected ->
                onEvent(TaskDetailEvent.TaskPriorityChanged(TaskPriority.valueOf(selected)))
            },
            enabled = userRole == UserRole.Admin
        )

        // Task Direction App (Dropdown)
        GenericDropdownMenu(
            label = "Task Direction App",
            selectedItem = taskState.taskDirectionApp.name,
            items = dropDownListState.taskDirectionApp.map { it.name },
            onItemSelected = { selected ->
                onEvent(
                    TaskDetailEvent.TaskDirectionAppChanged(
                        TaskDirectionApp.valueOf(
                            selected
                        )
                    )
                )
            },
            enabled = userRole == UserRole.Admin
        )

        // Task Upload Output (Dropdown)
        GenericDropdownMenu(
            label = "Task Upload Output",
            selectedItem = taskState.taskUploadOutput.name,
            items = dropDownListState.taskUploadOutput.map { it.name },
            onItemSelected = { selected ->
                onEvent(
                    TaskDetailEvent.TaskUploadOutputChanged(
                        TaskUploadOutput.valueOf(
                            selected
                        )
                    )
                )
            },
            enabled = userRole == UserRole.Admin
        )

        // Task Status (Dropdown)
        if (isTaskCreation.not()) {
            GenericDropdownMenu(
                label = "Task Status",
                selectedItem = taskState.taskCurrentStatus.name,
                items = getAvailableStatusOptions(),
                onItemSelected = { selected ->
                    onEvent(
                        TaskDetailEvent.TaskStatusTypeChanged(
                            TaskStatusType.valueOf(
                                selected
                            )
                        )
                    )
                }
            )
        }
        Spacer(Modifier.height(10.dp))

        if (isTaskCreation) {
            OutlinedTextField(
                value = taskState.taskCost.toString(),
                onValueChange = { value ->
                    onEvent(TaskDetailEvent.TaskCostChanged(value.toIntOrNull() ?: 0))
                },
                label = { Text("Task Cost") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
