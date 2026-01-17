package com.albarmajy.medscan.data.repository

import android.util.Log
import androidx.room.Transaction
import com.albarmajy.medscan.data.local.dao.MedicationDao
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<MedicationEntity>> =
        medicationDao.getAllActiveMedications()

    override suspend fun getMedicationById(id: Long): MedicationEntity? = medicationDao.getMedicationById(id)


    override fun getDosesWithMedicationForToday(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Flow<List<DoseWithMedication>> {

        Log.d("MedicationRepositoryImpl", "getTodayDoses: $startOfDay, $endOfDay")
        return medicationDao.getDosesWithMedicationForToday(startOfDay, endOfDay)
    }

    override fun getDoses(): Flow<List<DoseLogEntity>> {
        return medicationDao.getDoses()
    }

    override suspend fun addNewMedication(medication: MedicationEntity, doses: List<DoseLogEntity>) {
        val isMedicationExists = medicationDao.isMedicationExists(medication.id)
        var medId = medication.id
        if (!isMedicationExists){
            medId = medicationDao.insertMedication(medication)
        }

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
        Log.d("MedicationRepositoryImpl", "searchMedicineInReference: $results")

        return results.firstOrNull()
    }


    @Transaction
    override suspend fun addNewMedicationWithSchedule(
        medication: MedicationEntity,
        selectedTimes: List<LocalTime>,
        startDate: LocalDate,
        endDate: LocalDate?,
        isPermanent: Boolean
    ) {

        val isMedicationExists = medicationDao.isMedicationExists(medication.id)
        var medId = medication.id
        if (!isMedicationExists){
            medId = medicationDao.insertMedication(medication)
        }

        val existingPlan = medicationDao.getPlanByMedicationId(medId)

        if (existingPlan != null) {
            // سيناريو أ: تحديث الخطة القديمة
            // نقوم بمسح الجرعات المستقبلية (التي لم تؤخذ بعد) لنبدأ الخطة الجديدة من الآن
            medicationDao.deleteFuturePendingDoses(medId, LocalDateTime.now())
        }

        // 3. توليد الجرعات الجديدة (نفس الـ Loop السابق)
        val dosesToGenerate = mutableListOf<DoseLogEntity>()
        val finalEndDate = endDate ?: startDate.plusMonths(1)
        var currentDay = startDate

        while (!currentDay.isAfter(finalEndDate)) {
            selectedTimes.forEach { time ->
                val scheduledDateTime = LocalDateTime.of(currentDay, time)
                // لا نولد جرعات في الماضي إذا كان التاريخ هو اليوم
                if (scheduledDateTime.isAfter(LocalDateTime.now())) {
                    dosesToGenerate.add(
                        DoseLogEntity(
                            medicationId = medId,
                            scheduledTime = scheduledDateTime,
                            status = DoseStatus.PENDING
                        )
                    )
                }
            }
            currentDay = currentDay.plusDays(1)
        }

        // 4. حفظ الخطة الجديدة (أو تحديث الحالية)
        val newPlan = MedicationPlanEntity(
            id = existingPlan?.id ?: 0, // إذا كانت موجودة نستخدم نفس الـ ID لتحديثها
            medicationId = medId,
            startDate = startDate,
            endDate = endDate,
            timesOfDay = selectedTimes,
            isPermanent = isPermanent
        )

        medicationDao.insertMedicationPlan(newPlan)
        medicationDao.insertAllDoses(dosesToGenerate)
    }

}


