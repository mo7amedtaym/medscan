package com.albarmajy.medscan.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.scheduler.DoseAlarmReceiver
import java.time.ZoneId

class MedicationAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(dose: DoseLogEntity, medicationName: String) {
        val zdt = dose.scheduledTime.atZone(ZoneId.systemDefault())
        val timeInMillis = zdt.toInstant().toEpochMilli()
        val currentTime = System.currentTimeMillis()

        if (timeInMillis <= currentTime) {
            Log.w("AlarmScheduler", "Skipping dose ${dose.id}: Time ${dose.scheduledTime} is in the past.")
            return
        }

        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            putExtra("DOSE_ID", dose.id)
            putExtra("MED_NAME", medicationName)
            action = "com.albarmajy.medscan.ACTION_DOSE_ALARM_${dose.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dose.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    Log.d("AlarmScheduler", "Inexact Alarm set for $medicationName at $timeInMillis")
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    Log.d("AlarmScheduler", "Exact Alarm set for $medicationName at ${dose.scheduledTime}")
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancel(doseId: Long) {
        val intent = Intent(context, DoseAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmScheduler", "Alarm canceled for dose $doseId")
        }
    }
}