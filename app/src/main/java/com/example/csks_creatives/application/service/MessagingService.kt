package com.example.csks_creatives.application.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.csks_creatives.R
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    private val messagingCoroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Task Update", it.body ?: "New Task Assigned")
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MessagingServiceEntryPoint::class.java
        )
        val loginUseCase = entryPoint.loginUseCase()
        val userPersistenceUseCase = entryPoint.userPersistenceUseCase()
        messagingCoroutineScope.launch {
            val currentUser = userPersistenceUseCase.getCurrentUser()
            if (currentUser != null) {
                if (currentUser.userRole != UserRole.Admin) {
                    loginUseCase.saveNewFcmToken(currentUser.employeeId, newToken)
                } else {
                    loginUseCase.saveAdminFcmToken()
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TASK_CHANNEL", "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "TASK_CHANNEL")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}