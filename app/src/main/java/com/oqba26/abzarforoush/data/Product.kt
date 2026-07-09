package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.chitralabs.sheetz.annotation.Column
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    @Column(ignore = true)
    @SerialName("id") val id: Int = 0,
    
    @Column("نام کالا")
    @SerialName("name") val name: String = "",
    
    @Column("بارکد")
    @SerialName("barcode") val barcode: String? = null,
    
    @Column("قیمت (تومان)")
    @SerialName("price") val price: Double = 0.0,
    
    @Column("قیمت عمده")
    @SerialName("wholesalePrice") val wholesalePrice: Double = 0.0,

    @Column("قیمت همکار")
    @SerialName("partnerPrice") val partnerPrice: Double = 0.0,
    
    @Column("قیمت خرید")
    @SerialName("purchasePrice") val purchasePrice: Double = 0.0,
    
    @Column("واحد")
    @SerialName("unit") val unit: String = "عدد",
    
    @Column("تعداد موجودی")
    @SerialName("stock") val stock: Double = 0.0,

    @Column("حداقل موجودی")
    @SerialName("minStock") val minStock: Double = 0.0,
    
    @Column("دسته بندی")
    @SerialName("category") val category: String = "بدون دسته بندی",
    
    @Column(ignore = true)
    @SerialName("lastUpdated") val lastUpdated: Long = System.currentTimeMillis()
)
