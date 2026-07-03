package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceType {
    SALE, PURCHASE
}

@Serializable
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: InvoiceType = InvoiceType.SALE,
    val totalAmount: Double,
    val totalDiscount: Double = 0.0,
    val customerId: Int? = null,
    val supplierId: Int? = null,
    val amountPaid: Double = 0.0,
    val dueDate: Long? = null
)
