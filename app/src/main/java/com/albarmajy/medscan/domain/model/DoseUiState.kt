package com.albarmajy.medscan.domain.model

import java.time.LocalTime

data class DoseUiState(
    val id: Int,
    var hour: Int,
    val minute: Int = 0,
    var amPm: String = "PM",
    val amount: Int = 1
){
    init {
        if(hour > 12) {
            amPm = "PM"
            hour -= 12
        }
    }
    fun toLocalTime(): LocalTime {

        val hour24 = when {
            amPm == "PM" && hour < 12 -> hour + 12
            amPm == "AM" && hour == 12 -> 0
            else -> hour
        }
        return LocalTime.of(hour24, minute)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoseUiState) return false


        return id == other.id &&
                hour == other.hour &&
                minute == other.minute &&
                amPm == other.amPm &&
                amount == other.amount
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + hour
        result = 31 * result + minute
        result = 31 * result + amount
        result = 31 * result + amPm.hashCode()
        return result
    }
}