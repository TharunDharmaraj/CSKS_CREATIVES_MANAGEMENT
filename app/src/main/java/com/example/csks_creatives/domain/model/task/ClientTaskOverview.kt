package com.example.csks_creatives.domain.model.task

import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.model.utills.enums.TaskType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class ClientTaskOverview(
    val taskId: String = EMPTY_STRING,
    val taskName: String = EMPTY_STRING,
    val taskCreationTime: String = EMPTY_STRING,
    val clientId: String = EMPTY_STRING,
    val taskEstimate: Int = 0,
    val taskCost: Int = 0,
    val taskType: TaskType = TaskType.SHORTS_VIDEO,
    val currentStatus: TaskStatusType = TaskStatusType.BACKLOG,
    val taskDuration: String = EMPTY_STRING
)