package com.albarmajy.medscan.data.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.domain.repository.MedicationRepository
import com.albarmajy.medscan.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    // هنا نستخدم inject لجلب الـ Repository من تعريفات Koin
    private val repository: MedicationRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val medId = intent?.getIntExtra("EXTRA_MED_ID", -1) ?: return
        val medName = intent.getStringExtra("EXTRA_MED_NAME") ?: "Medicine"
        val action = intent.action

        if (action == "ACTION_DONE") {
            // تنفيذ العملية في الـ Repository
            handleDoneAction(context, medId)
        } else {
            // وقت المنبه الأصلي: إظهار الإشعار
            context?.let {
                val notificationHelper = NotificationHelper(it)
                notificationHelper.showNotification(medId, medName)
            }
        }
    }

    private fun handleDoneAction(context: Context?, doseId: Int) {
        // بما أن الـ Repository يعمل بـ Suspend functions، نستخدم Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // تحديث حالة الجرعة في Room
                repository.updateDoseStatusById(doseId.toLong(), DoseStatus.TAKEN, LocalDateTime.now())

                // إلغاء الإشعار بعد التحديث
                val notificationManager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(doseId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}