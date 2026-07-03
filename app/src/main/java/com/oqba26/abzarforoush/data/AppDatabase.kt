package com.oqba26.abzarforoush.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class, Invoice::class, InvoiceItem::class, Customer::class, DebtTransaction::class, Bundle::class, BundleItem::class, Expense::class, Supplier::class, Cheque::class], version = 13, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun debtTransactionDao(): DebtTransactionDao
    abstract fun bundleDao(): BundleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun chequeDao(): ChequeDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "abzar_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
