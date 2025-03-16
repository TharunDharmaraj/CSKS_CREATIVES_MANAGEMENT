package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class TaskCommentState(
    val commentString: String = EMPTY_STRING,
    val commentedBy: String = EMPTY_STRING
)
