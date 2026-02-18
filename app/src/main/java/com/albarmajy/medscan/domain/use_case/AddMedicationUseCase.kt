package com.albarmajy.medscan.domain.use_case

import com.albarmajy.medscan.domain.alarm.AlarmScheduler
import com.albarmajy.medscan.domain.model.Medication
import com.albarmajy.medscan.domain.repository.MedicationRepository

class CreatePlanUseCase(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(medication: Medication) {
        // 1. حفظ الدواء في قاعدة البيانات Room
        val id = repository.insertMedication(medication).toInt()
        
        // 2. جدولة المنبه بناءً على البيانات المحفوظة
        val alarmItem = MedicationAlarm(
            id = id,
            time = medication.reminderTime,
            medName = medication.name
        )
        
        alarmScheduler.schedule(alarmItem)
    }
}