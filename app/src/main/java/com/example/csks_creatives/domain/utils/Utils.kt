package com.example.csks_creatives.domain.utils

import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.FULLY_PAID
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.NOT_PAID
import com.example.csks_creatives.data.utils.Constants.REVISION_ONE
import com.example.csks_creatives.data.utils.Constants.REVISION_THREE
import com.example.csks_creatives.data.utils.Constants.REVISION_TWO
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    const val EMPTY_STRING = ""

    fun List<ClientTask>.getActiveTasks(): List<ClientTask> =
        this.filter { it.currentStatus != TaskStatusType.COMPLETED }

    fun List<ClientTask>.getCompletedTasks(): List<ClientTask> =
        this.filter { it.currentStatus == TaskStatusType.COMPLETED }

    fun getAllStatusOptionsForAdmin(): List<String> {
        return listOf(
            BACKLOG,
            IN_PROGRESS,
            IN_REVIEW,
            REVISION_ONE,
            REVISION_TWO,
            REVISION_THREE,
            COMPLETED
        )
    }

    fun getAvailableStatusOptions(currentStatus: TaskStatusType): List<String> {
        return when (currentStatus) {
            TaskStatusType.BACKLOG -> listOf(
                IN_PROGRESS,
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.BLOCKED -> listOf(
                BACKLOG,
                IN_PROGRESS,
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.IN_PROGRESS -> listOf(
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.IN_REVIEW -> listOf(
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.REVISION_ONE -> listOf(REVISION_TWO, REVISION_THREE, COMPLETED)
            TaskStatusType.REVISION_TWO -> listOf(REVISION_THREE, COMPLETED)
            TaskStatusType.REVISION_THREE -> listOf(COMPLETED)
            TaskStatusType.COMPLETED -> listOf(COMPLETED)
        }
    }

    fun getTasksPaidStatusList(currentPaidStatus: TaskPaidStatus): List<String> {
        return when (currentPaidStatus) {
            TaskPaidStatus.FULLY_PAID -> listOf(
                FULLY_PAID
            )

            TaskPaidStatus.NOT_PAID -> listOf(
                NOT_PAID,
                FULLY_PAID
            )
        }
    }

    fun formatTimeStamp(timeStampInMilliSeconds: String): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        return timeFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun getFormattedDateTimeFormat(timeStampInMilliSeconds: String): String {
        val dateFormat = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }
}