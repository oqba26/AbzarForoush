package com.oqba26.abzarforoush.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    @SerialName("DEBT_INCREASE") DEBT_INCREASE, 
    @SerialName("PAYMENT") PAYMENT
}

@Serializable
@Entity(tableName = "debt_transactions")
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id") val id: Int = 0,
    @SerialName("customerId")
    @ColumnInfo(name = "customerId")
    val customerId: Long? = null,
    @SerialName("supplierId")
    @ColumnInfo(name = "supplierId")
    val supplierId: Long? = null,
    @SerialName("invoiceId") val invoiceId: Int? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("description") val description: String? = null,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerialName("dueDate") val dueDate: Long? = null,
    @SerialName("type") val type: TransactionType,
    @SerialName("isPaid") val isPaid: Boolean = false,
    @SerialName("paymentTimestamp") val paymentTimestamp: Long? = null
)
