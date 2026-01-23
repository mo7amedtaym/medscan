package com.albarmajy.medscan.notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// notification/NotificationHelper.kt
object NotificationHelper {
    private const val CHANNEL_ID = "medication_channel"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تنبيهات الأدوية",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تستخدم لتذكيرك بموعد تناول الدواء"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(context: Context, medicineName: String, doseId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.alert_dark_frame)
            .setContentTitle("موعد الجرعة")
            .setContentText("حان وقت تناول $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(doseId, builder.build())
    }
}