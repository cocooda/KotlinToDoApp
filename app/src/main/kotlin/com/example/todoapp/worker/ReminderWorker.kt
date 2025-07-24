package com.example.todoapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.notifications.NotificationHelper

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt("TASK_ID", 0)
        val taskTitle = inputData.getString("TASK_TITLE") ?: "Your task"

        NotificationHelper.showNotification(
            applicationContext,
            notificationId = taskId,  // Use task ID for notification ID to keep unique
            title = "Task Reminder",
            content = "Reminder: \"$taskTitle\" is due!"
        )
        return Result.success()
    }
}

