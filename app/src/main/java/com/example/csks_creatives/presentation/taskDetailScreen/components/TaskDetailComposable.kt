package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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