package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    DEBT_INCREASE, // بدهی جدید (خرید نسیه)
    PAYMENT       // پرداخت (تسویه)
}

@Serializable
@Entity(tableName = "debt_transactions")
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int? = null,
    val supplierId: Int? = null,
    val invoiceId: Int? = null,
    val amount: Double = 0.0,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val type: TransactionType
)
