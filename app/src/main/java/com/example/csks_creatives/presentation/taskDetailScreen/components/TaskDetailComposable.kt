package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.tasks.*
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.TaskDetailViewModel
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCommentsEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent

@Composable
fun TaskDetailComposable(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    isTaskCreation: Boolean,
    userRole: UserRole,
    paddingValue: PaddingValues,
    onBackPress: () -> Unit
) {
    val taskState = viewModel.taskDetailState.collectAsState()
    val commentState = viewModel.taskCommentState.collectAsState()
    val visibilityState = viewModel.visibilityState.collectAsState()
    val dropDownListState = viewModel.dropDownListState.collectAsState()
    var annotatedText by remember { mutableStateOf(AnnotatedString("")) }
    val descriptionText by remember { mutableStateOf(taskState.value.taskDescription) }

    LaunchedEffect(descriptionText) {
        annotatedText = buildAnnotatedString {
            val regex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+)".toRegex()
            var lastIndex = 0
            regex.findAll(descriptionText).forEach { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1

                // Add normal text before the link
                append(descriptionText.substring(lastIndex, start))

                // Add clickable link
                pushStringAnnotation(tag = "URL", annotation = matchResult.value)
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(matchResult.value)
                }
                pop()

                lastIndex = end
            }

            append(descriptionText.substring(lastIndex))
        }
    }

    BackHandler {
        if (viewModel.hasUnsavedChanges().not()) {
            onBackPress()
        }
    }

    if (visibilityState.value.isBackButtonDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.changeBackButtonVisibilityState(false) },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Do you want to leave without saving?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.changeBackButtonVisibilityState(false)
                    onBackPress()
                }) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.changeBackButtonVisibilityState(false)
                }) {
                    Text("Stay")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            .padding(paddingValue)
            .verticalScroll(rememberScrollState())
    ) {
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
            singleLine = true,
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth()
        )

        // Task Description
        ClickableLinkTextField(
            text = taskState.value.taskDescription, onTextChange = {
                viewModel.onEvent(TaskDetailEvent.TaskDescriptionTextFieldChanged(it))
            }, readOnly = userRole != UserRole.Admin
        )

        // Estimate
        OutlinedTextField(
            value = taskState.value.taskEstimate.toString(),
            onValueChange = {
                if (userRole == UserRole.Admin) viewModel.onEvent(
                    TaskDetailEvent.TaskEstimateChanged(it.toIntOrNull() ?: 0)
                )
            },
            singleLine = true,
            label = { Text("Estimate (Hours)") },
            readOnly = userRole != UserRole.Admin,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Cost
        if (userRole == UserRole.Admin) {
            OutlinedTextField(
                value = taskState.value.taskCost.toString(),
                onValueChange = {
                    if (userRole == UserRole.Admin) viewModel.onEvent(
                        TaskDetailEvent.TaskCostChanged(it.toIntOrNull() ?: 0)
                    )
                },
                singleLine = true,
                label = { Text("Cost in Rupees") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Amount paid for task status
        DropdownMenuWithSelection(
            label = "Paid Status",
            selectedItem = dropDownListState.value.taskPaidStatusList.find { it == taskState.value.taskPaidStatus }?.name
                ?: "Select",
            items = viewModel.getAvailablePaidStatus(),
            onItemSelected = { selectedPaidStatus ->
                val taskType =
                    dropDownListState.value.taskPaidStatusList.find { it.name == selectedPaidStatus }
                taskType?.let { viewModel.onEvent(TaskDetailEvent.TaskPaidStatusChanged(it)) }
            },
            isVisible = userRole == UserRole.Admin && isTaskCreation.not()
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

        // Assign task type
        DropdownMenuWithSelection(
            label = "Task Type",
            selectedItem = dropDownListState.value.taskTypeList.find { it == taskState.value.taskType }?.name
                ?: "Select",
            items = dropDownListState.value.taskTypeList.map { it.name },
            onItemSelected = { selectedTaskType ->
                val taskType =
                    dropDownListState.value.taskTypeList.find { it.name == selectedTaskType }
                taskType?.let { viewModel.onEvent(TaskDetailEvent.TaskTypeChanged(it)) }
            },
            enabled = userRole == UserRole.Admin,
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

        // Task Priority (Dropdown)
        DropdownMenuWithSelection(
            label = "Task Priority",
            selectedItem = dropDownListState.value.taskPriority.find { it == taskState.value.taskPriority }?.name
                ?: "Select",
            items = dropDownListState.value.taskPriority.map { it.name },
            onItemSelected = { selectedStatus ->
                viewModel.onEvent(
                    TaskDetailEvent.TaskPriorityChanged(
                        TaskPriority.valueOf(
                            selectedStatus
                        )
                    )
                )
            },
            enabled = userRole == UserRole.Admin
        )
        Spacer(Modifier.height(10.dp))

        // Task Direction App (Dropdown)
        DropdownMenuWithSelection(
            label = "Task Direction App",
            selectedItem = dropDownListState.value.taskDirectionApp.find { it == taskState.value.taskDirectionApp }?.name
                ?: "Select",
            items = dropDownListState.value.taskDirectionApp.map { it.name },
            onItemSelected = { selectedStatus ->
                viewModel.onEvent(
                    TaskDetailEvent.TaskDirectionAppChanged(
                        TaskDirectionApp.valueOf(
                            selectedStatus
                        )
                    )
                )
            },
            enabled = userRole == UserRole.Admin
        )
        Spacer(Modifier.height(10.dp))

        // Task Upload Output (Dropdown)
        DropdownMenuWithSelection(
            label = "Task Upload Output",
            selectedItem = dropDownListState.value.taskUploadOutput.find { it == taskState.value.taskUploadOutput }?.name
                ?: "Select",
            items = dropDownListState.value.taskUploadOutput.map { it.name },
            onItemSelected = { selectedStatus ->
                viewModel.onEvent(
                    TaskDetailEvent.TaskUploadOutputChanged(
                        TaskUploadOutput.valueOf(
                            selectedStatus
                        )
                    )
                )
            },
            enabled = userRole == UserRole.Admin
        )
        Spacer(Modifier.height(10.dp))

        if (visibilityState.value.isStatusHistoryVisible) {
            Text("Task Status History", style = MaterialTheme.typography.titleMedium)
            taskState.value.taskStatusHistory.forEach { statusEntry ->
                Text("${statusEntry.taskStatusType.name} - ${statusEntry.getDurationString()}")
            }
        }


        // TODO ALLOW COMMENTS DURING TASKS CREATION - Use a Queuing Mechanism to post tasks once Task Created
        if (isTaskCreation.not()) {
            if (taskState.value.taskComments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "Comments",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        taskState.value.taskComments.forEach { comment ->
                            Text(
                                "${comment.commentedBy}: ${comment.commentString} (${comment.commentTimeStamp})",
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No comments yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }


// Comment Input Section
            OutlinedTextField(
                value = commentState.value.commentString,
                onValueChange = {
                    viewModel.onCommentEvent(TaskCommentsEvent.commentStringChanged(it))
                },
                label = { Text("Add a comment") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.onCommentEvent(TaskCommentsEvent.CreateComment) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isTaskCreation.not()
            ) {
                Text("Post Comment")
            }
        }
    }
}

@Composable
fun ClickableLinkTextField(
    text: String, onTextChange: (String) -> Unit, readOnly: Boolean
) {
    val context = LocalContext.current
    val annotatedText = remember(text) {
        buildAnnotatedString {
            val regex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+)".toRegex()
            var lastIndex = 0
            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                append(text.substring(lastIndex, start))
                pushStringAnnotation(tag = "URL", annotation = match.value)
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(match.value)
                }
                pop()
                lastIndex = end
            }
            append(text.substring(lastIndex))
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { if (!readOnly) onTextChange(it) },
        readOnly = readOnly,
        label = { Text("Task Description") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default
        ),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        visualTransformation = {
            TransformedText(annotatedText, OffsetMapping.Identity)
        },
        interactionSource = remember { MutableInteractionSource() }.also { source ->
            LaunchedEffect(source) {
                source.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        val offset = interaction.press.pressPosition.x.toInt()
                        annotatedText.getStringAnnotations(
                            tag = "URL", start = offset, end = offset
                        ).firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                            context.startActivity(intent)
                        }
                    }
                }
            }
        })
}