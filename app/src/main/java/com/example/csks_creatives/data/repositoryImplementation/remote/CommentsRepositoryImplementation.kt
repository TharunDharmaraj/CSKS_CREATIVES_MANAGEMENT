package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.COMMENT_COMMENTED_BY
import com.example.csks_creatives.data.utils.Constants.COMMENT_ID
import com.example.csks_creatives.data.utils.Constants.COMMENT_STRING
import com.example.csks_creatives.data.utils.Constants.COMMENT_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.COMMENT_TIME_STAMP
import com.example.csks_creatives.data.utils.Constants.TASKS_COLLECTION
import com.example.csks_creatives.domain.model.task.Comment
import com.example.csks_creatives.domain.repository.remote.CommentsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentsRepository {
    private val logTag = "CommentsRepository"

    override fun getComments(taskId: String): Flow<List<Comment>> = callbackFlow {
        val collectionRef = getCommentPath(taskId)
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val comments = snapshot.documents.mapNotNull { document ->
                    document.toObject(Comment::class.java)
                }
                trySend(comments).isSuccess
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    // TODO EDIT COMMENT FUNCTIONALITY WITH EDIT OPTION, IS_EDITED FLAG TO SHOW EDITED STATUS
    override suspend fun postComment(
        taskId: String, employeeId: String, commentToBePosted: Comment
    ) {
        try {
            val commentPath = getCommentPath(taskId)
            val commentDocumentKey = commentToBePosted.commentId
            commentPath.document(commentDocumentKey).set(
                hashMapOf(
                    COMMENT_ID to commentToBePosted.commentId,
                    COMMENT_STRING to commentToBePosted.commentString,
                    COMMENT_COMMENTED_BY to commentToBePosted.commentedBy,
                    COMMENT_TIME_STAMP to commentToBePosted.commentTimeStamp
                ), SetOptions.merge()
            ).await()
            Log.d(logTag + "Post", "Successfully posted Comment : $commentToBePosted")
        } catch (exception: Exception) {
            Log.d(logTag + "Post", "Error $exception in posting Comment : $commentToBePosted")
        }
    }

    private fun getCommentPath(taskId: String) =
        firestore.collection(TASKS_COLLECTION).document(taskId).collection(COMMENT_SUB_COLLECTION)
}