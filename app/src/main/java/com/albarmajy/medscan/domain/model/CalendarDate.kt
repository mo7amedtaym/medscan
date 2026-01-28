package com.albarmajy.medscan.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDate(
    val date: LocalDate,
    val isSelected: Boolean = false,
    val hasDoses: Boolean = true
) {
    val dayName: String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val dateNumber: String = date.dayOfMonth.toString()
    val fullDisplay: String = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
}