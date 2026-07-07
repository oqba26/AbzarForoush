package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class CustomerType {
    PERSON,      // شخص
    COMPANY,     // شرکت
    INSTITUTION, // موسسه
    OTHER        // سایر
}

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String? = null,
    val landline: String? = null,
    val address: String? = null,
    val type: CustomerType = CustomerType.PERSON,
    val totalDebt: Double = 0.0
)
