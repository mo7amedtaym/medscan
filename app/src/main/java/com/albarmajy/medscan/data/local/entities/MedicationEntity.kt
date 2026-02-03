package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val dosage: String,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)