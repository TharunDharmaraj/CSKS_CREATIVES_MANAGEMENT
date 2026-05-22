package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.*
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.TaskDetailViewModel
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDeletionEvent

@RequiresApi(Build.VERSION_CODES.O)
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

    if (visibilityState.value.isTaskDeletionDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.changeBackButtonVisibilityState(false) },
            containerColor = charCoal,
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = red, modifier = Modifier.size(32.dp)) },
            title = {
                Text(
                    "Delete Task?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = white,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${taskState.value.taskTitle}\"? This action cannot be undone.",
                    color = silverGrey,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onTaskDeleteEvent(TaskDeletionEvent.DeleteTask) },
                    colors = ButtonDefaults.buttonColors(containerColor = red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Permanently", color = white, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onTaskDeleteEvent(TaskDeletionEvent.CancelDeleteTask) }) {
                    Text("Cancel", color = silverGrey)
                }
            })
    }

    if (visibilityState.value.isBackButtonDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.changeBackButtonVisibilityState(false) },
            containerColor = charCoal,
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = goldenRod, modifier = Modifier.size(32.dp)) },
            title = {
                Text(
                    "Unsaved Changes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = white,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "You have unsaved changes. If you leave now, your progress will be lost.",
                    color = silverGrey,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeBackButtonVisibilityState(false)
                        onBackPress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = vividCerulean),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Discard & Leave", color = white, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.changeBackButtonVisibilityState(false) }) {
                    Text("Stay", color = silverGrey)
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
            getAvailableStatusOptions = { viewModel.getAvailableStatusOptions(userRole) }
        )
    }
}
