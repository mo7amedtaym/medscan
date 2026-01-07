package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.albarmajy.medscan.domain.model.DoseStatus
import java.time.LocalDateTime

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledTime: LocalDateTime,
    val actualTime: LocalDateTime? = null,
    val dosage: String? = null,
    val status: DoseStatus = DoseStatus.PENDING
)