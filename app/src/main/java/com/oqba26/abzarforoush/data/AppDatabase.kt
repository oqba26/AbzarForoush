package com.oqba26.abzarforoush.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Product::class, Invoice::class, InvoiceItem::class, Customer::class, DebtTransaction::class, Bundle::class, BundleItem::class, Expense::class, Supplier::class, Cheque::class, PendingDeletion::class], version = 16, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun debtTransactionDao(): DebtTransactionDao
    abstract fun bundleDao(): BundleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun chequeDao(): ChequeDao
    abstract fun pendingDeletionDao(): PendingDeletionDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `debt_transactions` ADD COLUMN `isPaid` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `debt_transactions` ADD COLUMN `paymentTimestamp` INTEGER")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `pending_deletions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tableName` TEXT NOT NULL, `identifier` TEXT NOT NULL, `identifierColumn` TEXT NOT NULL DEFAULT 'id')")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. ایجاد جداول کاملاً جدید
                db.execSQL("CREATE TABLE IF NOT EXISTS `bundles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `discount` REAL NOT NULL DEFAULT 0.0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `bundle_items` (`bundleId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `quantity` REAL NOT NULL, PRIMARY KEY(`bundleId`, `productId`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bundle_items_productId` ON `bundle_items` (`productId`)")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `description` TEXT, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `suppliers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phoneNumber` TEXT, `address` TEXT, `totalDebtToSupplier` REAL NOT NULL DEFAULT 0.0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cheques` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `chequeNumber` TEXT NOT NULL, `bankName` TEXT NOT NULL, `amount` REAL NOT NULL, `dueDate` INTEGER NOT NULL, `personName` TEXT NOT NULL, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `description` TEXT, `timestamp` INTEGER NOT NULL)")

                // 2. اصلاح جداول موجود
                
                // جدول مشتریان (اصلاح بر اساس لاگ کرش)
                addColumnIfNotExists(db, "customers", "address", "TEXT")
                addColumnIfNotExists(db, "customers", "landline", "TEXT")
                addColumnIfNotExists(db, "customers", "type", "TEXT NOT NULL DEFAULT 'PERSON'")

                // جدول محصولات
                addColumnIfNotExists(db, "products", "wholesalePrice", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "products", "partnerPrice", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "products", "minStock", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "products", "barcode", "TEXT")
                addColumnIfNotExists(db, "products", "lastUpdated", "INTEGER NOT NULL DEFAULT 0")

                // جدول آیتم‌های فاکتور
                addColumnIfNotExists(db, "invoice_items", "purchasePriceAtSale", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "invoice_items", "discount", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "invoice_items", "unit", "TEXT NOT NULL DEFAULT 'عدد'")

                // جدول فاکتورها
                addColumnIfNotExists(db, "invoices", "totalDiscount", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "invoices", "amountPaid", "REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "invoices", "dueDate", "INTEGER")
                addColumnIfNotExists(db, "invoices", "supplierId", "INTEGER")
                addColumnIfNotExists(db, "invoices", "type", "TEXT NOT NULL DEFAULT 'SALE'")
            }

            private fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDefinition: String) {
                try {
                    val cursor = db.query("PRAGMA table_info(`$tableName`)")
                    var exists = false
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        if (name == columnName) {
                            exists = true
                            break
                        }
                    }
                    cursor.close()
                    if (!exists) {
                        db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
                    }
                } catch (_: Exception) {
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "abzar_database")
                    .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
