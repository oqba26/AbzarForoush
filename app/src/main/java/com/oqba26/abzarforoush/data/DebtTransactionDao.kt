package com.oqba26.abzarforoush.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtTransactionDao {
    @Query("SELECT * FROM debt_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<DebtTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: DebtTransaction)

    @Update
    suspend fun update(transaction: DebtTransaction)

    @Delete
    suspend fun delete(transaction: DebtTransaction)
    
    @Query("DELETE FROM debt_transactions WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Int)

    @Query("SELECT * FROM debt_transactions")
    suspend fun getAllTransactionsList(): List<DebtTransaction>

    @Query("SELECT * FROM debt_transactions WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<DebtTransaction>>
}
