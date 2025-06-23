package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.darkSlateBlue
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCommentsEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun TaskDetailPager(
    taskState: TaskDetailState,
    commentState: TaskCommentState,
    dropDownListState: DropDownListState,
    visibilityState: TaskDetailsSectionVisibilityState,
    userRole: UserRole,
    isTaskCreation: Boolean,
    onEvent: (TaskDetailEvent) -> Unit,
    onCommentEvent: (TaskCommentsEvent) -> Unit,
    getAvailableStatusOptions: () -> List<String>,
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // Dynamically setting tab titles
    val tabTitles = when (userRole) {
        UserRole.Admin -> {
            if (isTaskCreation.not()) {
                listOf("Task", "Amount", "History", "Chat")
            } else {
                listOf("New Task")
            }
        }

        UserRole.Employee -> listOf("Task", "History", "Chat")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = darkSlateBlue,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        HorizontalPager(count = tabTitles.size, state = pagerState) { page ->
            when (userRole) {
                UserRole.Admin -> {
                    when (page) {
                        0 -> TaskDetailTabContent(
                            taskState = taskState,
                            dropDownListState = dropDownListState,
                            userRole = userRole,
                            isTaskCreation = isTaskCreation,
                            onEvent = onEvent,
                            getAvailableStatusOptions = getAvailableStatusOptions,
                        )

                        1 -> AmountSection(
                            taskState = taskState,
                            dropDownListState = dropDownListState,
                            onEvent = onEvent
                        )

                        2 -> TaskStatusHistoryComposable(
                            statusHistory = taskState.taskStatusHistory,
                            isVisible = visibilityState.isStatusHistoryVisible
                        )

                        3 -> {
                            CommentsComposable(
                                taskState = taskState,
                                userRole = userRole,
                                commentState = commentState,
                                onCommentStringChanged = {
                                    onCommentEvent(TaskCommentsEvent.CommentStringChanged(it))
                                },
                                onCommentPosted = {
                                    onCommentEvent(TaskCommentsEvent.CreateComment)
                                }
                            )
                        }
                    }
                }

                UserRole.Employee -> {
                    when (page) {
                        0 -> {
                            TaskDetailTabContent(
                                taskState = taskState,
                                dropDownListState = dropDownListState,
                                userRole = userRole,
                                isTaskCreation = isTaskCreation,
                                onEvent = onEvent,
                                getAvailableStatusOptions = getAvailableStatusOptions
                            )
                        }

                        1 -> TaskStatusHistoryComposable(
                            statusHistory = taskState.taskStatusHistory,
                            isVisible = visibilityState.isStatusHistoryVisible
                        )

                        2 -> {
                            Column(Modifier.fillMaxSize()) {
                                CommentsComposable(
                                    taskState = taskState,
                                    userRole = userRole,
                                    {
                                        onCommentEvent(TaskCommentsEvent.CommentStringChanged(it))
                                    },
                                    commentState
                                ) {
                                    onCommentEvent(TaskCommentsEvent.CreateComment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}