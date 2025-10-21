package com.nikoladx.trailator.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nikoladx.trailator.R
import com.nikoladx.trailator.data.models.TrailObject

class NotificationService(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "nearby_objects_channel"
        private const val CHANNEL_NAME = "Objekti u blizini"
        private const val CHANNEL_DESCRIPTION = "Notifikacije o objektima u blizini"

        const val NOTIFICATION_ID_NEARBY = 1001

        private const val MIN_NOTIFICATION_INTERVAL = 5 * 60 * 1000L // 5 minuta
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notifiedObjects = mutableSetOf<String>()
    private var lastNotificationTime = 0L

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNearbyObjectsNotification(objects: List<TrailObject>): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
            return false
        }

        val newObjects = objects.filter { it.id !in notifiedObjects }
        if (newObjects.isEmpty()) {
            return false
        }

        val title = when {
            newObjects.size == 1 -> "Objekat u blizini"
            else -> "Objekti u blizini (${newObjects.size})"
        }

        val message = formatObjectsMessage(newObjects)

        sendNotification(
            notificationId = NOTIFICATION_ID_NEARBY,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )

        notifiedObjects.addAll(newObjects.map { it.id })
        lastNotificationTime = currentTime

        return true
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(
        notificationId: Int,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        channelId: String = CHANNEL_ID
    ) {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun formatObjectsMessage(objects: List<TrailObject>): String {
        return when {
            objects.size == 1 -> objects.first().title
            objects.size <= 3 -> objects.joinToString(", ") { it.title }
            else -> "${objects.take(2).joinToString(", ") { it.title }} i još ${objects.size - 2}"
        }
    }

    fun resetNotifiedObjects() {
        notifiedObjects.clear()
    }
}