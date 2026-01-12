package com.albarmajy.medscan.data.local.converters

import androidx.room.TypeConverter
import com.albarmajy.medscan.domain.model.DoseStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromDoseStatus(status: DoseStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDoseStatus(status: String): DoseStatus {
        return DoseStatus.valueOf(status)
    }

    @TypeConverter
    fun fromLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromLocalTime(value: Int?): LocalTime? {
        return value?.let { LocalTime.ofNanoOfDay(it.toLong() * 1_000_000) }
    }

    @TypeConverter
    fun localTimeToNano(time: LocalTime?): Int? {
        return time?.nano?.div(1_000_000) // تخزين الملي ثانية كـ Integer
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<LocalTime>? {
        val listType = object : TypeToken<List<String>>() {}.type
        val stringList: List<String> = Gson().fromJson(value, listType) ?: return null
        return stringList.map { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromListToString(list: List<LocalTime>?): String? {
        val stringList = list?.map { it.toString() }
        return Gson().toJson(stringList)
    }
}