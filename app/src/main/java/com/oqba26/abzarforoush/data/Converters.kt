package com.oqba26.abzarforoush.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Suppress("unused")
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        if (value == null) return null
        // سازگاری با داده‌های قدیمی: اگر مقدار خیلی بزرگ بود، یعنی میلی‌ثانیه است
        return if (value > 1_000_000_000_000L) {
            Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.ofEpochDay(value)
        }
    }

    @TypeConverter
    fun dateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
