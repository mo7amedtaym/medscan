package com.albarmajy.medscan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

@Dao
interface MedicationDao {

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :medId LIMIT 1")
    fun getMedicationWithPlanById(medId: Long): Flow<MedicationWithPlan?>

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveMedicationsWithPlans(): Flow<List<MedicationWithPlan>>

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 0 ORDER BY createdAt DESC")
    fun getPausedMedicationsWithPlans(): Flow<List<MedicationWithPlan>>

    @Transaction
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :query || '%'")
    fun searchMedicationsWithPlans(query: String): Flow<List<MedicationWithPlan>>

    @Query("UPDATE medications SET isActive = :status WHERE id = :medId")
    suspend fun updateMedicationStatus(medId: Long, status: Boolean)
    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)


    @Query("UPDATE dose_logs SET status = :status, actualTime = :actualTime WHERE id = :doseId")
    suspend fun updateDoseStatus(doseId: Long, status: DoseStatus, actualTime: LocalDateTime?)

    @Query("SELECT * FROM medication_plans WHERE isPermanent = 1")
    suspend fun getAllPermanentPlans(): List<MedicationPlanEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM medications WHERE id = :medId)")
    suspend fun isMedicationExists(medId: Long): Boolean

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert
    suspend fun insertDoseLog(doseLog: DoseLogEntity)

    @Insert
    suspend fun insertMedicationPlan(plan: MedicationPlanEntity): Long

    //plan query
    @Query("DELETE FROM medication_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)

    @Query("UPDATE medication_plans SET endDate = :endDate, isPermanent = 0 WHERE id = :planId")
    suspend fun updatePlanEndDate(planId: Long, endDate: LocalDate)

    @Query("DELETE FROM dose_logs WHERE medicationId = :medId AND scheduledTime > :now")
    suspend fun deleteFutureDoses(medId: Long, now: LocalDateTime)



    @Insert
    suspend fun insertAllDoses(doses: List<DoseLogEntity>)

    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getAllActiveMedications(): Flow<List<MedicationEntity>>

    @Transaction
    @Query("""
        SELECT * FROM dose_logs 
        WHERE scheduledTime BETWEEN :startOfDay AND :endOfDay 
        ORDER BY scheduledTime ASC
    """)
    fun getDosesWithMedicationForDate(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Flow<List<DoseWithMedication>>

    @Query("SELECT * FROM medication_plans WHERE medicationId = :medId LIMIT 1")
    suspend fun getPlanByMedicationId(medId: Long): MedicationPlanEntity?

    @Query("DELETE FROM dose_logs WHERE medicationId = :medId AND status = 'PENDING' AND scheduledTime >= :fromTime")
    suspend fun deleteFuturePendingDoses(medId: Long, fromTime: LocalDateTime)

    @Transaction
    suspend fun insertFullMedicationData(
        medication: MedicationEntity,
        plan: MedicationPlanEntity,
        doses: List<DoseLogEntity>
    ) {
        val medId = if (isMedicationExists(medication.id)) {
            medication.id
        } else {
            insertMedication(medication)
        }

        val planId = insertMedicationPlan(plan.copy(medicationId = medId))

        val dosesWithIds = doses.map { it.copy(medicationId = medId, planId = planId) }
        insertAllDoses(dosesWithIds)
    }

    @Query("""
        SELECT * FROM dose_logs
        ORDER BY scheduledTime ASC
    """)
    fun getDoses(): Flow<List<DoseLogEntity>>

    @Update
    suspend fun updateDoseLog(doseLog: DoseLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReferences(references: List<MedicineReferenceEntity>)

    @Query("""
    SELECT * FROM medicine_reference 
    WHERE LOWER(trade_name_en) = LOWER(:query) 
    OR LOWER(trade_name_en) LIKE LOWER(:query) || '%' 
    LIMIT 1
    """)
    suspend fun searchMedicine(query: String): List<MedicineReferenceEntity>

    companion object
}

@Dao
interface DoseLogDao {
    @Query("SELECT scheduledTime FROM dose_logs WHERE planId = :planId ORDER BY scheduledTime DESC LIMIT 1")
    suspend fun getLastDoseTime(planId: Long): LocalDateTime?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllDoses(doses: List<DoseLogEntity>): List<Long>

    @Query("UPDATE dose_logs SET status = :newStatus, actualTime = :currentTime WHERE id = :doseId")
    suspend fun updateDoseStatus(doseId: Long, newStatus: DoseStatus, currentTime: LocalDateTime = LocalDateTime.now())

    @Query("SELECT * FROM dose_logs WHERE planId = :planId")
    suspend fun getDosesForPlan(planId: Long): List<DoseLogEntity>
}

