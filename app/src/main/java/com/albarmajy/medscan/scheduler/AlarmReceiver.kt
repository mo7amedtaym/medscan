package com.albarmajy.medscan.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.albarmajy.medscan.notification.NotificationHelper

// scheduler/AlarmReceiver.kt
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val medicineName = intent?.getStringExtra("EXTRA_MEDICINE_NAME") ?: "Medicine"
        val doseId = intent?.getIntExtra("EXTRA_DOSE_ID", 0) ?: 0

        NotificationHelper.showNotification(context!!, medicineName, doseId)
    }
}