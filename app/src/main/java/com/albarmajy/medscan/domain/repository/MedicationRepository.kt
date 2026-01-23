package com.albarmajy.medscan.domain.repository

import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: Long): MedicationEntity?

    fun getDosesWithMedicationForToday(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseWithMedication>>
    fun getDoses(): Flow<List<DoseLogEntity>>

    suspend fun addNewMedication(medication: MedicationEntity)

    suspend fun updateDoseStatus(doseLog: DoseLogEntity)

    suspend fun searchMedicineInReference(query: String): MedicineReferenceEntity?

    suspend fun addNewMedicationWithSchedule(
        plan: MedicationPlanEntity,
        dosagePerDose: String?
    )


    suspend fun updateDoseStatus(doseId: Long, newStatus: DoseStatus)

    suspend fun getAllPermanentPlans(): List<MedicationPlanEntity>

    suspend fun getLastDoseDateForPlan(planId: Long): LocalDate?

    suspend fun generateDosesRange(
        plan: MedicationPlanEntity,
        startDate: LocalDate,
        endDate: LocalDate
    )
}