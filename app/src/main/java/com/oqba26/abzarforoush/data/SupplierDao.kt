package com.oqba26.abzarforoush.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET totalDebtToSupplier = totalDebtToSupplier + :amount WHERE id = :supplierId")
    suspend fun updateDebt(supplierId: Int, amount: Double)
}
