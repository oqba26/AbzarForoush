package com.oqba26.abzarforoush.data

import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val products: List<Product>,
    val customers: List<Customer>,
    val invoices: List<Invoice>,
    val invoiceItems: List<InvoiceItem>
)
