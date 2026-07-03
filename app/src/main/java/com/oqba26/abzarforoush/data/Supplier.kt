package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val totalDebtToSupplier: Double = 0.0 // چقدر به این تامین‌کننده بدهکاریم
)
