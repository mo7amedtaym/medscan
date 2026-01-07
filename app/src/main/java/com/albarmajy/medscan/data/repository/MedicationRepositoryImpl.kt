package com.albarmajy.medscan.data.repository

import android.util.Log
import com.albarmajy.medscan.data.local.dao.MedicationDao
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<MedicationEntity>> =
        medicationDao.getAllActiveMedications()

    override suspend fun getMedicationById(id: Int): MedicationEntity? = medicationDao.getMedicationById(id)


    override fun getTodayDoses(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseLogEntity>> {

        Log.d("MedicationRepositoryImpl", "getTodayDoses: $startOfDay, $endOfDay")
        return medicationDao.getDosesForToday(startOfDay, endOfDay)
    }

    override fun getDoses(): Flow<List<DoseLogEntity>> {
        return medicationDao.getDoses()
    }

    override suspend fun addNewMedication(medication: MedicationEntity, doses: List<DoseLogEntity>) {
        val medId = medicationDao.insertMedication(medication)
        doses.forEach { dose ->
            Log.d("DoseLogEntity", "dose: $dose")
            medicationDao.insertDoseLog(dose.copy(medicationId = medId))
        }
    }

    override suspend fun updateDoseStatus(doseLog: DoseLogEntity) {
        medicationDao.updateDoseLog(doseLog)
    }

    override suspend fun searchMedicineInReference(query: String): MedicineReferenceEntity? {
        val firstWord = query.split(" ").firstOrNull() ?: return null

        val results = medicationDao.searchMedicine(firstWord)

        return results.firstOrNull()
    }
}