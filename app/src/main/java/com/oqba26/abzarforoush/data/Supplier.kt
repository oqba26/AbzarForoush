package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("totalDebtToSupplier") val totalDebtToSupplier: Double = 0.0 // چقدر به این تامین‌کننده بدهکاریم
)
