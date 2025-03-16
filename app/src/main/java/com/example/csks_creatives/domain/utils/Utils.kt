package com.example.csks_creatives.domain.utils

import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.REVISION_ONE
import com.example.csks_creatives.data.utils.Constants.REVISION_THREE
import com.example.csks_creatives.data.utils.Constants.REVISION_TWO
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    const val EMPTY_STRING = ""

    fun List<ClientTask>.getActiveTasks(): List<ClientTask> =
        this.filter { it.currentStatus != TaskStatusType.COMPLETED }

    fun List<ClientTask>.getCompletedTasks(): List<ClientTask> =
        this.filter { it.currentStatus == TaskStatusType.COMPLETED }

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

    fun formatTimeStamp(timeStampInMilliSeconds: String): String {
        val sdf = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        return sdf.format(Date(timeStampInMilliSeconds.toLong()))
    }
}