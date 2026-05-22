package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskDirectionApp
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPriority
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskUploadOutput
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
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
    if (isTaskCreation) {
        TaskCreationForm(
            taskState = taskState,
            dropDownListState = dropDownListState,
            onEvent = onEvent
        )
    } else {
        val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section 1: General Details
            Text(
                text = "General Details",
                style = MaterialTheme.typography.titleMedium,
                color = vividCerulean,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ModernTaskTextField(
                value = taskState.taskTitle,
                onValueChange = { if (userRole == UserRole.Admin) onEvent(TaskDetailEvent.TaskTitleTextFieldChanged(it)) },
                label = "Task Name",
                icon = Icons.Default.Edit,
                readOnly = userRole != UserRole.Admin,
                focusManager = focusManager
            )

            ClickableLinkTextField(
                text = taskState.taskDescription,
                onTextChange = { onEvent(TaskDetailEvent.TaskDescriptionTextFieldChanged(it)) },
                readOnly = userRole != UserRole.Admin
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ModernTaskTextField(
                        value = taskState.taskEstimate.toString(),
                        onValueChange = { if (userRole == UserRole.Admin) onEvent(TaskDetailEvent.TaskEstimateChanged(it.toIntOrNull() ?: 0)) },
                        label = "Estimate (h)",
                        icon = Icons.Default.DateRange,
                        keyboardType = KeyboardType.Number,
                        readOnly = userRole != UserRole.Admin,
                        focusManager = focusManager
                    )
                }
                if (userRole == UserRole.Admin) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModernTaskTextField(
                            value = taskState.taskCost.toString(),
                            onValueChange = { onEvent(TaskDetailEvent.TaskCostChanged(it.toIntOrNull() ?: 0)) },
                            label = "Cost",
                            icon = Icons.Default.Info,
                            keyboardType = KeyboardType.Number,
                            focusManager = focusManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Task Classification
            Text(
                text = "Task Classification",
                style = MaterialTheme.typography.titleMedium,
                color = vividCerulean,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Priority at top of Classification
            GenericDropdownMenu(
                label = "Priority",
                selectedItem = taskState.taskPriority.name,
                items = dropDownListState.taskPriority.map { it.name },
                onItemSelected = { selected ->
                    onEvent(TaskDetailEvent.TaskPriorityChanged(TaskPriority.valueOf(selected)))
                },
                enabled = userRole == UserRole.Admin
            )

            GenericDropdownMenu(
                label = "Task Type",
                selectedItem = taskState.taskType.name,
                items = dropDownListState.taskTypeList.map { it.name },
                onItemSelected = { selected ->
                    dropDownListState.taskTypeList.find { it.name == selected }?.let { onEvent(TaskDetailEvent.TaskTypeChanged(it)) }
                },
                enabled = userRole == UserRole.Admin
            )

            GenericDropdownMenu(
                label = "Status",
                selectedItem = taskState.taskCurrentStatus.name,
                items = getAvailableStatusOptions(),
                onItemSelected = { selected ->
                    onEvent(TaskDetailEvent.TaskStatusTypeChanged(TaskStatusType.valueOf(selected)))
                },
                enabled = true 
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Task Distribution (Now at Bottom)
            Text(
                text = "Task Distribution",
                style = MaterialTheme.typography.titleMedium,
                color = vividCerulean,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            GenericDropdownMenu(
                label = "Assigned Employee",
                selectedItem = dropDownListState.employeeList.find { it.employeeId == taskState.taskAssignedTo }?.employeeName ?: "Not Assigned",
                items = dropDownListState.employeeList.map { it.employeeName },
                onItemSelected = { selected ->
                    dropDownListState.employeeList.find { it.employeeName == selected }?.employeeId?.let {
                        onEvent(TaskDetailEvent.TaskAssignedToEmployeeChanged(it))
                    }
                },
                enabled = userRole == UserRole.Admin,
                isVisible = userRole != UserRole.Employee
            )

            GenericDropdownMenu(
                label = "Client",
                selectedItem = dropDownListState.clientsList.find { it.clientId == taskState.taskClientId }?.clientName ?: "No Client",
                items = dropDownListState.clientsList.map { it.clientName },
                onItemSelected = { selected ->
                    dropDownListState.clientsList.find { it.clientName == selected }?.clientId?.let {
                        onEvent(TaskDetailEvent.TaskClientIdChanged(it))
                    }
                },
                enabled = userRole == UserRole.Admin,
                isVisible = userRole != UserRole.Employee
            )

            GenericDropdownMenu(
                label = "Direction App",
                selectedItem = taskState.taskDirectionApp.name,
                items = dropDownListState.taskDirectionApp.map { it.name },
                onItemSelected = { selected ->
                    onEvent(TaskDetailEvent.TaskDirectionAppChanged(TaskDirectionApp.valueOf(selected)))
                },
                enabled = userRole == UserRole.Admin
            )

            GenericDropdownMenu(
                label = "Upload Output To",
                selectedItem = taskState.taskUploadOutput.name,
                items = dropDownListState.taskUploadOutput.map { it.name },
                onItemSelected = { selected ->
                    onEvent(TaskDetailEvent.TaskUploadOutputChanged(TaskUploadOutput.valueOf(selected)))
                },
                enabled = userRole == UserRole.Admin
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TaskCreationForm(
    taskState: TaskDetailState,
    dropDownListState: DropDownListState,
    onEvent: (TaskDetailEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.titleMedium,
            color = vividCerulean,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ModernTaskTextField(
            value = taskState.taskTitle,
            onValueChange = { onEvent(TaskDetailEvent.TaskTitleTextFieldChanged(it)) },
            label = "Task Name",
            icon = Icons.Default.Edit,
            focusManager = focusManager
        )

        ModernTaskTextField(
            value = taskState.taskDescription,
            onValueChange = { onEvent(TaskDetailEvent.TaskDescriptionTextFieldChanged(it)) },
            label = "Description / Attachment URL",
            icon = Icons.Default.Info,
            singleLine = false,
            focusManager = focusManager
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ModernTaskTextField(
                    value = if (taskState.taskEstimate == 0) "" else taskState.taskEstimate.toString(),
                    onValueChange = { onEvent(TaskDetailEvent.TaskEstimateChanged(it.toIntOrNull() ?: 0)) },
                    label = "Estimate (h)",
                    icon = Icons.Default.DateRange,
                    keyboardType = KeyboardType.Number,
                    focusManager = focusManager
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ModernTaskTextField(
                    value = if (taskState.taskCost == 0) "" else taskState.taskCost.toString(),
                    onValueChange = { onEvent(TaskDetailEvent.TaskCostChanged(it.toIntOrNull() ?: 0)) },
                    label = "Cost",
                    icon = Icons.Default.Info,
                    keyboardType = KeyboardType.Number,
                    focusManager = focusManager
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Assignment",
            style = MaterialTheme.typography.titleMedium,
            color = vividCerulean,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        GenericDropdownMenu(
            label = "Assign to Employee",
            selectedItem = dropDownListState.employeeList.find { it.employeeId == taskState.taskAssignedTo }?.employeeName
                ?: "Select Employee",
            items = dropDownListState.employeeList.map { it.employeeName },
            onItemSelected = { selected ->
                dropDownListState.employeeList.find { it.employeeName == selected }?.employeeId?.let {
                    onEvent(TaskDetailEvent.TaskAssignedToEmployeeChanged(it))
                }
            }
        )

        GenericDropdownMenu(
            label = "Client",
            selectedItem = dropDownListState.clientsList.find { it.clientId == taskState.taskClientId }?.clientName
                ?: "Select Client",
            items = dropDownListState.clientsList.map { it.clientName },
            onItemSelected = { selected ->
                dropDownListState.clientsList.find { it.clientName == selected }?.clientId?.let {
                    onEvent(TaskDetailEvent.TaskClientIdChanged(it)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Task Configuration",
            style = MaterialTheme.typography.titleMedium,
            color = vividCerulean,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        GenericDropdownMenu(
            label = "Task Type",
            selectedItem = taskState.taskType.name,
            items = dropDownListState.taskTypeList.map { it.name },
            onItemSelected = { selected ->
                dropDownListState.taskTypeList.find { it.name == selected }
                    ?.let { onEvent(TaskDetailEvent.TaskTypeChanged(it)) }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                GenericDropdownMenu(
                    label = "Priority",
                    selectedItem = taskState.taskPriority.name,
                    items = dropDownListState.taskPriority.map { it.name },
                    onItemSelected = { selected ->
                        onEvent(TaskDetailEvent.TaskPriorityChanged(TaskPriority.valueOf(selected)))
                    }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                GenericDropdownMenu(
                    label = "Direction App",
                    selectedItem = taskState.taskDirectionApp.name,
                    items = dropDownListState.taskDirectionApp.map { it.name },
                    onItemSelected = { selected ->
                        onEvent(TaskDetailEvent.TaskDirectionAppChanged(TaskDirectionApp.valueOf(selected)))
                    }
                )
            }
        }

        GenericDropdownMenu(
            label = "Upload Output To",
            selectedItem = taskState.taskUploadOutput.name,
            items = dropDownListState.taskUploadOutput.map { it.name },
            onItemSelected = { selected ->
                onEvent(TaskDetailEvent.TaskUploadOutputChanged(TaskUploadOutput.valueOf(selected)))
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ModernTaskTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = vividCerulean) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = singleLine,
        readOnly = readOnly,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = white,
            unfocusedTextColor = white,
            focusedBorderColor = vividCerulean,
            unfocusedBorderColor = if (readOnly) silverGrey.copy(alpha = 0.1f) else silverGrey.copy(alpha = 0.3f),
            focusedLabelColor = vividCerulean,
            unfocusedLabelColor = silverGrey,
            disabledTextColor = silverGrey,
            disabledBorderColor = silverGrey.copy(alpha = 0.1f)
        ),
        enabled = !readOnly || value.isNotEmpty(), // Still enabled so user can see it but readOnly is true
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}
