package com.oqba26.abzarforoush.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceType {
    @SerialName("SALE") SALE, 
    @SerialName("PURCHASE") PURCHASE
}

@Serializable
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id") val id: Int = 0,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerialName("type") val type: InvoiceType = InvoiceType.SALE,
    @SerialName("totalAmount") val totalAmount: Double = 0.0,
    @SerialName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerialName("customerId")
    @ColumnInfo(name = "customerId")
    val customerId: Long? = null,
    @SerialName("supplierId")
    @ColumnInfo(name = "supplierId")
    val supplierId: Long? = null,
    @SerialName("amountPaid") val amountPaid: Double = 0.0,
    @SerialName("dueDate") val dueDate: Long? = null
)
