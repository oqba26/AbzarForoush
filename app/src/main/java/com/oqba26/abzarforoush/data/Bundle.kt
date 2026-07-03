package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "bundles")
data class Bundle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val discount: Double = 0.0 // Discount for the whole bundle
)

@Serializable
@Entity(
    tableName = "bundle_items",
    primaryKeys = ["bundleId", "productId"],
    indices = [Index(value = ["productId"])]
)
data class BundleItem(
    val bundleId: Int,
    val productId: Int,
    val quantity: Double
)
