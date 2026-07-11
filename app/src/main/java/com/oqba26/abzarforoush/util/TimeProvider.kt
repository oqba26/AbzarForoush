package com.oqba26.abzarforoush.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * مدیریت زمان به صورت متمرکز برای رعایت استانداردهای ۲۰۲۶ و قابلیت تست‌پذیری.
 */
interface TimeProvider {
    fun getToday(): LocalDate
    fun now(): LocalDateTime
    fun currentTimeMillis(): Long
}

/**
 * پیاده‌سازی واقعی زمان که در نسخه نهایی استفاده می‌شود.
 */
class RealTimeProvider : TimeProvider {
    override fun getToday(): LocalDate = LocalDate.now()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

/**
 * پیاده‌سازی ثابت برای زمان (مفید برای تست و تحلیل).
 * در اینجا زمان روی ۲۰۲۶-۰۷-۱۰ قفل شده است.
 */
@Suppress("unused")
class FixedTimeProvider(
    private val fixedDate: LocalDate = LocalDate.of(2026, 7, 10)
) : TimeProvider {
    override fun getToday(): LocalDate = fixedDate
    override fun now(): LocalDateTime = fixedDate.atStartOfDay()
    override fun currentTimeMillis(): Long = 
        fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
