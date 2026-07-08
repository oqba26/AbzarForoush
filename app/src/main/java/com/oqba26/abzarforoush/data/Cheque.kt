package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class ChequeType {
    RECEIVABLE, // دریافتی (از مشتری)
    PAYABLE     // پرداختی (به تامین‌کننده)
}

@Serializable
enum class ChequeStatus {
    PENDING,    // در انتظار سررسید
    CLEARED,    // پاس شده
    BOUNCED,    // برگشت خورده
    CANCELLED   // ابطال شده
}

@Serializable
@Entity(tableName = "cheques")
data class Cheque(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val chequeNumber: String,
    val bankName: String,
    val amount: Double = 0.0,
    val dueDate: Long = 0L,
    val personName: String, // نام مشتری یا تامین‌کننده
    val type: ChequeType,
    val status: ChequeStatus = ChequeStatus.PENDING,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
