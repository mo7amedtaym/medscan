package com.albarmajy.medscan.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.albarmajy.medscan.notification.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val medId = intent?.getIntExtra("EXTRA_MED_ID", -1) ?: return
        val medName = intent.getStringExtra("EXTRA_MED_NAME") ?: "Medicine"

        // هنا نقوم بإظهار الإشعار مباشرة
        val notificationHelper = NotificationHelper(context!!)
        notificationHelper.showNotification(medId, medName)
    }
}