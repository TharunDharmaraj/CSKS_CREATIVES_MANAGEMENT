package com.example.csks_creatives.application

import android.app.Application
import androidx.work.*
import com.example.csks_creatives.application.worker.FCMTokenRefreshWorker
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFireStore()
        scheduleFCMTokenUpdate()
    }

    private fun initializeFireStore() {
        Firebase.initialize(this)
    }

    private fun scheduleFCMTokenUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<FCMTokenRefreshWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FCMTokenUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}