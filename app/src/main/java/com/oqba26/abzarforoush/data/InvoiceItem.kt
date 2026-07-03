package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceId: Int,
    val productName: String,
    val quantity: Double,
    val unit: String = "عدد",
    val priceAtSale: Double,
    val purchasePriceAtSale: Double = 0.0,
    val discount: Double = 0.0
)
