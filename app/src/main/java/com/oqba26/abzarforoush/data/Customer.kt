package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomerType {
    @SerialName("PERSON") PERSON,
    @SerialName("COMPANY") COMPANY,
    @SerialName("INSTITUTION") INSTITUTION,
    @SerialName("OTHER") OTHER
}

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("landline") val landline: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("type") val type: CustomerType = CustomerType.PERSON,
    @SerialName("totalDebt") val totalDebt: Double = 0.0
)
