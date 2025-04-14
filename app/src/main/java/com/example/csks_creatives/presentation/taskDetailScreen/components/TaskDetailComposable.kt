package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.TaskDetailViewModel

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
            .padding(paddingValue)
    ) {
        TaskDetailPager(
            taskState = taskState.value,
            commentState = commentState.value,
            dropDownListState = dropDownListState.value,
            visibilityState = visibilityState.value,
            userRole = userRole,
            isTaskCreation = isTaskCreation,
            onEvent = { viewModel.onEvent(it) },
            onCommentEvent = { viewModel.onCommentEvent(it) },
            getAvailableStatusOptions = { viewModel.getAvailableStatusOptions() }
        )
    }
}