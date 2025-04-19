package com.example.csks_creatives.domain.model.task

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_COST
import com.example.csks_creatives.data.utils.Constants.TASK_CREATION_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_DIRECTION_APP
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ESTIMATE
import com.example.csks_creatives.data.utils.Constants.TASK_FULLY_PAID_DATE
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_PAID_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_PRIORITY
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.data.utils.Constants.TASK_TYPE
import com.example.csks_creatives.data.utils.Constants.TASK_UPLOAD_OUTPUT
import com.example.csks_creatives.domain.model.utills.enums.tasks.*
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// Tasks / ClientTask
@Keep
@IgnoreExtraProperties
data class ClientTask(
    @PropertyName(TASK_ID) val taskId: String = EMPTY_STRING, // Auto Generated UUID
    @PropertyName(TASK_CREATION_TIME) val taskCreationTime: String = EMPTY_STRING,
    @PropertyName(TASK_CLIENT_ID) val clientId: String = EMPTY_STRING,
    @PropertyName(TASK_EMPLOYEE_ID) val employeeId: String = EMPTY_STRING,
    @PropertyName(TASK_TASK_NAME) val taskName: String = EMPTY_STRING,
    @PropertyName(TASK_ATTACHMENT) val taskAttachment: String = EMPTY_STRING, // Task Description
    @PropertyName(TASK_ESTIMATE) val taskEstimate: Int = 0, // Task Estimate in hours
    @PropertyName(TASK_COST) val taskCost: Int = 0, // Cost Estimate for the task
    @PropertyName(TASK_PAID_STATUS) val taskPaidStatus: TaskPaidStatus = TaskPaidStatus.NOT_PAID,
    @PropertyName(TASK_FULLY_PAID_DATE) val taskFullyPaidDate: String = EMPTY_STRING,
    @PropertyName(TASK_TYPE) val taskType: TaskType = TaskType.SHORTS_VIDEO,
    @PropertyName(TASK_PRIORITY) val taskPriority: TaskPriority = TaskPriority.MEDIUM,
    @PropertyName(TASK_DIRECTION_APP) val taskDirectionApp: TaskDirectionApp = TaskDirectionApp.TEAMS,
    @PropertyName(TASK_UPLOAD_OUTPUT) val taskUploadOutput: TaskUploadOutput = TaskUploadOutput.CSKS_CREATIVES,
    @PropertyName(TASK_CURRENT_STATUS) val currentStatus: TaskStatusType = TaskStatusType.BACKLOG, // Current Task Status
    @PropertyName(TASK_STATUS_HISTORY_SUB_COLLECTION) val statusHistory: List<TaskStatusHistory>, // Status History EG : Backlog - 3 days, In-Progress - 2 days etc..
    @PropertyName(TASK_PAYMENTS_INFO_SUB_COLLECTION) val paymentHistory: List<PaymentInfo> // Payments Info History - Only available for Partially paid tasks
) {
    constructor() : this(
        EMPTY_STRING,
        EMPTY_STRING,
        EMPTY_STRING,
        EMPTY_STRING,
        EMPTY_STRING,
        EMPTY_STRING,
        0,
        0,
        TaskPaidStatus.NOT_PAID,
        EMPTY_STRING,
        TaskType.SHORTS_VIDEO,
        TaskPriority.MEDIUM,
        TaskDirectionApp.TEAMS,
        TaskUploadOutput.CSKS_CREATIVES,
        TaskStatusType.BACKLOG,
        emptyList(),
        emptyList()
    )
}
