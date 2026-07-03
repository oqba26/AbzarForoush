package com.oqba26.abzarforoush.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.oqba26.abzarforoush.data.Product
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.ui.components.AddProductDialog
import com.oqba26.abzarforoush.ui.components.BarcodeScannerDialog
import com.oqba26.abzarforoush.ui.components.CartSheetContent
import com.oqba26.abzarforoush.ui.components.EditProductDialog
import com.oqba26.abzarforoush.ui.components.ProductItem
import com.oqba26.abzarforoush.ui.components.SearchBar
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianNumber
import com.oqba26.abzarforoush.util.toPersianPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    externalShowCart: Boolean = false,
    onCloseCart: () -> Unit = {},
    onImportCsv: () -> Unit,
    onImportExcel: () -> Unit,
    onExportExcel: () -> Unit,
    onImportBackup: () -> Unit,
    onExportBackup: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allPossibleCategories = remember(categories) {
        (categories + com.oqba26.abzarforoush.util.CategorizationHelper.defaultCategories).distinct()
    }
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val bundles by viewModel.allBundles.collectAsState()
    val suggestions by viewModel.crossSellSuggestions.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerIdForCart.collectAsState()
    val selectedSupplierId by viewModel.selectedSupplierIdForCart.collectAsState()
    val isPurchaseMode by viewModel.isPurchaseMode.collectAsState()
    
    val context = LocalContext.current
    val settingsManager = remember { com.oqba26.abzarforoush.data.SettingsManager(context) }
    val shopName by settingsManager.shopName.collectAsState(initial = "")
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    var showDialog by remember { mutableStateOf(value = false) }
    var showScanner by remember { mutableStateOf(value = false) }
    var showCart by remember(externalShowCart) { mutableStateOf(externalShowCart) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var scanTarget by remember { mutableStateOf<String?>(null) } // "search" or "add"
    var lastSavedInvoice by remember { mutableStateOf<String?>(null) }
    var showFileMenu by remember { mutableStateOf(false) }
    var showShoppingList by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        }
    }

    if (productToDelete != null) {
        Dialog(onDismissRequest = { productToDelete = null }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "حذف محصول",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "آیا از حذف محصول «${productToDelete?.name}» اطمینان دارید؟",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    productToDelete?.let { viewModel.deleteProduct(it) }
                                    productToDelete = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("تایید", color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Button(
                                onClick = { productToDelete = null },
                                modifier = Modifier.weight(1f),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("انصراف", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }
    }

    if (lastSavedInvoice != null) {
        Dialog(onDismissRequest = { lastSavedInvoice = null }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "فاکتور ثبت شد",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "فاکتور با موفقیت در سیستم ذخیره شد. آیا می‌خواهید آن را به اشتراک بگذارید؟",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, lastSavedInvoice)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                    lastSavedInvoice = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("اشتراک‌گذاری فاکتور")
                            }
                            
                            Button(
                                onClick = { lastSavedInvoice = null },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("فعلاً نه (فقط ذخیره)")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCart) {
        ModalBottomSheet(
            onDismissRequest = { 
                showCart = false
                onCloseCart()
            },
            sheetState = sheetState
        ) {
            CartSheetContent(
                cartItems = cartItems,
                customers = customers,
                suppliers = suppliers,
                initialCustomerId = selectedCustomerId,
                initialSupplierId = selectedSupplierId,
                isPurchaseModeInitial = isPurchaseMode,
                onRemove = { viewModel.removeFromCart(it) },
                onUpdatePrice = { item, price -> viewModel.updateCartItemPrice(item, price) },
                onUpdateQuantity = { item, qty -> viewModel.updateCartItemQuantity(item, qty) },
                onCustomerSelected = { viewModel.selectCustomerForCart(it) },
                onSupplierSelected = { viewModel.selectSupplierForCart(it) },
                suggestions = suggestions,
                onAddSuggestion = { viewModel.addToCart(it) },
                onCheckout = { customerId, supplierId, amountPaid, discount, customerName, customerPhone, dueDate, invoiceType ->
                    val invoiceText = StringBuilder()
                    invoiceText.append(if (invoiceType == com.oqba26.abzarforoush.data.InvoiceType.PURCHASE) "📋 فاکتور خرید ابزارفروشی\n" else "📋 فاکتور فروش ابزارفروشی\n")
                    invoiceText.append("---------------------------\n")
                    if (!customerName.isNullOrBlank()) {
                        invoiceText.append(if (invoiceType == com.oqba26.abzarforoush.data.InvoiceType.PURCHASE) "👤 تامین‌کننده: $customerName\n" else "👤 مشتری: $customerName\n")
                        if (!customerPhone.isNullOrBlank()) {
                            invoiceText.append("📞 تلفن: ${customerPhone.toPersianDigits()}\n")
                        }
                        invoiceText.append("---------------------------\n")
                    }
                    cartItems.forEach { item ->
                        invoiceText.append("${item.product.name}\n")
                        invoiceText.append("${item.quantity.toPersianNumber()} ${item.product.unit} × ${item.sellPrice.toPersianPrice()} = ${item.totalPrice.toPersianPrice()}\n")
                        if (item.discount > 0) {
                            invoiceText.append("تخفیف کالا: ${item.discount.toPersianPrice()}\n")
                        }
                        invoiceText.append("---------------------------\n")
                    }
                    val totalBeforeDiscount = cartItems.sumOf { it.totalPrice }
                    val finalTotal = totalBeforeDiscount - discount
                    
                    if (discount > 0) {
                        invoiceText.append("📉 تخفیف کلی: ${discount.toPersianPrice()}\n")
                    }
                    invoiceText.append("💰 جمع کل: ${finalTotal.toPersianPrice()}\n")
                    
                    if (customerId != null || supplierId != null || !customerName.isNullOrBlank()) {
                        invoiceText.append("💵 پرداختی: ${amountPaid.toPersianPrice()}\n")
                        val remaining = finalTotal - amountPaid
                        if (remaining > 0) {
                            invoiceText.append("📉 مانده بدهی: ${remaining.toPersianPrice()}\n")
                            dueDate?.let {
                                invoiceText.append("📅 سررسید: ${it.toPersianDateString()}\n")
                            }
                        } else if (remaining < 0) {
                            invoiceText.append("⤴️ طلب: ${(-remaining).toPersianPrice()}\n")
                        }
                    }
                    invoiceText.append("---------------------------\n")
                    invoiceText.append(if (invoiceType == com.oqba26.abzarforoush.data.InvoiceType.PURCHASE) "ثبت در ورودی انبار" else "🙏 ممنون از خرید شما")

                    lastSavedInvoice = invoiceText.toString()
                    viewModel.checkout(
                        customerId = customerId, 
                        supplierId = supplierId,
                        amountPaid = amountPaid, 
                        totalDiscount = discount, 
                        dueDate = dueDate,
                        type = invoiceType
                    )
                    showCart = false
                }
            )
        }
    }

    if (showShoppingList) {
        val lowStockProducts = products.filter { it.minStock > 0 && it.stock <= it.minStock }
        Dialog(onDismissRequest = { showShoppingList = false }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "لیست خرید پیشنهاد شده",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (lowStockProducts.isEmpty()) {
                            Text("خوشبختانه موجودی تمام کالاها کافی است.")
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                items(lowStockProducts) { p ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(p.name, modifier = Modifier.weight(1f))
                                        Text("موجودی: ${p.stock.toPersianNumber()} ${p.unit}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val listText = StringBuilder("📋 لیست خرید فروشگاه:\n\n")
                                    lowStockProducts.forEach { p ->
                                        listText.append("- ${p.name} (موجودی فعلی: ${p.stock.toPersianNumber()} ${p.unit})\n")
                                    }
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, listText.toString())
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("ارسال لیست برای تامین‌کننده")
                            }
                        }
                        TextButton(
                            onClick = { showShoppingList = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("بستن")
                        }
                    }
                }
            }
        }
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false }
        ) { barcode ->
            scope.launch {
                if (scanTarget == "search") {
                    val existingProduct = viewModel.getProductByBarcode(barcode)
                    if (existingProduct != null) {
                        productToEdit = existingProduct
                    } else {
                        viewModel.onSearchQueryChange(barcode)
                    }
                }
                showScanner = false
            }
        }
    }

    if (showDialog) {
        AddProductDialog(
            categories = allPossibleCategories,
            onDismiss = { showDialog = false }
        ) { name, price, wholesale, partner, purchasePrice, stock, minStock, category, unit, barcode ->
            viewModel.addProduct(name, price, wholesale, partner, purchasePrice, stock, minStock, category, unit, barcode)
            showDialog = false
        }
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            categories = allPossibleCategories,
            onDismiss = { productToEdit = null },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (shopName.isNotBlank()) "ابزار فروشی $shopName" else "ابزار فروشی",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // دکمه فایل‌ها
                    Box {
                        Surface(
                            onClick = { showFileMenu = true },
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImportExport,
                                    contentDescription = "فایل‌ها",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "فایل‌ها",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showFileMenu,
                            onDismissRequest = { showFileMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("ورود از CSV") }, onClick = { onImportCsv(); showFileMenu = false })
                            DropdownMenuItem(text = { Text("ورود از اکسل") }, onClick = { onImportExcel(); showFileMenu = false })
                            DropdownMenuItem(text = { Text("خروجی اکسل") }, onClick = { onExportExcel(); showFileMenu = false })
                            DropdownMenuItem(text = { Text("ورود بک‌آپ") }, onClick = { onImportBackup(); showFileMenu = false })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("لیست خرید (کمبود موجودی)") }, onClick = { showShoppingList = true; showFileMenu = false })
                            DropdownMenuItem(text = { Text("خروجی بک‌آپ") }, onClick = { onExportBackup(); showFileMenu = false })
                        }
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    // دکمه افزودن محصول
                    Surface(
                        onClick = { showDialog = true },
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Product",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "افزودن محصول",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.syncWithSupabase {
                    isRefreshing = false
                }
            },
            state = pullRefreshState,
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onScanClick = {
                        scanTarget = "search"
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            showScanner = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                // Category Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategoryChange(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                if (products.isEmpty()) {
                    Text(
                        text = "هنوز محصولی ثبت نشده است.",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    val selectedCustomer = customers.find { it.id == selectedCustomerId }
                    if (selectedCustomer != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "فروش برای: ${selectedCustomer.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { viewModel.selectCustomerForCart(null) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("لغو فروش", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (selectedCategory == "پکیج‌ها") {
                            items(bundles) { bundleWithProducts ->
                                com.oqba26.abzarforoush.ui.components.BundleItemCard(
                                    bundleWithProducts = bundleWithProducts,
                                    isSaleMode = selectedCustomerId != null,
                                    onAddToCart = { viewModel.addBundleToCart(bundleWithProducts) },
                                    onDelete = { viewModel.deleteBundle(bundleWithProducts.bundle) }
                                )
                            }
                        } else {
                            items(products) { product ->
                                ProductItem(
                                    product = product,
                                    isInCart = cartItems.any { it.product.id == product.id },
                                    isSaleMode = selectedCustomerId != null,
                                    onDelete = { productToDelete = product },
                                    onEdit = { productToEdit = product },
                                    onAddToCart = { viewModel.addToCart(product) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
