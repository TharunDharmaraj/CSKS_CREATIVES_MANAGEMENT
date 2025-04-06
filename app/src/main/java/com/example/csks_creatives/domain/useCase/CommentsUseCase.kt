package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.task.Comment
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.CommentsRepository
import com.example.csks_creatives.domain.useCase.factories.CommentsUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.formatTimeStamp
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject

class CommentsUseCase @Inject constructor(
    private val commentsRepository: CommentsRepository
) : CommentsUseCaseFactory {
    override fun create(): CommentsUseCase {
        return CommentsUseCase(commentsRepository)
    }

    override fun getComments(taskId: String): Flow<ResultState<List<Comment>>> {
        return flow {
            emit(ResultState.Loading)
            try {
                commentsRepository.getComments(taskId).collect { comments ->
                    emit(
                        ResultState.Success(comments.sortedBy { it.commentTimeStamp }
                            .map { comment ->
                                comment.copy(
                                    commentTimeStamp = formatTimeStamp(comment.commentTimeStamp)
                                )
                            }
                        )
                    )
                }
            } catch (exception: Exception) {
                emit(ResultState.Error("Failed to fetch comments ${exception.message}"))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun postComment(
        taskId: String,
        employeeId: String,
        comment: Comment
    ): ResultState<String> {
        return try {
            if (comment.commentString.isEmpty()) return ResultState.Error("Empty Comment")
            if (employeeId.isEmpty()) return ResultState.Error("No User found to comment")
            val commentToBePosted = comment.copy(
                commentId = UUID.randomUUID().toString(),
                commentTimeStamp = getCurrentTimeAsString(),
                commentedBy = employeeId
            )
            commentsRepository.postComment(taskId, employeeId, commentToBePosted)
            ResultState.Success("Comment Posted Successfully")
        } catch (exception: Exception) {
            ResultState.Error("Failed to post comment ${exception.message}")
        }
    }
}