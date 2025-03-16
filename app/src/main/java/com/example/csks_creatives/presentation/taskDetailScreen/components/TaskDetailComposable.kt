package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
            }
        )
    }

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
            singleLine = true,
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

        if (visibilityState.value.isStatusHistoryVisible) {
            Text("Task Status History", style = MaterialTheme.typography.titleMedium)
            taskState.value.taskStatusHistory.forEach { statusEntry ->
                Text("${statusEntry.taskStatusType.name} - ${statusEntry.getDurationString()}")
            }
        }


        // TODO ALLOW COMMENTS DURING TASKS CREATION - Use a Queuing Mechanism to post tasks once Task Created
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

    OutlinedTextField(value = text,
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
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    )
}