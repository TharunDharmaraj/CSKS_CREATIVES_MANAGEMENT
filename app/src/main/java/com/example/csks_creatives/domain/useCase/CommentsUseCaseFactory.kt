package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.task.Comment
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import kotlinx.coroutines.flow.Flow

interface CommentsUseCaseFactory {
    fun create(): CommentsUseCase

    fun getComments(taskId: String): Flow<ResultState<List<Comment>>>

    suspend fun postComment(
        taskId: String,
        employeeId: String,
        comment: Comment
    ): ResultState<String>
}