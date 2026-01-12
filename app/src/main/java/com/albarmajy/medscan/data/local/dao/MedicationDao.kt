package com.albarmajy.medscan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlan
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM medications WHERE id = :medId)")
    suspend fun isMedicationExists(medId: Long): Boolean

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?


    @Insert
    suspend fun insertDoseLog(doseLog: DoseLogEntity)

    @Insert
    suspend fun insertMedicationPlan(plan: MedicationPlan)

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
    fun getDosesWithMedicationForToday(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Flow<List<DoseWithMedication>>

    @Query("SELECT * FROM medication_plans WHERE medicationId = :medId LIMIT 1")
    suspend fun getPlanByMedicationId(medId: Long): MedicationPlan?

    @Query("DELETE FROM dose_logs WHERE medicationId = :medId AND status = 'PENDING' AND scheduledTime >= :fromTime")
    suspend fun deleteFuturePendingDoses(medId: Long, fromTime: LocalDateTime)


    @Transaction
    suspend fun insertFullMedicationData(
        medication: MedicationEntity,
        plan: MedicationPlan,
        doses: List<DoseLogEntity>
    ) {
        val medId = insertMedication(medication)
        insertMedicationPlan(plan.copy(medicationId = medId))
        val dosesWithId = doses.map { it.copy(medicationId = medId) }
        insertAllDoses(dosesWithId)
    }

    @Query("""
        SELECT * FROM dose_logs
        ORDER BY scheduledTime ASC
    """)
    fun getDoses(): Flow<List<DoseLogEntity>>

    @Update
    suspend fun updateDoseLog(doseLog: DoseLogEntity)



    //references side
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