package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "medication_plans",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val timesOfDay: List<LocalTime>,
    val isPermanent: Boolean = false
)