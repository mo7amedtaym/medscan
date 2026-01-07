package com.albarmajy.medscan.domain.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val type: String,
    val totalStock: Int
)