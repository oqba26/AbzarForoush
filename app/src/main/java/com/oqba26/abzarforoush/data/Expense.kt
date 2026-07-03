package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseCategory {
    RENT,        // اجاره
    BILLS,       // قبوض (آب، برق، گاز)
    SALARY,      // حقوق و دستمزد
    TAX,         // مالیات
    TRANSPORT,   // حمل و نقل
    REPAIR,      // تعمیرات و نگهداری
    MARKETING,   // تبلیغات
    OTHER        // سایر هزینه‌ها
}

@Serializable
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
