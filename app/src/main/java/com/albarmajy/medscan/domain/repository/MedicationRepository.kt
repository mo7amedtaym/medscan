package com.albarmajy.medscan.domain.repository

import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: Long): MedicationEntity?

    fun getDosesWithMedicationForToday(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseWithMedication>>
    fun getDoses(): Flow<List<DoseLogEntity>>

    suspend fun addNewMedication(medication: MedicationEntity, doses: List<DoseLogEntity>)

    suspend fun updateDoseStatus(doseLog: DoseLogEntity)

    suspend fun searchMedicineInReference(query: String): MedicineReferenceEntity?

    suspend fun addNewMedicationWithSchedule(
        medication: MedicationEntity,
        selectedTimes: List<LocalTime>,
        startDate: LocalDate,
        endDate: LocalDate?,
        isPermanent: Boolean
    )
}