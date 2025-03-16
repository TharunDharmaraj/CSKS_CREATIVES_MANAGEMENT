package com.example.csks_creatives.domain.model.task

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_CREATION_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_POINT
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// Tasks / ClientID / EmployeeId / ClientTask
@Keep
@IgnoreExtraProperties
data class ClientTask(
    @PropertyName(TASK_ID) val taskId: String = EMPTY_STRING, // Auto Generated UUID
    @PropertyName(TASK_CREATION_TIME) val taskCreationTime: String = EMPTY_STRING,
    @PropertyName(TASK_CLIENT_ID) val clientId: String = EMPTY_STRING,
    @PropertyName(TASK_EMPLOYEE_ID) val employeeId: String = EMPTY_STRING,
    @PropertyName(TASK_TASK_NAME) val taskName: String = EMPTY_STRING,
    @PropertyName(TASK_ATTACHMENT) val taskAttachment: String = EMPTY_STRING, // Task Description
    @PropertyName(TASK_POINT) val taskPoint: Int = 0, // Story Points
    @PropertyName(TASK_CURRENT_STATUS) val currentStatus: TaskStatusType = TaskStatusType.BACKLOG, // Current Task Status
    @PropertyName(TASK_STATUS_HISTORY_SUB_COLLECTION) val statusHistory: List<TaskStatusHistory>, // Status History EG : Backlog - 3 days, In-Progress - 2 days etc..
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        TaskStatusType.BACKLOG,
        emptyList()
    )
}
