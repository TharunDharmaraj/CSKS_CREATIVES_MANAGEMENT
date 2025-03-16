package com.example.csks_creatives.domain.model.task

import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class ClientTaskOverview(
    val taskId: String = EMPTY_STRING,
    val taskName: String = EMPTY_STRING,
    val taskCreationTime: String = EMPTY_STRING,
    val clientId: String = EMPTY_STRING,
    val taskPoint: Int = 0,
    val currentStatus: TaskStatusType = TaskStatusType.BACKLOG,
    val taskDuration: String = EMPTY_STRING
)