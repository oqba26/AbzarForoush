package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.chitralabs.sheetz.annotation.Column
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    @Column(ignore = true)
    val id: Int = 0,
    
    @Column("نام کالا")
    val name: String = "",
    
    @Column("بارکد")
    val barcode: String? = null,
    
    @Column("قیمت (تومان)")
    val price: Double = 0.0,
    
    @Column("قیمت عمده")
    val wholesalePrice: Double = 0.0,

    @Column("قیمت همکار")
    val partnerPrice: Double = 0.0,
    
    @Column("قیمت خرید")
    val purchasePrice: Double = 0.0,
    
    @Column("واحد")
    val unit: String = "عدد",
    
    @Column("تعداد موجودی")
    val stock: Double = 0.0,

    @Column("حداقل موجودی")
    val minStock: Double = 0.0,
    
    @Column("دسته بندی")
    val category: String = "بدون دسته بندی",
    
    @Column(ignore = true)
    val lastUpdated: Long = System.currentTimeMillis()
)
