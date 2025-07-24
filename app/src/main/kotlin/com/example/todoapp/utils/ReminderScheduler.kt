package com.example.todoapp.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todoapp.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import androidx.work.Data

object ReminderScheduler {
    fun scheduleReminder(context: Context, delayMillis: Long, taskId: Int, taskTitle: String) {
        val inputData = Data.Builder()
            .putInt("TASK_ID", taskId)
            .putString("TASK_TITLE", taskTitle)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)  // Pass the task info here
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
