package com.oqba26.abzarforoush.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.CartItem
import com.oqba26.abzarforoush.data.Customer
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheetContent(
    cartItems: List<CartItem>,
    customers: List<Customer>,
    suppliers: List<com.oqba26.abzarforoush.data.Supplier> = emptyList(),
    initialCustomerId: Long? = null,
    initialSupplierId: Long? = null,
    isPurchaseModeInitial: Boolean = false,
    showTypeToggle: Boolean = true,
    onRemove: (CartItem) -> Unit,
    onUpdatePrice: (CartItem, Double) -> Unit,
    onUpdateQuantity: (CartItem, Double) -> Unit,
    onCustomerSelected: (Long?) -> Unit,
    onSupplierSelected: (Long?) -> Unit = {},
    onCheckout: (Long?, Long?, Double, Double, String?, String?, Long?, List<Pair<Double, Long?>>?, com.oqba26.abzarforoush.data.InvoiceType) -> Unit,
    suggestions: List<com.oqba26.abzarforoush.data.Product> = emptyList(),
    onAddSuggestion: (com.oqba26.abzarforoush.data.Product) -> Unit = {}
) {
    var isPurchaseMode by remember { mutableStateOf(isPurchaseModeInitial) }
    var selectedCustomerId by remember(initialCustomerId) { mutableStateOf(initialCustomerId) }
    var selectedSupplierId by remember(initialSupplierId) { mutableStateOf(initialSupplierId) }
    var manualCustomerName by remember { mutableStateOf("") }
    var manualCustomerPhone by remember { mutableStateOf("") }

    // Sync manual fields if a customer/supplier is selected
    LaunchedEffect(selectedCustomerId, selectedSupplierId, isPurchaseMode) {
        if (!isPurchaseMode) {
            val customer = customers.find { it.id == selectedCustomerId }
            if (customer != null) {
                manualCustomerName = customer.name
                manualCustomerPhone = customer.phoneNumber ?: ""
            }
        } else {
            val supplier = suppliers.find { it.id == selectedSupplierId }
            if (supplier != null) {
                manualCustomerName = supplier.name
                manualCustomerPhone = supplier.phoneNumber ?: ""
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var suggestedContacts by remember { mutableStateOf<List<com.oqba26.abzarforoush.util.ContactInfo>>(emptyList()) }

    // Search contacts if name matches (for new customers)
    LaunchedEffect(manualCustomerName) {
        suggestedContacts = if (manualCustomerName.length > 1 && manualCustomerPhone.isBlank() && !isPurchaseMode) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                com.oqba26.abzarforoush.util.ContactHelper.getContactsByName(context, manualCustomerName)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    var amountPaidText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var installments by remember { mutableStateOf<List<Pair<Double, Long?>>>(emptyList()) }
    
    val totalAmount = cartItems.sumOf { it.totalPrice }
    var totalDiscountText by remember { mutableStateOf("") }
    val finalAmount = totalAmount - (totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0)
    val remainingDebt = finalAmount - (amountPaidText.cleanNumber().toDoubleOrNull() ?: 0.0)
    val isDebt = remainingDebt > 0 && (selectedCustomerId != null || manualCustomerName.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isPurchaseMode) "فاکتور خرید (ورودی انبار)" else "فاکتور فروش جاری",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (showTypeToggle) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                FilterChip(
                    selected = !isPurchaseMode,
                    onClick = { 
                        isPurchaseMode = false
                        onCustomerSelected(selectedCustomerId)
                        onSupplierSelected(null)
                    },
                    label = { Text("فروش به مشتری") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = isPurchaseMode,
                    onClick = { 
                        isPurchaseMode = true
                        onCustomerSelected(null)
                        onSupplierSelected(selectedSupplierId)
                    },
                    label = { Text("خرید از تامین‌کننده") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "فاکتور خالی است.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item, onRemove, onUpdateQuantity, onUpdatePrice)
                }

                if (suggestions.isNotEmpty()) {
                    item {
                        SuggestionsSection(suggestions, onAddSuggestion)
                    }
                }

                item {
                    CustomerInfoSection(
                        isPurchaseMode = isPurchaseMode,
                        manualCustomerName = manualCustomerName,
                        onNameChange = { name, id, phone ->
                            manualCustomerName = name
                            if (!isPurchaseMode) {
                                selectedCustomerId = id
                                manualCustomerPhone = phone ?: ""
                                onCustomerSelected(id)
                            } else {
                                selectedSupplierId = id
                                manualCustomerPhone = phone ?: ""
                                onSupplierSelected(id)
                            }
                        },
                        manualCustomerPhone = manualCustomerPhone,
                        onPhoneChange = { manualCustomerPhone = it },
                        customers = customers,
                        suppliers = suppliers,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        amountPaidText = amountPaidText,
                        onAmountPaidChange = { amountPaidText = it },
                        totalDiscountText = totalDiscountText,
                        onTotalDiscountChange = { totalDiscountText = it },
                        isDebt = isDebt,
                        remainingDebt = remainingDebt,
                        dueDate = dueDate,
                        onDueDateChange = { dueDate = it },
                        installments = installments,
                        onInstallmentsChange = { installments = it },
                        suggestedContacts = suggestedContacts,
                        onContactSelected = { contact ->
                            manualCustomerName = contact.name
                            manualCustomerPhone = contact.phoneNumber
                            suggestedContacts = emptyList()
                        }
                    )
                }
            }
        }

        // Fixed Footer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "قابل پرداخت:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = finalAmount.toPersianPrice(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val discount = totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0
                        val paid = amountPaidText.cleanNumber().toDoubleOrNull() ?: 0.0
                        onCheckout(
                            if (!isPurchaseMode) selectedCustomerId else null,
                            if (isPurchaseMode) selectedSupplierId else null,
                            paid,
                            discount,
                            manualCustomerName,
                            manualCustomerPhone,
                            dueDate,
                            installments,
                            if (isPurchaseMode) com.oqba26.abzarforoush.data.InvoiceType.PURCHASE else com.oqba26.abzarforoush.data.InvoiceType.SALE
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تایید و ثبت نهایی فاکتور", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onRemove: (CartItem) -> Unit,
    onUpdateQuantity: (CartItem, Double) -> Unit,
    onUpdatePrice: (CartItem, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemove(item) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity with label
                Column(modifier = Modifier.weight(1f)) {
                    Text("تعداد (${item.product.unit})", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (item.quantity > 0) onUpdateQuantity(item, item.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, null)
                        }
                        Text(
                            text = item.quantity.toString().toPersianDigits(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { onUpdateQuantity(item, item.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }

                // Price unit
                OutlinedTextField(
                    value = if (item.sellPrice == 0.0) "" else item.sellPrice.toLong().toString(),
                    onValueChange = { input ->
                        val cleaned = input.cleanNumber()
                        cleaned.toDoubleOrNull()?.let { onUpdatePrice(item, it) }
                    },
                    label = { Text("قیمت واحد") },
                    modifier = Modifier.weight(1.2f),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianNumberVisualTransformation()
                )
            }

            Text(
                text = "جمع: ${item.totalPrice.toPersianPrice()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerInfoSection(
    isPurchaseMode: Boolean,
    manualCustomerName: String,
    onNameChange: (String, Long?, String?) -> Unit,
    manualCustomerPhone: String,
    onPhoneChange: (String) -> Unit,
    customers: List<Customer>,
    suppliers: List<com.oqba26.abzarforoush.data.Supplier>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    amountPaidText: String,
    onAmountPaidChange: (String) -> Unit,
    totalDiscountText: String,
    onTotalDiscountChange: (String) -> Unit,
    isDebt: Boolean,
    remainingDebt: Double,
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    installments: List<Pair<Double, Long?>>,
    onInstallmentsChange: (List<Pair<Double, Long?>>) -> Unit,
    suggestedContacts: List<com.oqba26.abzarforoush.util.ContactInfo> = emptyList(),
    onContactSelected: (com.oqba26.abzarforoush.util.ContactInfo) -> Unit = {}
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = if (isPurchaseMode) "اطلاعات تامین‌کننده" else "اطلاعات مشتری",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = manualCustomerName,
                onValueChange = { input ->
                    val matchId: Long?
                    val matchPhone: String?
                    if (!isPurchaseMode) {
                        val match = customers.find { it.name == input }
                        matchId = match?.id
                        matchPhone = match?.phoneNumber
                    } else {
                        val match = suppliers.find { it.name == input }
                        matchId = match?.id
                        matchPhone = match?.phoneNumber
                    }
                    onNameChange(input, matchId, matchPhone)
                    onExpandedChange(true)
                },
                label = { Text(if (isPurchaseMode) "نام تامین‌کننده" else "نام مشتری (جستجوی هوشمند)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
            )

            val filteredItems = if (!isPurchaseMode) {
                customers.filter { it.name.contains(manualCustomerName, ignoreCase = true) }
            } else {
                suppliers.filter { it.name.contains(manualCustomerName, ignoreCase = true) }
            }

            if (filteredItems.isNotEmpty() && expanded) {
                ExposedDropdownMenu(
                    expanded = true,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    filteredItems.forEach { item ->
                        val name = if (item is Customer) item.name else (item as com.oqba26.abzarforoush.data.Supplier).name
                        val phone = if (item is Customer) item.phoneNumber else (item as com.oqba26.abzarforoush.data.Supplier).phoneNumber
                        val id = if (item is Customer) item.id else (item as com.oqba26.abzarforoush.data.Supplier).id

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(name)
                                    phone?.let {
                                        Text(it.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            },
                            onClick = {
                                onNameChange(name, id, phone)
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }

        if (suggestedContacts.isNotEmpty()) {
            Text(
                text = "یافت شده در مخاطبین گوشی:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                items(suggestedContacts.size) { index ->
                    val contact = suggestedContacts[index]
                    androidx.compose.material3.SuggestionChip(
                        onClick = { onContactSelected(contact) },
                        label = {
                            Column {
                                Text(contact.name, style = MaterialTheme.typography.labelSmall)
                                Text(contact.phoneNumber.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Gray)
                            }
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = manualCustomerPhone.toPersianDigits(),
            onValueChange = { onPhoneChange(it.cleanNumber()) },
            label = { Text("شماره تماس") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = amountPaidText,
            onValueChange = { onAmountPaidChange(it.cleanNumber()) },
            label = { Text("مبلغ پرداختی (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PersianNumberVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = totalDiscountText,
            onValueChange = { onTotalDiscountChange(it.cleanNumber()) },
            label = { Text("تخفیف روی کل فاکتور (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PersianNumberVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        if (isDebt) {
            InstallmentSection(remainingDebt, dueDate, onDueDateChange, installments, onInstallmentsChange)
        }
    }
}

@Composable
fun SuggestionsSection(suggestions: List<com.oqba26.abzarforoush.data.Product>, onAddSuggestion: (com.oqba26.abzarforoush.data.Product) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "پیشنهاد خرید:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { product ->
                androidx.compose.material3.SuggestionChip(
                    onClick = { onAddSuggestion(product) },
                    label = { Text(product.name, style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@Composable
fun InstallmentSection(
    remainingDebt: Double,
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    installments: List<Pair<Double, Long?>>,
    onInstallmentsChange: (List<Pair<Double, Long?>>) -> Unit
) {
    var showInstallmentDialog by remember { mutableStateOf(false) }
    var showDatePickerForIndex by remember { mutableStateOf<Int?>(null) } // -1 for main dueDate, >=0 for installments

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مدیریت پرداخت و اقساط", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = { showInstallmentDialog = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("افزودن قسط")
            }
        }

        // Main Due Date (if no installments or as the primary one)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            onClick = { showDatePickerForIndex = -1 }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("موعد نهایی تسویه", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = dueDate?.toPersianDateString() ?: "انتخاب تاریخ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Installments List
        installments.forEachIndexed { index, pair ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قسط ${index + 1}", style = MaterialTheme.typography.labelSmall)
                        Text(pair.first.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تاریخ سررسید", style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = { showDatePickerForIndex = index }) {
                            Text(pair.second?.toPersianDateString() ?: "انتخاب")
                        }
                    }
                    IconButton(onClick = { 
                        val newList = installments.toMutableList()
                        newList.removeAt(index)
                        onInstallmentsChange(newList)
                    }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (showInstallmentDialog) {
            AddInstallmentDialog(
                remainingDebt = remainingDebt - installments.sumOf { it.first },
                onDismiss = { showInstallmentDialog = false },
                onConfirm = { amount, date ->
                    onInstallmentsChange(installments + (amount to date))
                    showInstallmentDialog = false
                }
            )
        }

        showDatePickerForIndex?.let { index ->
            ShamsiDatePicker(
                initialTimestamp = if (index == -1) dueDate else installments.getOrNull(index)?.second,
                onDismiss = { showDatePickerForIndex = null },
                onDateSelected = { timestamp ->
                    if (index == -1) {
                        onDueDateChange(timestamp)
                    } else {
                        val newList = installments.toMutableList()
                        newList[index] = newList[index].first to timestamp
                        onInstallmentsChange(newList)
                    }
                    showDatePickerForIndex = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentDialog(
    remainingDebt: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long?) -> Unit
) {
    var amountText by remember { mutableStateOf(if (remainingDebt > 0) remainingDebt.toLong().toString() else "") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("افزودن قسط جدید", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.cleanNumber() },
                    label = { Text("مبلغ قسط (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianNumberVisualTransformation()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dueDate?.toPersianDateString() ?: "",
                    onValueChange = { },
                    label = { Text("تاریخ سررسید") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val amt = amountText.cleanNumber().toDoubleOrNull() ?: 0.0
                            if (amt > 0) onConfirm(amt, dueDate) 
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("تایید") }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("انصراف") }
                }
            }
        }
    }
    
    if (showDatePicker) {
        ShamsiDatePicker(
            initialTimestamp = dueDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { 
                dueDate = it
                showDatePicker = false
            }
        )
    }
}
