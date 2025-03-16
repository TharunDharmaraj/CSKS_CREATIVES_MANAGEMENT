package com.example.csks_creatives.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FCMTokenRefreshWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
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