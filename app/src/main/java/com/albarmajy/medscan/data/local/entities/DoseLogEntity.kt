package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.albarmajy.medscan.domain.model.DoseStatus
import java.time.LocalDateTime

@Entity(
    tableName = "dose_logs",
    indices = [Index(value = ["scheduledTime"])],
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledTime: LocalDateTime,
    val actualTime: Long? = null,
    val dosage: String? = null,
    val status: DoseStatus = DoseStatus.PENDING


)

