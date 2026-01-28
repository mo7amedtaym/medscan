package com.albarmajy.medscan.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class MedicationWithPlan(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val plan: MedicationPlanEntity?
){
    fun getNextDoseTime(): LocalDateTime? {
        val plan = this.plan ?: return null
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        val nextTimeToday = plan.timesOfDay
            .map { LocalDateTime.of(today, it) }
            .filter { it.isAfter(now) }
            .minByOrNull { it }

        if (nextTimeToday != null) return nextTimeToday

        val tomorrow = today.plusDays(1)
        if (plan.endDate != null && tomorrow.isAfter(plan.endDate)) return null

        return plan.timesOfDay
            .map { LocalDateTime.of(tomorrow, it) }
            .minByOrNull { it }
    }
}