package com.oqba26.abzarforoush.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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
import com.oqba26.abzarforoush.data.CartItem
import com.oqba26.abzarforoush.data.Customer
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheetContent(
    cartItems: List<CartItem>,
    customers: List<Customer>,
    suppliers: List<com.oqba26.abzarforoush.data.Supplier> = emptyList(),
    initialCustomerId: Int? = null,
    initialSupplierId: Int? = null,
    isPurchaseModeInitial: Boolean = false,
    onRemove: (CartItem) -> Unit,
    onUpdatePrice: (CartItem, Double) -> Unit,
    onUpdateQuantity: (CartItem, Double) -> Unit,
    onCustomerSelected: (Int?) -> Unit,
    onSupplierSelected: (Int?) -> Unit = {},
    onCheckout: (Int?, Int?, Double, Double, String?, String?, Long?, com.oqba26.abzarforoush.data.InvoiceType) -> Unit,
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
    var amountPaidText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    
    val totalAmount = cartItems.sumOf { it.totalPrice }
    var totalDiscountText by remember { mutableStateOf("") }
    val finalAmount = totalAmount - (totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0)
    val isDebt = (amountPaidText.cleanNumber().toDoubleOrNull() ?: 0.0) < finalAmount && (selectedCustomerId != null || manualCustomerName.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isPurchaseMode) "فاکتور خرید (ورودی انبار)" else "فاکتور فروش جاری",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            FilterChip(
                selected = !isPurchaseMode,
                onClick = { isPurchaseMode = false },
                label = { Text("فروش به مشتری") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = isPurchaseMode,
                onClick = { isPurchaseMode = true },
                label = { Text("خرید از تامین‌کننده") },
                modifier = Modifier.weight(1f)
            )
        }

        if (cartItems.isEmpty()) {
            Text(text = "فاکتور خالی است.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(cartItems) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1.5f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(onClick = { if (item.quantity > 1) onUpdateQuantity(item, item.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                OutlinedTextField(
                                    value = item.quantity.toString(),
                                    onValueChange = { input ->
                                        val cleaned = input.cleanNumber()
                                        cleaned.toDoubleOrNull()?.let { onUpdateQuantity(item, it) }
                                    },
                                    placeholder = { Text(item.product.unit, style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = PersianNumberVisualTransformation(),
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                    singleLine = true
                                )
                                IconButton(onClick = { onUpdateQuantity(item, item.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }

                            OutlinedTextField(
                                value = if (item.sellPrice == 0.0) "" else item.sellPrice.toLong().toString(),
                                onValueChange = { input ->
                                    val cleaned = input.cleanNumber()
                                    cleaned.toDoubleOrNull()?.let { onUpdatePrice(item, it) }
                                },
                                label = { Text("قیمت واحد (تومان)", maxLines = 1) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(2f),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true
                            )
                        }
                        
                        Text(
                            text = "جمع: ${item.totalPrice.toPersianPrice()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    HorizontalDivider()
                }

                if (suggestions.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "پیشنهاد خرید (بر اساس تجربه قبلی):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(suggestions) { product ->
                                    androidx.compose.material3.SuggestionChip(
                                        onClick = { onAddSuggestion(product) },
                                        label = { Text(product.name, style = MaterialTheme.typography.labelSmall) },
                                        icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Text(if (isPurchaseMode) "اطلاعات تامین‌کننده" else "اطلاعات مشتری", style = MaterialTheme.typography.titleMedium)
            
            // Intelligent Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = manualCustomerName,
                    onValueChange = { 
                        manualCustomerName = it
                        expanded = true
                        if (!isPurchaseMode) {
                            val match = customers.find { c -> c.name == it }
                            if (match != null) {
                                selectedCustomerId = match.id
                                manualCustomerPhone = match.phoneNumber ?: ""
                                onCustomerSelected(match.id)
                            } else {
                                selectedCustomerId = null
                                onCustomerSelected(null)
                            }
                        } else {
                            val match = suppliers.find { s -> s.name == it }
                            if (match != null) {
                                selectedSupplierId = match.id
                                manualCustomerPhone = match.phoneNumber ?: ""
                                onSupplierSelected(match.id)
                            } else {
                                selectedSupplierId = null
                                onSupplierSelected(null)
                            }
                        }
                    },
                    label = { Text(if (isPurchaseMode) "نام تامین‌کننده" else "نام مشتری (جستجوی هوشمند)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                )
                
                val filteredItems = if (!isPurchaseMode) {
                    customers.filter { it.name.contains(manualCustomerName, ignoreCase = true) || (it.phoneNumber?.contains(manualCustomerName) ?: false) }
                } else {
                    suppliers.filter { it.name.contains(manualCustomerName, ignoreCase = true) || (it.phoneNumber?.contains(manualCustomerName) ?: false) }
                }
                
                if (filteredItems.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
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
                                    if (!isPurchaseMode) {
                                        selectedCustomerId = id
                                        onCustomerSelected(id)
                                    } else {
                                        selectedSupplierId = id
                                        onSupplierSelected(id)
                                    }
                                    manualCustomerName = name
                                    manualCustomerPhone = phone ?: ""
                                    expanded = false 
                                }
                            )
                        }
                    }
                }
            }

            // Phone Field (Name field is now part of the search)
            OutlinedTextField(
                value = manualCustomerPhone.toPersianDigits(),
                onValueChange = { manualCustomerPhone = it.cleanNumber() },
                label = { Text("شماره تماس") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = amountPaidText,
                onValueChange = { input -> 
                    val cleaned = input.cleanNumber()
                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                        amountPaidText = cleaned
                    }
                },
                label = { Text("مبلغ پرداختی (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PersianNumberVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = totalDiscountText,
                onValueChange = { input -> 
                    val cleaned = input.cleanNumber()
                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                        totalDiscountText = cleaned
                    }
                },
                label = { Text("تخفیف روی کل فاکتور (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PersianNumberVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            if (isDebt) {
                Spacer(Modifier.height(8.dp))
                Text("تاریخ سررسید بدهی", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "۷ روزه" to (7L * 24 * 60 * 60 * 1000),
                        "۱۵ روزه" to (15L * 24 * 60 * 60 * 1000),
                        "۳۰ روزه" to (30L * 24 * 60 * 60 * 1000)
                    ).forEach { (label, duration) ->
                        val targetDate = System.currentTimeMillis() + duration
                        FilterChip(
                            selected = dueDate == targetDate,
                            onClick = { dueDate = if (dueDate == targetDate) null else targetDate },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "قابل پرداخت:", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = finalAmount.toPersianPrice(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = { 
                    val discount = totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0
                    val paid = amountPaidText.cleanNumber().toDoubleOrNull() ?: 0.0
                    onCheckout(
                        selectedCustomerId, 
                        selectedSupplierId, 
                        paid, 
                        discount, 
                        manualCustomerName, 
                        manualCustomerPhone, 
                        dueDate,
                        if (isPurchaseMode) com.oqba26.abzarforoush.data.InvoiceType.PURCHASE else com.oqba26.abzarforoush.data.InvoiceType.SALE
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("تایید و ثبت نهایی فاکتور")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
