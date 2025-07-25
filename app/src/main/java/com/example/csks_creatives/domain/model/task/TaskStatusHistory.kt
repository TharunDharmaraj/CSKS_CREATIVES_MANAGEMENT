package com.example.csks_creatives.domain.model.task

import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_START_TIME
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsLong
import com.google.firebase.firestore.PropertyName
import java.util.concurrent.TimeUnit

// Tasks / taskId / StatusHistory / StatusType
data class TaskStatusHistory(
    val taskStatusType: TaskStatusType = TaskStatusType.BACKLOG,
    @PropertyName(TASK_STATUS_HISTORY_START_TIME) val startTime: String = EMPTY_STRING,
    @PropertyName(TASK_STATUS_HISTORY_END_TIME) val endTime: String = EMPTY_STRING,
    @PropertyName(TASK_STATUS_HISTORY_END_TIME) val elapsedTime: Long = 0L,
) {
    fun getDurationString(): String {
        if (endTime.toLong() == TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE) {
            val (days, hours, minutes) = getHoursAndMinutes(
                getCurrentTimeAsLong(),
                startTime.toLong(),
                elapsedTime
            )
            return "Current State - Working for $days Days $hours Hours $minutes Minutes"
        }
        val (days, hours, minutes) = getHoursAndMinutes(endTime.toLong(), startTime.toLong(), elapsedTime)
        return "$days Days $hours hours $minutes minutes"
    }

    private fun getHoursAndMinutes(endTime: Long, startTime: Long, elapsedTime: Long): Triple<String, String, String> {
        val timeDiff = if(startTime == 0L) 0 else endTime - startTime
        val durationMillis = elapsedTime + timeDiff
        val days = TimeUnit.MILLISECONDS.toDays(durationMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        return Triple(days.toString(), hours.toString(), minutes.toString())
    }
}