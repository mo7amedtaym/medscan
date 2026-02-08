package com.albarmajy.medscan.data.repository

import android.util.Log
import com.albarmajy.medscan.data.local.dao.DoseLogDao
import com.albarmajy.medscan.data.local.dao.MedicationDao
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.repository.MedicationRepository
import com.albarmajy.medscan.scheduler.MedicationAlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val doseDao: DoseLogDao,
    private val alarmScheduler: MedicationAlarmScheduler
) : MedicationRepository {
    override fun getMedicationWithPlanById(id: Long): Flow<MedicationWithPlan?> {
        return medicationDao.getMedicationWithPlanById(id)
    }

    override fun getActiveMedicationsWithPlans(): Flow<List<MedicationWithPlan>> = medicationDao.getActiveMedicationsWithPlans()


    override fun getPausedMedicationsWithPlans(): Flow<List<MedicationWithPlan>> = medicationDao.getPausedMedicationsWithPlans()

    override fun searchMedicationsWithPlans(query: String): Flow<List<MedicationWithPlan>> = medicationDao.searchMedicationsWithPlans(query)
    override suspend fun updatePlanEndDate(planId: Long) {
        medicationDao.updatePlanEndDate(planId, LocalDate.now())
    }


    override suspend fun updateMedicationStatus(medId: Long, status: Boolean) {
        medicationDao.updateMedicationStatus(medId, status)
    }

    override fun getAllMedications(): Flow<List<MedicationEntity>> =
        medicationDao.getAllActiveMedications()

    override suspend fun getMedicationById(id: Long): MedicationEntity? = medicationDao.getMedicationById(id)
    override suspend fun deleteMedication(id: Long) {
        medicationDao.deleteMedication(id)
    }

    override fun getDosesWithMedicationForToday(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseWithMedication>> {

        Log.d("MedicationRepositoryImpl", "getTodayDoses: $startOfDay, $endOfDay")
        return medicationDao.getDosesWithMedicationForDate(startOfDay, endOfDay).map { list ->
            list.map { dose ->
                if (dose.dose.scheduledTime.isBefore(LocalDateTime.now().minusMinutes(15)) &&
                    dose.dose.status == DoseStatus.PENDING) {
                    updateDoseStatus(dose.dose.id, DoseStatus.MISSED)
                }
                dose
            }
        }
    }

    override fun getDosesWithMedicationForDate(date: LocalDate): Flow<List<DoseWithMedication>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        return medicationDao.getDosesWithMedicationForDate(startOfDay, endOfDay).map { list ->
            list.map { dose ->
                if (dose.dose.scheduledTime.isBefore(LocalDateTime.now().minusMinutes(15)) &&
                    dose.dose.status == DoseStatus.PENDING) {
                    updateDoseStatus(dose.dose.id, DoseStatus.MISSED)
                }
                dose
            }
        }
    }

    override fun getDoses(): Flow<List<DoseLogEntity>> {
        return medicationDao.getDoses()
    }

    override suspend fun addNewMedication(medication: MedicationEntity) {
        val isMedicationExists = medicationDao.isMedicationExists(medication.id)
        if (!isMedicationExists){
            medicationDao.insertMedication(medication)
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

    override suspend fun addNewMedicationWithSchedule(
        plan: MedicationPlanEntity,
        dosagePerDose: String?,
        includePastDoses: Boolean
    ) {
        val planId = medicationDao.insertMedicationPlan(plan)

        val calculationEndDate = if (plan.isPermanent) {
            plan.startDate.plusDays(30)
        } else {
            plan.endDate ?: plan.startDate.plusDays(30)
        }

        val generatedDoses = mutableListOf<DoseLogEntity>()
        var currentDay = plan.startDate

        while (!currentDay.isAfter(calculationEndDate)) {
            plan.timesOfDay.forEach { time ->
                generatedDoses.add(
                    DoseLogEntity(
                        medicationId = plan.medicationId,
                        planId = planId,
                        scheduledTime = LocalDateTime.of(currentDay, time),
                        dosage = dosagePerDose,
                        status = DoseStatus.PENDING
                    )
                )
            }
            currentDay = currentDay.plusDays(1)
        }

        medicationDao.insertAllDoses(generatedDoses)
    }

    override suspend fun updateDoseStatus(
        doseId: Long,
        newStatus: DoseStatus
    ) {
        doseDao.updateDoseStatus(doseId, newStatus)
    }

    override suspend fun getAllPermanentPlans(): List<MedicationPlanEntity> {
        return medicationDao.getAllPermanentPlans()
    }

    override suspend fun getLastDoseDateForPlan(planId: Long): LocalDate? {
        doseDao.getLastDoseTime(planId)?.let {
            return it.toLocalDate()
        }
        return null
    }

    override suspend fun generateDosesRange(
        plan: MedicationPlanEntity,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val medicationName = getMedicationById(plan.medicationId)?.name ?: "Unknown Medication"
        val generatedDoses = mutableListOf<DoseLogEntity>()
        var currentDay = startDate

        val today = LocalDate.now()
        if (currentDay.isBefore(today)) currentDay = today

        while (!currentDay.isAfter(endDate)) {
            plan.timesOfDay.forEach { time ->
                generatedDoses.add(
                    DoseLogEntity(
                        medicationId = plan.medicationId,
                        planId = plan.id,
                        scheduledTime = LocalDateTime.of(currentDay, time),
                        status = DoseStatus.PENDING
                    )
                )
            }
            currentDay = currentDay.plusDays(1)
        }

        if (generatedDoses.isNotEmpty()) {
            val newIds = doseDao.insertAllDoses(generatedDoses)

            generatedDoses.forEachIndexed { index, dose ->
                val assignedId = newIds[index]
                alarmScheduler.schedule(dose.copy(id = assignedId), medicationName)
            }
        }


    }

    override suspend fun deletePlan(plan: MedicationPlanEntity) {
        medicationDao.deletePlan(plan.id)
    }

    override suspend fun updatePlanEndDate(planId: Long, newEndDate: LocalDate) {
        medicationDao.updatePlanEndDate(planId, newEndDate)
    }

    override suspend fun deleteFutureDoses(
        medicationId: Long,
        currentTime: LocalDateTime
    ) {
        doseDao.deleteFutureDoses(medicationId, currentTime)
    }


}


