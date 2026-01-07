package com.albarmajy.medscan.data.local.converters

import androidx.room.TypeConverter
import com.albarmajy.medscan.domain.model.DoseStatus
import java.time.Instant
import java.time.LocalDateTime
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
}