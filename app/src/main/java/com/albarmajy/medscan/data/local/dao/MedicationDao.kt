package com.albarmajy.medscan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicationDao {
    @Insert
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert
    suspend fun insertDoseLog(doseLog: DoseLogEntity)

    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getAllActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): MedicationEntity?

    @Query("""
        SELECT * FROM dose_logs 
        WHERE scheduledTime BETWEEN :startOfDay AND :endOfDay 
        ORDER BY scheduledTime ASC
    """)
    fun getDosesForToday(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseLogEntity>>

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
}