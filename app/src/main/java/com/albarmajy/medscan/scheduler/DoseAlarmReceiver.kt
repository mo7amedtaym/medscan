package com.albarmajy.medscan.scheduler

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class DoseAlarmReceiver1 : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "Alarm Received for Dose ID: ${intent.getLongExtra("DOSE_ID", -1)}")
        val doseId = intent.getLongExtra("DOSE_ID", -1)
        val medName = intent.getStringExtra("MED_NAME") ?: "Medication"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // إنشاء القناة (ضروري لأندرويد 8+)
        val channelId = "medication_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm) // أيقونة مؤقتة للتجربة
            .setContentTitle("حان موعد الدواء!")
            .setContentText("لا تنسَ أخذ جرعتك من $medName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(doseId.toInt(), notification)
    }
}

class DoseAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getLongExtra("DOSE_ID", -1)
        val medName = intent.getStringExtra("MED_NAME") ?: "Medication"

        // التحقق من وصول البيانات
        if (doseId != -1L) {
            showNotification(context, medName, doseId.toInt())
        }
    }

    private fun showNotification(context: Context, medicineName: String, doseId: Int) {
        val channelId = "medication_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Medication Notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. بناء الإشعار
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.alert_dark_frame) // تأكد من أن الأيقونة موجودة
            .setContentTitle("حان موعد الدواء!")
            .setContentText("It's time to take $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        // 3. إظهار الإشعار
        with(NotificationManagerCompat.from(context)) {
            // التحقق من تصريح الإشعارات لأندرويد 13+
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(doseId, builder.build())
            }
        }
    }
}