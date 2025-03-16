package com.example.csks_creatives.application

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFireStore()
    }

    private fun initializeFireStore() {
        Firebase.initialize(this)
    }
}
