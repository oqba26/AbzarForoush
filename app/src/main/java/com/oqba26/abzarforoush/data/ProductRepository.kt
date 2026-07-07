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
    private val chequeDao: ChequeDao
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

    fun getDebtTransactions(customerId: Int): Flow<List<DebtTransaction>> {
        return debtTransactionDao.getTransactionsForCustomer(customerId)
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

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
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
                val debtAmount = invoice.totalAmount - invoice.amountPaid
                if (debtAmount > 0) {
                    customerDao.updateDebt(cId, debtAmount)
                    debtTransactionDao.insert(
                        DebtTransaction(
                            customerId = cId,
                            invoiceId = id.toInt(),
                            amount = debtAmount,
                            description = "بابت فاکتور فروش #${id}",
                            dueDate = invoice.dueDate,
                            type = TransactionType.DEBT_INCREASE
                        )
                    )
                }
            }
        } else if (invoice.type == InvoiceType.PURCHASE) {
            invoice.supplierId?.let { sId ->
                val debtAmount = invoice.totalAmount - invoice.amountPaid
                if (debtAmount > 0) {
                    supplierDao.updateDebt(sId, debtAmount)
                    debtTransactionDao.insert(
                        DebtTransaction(
                            supplierId = sId,
                            invoiceId = id.toInt(),
                            amount = debtAmount,
                            description = "بابت فاکتور خرید #${id}",
                            dueDate = invoice.dueDate,
                            type = TransactionType.DEBT_INCREASE
                        )
                    )
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

    suspend fun settleCustomerDebt(customerId: Int, amount: Double, description: String? = null) {
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

    suspend fun getLastPriceForCustomer(customerId: Int, productName: String): Double? {
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
}
