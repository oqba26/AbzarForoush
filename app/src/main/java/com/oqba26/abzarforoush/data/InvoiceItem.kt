package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id") val id: Int = 0,
    @SerialName("invoiceId") val invoiceId: Int,
    @SerialName("productName") val productName: String,
    @SerialName("quantity") val quantity: Double = 0.0,
    @SerialName("unit") val unit: String = "عدد",
    @SerialName("priceAtSale") val priceAtSale: Double = 0.0,
    @SerialName("purchasePriceAtSale") val purchasePriceAtSale: Double = 0.0,
    @SerialName("discount") val discount: Double = 0.0
)
