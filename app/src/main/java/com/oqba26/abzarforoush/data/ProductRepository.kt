package com.oqba26.abzarforoush.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    private val debtTransactionDao: DebtTransactionDao,
    private val bundleDao: BundleDao,
    private val expenseDao: ExpenseDao,
    private val supplierDao: SupplierDao,
    private val chequeDao: ChequeDao,
    private val pendingDeletionDao: PendingDeletionDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoices()
    val allInvoiceItems: Flow<List<InvoiceItem>> = invoiceDao.getAllInvoiceItemsFlow()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allBundles: Flow<List<BundleWithProducts>> = bundleDao.getAllBundles()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    val allCheques: Flow<List<Cheque>> = chequeDao.getAllCheques()

    suspend fun getAllSuppliersList(): List<Supplier> = supplierDao.getAllSuppliersList()
    suspend fun getAllChequesList(): List<Cheque> = chequeDao.getAllChequesList()
    suspend fun getAllExpensesList(): List<Expense> = expenseDao.getAllExpensesList()
    suspend fun getAllBundlesList(): List<Bundle> = bundleDao.getAllBundlesList()
    suspend fun getAllBundleItemsList(): List<BundleItem> = bundleDao.getAllBundleItemsList()

    fun getDebtTransactions(customerId: Long): Flow<List<DebtTransaction>> {
        return debtTransactionDao.getTransactionsForCustomer(customerId)
    }

    fun getSupplierTransactions(supplierId: Long): Flow<List<DebtTransaction>> {
        return debtTransactionDao.getTransactionsForSupplier(supplierId)
    }

    suspend fun getAllTransactionsList(): List<DebtTransaction> {
        return debtTransactionDao.getAllTransactionsList()
    }

    suspend fun insert(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun update(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)
    }

    suspend fun insertBundle(bundle: Bundle, items: List<BundleItem>) {
        val bundleId = bundleDao.insertBundle(bundle)
        bundleDao.insertBundleItems(items.map { it.copy(bundleId = bundleId.toInt()) })
    }

    suspend fun deleteBundle(bundle: Bundle) {
        bundleDao.deleteBundle(bundle)
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun insertSupplier(supplier: Supplier) {
        supplierDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) {
        supplierDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) {
        supplierDao.deleteSupplier(supplier)
    }

    suspend fun insertCheque(cheque: Cheque) {
        chequeDao.insertCheque(cheque)
    }

    suspend fun updateCheque(cheque: Cheque) {
        chequeDao.updateCheque(cheque)
    }

    suspend fun deleteCheque(cheque: Cheque) {
        chequeDao.deleteCheque(cheque)
    }

    suspend fun insertInvoiceRaw(invoice: Invoice) {
        invoiceDao.insertInvoice(invoice)
    }

    suspend fun insertInvoiceItemsRaw(items: List<InvoiceItem>) {
        invoiceDao.insertInvoiceItems(items)
    }

    suspend fun insertTransactionRaw(transaction: DebtTransaction) {
        debtTransactionDao.insert(transaction)
    }

    suspend fun saveInvoice(
        invoice: Invoice, 
        items: List<InvoiceItem>, 
        installments: List<Pair<Double, Long?>>? = null
    ) {
        val id = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = id.toInt()) }
        invoiceDao.insertInvoiceItems(itemsWithId)
        
        // Update stock for each product
        items.forEach { item ->
            if (invoice.type == InvoiceType.SALE) {
                productDao.reduceStock(item.productName, item.quantity)
            } else {
                productDao.restoreStock(item.productName, item.quantity) // Purchase increases stock
            }
        }

        // Update customer debt if applicable (only for sales)
        if (invoice.type == InvoiceType.SALE) {
            invoice.customerId?.let { cId ->
                val totalDebt = invoice.totalAmount - invoice.amountPaid
                if (totalDebt > 0) {
                    customerDao.updateDebt(cId, totalDebt)
                    
                    if (!installments.isNullOrEmpty()) {
                        installments.forEachIndexed { index, pair ->
                            debtTransactionDao.insert(
                                DebtTransaction(
                                    customerId = cId,
                                    invoiceId = id.toInt(),
                                    amount = pair.first,
                                    description = "قسط ${index + 1} فاکتور فروش #${id}",
                                    dueDate = pair.second,
                                    type = TransactionType.DEBT_INCREASE
                                )
                            )
                        }
                    } else {
                        debtTransactionDao.insert(
                            DebtTransaction(
                                customerId = cId,
                                invoiceId = id.toInt(),
                                amount = totalDebt,
                                description = "بابت فاکتور فروش #${id}",
                                dueDate = invoice.dueDate,
                                type = TransactionType.DEBT_INCREASE
                            )
                        )
                    }
                }
            }
        } else if (invoice.type == InvoiceType.PURCHASE) {
            invoice.supplierId?.let { sId ->
                val totalDebt = invoice.totalAmount - invoice.amountPaid
                if (totalDebt > 0) {
                    supplierDao.updateDebt(sId, totalDebt)
                    
                    if (!installments.isNullOrEmpty()) {
                        installments.forEachIndexed { index, pair ->
                            debtTransactionDao.insert(
                                DebtTransaction(
                                    supplierId = sId,
                                    invoiceId = id.toInt(),
                                    amount = pair.first,
                                    description = "قسط ${index + 1} فاکتور خرید #${id}",
                                    dueDate = pair.second,
                                    type = TransactionType.DEBT_INCREASE
                                )
                            )
                        }
                    } else {
                        debtTransactionDao.insert(
                            DebtTransaction(
                                supplierId = sId,
                                invoiceId = id.toInt(),
                                amount = totalDebt,
                                description = "بابت فاکتور خرید #${id}",
                                dueDate = invoice.dueDate,
                                type = TransactionType.DEBT_INCREASE
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        // 1. Revert stock
        invoiceWithItems.items.forEach { item ->
            if (invoiceWithItems.invoice.type == InvoiceType.SALE) {
                productDao.restoreStock(item.productName, item.quantity)
            } else {
                productDao.reduceStock(item.productName, item.quantity)
            }
        }

        // 2. Revert customer debt if applicable
        if (invoiceWithItems.invoice.type == InvoiceType.SALE) {
            invoiceWithItems.invoice.customerId?.let { cId ->
                val debtAmount = invoiceWithItems.invoice.totalAmount - invoiceWithItems.invoice.amountPaid
                if (debtAmount > 0) {
                    customerDao.updateDebt(cId, -debtAmount)
                }
            }
        }

        // 3. Delete transactions
        debtTransactionDao.deleteByInvoiceId(invoiceWithItems.invoice.id)

        // 4. Delete from DB
        invoiceDao.deleteInvoice(invoiceWithItems.invoice.id)
        invoiceDao.deleteInvoiceItems(invoiceWithItems.invoice.id)
    }

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun settleCustomerDebt(customerId: Long, amount: Double, description: String? = null) {
        customerDao.updateDebt(customerId, -amount)
        debtTransactionDao.insert(
            DebtTransaction(
                customerId = customerId,
                amount = -amount,
                description = description ?: "تسویه دستی",
                type = TransactionType.PAYMENT
            )
        )
    }

    suspend fun payInstallment(transaction: DebtTransaction) {
        if (transaction.isPaid) return
        
        val updatedTransaction = transaction.copy(
            isPaid = true,
            paymentTimestamp = System.currentTimeMillis()
        )
        debtTransactionDao.update(updatedTransaction)
        
        // Reduce debt
        if (transaction.customerId != null) {
            customerDao.updateDebt(transaction.customerId, -transaction.amount)
        } else if (transaction.supplierId != null) {
            supplierDao.updateDebt(transaction.supplierId, -transaction.amount)
        }

        // Update Invoice amountPaid
        transaction.invoiceId?.let { invId ->
            val invoiceWithItems = invoiceDao.getInvoiceById(invId)
            val invoice = invoiceWithItems.invoice
            invoiceDao.updateInvoice(invoice.copy(amountPaid = invoice.amountPaid + transaction.amount))
        }
    }

    suspend fun revertInstallmentPayment(transaction: DebtTransaction) {
        if (!transaction.isPaid) return
        
        val updatedTransaction = transaction.copy(
            isPaid = false,
            paymentTimestamp = null
        )
        debtTransactionDao.update(updatedTransaction)
        
        // Increase debt back
        if (transaction.customerId != null) {
            customerDao.updateDebt(transaction.customerId, transaction.amount)
        } else if (transaction.supplierId != null) {
            supplierDao.updateDebt(transaction.supplierId, transaction.amount)
        }

        // Update Invoice amountPaid
        transaction.invoiceId?.let { invId ->
            val invoiceWithItems = invoiceDao.getInvoiceById(invId)
            val invoice = invoiceWithItems.invoice
            invoiceDao.updateInvoice(invoice.copy(amountPaid = (invoice.amountPaid - transaction.amount).coerceAtLeast(0.0)))
        }
    }

    suspend fun deleteInstallment(transaction: DebtTransaction) {
        // If it was paid, we should probably revert the financial effect first
        if (transaction.isPaid) {
            revertInstallmentPayment(transaction)
        } else {
            // If it was a debt increase, deleting it should logically reduce the customer's debt 
            // as the record of that debt is being removed
            if (transaction.type == TransactionType.DEBT_INCREASE) {
                if (transaction.customerId != null) {
                    customerDao.updateDebt(transaction.customerId, -transaction.amount)
                } else if (transaction.supplierId != null) {
                    supplierDao.updateDebt(transaction.supplierId, -transaction.amount)
                }
            }
        }
        debtTransactionDao.delete(transaction)
    }

    suspend fun getLastPriceForCustomer(customerId: Long, productName: String): Double? {
        return invoiceDao.getLastPriceForCustomer(customerId, productName)
    }

    suspend fun getFullBackup(): AppBackup {
        return AppBackup(
            products = productDao.getAllProductsList(),
            customers = customerDao.getAllCustomersList(),
            invoices = invoiceDao.getAllInvoicesRaw(),
            invoiceItems = invoiceDao.getAllInvoiceItemsRaw()
        )
    }

    suspend fun restoreBackup(backup: AppBackup) {
        backup.products.forEach { productDao.insertProduct(it) }
        backup.customers.forEach { customerDao.insertCustomer(it) }
        // For invoices and items, we might need to be careful with IDs
        // But for a true restore, we just insert them as they are
        backup.invoices.forEach { invoiceDao.insertInvoice(it) }
        invoiceDao.insertInvoiceItems(backup.invoiceItems)
    }

    // --- Pending Deletions ---
    suspend fun addPendingDeletion(tableName: String, identifier: String, column: String = "id") {
        pendingDeletionDao.insert(PendingDeletion(tableName = tableName, identifier = identifier, identifierColumn = column))
    }

    suspend fun getPendingDeletions(): List<PendingDeletion> = pendingDeletionDao.getAll()

    suspend fun removePendingDeletion(deletion: PendingDeletion) {
        pendingDeletionDao.delete(deletion)
    }
}
