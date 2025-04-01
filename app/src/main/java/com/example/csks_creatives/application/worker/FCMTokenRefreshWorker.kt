package com.example.csks_creatives.application.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*

@HiltWorker
class FCMTokenRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val loginUseCase: UserLoginUseCase,
    private val userPersistenceUseCase: UserPersistenceUseCase
) : Worker(context, workerParams) {
    private val fcmTokenRefreshWorkerCoroutineScope = CoroutineScope(Dispatchers.IO)
    override fun doWork(): Result {
        return try {
            fcmTokenRefreshWorkerCoroutineScope.launch {
                val currentUser = userPersistenceUseCase.getCurrentUser()
                if (currentUser != null && currentUser.userRole == UserRole.Employee) {
                    loginUseCase.saveFcmToken(currentUser.employeeId)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("FCMWorker", "Token update failed", e)
            Result.retry()
        }
    }
}