package com.albarmajy.medscan.domain.repository

import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: Int): MedicationEntity?

    fun getTodayDoses(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseLogEntity>>
    fun getDoses(): Flow<List<DoseLogEntity>>

    suspend fun addNewMedication(medication: MedicationEntity, doses: List<DoseLogEntity>)

    suspend fun updateDoseStatus(doseLog: DoseLogEntity)

    suspend fun searchMedicineInReference(query: String): MedicineReferenceEntity?
}