package com.albarmajy.medscan.domain.model

import java.time.LocalTime

data class DoseUiState(
    val id: Int,
    val hour: Int,
    val minute: Int = 0,
    val amPm: String = "AM",
    val amount: Int = 1
){
    fun toLocalTime(): LocalTime {
        val hour24 = when {
            amPm == "PM" && hour < 12 -> hour + 12
            amPm == "AM" && hour == 12 -> 0
            else -> hour
        }
        return LocalTime.of(hour24, minute)
    }
}