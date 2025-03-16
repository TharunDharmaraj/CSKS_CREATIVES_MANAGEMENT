package com.example.csks_creatives.application

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.csks_creatives.data.worker.FCMTokenRefreshWorker
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
            ExistingPeriodicWorkPolicy.KEEP,  // Ensures only one instance runs
            workRequest
        )
    }
}