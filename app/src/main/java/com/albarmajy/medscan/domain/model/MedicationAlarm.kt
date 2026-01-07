package com.albarmajy.medscan.domain.model

import java.time.LocalDateTime

data class MedicationDose(
    val id: Int = 0,
    val medicationId: Int,
    val scheduledTime: Int,
    val isTaken: Boolean = false,
    val actualTakenTime: LocalDateTime? = null
)