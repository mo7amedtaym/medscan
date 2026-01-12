package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.albarmajy.medscan.domain.model.RecurrenceType
import java.time.LocalDateTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val dosage: String,
    val category: String? = null,
//    val recurrenceType: RecurrenceType,
//    val intervalHours: Int,
//    val startDate: LocalDateTime,
    val isActive: Boolean = true
)