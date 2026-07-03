package com.oqba26.abzarforoush.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.chitralabs.sheetz.Sheetz
import com.oqba26.abzarforoush.util.InvoicePdfHelper
import com.oqba26.abzarforoush.util.SupabaseManager
import com.oqba26.abzarforoush.util.toPersianNumber
import com.oqba26.abzarforoush.util.toPersianPrice
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("همه")
    val selectedCategory = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allProducts: StateFlow<List<Product>> = combine(
        _searchQuery,
        _selectedCategory,
        repository.allProducts,
    ) { query, category, products ->
        val priceFilter = extractPriceFilter(query)
        val cleanQuery = query.replace(Regex("(زیر|بالای|بیش از|کمتر از)\\s*\\d+"), "").trim()
        
        products.filter { product ->
            val matchesQuery = product.name.contains(cleanQuery, ignoreCase = true) || 
                             (product.barcode?.contains(cleanQuery) ?: false) ||
                             product.category.contains(cleanQuery, ignoreCase = true)
            
            val matchesCategory = (category == "همه") || (product.category == category)
            
            val matchesPrice = when (priceFilter) {
                is PriceFilter.Under -> product.price <= priceFilter.amount
                is PriceFilter.Above -> product.price >= priceFilter.amount
                null -> true
            }
            
            matchesQuery && matchesCategory && matchesPrice
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    sealed class PriceFilter {
        data class Under(val amount: Double) : PriceFilter()
        data class Above(val amount: Double) : PriceFilter()
    }

    private fun extractPriceFilter(query: String): PriceFilter? {
        val underRegex = Regex("(زیر|کمتر از)\\s*(\\d+)")
        val aboveRegex = Regex("(بالای|بیش از)\\s*(\\d+)")
        
        underRegex.find(query)?.let { 
            it.groupValues[2].toDoubleOrNull()?.let { amount ->
                // Handle "k" or "toman" logic if needed, but for now simple digits
                val actualAmount = if (amount < 1000) amount * 1000 else amount
                return PriceFilter.Under(actualAmount)
            }
        }
        
        aboveRegex.find(query)?.let { 
            it.groupValues[2].toDoubleOrNull()?.let { amount ->
                val actualAmount = if (amount < 1000) amount * 1000 else amount
                return PriceFilter.Above(actualAmount)
            }
        }
        
        return null
    }

    val categories: StateFlow<List<String>> = repository.allProducts
        .map { products: List<Product> -> 
            (listOf("همه", "پکیج‌ها") + products.asSequence().map { it.category }.distinct().filter { it != "بدون دسته بندی" }.toList() + "بدون دسته بندی").distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("همه")
        )

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    val allInvoices: StateFlow<List<InvoiceWithItems>> = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allInvoiceItems: StateFlow<List<InvoiceItem>> = repository.allInvoiceItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allBundles: StateFlow<List<BundleWithProducts>> = repository.allBundles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allCheques: StateFlow<List<Cheque>> = repository.allCheques.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun addExpense(amount: Double, category: ExpenseCategory, description: String?) {
        viewModelScope.launch {
            repository.insertExpense(Expense(amount = amount, category = category, description = description))
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addSupplier(name: String, phone: String? = null, address: String? = null) {
        viewModelScope.launch {
            repository.insertSupplier(Supplier(name = name, phoneNumber = phone, address = address))
        }
    }

    fun updateSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    fun addCheque(chequeNumber: String, bankName: String, amount: Double, dueDate: Long, personName: String, type: ChequeType) {
        viewModelScope.launch {
            repository.insertCheque(Cheque(chequeNumber = chequeNumber, bankName = bankName, amount = amount, dueDate = dueDate, personName = personName, type = type))
        }
    }

    fun updateCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.updateCheque(cheque)
        }
    }

    fun deleteCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.deleteCheque(cheque)
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _selectedCustomerIdForCart = MutableStateFlow<Int?>(null)
    val selectedCustomerIdForCart = _selectedCustomerIdForCart.asStateFlow()

    private val _selectedSupplierIdForCart = MutableStateFlow<Int?>(null)
    val selectedSupplierIdForCart = _selectedSupplierIdForCart.asStateFlow()

    private val _isPurchaseMode = MutableStateFlow(false)
    val isPurchaseMode = _isPurchaseMode.asStateFlow()

    fun selectCustomerForCart(customerId: Int?) {
        _selectedCustomerIdForCart.value = customerId
        if (customerId != null) _isPurchaseMode.value = false
    }

    fun selectSupplierForCart(supplierId: Int?) {
        _selectedSupplierIdForCart.value = supplierId
        if (supplierId != null) _isPurchaseMode.value = true
    }

    fun setPurchaseMode(enabled: Boolean) {
        _isPurchaseMode.value = enabled
        if (enabled) _selectedCustomerIdForCart.value = null
        else _selectedSupplierIdForCart.value = null
    }

    fun addToCart(product: Product) {
        val customerId = _selectedCustomerIdForCart.value
        viewModelScope.launch {
            val historicalPrice = customerId?.let {
                repository.getLastPriceForCustomer(it, product.name)
            }
            
            val sellPrice = historicalPrice ?: product.price
            
            val currentList = _cartItems.value.toMutableList()
            val existingItem = currentList.find { it.product.id == product.id }
            
            if (existingItem != null) {
                val index = currentList.indexOf(existingItem)
                currentList[index] = existingItem.copy(quantity = existingItem.quantity + 1.0)
            } else {
                currentList.add(CartItem(product, 1.0, sellPrice = sellPrice))
            }
            _cartItems.value = currentList
        }
    }

    fun addBundleToCart(bundleWithProducts: BundleWithProducts) {
        val customerId = _selectedCustomerIdForCart.value
        viewModelScope.launch {
            val currentList = _cartItems.value.toMutableList()
            bundleWithProducts.bundleItems.forEach { bundleItem ->
                val product = bundleWithProducts.products.find { it.id == bundleItem.productId }
                if (product != null) {
                    val historicalPrice = customerId?.let {
                        repository.getLastPriceForCustomer(it, product.name)
                    }
                    val basePrice = historicalPrice ?: product.price
                    // Apply bundle discount proportionally or just add as is? 
                    // Let's assume bundle discount is applied to the total later or we can reduce price here.
                    // For simplicity, let's just add items at their base price.
                    val sellPrice = basePrice
                    
                    val existingItem = currentList.find { it.product.id == product.id }
                    if (existingItem != null) {
                        val index = currentList.indexOf(existingItem)
                        currentList[index] = existingItem.copy(quantity = existingItem.quantity + bundleItem.quantity)
                    } else {
                        currentList.add(CartItem(product, bundleItem.quantity, sellPrice = sellPrice))
                    }
                }
            }
            _cartItems.value = currentList
        }
    }

    fun updateCartItemPrice(cartItem: CartItem, newPrice: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOf(cartItem)
        if (index != -1) {
            currentList[index] = cartItem.copy(sellPrice = newPrice)
            _cartItems.value = currentList
        }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOf(cartItem)
        if (index != -1) {
            currentList[index] = cartItem.copy(quantity = newQuantity)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        currentList.remove(cartItem)
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomerIdForCart.value = null
    }

    fun checkout(
        customerId: Int? = null,
        supplierId: Int? = null,
        amountPaid: Double = 0.0, 
        totalDiscount: Double = 0.0, 
        dueDate: Long? = null,
        type: InvoiceType = InvoiceType.SALE
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val total = items.sumOf { it.totalPrice }
            val invoice = Invoice(
                totalAmount = total,
                totalDiscount = totalDiscount,
                customerId = customerId,
                supplierId = supplierId,
                amountPaid = amountPaid,
                dueDate = dueDate,
                type = type
            )
            val invoiceItems = items.map {
                InvoiceItem(
                    invoiceId = 0,
                    productName = it.product.name,
                    quantity = it.quantity,
                    unit = it.product.unit,
                    priceAtSale = it.sellPrice,
                    purchasePriceAtSale = if (type == InvoiceType.PURCHASE) it.sellPrice else it.product.purchasePrice,
                    discount = it.discount
                )
            }
            repository.saveInvoice(invoice, invoiceItems)
            clearCart()
        }
    }

    fun addProduct(name: String, price: Double, wholesalePrice: Double, partnerPrice: Double, purchasePrice: Double, stock: Double, minStock: Double = 0.0, category: String = "بدون دسته بندی", unit: String, barcode: String? = null) {
        viewModelScope.launch {
            repository.insert(Product(name = name, price = price, wholesalePrice = wholesalePrice, partnerPrice = partnerPrice, purchasePrice = purchasePrice, stock = stock, minStock = minStock, category = category, unit = unit, barcode = barcode))
            launch { silentSync() }
        }
    }

    fun deleteBundle(bundle: Bundle) {
        viewModelScope.launch {
            repository.deleteBundle(bundle)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product)
            launch { silentSync() }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
            // Background attempt to delete from Supabase
            launch {
                try {
                    SupabaseManager.getClient()?.postgrest?.from("products")?.delete {
                        filter { Product::id eq product.id }
                    }
                } catch (_: Exception) {
                    Log.d("Sync", "Silent delete failed, skipping")
                }
            }
        }
    }

    fun addCustomer(name: String, phoneNumber: String? = null) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phoneNumber = phoneNumber))
            launch { silentSync() }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            launch { silentSync() }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            launch { silentSync() }
        }
    }

    fun settleCustomerDebt(customerId: Int, amount: Double, description: String? = null) {
        viewModelScope.launch {
            repository.settleCustomerDebt(customerId, amount, description)
            launch { silentSync() }
        }
    }

    private suspend fun silentSync() {
        val client = SupabaseManager.getClient() ?: return
        try {
            // Push all data to Supabase (upsert handles updates)
            val products = repository.allProducts.first()
            client.postgrest["products"].upsert(products)
            
            val customers = repository.allCustomers.first()
            client.postgrest["customers"].upsert(customers)
            
            val invoices = repository.allInvoices.first().map { it.invoice }
            client.postgrest["invoices"].upsert(invoices)
            
            val invoiceItems = repository.allInvoiceItems.first()
            client.postgrest["invoice_items"].upsert(invoiceItems)
            
            val transactions = repository.getAllTransactionsList()
            client.postgrest["debt_transactions"].upsert(transactions)
        } catch (_: Exception) {
            Log.d("Sync", "Silent sync skipped: No internet")
        }
    }

    fun getDebtTransactions(customerId: Int): Flow<List<DebtTransaction>> {
        return repository.getDebtTransactions(customerId)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return repository.getProductByBarcode(barcode)
    }

    fun importFromCsv(csvUri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(csvUri)?.use { inputStream ->
                    val rows = com.github.doyaaaaaken.kotlincsv.dsl.csvReader().readAllWithHeader(inputStream)
                    rows.forEach { row ->
                        val name = row["name"] ?: ""
                        val price = row["price"]?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val stock = row["stock"]?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val minStock = row["min_stock"]?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val category = row["category"] ?: "بدون دسته بندی"
                        val unit = row["unit"] ?: "عدد"
                        val barcode = row["barcode"]

                        if (name.isNotBlank()) {
                            repository.insert(Product(name = name, price = price, stock = stock, minStock = minStock, category = category, unit = unit, barcode = barcode))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ImportCSV", "Failed to import", e)
            }
        }
    }

    fun exportFullBackup(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val backup = repository.getFullBackup()
                val jsonString = Json.encodeToString(backup)
                
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, jsonString)
                    type = "application/json"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, "نسخه پشتیبان کامل برنامه")
                shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                Log.e("ExportBackup", "Failed to export", e)
            }
        }
    }

    fun importFullBackup(jsonUri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(jsonUri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val backup = Json.decodeFromString<AppBackup>(jsonString)
                    repository.restoreBackup(backup)
                }
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to import", e)
            }
        }
    }

    fun shareInvoiceAsPdf(context: android.content.Context, invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch {
            val settings = SettingsManager(context)
            val name = settings.shopName.first()
            val phone = settings.shopPhone.first()
            val address = settings.shopAddress.first()
            val taxId = settings.shopTaxId.first()
            
            InvoicePdfHelper.generateAndShareInvoice(
                context, 
                invoiceWithItems,
                shopName = name,
                shopPhone = phone,
                shopAddress = address,
                shopTaxId = taxId
            )
        }
    }

    // --- On-Device AI Insights (Offline Logic) ---
    val aiInsights: StateFlow<List<String>> = combine(
        allInvoices,
        allInvoiceItems
    ) { invoices, items ->
        val insights = mutableListOf<String>()
        if (invoices.isEmpty()) return@combine listOf("هنوز فاکتوری برای تحلیل ثبت نشده است.")

        val now = System.currentTimeMillis()
        
        // 1. Seasonal Insight (Summer logic for Tir/July)
        val summerKeywords = listOf("کولر", "پمپ", "آب", "شیلنگ", "اتصالات", "فن", "پنکه", "قیر", "ایزوگام")
        val summerSales = items.filter { item -> 
            summerKeywords.any { it in item.productName } 
        }.sumOf { it.quantity }
        
        if (summerSales > 0) {
            insights.add("☀️ با توجه به فصل گرما، فروش ملزومات آبی و سرمایشی ${summerSales.toInt().toPersianNumber()} مورد بوده؛ پیشنهاد می‌شود موجودی این اقلام را شارژ نگه دارید.")
        }

        // 2. Growth Analysis (This week vs Last week)
        val oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000)
        val twoWeeksAgo = now - (14L * 24 * 60 * 60 * 1000)
        
        val thisWeekSales = invoices.filter { it.invoice.timestamp in oneWeekAgo..now }.sumOf { it.invoice.totalAmount }
        val lastWeekSales = invoices.filter { it.invoice.timestamp in twoWeeksAgo..oneWeekAgo }.sumOf { it.invoice.totalAmount }
        
        if (thisWeekSales > lastWeekSales && lastWeekSales > 0) {
            val growth = ((thisWeekSales - lastWeekSales) / lastWeekSales * 100).toInt()
            insights.add("📈 روند فروش شما نسبت به هفته قبل ${growth.toPersianNumber()}% رشد داشته است. عالیه!")
        }

        // 3. Future Income Estimation (Quantified)
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val lastMonthSales = invoices.filter { it.invoice.timestamp > thirtyDaysAgo }.sumOf { it.invoice.totalAmount }
        if (lastMonthSales > 0) {
            val dailyAvg = lastMonthSales / 30
            val predictedMonth = dailyAvg * 30
            insights.add("🔮 پیش‌بینی درآمد ۳۰ روز آینده: حدود ${predictedMonth.toPersianPrice()} (بر اساس میانگین فروش روزانه اخیر).")
        }

        // 4. Top Performer Insight
        val topProduct = items.groupBy { it.productName }
            .maxByOrNull { it.value.sumOf { i -> i.quantity } }?.key
        topProduct?.let {
            insights.add("🏆 کالای «$it» پرچم‌دار فروش شماست؛ شاید بد نباشد روی خرید عمده‌تر آن با قیمت کمتر مذاکره کنید.")
        }

        // 4. Low Stock Warning in Insights
        val lowStockCount = repository.allProducts.first().count { it.minStock > 0 && it.stock <= it.minStock }
        if (lowStockCount > 0) {
            insights.add("⚠️ تعداد ${lowStockCount.toPersianNumber()} کالا در وضعیت کمبود موجودی هستند. لیست خرید را چک کنید.")
        }

        // 5. Overdue Debts
        val overdueCount = invoices.count { 
            val dueDate = it.invoice.dueDate
            dueDate != null && dueDate < now && (it.invoice.totalAmount > it.invoice.amountPaid)
        }
        if (overdueCount > 0) {
            insights.add("🔔 تعداد ${overdueCount.toPersianNumber()} فاکتور نسیه از تاریخ سررسید گذشته‌اند! لطفا بخش تسویه حساب را بررسی کنید.")
        }

        // 6. Stock-out Prediction
        val products = repository.allProducts.first()
        val thirtyDaysAgoForStock = now - (30L * 24 * 60 * 60 * 1000)
        products.filter { it.stock > 0 }.forEach { product ->
            val salesInLastMonth = items.filter { 
                it.productName == product.name && (invoices.find { inv -> inv.invoice.id == it.invoiceId }?.invoice?.timestamp ?: 0) > thirtyDaysAgoForStock 
            }.sumOf { it.quantity }
            if (salesInLastMonth > 0) {
                val dailyRate = salesInLastMonth / 30
                val daysLeft = (product.stock / dailyRate).toInt()
                if (daysLeft in 1..7) {
                    insights.add("📉 کالای «${product.name}» با سرعت فعلی فروش، تا ${daysLeft.toPersianNumber()} روز دیگر تمام می‌شود.")
                }
            }
        }

        insights
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("در حال تحلیل داده‌ها...")
    )

    val crossSellSuggestions: StateFlow<List<Product>> = combine(
        cartItems,
        allInvoices,
        repository.allProducts
    ) { currentCart, invoices, allProducts ->
        if (currentCart.isEmpty()) return@combine emptyList()
        
        val cartProductNames = currentCart.map { it.product.name }.toSet()
        
        // Find products often bought with products in cart
        val associatedProductNames = invoices.flatMap { invoiceWithItems ->
            val hasCartProduct = invoiceWithItems.items.any { it.productName in cartProductNames }
            if (hasCartProduct) {
                invoiceWithItems.items.map { it.productName }
            } else emptyList()
        }
        .filter { it !in cartProductNames }
        .groupBy { it }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)
        .map { it.first }

        allProducts.filter { it.name in associatedProductNames }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun listenToRealtimeChanges() {
        viewModelScope.launch {
            val client = SupabaseManager.getClient() ?: return@launch
            val channel = client.channel("db-changes")
            
            // Separate flows for each table
            launch {
                channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "products" }.collect { 
                    repository.insert(it.decodeRecord<Product>()) 
                }
            }
            launch {
                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "products" }.collect { 
                    repository.update(it.decodeRecord<Product>()) 
                }
            }
            launch {
                channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "customers" }.collect { 
                    repository.insertCustomer(it.decodeRecord<Customer>()) 
                }
            }
            launch {
                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "customers" }.collect { 
                    repository.updateCustomer(it.decodeRecord<Customer>()) 
                }
            }

            channel.subscribe()
        }
    }

    init {
        listenToRealtimeChanges()
    }

    fun syncWithSupabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val client = SupabaseManager.getClient() ?: return@launch
            try {
                // 1. Pull latest data from Supabase to local DB
                val remoteProducts = client.postgrest["products"].select().decodeList<Product>()
                remoteProducts.forEach { repository.insert(it) }

                val remoteCustomers = client.postgrest["customers"].select().decodeList<Customer>()
                remoteCustomers.forEach { repository.insertCustomer(it) }

                // 2. Push local data to Supabase (upsert)
                val products = repository.allProducts.first()
                client.postgrest["products"].upsert(products)

                val customers = repository.allCustomers.first()
                client.postgrest["customers"].upsert(customers)

                val invoices = repository.allInvoices.first().map { it.invoice }
                client.postgrest["invoices"].upsert(invoices)

                val invoiceItems = repository.allInvoiceItems.first()
                client.postgrest["invoice_items"].upsert(invoiceItems)

                val transactions = repository.getAllTransactionsList()
                client.postgrest["debt_transactions"].upsert(transactions)
                
                Log.d("Sync", "Full sync completed")
            } catch (e: Exception) {
                Log.e("Sync", "Manual sync failed", e)
            } finally {
                onComplete()
            }
        }
    }

    fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch {
            repository.deleteInvoice(invoiceWithItems)
        }
    }

    fun exportToExcel(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val products = repository.allProducts.first()
                val tempFile = java.io.File(context.cacheDir, "Inventory_Backup.xlsx")
                
                // Sheetz.write makes it incredibly easy in 2026!
                Sheetz.write(products, tempFile.absolutePath)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context, 
                    "${context.packageName}.fileprovider", 
                    tempFile
                )
                
                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val shareIntent = android.content.Intent.createChooser(sendIntent, "خروجی اکسل انبار")
                shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                Log.e("ExportExcel", "Failed to export", e)
            }
        }
    }

    fun importFromExcel(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Copy the stream to a temp file because Sheetz.read expects a file path or Path object
                val tempFile = java.io.File(context.cacheDir, "temp_import.xlsx")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                if (tempFile.exists()) {
                    val products: List<Product> = Sheetz.read(tempFile.absolutePath, Product::class.java)
                    products.forEach { product ->
                        if (product.name.isNotBlank()) {
                            repository.insert(product.copy(id = 0))
                        }
                    }
                    tempFile.delete() // Clean up
                }
            } catch (e: Exception) {
                Log.e("ImportExcel", "Failed to import", e)
            }
        }
    }
}
