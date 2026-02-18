package com.albarmajy.medscan.domain.alarm

import java.time.LocalDateTime

interface AlarmScheduler {
    fun schedule(item: MedicationAlarm)
    fun cancel(item: MedicationAlarm)
}

data class MedicationAlarm(
    val id: Int,
    val time: LocalDateTime,
    val medName: String,

)