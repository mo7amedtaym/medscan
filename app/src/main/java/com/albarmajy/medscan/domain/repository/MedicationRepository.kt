package com.albarmajy.medscan.domain.repository

import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface MedicationRepository {

    fun getMedicationWithPlanById(id: Long): Flow<MedicationWithPlan?>
    fun getActiveMedicationsWithPlans(): Flow<List<MedicationWithPlan>>
    fun getPausedMedicationsWithPlans(): Flow<List<MedicationWithPlan>>
    fun searchMedicationsWithPlans(query: String): Flow<List<MedicationWithPlan>>
    suspend fun updatePlanEndDate(planId: Long)
    suspend fun updateMedicationStatus(medId: Long, status: Boolean)
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: Long): MedicationEntity?
    suspend fun deleteMedication(id: Long)

    fun getDosesWithMedicationForToday(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseWithMedication>>
    fun getDosesWithMedicationForDate(date: LocalDate): Flow<List<DoseWithMedication>>
    fun getDoses(): Flow<List<DoseLogEntity>>

    suspend fun addNewMedication(medication: MedicationEntity)

    suspend fun updateDoseStatus(doseLog: DoseLogEntity)

    suspend fun searchMedicineInReference(query: String): MedicineReferenceEntity?

    suspend fun addNewMedicationWithSchedule(
        plan: MedicationPlanEntity,
        dosagePerDose: String?,
        includePastDoses: Boolean,
    )


    suspend fun updateDoseStatus(doseId: Long, newStatus: DoseStatus)

    suspend fun getAllPermanentPlans(): List<MedicationPlanEntity>

    suspend fun getLastDoseDateForPlan(planId: Long): LocalDate?

    suspend fun generateDosesRange(
        plan: MedicationPlanEntity,
        startDate: LocalDate,
        endDate: LocalDate
    )

    suspend fun deletePlan(plan: MedicationPlanEntity)
    suspend fun updatePlanEndDate(planId: Long, newEndDate: LocalDate)
    suspend fun deleteFutureDoses(medicationId: Long, currentTime: LocalDateTime = LocalDateTime.now())


}