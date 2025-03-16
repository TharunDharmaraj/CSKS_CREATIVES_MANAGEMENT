package com.example.csks_creatives.application.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO When app grows bigger, to use cloud functions to trigger FCM to employee device when a task is assigned to them. It is on hold now, since it requires billing
class MessagingService @Inject constructor(
    private val loginUseCase: UserLoginUseCase,
    private val userPersistenceUseCase: UserPersistenceUseCase
) : FirebaseMessagingService() {
    private val messagingCoroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Task Update", it.body ?: "New Task Assigned")
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        messagingCoroutineScope.launch {
            val currentUser = userPersistenceUseCase.getCurrentUser()
            if (currentUser != null && currentUser.userRole == UserRole.Admin) {
                loginUseCase.saveNewFcmToken(currentUser.employeeId, newToken)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}