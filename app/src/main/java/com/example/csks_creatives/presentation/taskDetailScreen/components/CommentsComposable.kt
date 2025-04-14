package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.data.utils.Constants.ADMIN_DOCUMENT_NAME
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskCommentState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailState
import kotlinx.coroutines.launch

@Composable
fun CommentsComposable(
    taskState: TaskDetailState,
    userRole: UserRole,
    onCommentStringChanged: (String) -> Unit,
    commentState: TaskCommentState,
    onCommentPosted: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom once on first composition
    LaunchedEffect(taskState.taskComments.size) {
        if (taskState.taskComments.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(taskState.taskComments.lastIndex)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f) // Fills all available space
                .padding(horizontal = 8.dp)
        ) {
            if (taskState.taskComments.isNotEmpty()) {
                items(taskState.taskComments.size) { index ->
                    val comment = taskState.taskComments[index]
                    var isMyComment = false
                    val isAdminComment = comment.commentedBy == ADMIN_DOCUMENT_NAME
                    val isCurrentUserIsAdmin = userRole == UserRole.Admin
                    if (isCurrentUserIsAdmin && isAdminComment) {
                        isMyComment = true
                    } else if (!isCurrentUserIsAdmin && !isAdminComment) {
                        isMyComment = true
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMyComment) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            color = if (isMyComment) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(4.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMyComment) {
                                    Text(
                                        text = comment.commentedBy,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = comment.commentString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = comment.commentTimeStamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No comments yet",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Fixed Bottom Text Field
        PostCommentsComposable(
            commentState = commentState,
            onCommentStringChanged = onCommentStringChanged,
            onCommentPosted = onCommentPosted
        )
    }
}

@Composable
fun PostCommentsComposable(
    commentState: TaskCommentState,
    onCommentStringChanged: (String) -> Unit,
    onCommentPosted: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
        OutlinedTextField(
            value = commentState.commentString,
            onValueChange = { onCommentStringChanged(it) },
            label = { Text("Add a comment") },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                onCommentPosted()
            })
        )
    }
}
