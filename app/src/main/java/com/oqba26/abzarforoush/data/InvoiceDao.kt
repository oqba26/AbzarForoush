package com.oqba26.abzarforoush.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesRaw(): List<Invoice>

    @Query("SELECT * FROM invoice_items")
    suspend fun getAllInvoiceItemsRaw(): List<InvoiceItem>

    @Query("SELECT * FROM invoice_items")
    fun getAllInvoiceItemsFlow(): Flow<List<InvoiceItem>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: Int): InvoiceWithItems

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun deleteInvoice(invoiceId: Int)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItems(invoiceId: Int)

    @Query("""
        SELECT priceAtSale FROM invoice_items 
        INNER JOIN invoices ON invoice_items.invoiceId = invoices.id 
        WHERE invoices.customerId = :customerId AND invoice_items.productName = :productName 
        ORDER BY invoices.timestamp DESC LIMIT 1
    """)
    suspend fun getLastPriceForCustomer(customerId: Int, productName: String): Double?
}

data class InvoiceWithItems(
    @androidx.room.Embedded val invoice: Invoice,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)
