package com.example.todoapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * A helper object to manage the creation and display of task reminder notifications.
 * It handles notification channel setup (required for Android 8.0+),
 * runtime permission checks (for Android 13+), and actual notification delivery.
 */
object NotificationHelper {

    // Constants for notification channel ID and details
    private const val CHANNEL_ID = "todo_reminder_channel"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESC = "Notifications for task reminders"

    /**
     * Creates a notification channel if the device is running Android 8.0 (API 26) or higher.
     * This is required by the system for notifications to show.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define a new channel with high importance
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)    // Enable LED lights
                enableVibration(true) // Enable vibration on notifications
            }

            // Register the channel with the system
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification using the NotificationManagerCompat.
     *
     * @param context the application context
     * @param notificationId a unique ID for the notification (used to update/cancel)
     * @param title the title shown in the notification
     * @param content the message content of the notification
     */
    fun showNotification(context: Context, notificationId: Int, title: String, content: String) {
        // Build the notification with title, content, and icon
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Default icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Appear immediately
            .setAutoCancel(true) // Dismiss when tapped

        // For Android 13 (API 33) and above, check if POST_NOTIFICATIONS permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
            // If permission is not granted, do not show the notification
        } else {
            // No runtime permission needed for Android 12 and below
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}
