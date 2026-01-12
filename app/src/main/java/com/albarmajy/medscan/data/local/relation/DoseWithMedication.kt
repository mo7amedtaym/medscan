package com.albarmajy.medscan.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity

data class DoseWithMedication(
    @Embedded val dose: DoseLogEntity,
    @Relation(
        parentColumn = "medicationId",
        entityColumn = "id"
    )
    val medication: MedicationEntity
)