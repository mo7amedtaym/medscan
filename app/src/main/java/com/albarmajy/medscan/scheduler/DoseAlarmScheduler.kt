package com.albarmajy.medscan.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.albarmajy.medscan.ui.navigation.Routes

class DoseAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(doseId: Int, timeInMillis: Long, medicineName: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MEDICINE_NAME", medicineName)
            putExtra("EXTRA_DOSE_ID", doseId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId, // معرف فريد لكل جرعة حتى لا تختلط التنبيهات
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ضبط التنبيه بدقة حتى لو كان الهاتف في وضع السكون
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    fun cancel(doseId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}