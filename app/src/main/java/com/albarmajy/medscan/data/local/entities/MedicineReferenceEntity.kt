package com.albarmajy.medscan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_reference")
data class MedicineReferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trade_name_en: String,
    val trade_name_ar: String,
    val active_ingredient: String,
    val form: String,
    val strength: String,




)