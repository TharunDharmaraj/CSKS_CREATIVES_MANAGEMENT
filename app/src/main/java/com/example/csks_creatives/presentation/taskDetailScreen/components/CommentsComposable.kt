package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.data.utils.Constants.ADMIN_DOCUMENT_NAME
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.*
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

    LaunchedEffect(taskState.taskComments.size) {
        if (taskState.taskComments.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(taskState.taskComments.lastIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            if (taskState.taskComments.isNotEmpty()) {
                items(taskState.taskComments.size) { index ->
                    val comment = taskState.taskComments[index]
                    val isAdminComment = comment.commentedBy == ADMIN_DOCUMENT_NAME
                    val isCurrentUserIsAdmin = userRole == UserRole.Admin
                    val isMyComment = if (isCurrentUserIsAdmin) isAdminComment else !isAdminComment

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (isMyComment) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isMyComment) Alignment.End else Alignment.Start,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            if (!isMyComment) {
                                Text(
                                    text = comment.commentedBy,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = vividCerulean,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMyComment) 16.dp else 2.dp,
                                    bottomEnd = if (isMyComment) 2.dp else 16.dp
                                ),
                                color = if (isMyComment) vividCerulean else charCoalPurple,
                                border = if (isMyComment) null else BorderStroke(1.dp, silverGrey.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = comment.commentString,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = white
                                    )
                                    Text(
                                        text = comment.commentTimeStamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMyComment) white.copy(alpha = 0.7f) else silverGrey,
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start a conversation...", color = silverGrey, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

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
    Surface(
        color = charCoal,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentState.commentString,
                onValueChange = { onCommentStringChanged(it) },
                placeholder = { Text("Type your message...", color = silverGrey) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = white,
                    unfocusedTextColor = white,
                    focusedContainerColor = charCoalPurple,
                    unfocusedContainerColor = charCoalPurple,
                    focusedBorderColor = vividCerulean.copy(alpha = 0.5f),
                    unfocusedBorderColor = transparent
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onCommentPosted() })
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onCommentPosted,
                enabled = commentState.commentString.isNotBlank(),
                modifier = Modifier
                    .background(if (commentState.commentString.isNotBlank()) vividCerulean else charCoalPurple, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (commentState.commentString.isNotBlank()) white else silverGrey,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
