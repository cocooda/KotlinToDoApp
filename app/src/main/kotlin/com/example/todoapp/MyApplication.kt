package com.example.todoapp

import android.app.Application
import com.example.todoapp.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
