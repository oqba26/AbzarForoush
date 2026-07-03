package com.oqba26.abzarforoush.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtTransactionDao {
    @Query("SELECT * FROM debt_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<DebtTransaction>>

    @Insert
    suspend fun insert(transaction: DebtTransaction)

    @Delete
    suspend fun delete(transaction: DebtTransaction)
    
    @Query("DELETE FROM debt_transactions WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Int)

    @Query("SELECT * FROM debt_transactions")
    suspend fun getAllTransactionsList(): List<DebtTransaction>
}
