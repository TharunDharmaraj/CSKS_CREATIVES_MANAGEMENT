package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.task.Comment
import kotlinx.coroutines.flow.Flow

interface CommentsRepository {
    fun getComments(taskId: String): Flow<List<Comment>>

    suspend fun postComment(taskId: String, employeeId: String, commentToBePosted: Comment)
}