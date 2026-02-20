package com.albarmajy.medscan.data.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.albarmajy.medscan.notification.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val medId = intent?.getIntExtra("EXTRA_MED_ID", -1) ?: return
        val medName = intent.getStringExtra("EXTRA_MED_NAME") ?: "Medicine"
        val action = intent.action

        if (action == "ACTION_DONE") {
            // هنا نحدث قاعدة البيانات (يفضل استخدام UseCase أو Repository)
            // ملاحظة: بما أننا في Receiver، يجب استخدام CoroutineScope


            // لإلغاء الإشعار بعد الضغط على الزر
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(doseId)
        }
        val notificationHelper = NotificationHelper(context!!)
        notificationHelper.showNotification(medId, medName)
    }
}