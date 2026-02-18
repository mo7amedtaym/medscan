package com.albarmajy.medscan.notification

import android.R
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val channelId = "medication_channel"

    fun showNotification(id: Int, medName: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.alert_dark_frame)
            .setContentTitle("حان موعد الدواء")
            .setContentText("موعد جرعة: $medName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}