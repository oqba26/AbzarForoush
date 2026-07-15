package com.oqba26.abzarforoush.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.Customer
import com.oqba26.abzarforoush.data.CustomerType
import com.oqba26.abzarforoush.data.InvoiceWithItems
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.*

@Composable
fun CustomerItemCard(
    customer: Customer,
    viewModel: ProductViewModel,
    onSettleDebt: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNewInvoice: () -> Unit,
    onViewDetails: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(value = false) }
    var showSettleDialog by remember { mutableStateOf(value = false) }
    
    // بهینه‌سازی: فقط وقتی آیتم باز شد فاکتورها را پردازش می‌کنیم
    val allInvoices by viewModel.allInvoices.collectAsState()

    val customerInvoices = remember(allInvoices, customer.id, isExpanded) {
        if (!isExpanded) emptyList()
        else {
            allInvoices.filter { it.invoice.customerId == customer.id }.sortedByDescending { it.invoice.timestamp }
        }
    }

    val totalPurchased = customerInvoices.sumOf { it.invoice.totalAmount }
    val totalPaid = customerInvoices.sumOf { it.invoice.amountPaid }

    val typeIcon = when (customer.type) {
        CustomerType.PERSON -> Icons.Default.Person
        CustomerType.COMPANY -> Icons.Default.Business
        CustomerType.INSTITUTION -> Icons.Default.HomeWork
        CustomerType.OTHER -> Icons.Default.CorporateFare
    }

    if (showSettleDialog) {
        SettleDebtDialog(
            currentDebt = customer.totalDebt,
            onDismiss = { showSettleDialog = false },
            onConfirm = { amount ->
                onSettleDebt(amount)
                showSettleDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isExpanded) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(typeIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = customer.name, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        customer.phoneNumber?.let {
                            Text(
                                text = it.toPersianDigits(), 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNewInvoice) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "New Invoice", tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "بدهی فعلی:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = customer.totalDebt.toPersianPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (customer.totalDebt > 0) Color.Red else Color(0xFF2E7D32),
                    fontWeight = FontWeight.ExtraBold
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Contact Info
                    if (!customer.landline.isNullOrBlank()) {
                        ContactInfoRow(Icons.Default.Phone, "تلفن ثابت: ${customer.landline.toPersianDigits()}")
                    }
                    if (!customer.address.isNullOrBlank()) {
                        ContactInfoRow(Icons.Default.LocationOn, "آدرس: ${customer.address}")
                    }

                    Spacer(Modifier.height(12.dp))

                    // Stats Cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("کل خریدها", totalPurchased.toPersianPrice(), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                        StatBox("مجموع دریافتی", totalPaid.toPersianPrice(), Color(0xFF2E7D32), Modifier.weight(1f))
                    }
                    
                    if (customer.totalDebt > 0) {
                        Button(
                            onClick = { showSettleDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Default.Payments, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ثبت دریافتی و تسویه")
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "📜 سابقه فاکتورها (۱۰ مورد آخر)", 
                        style = MaterialTheme.typography.labelLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (customerInvoices.isEmpty()) {
                        Text("سابقه ای یافت نشد.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        customerInvoices.take(10).forEach { inv ->
                            DetailedCustomerInvoiceItem(inv)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DetailedCustomerInvoiceItem(invoiceWithItems: InvoiceWithItems) {
    val inv = invoiceWithItems.invoice
    val isSettled = inv.amountPaid >= inv.totalAmount
    
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("فاکتور #${inv.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(inv.timestamp.toPersianDateTimeString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Surface(
                color = (if (isSettled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = if (isSettled) "تسویه" else "نسیه",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = if (isSettled) Color(0xFF2E7D32) else Color.Red
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("مبلغ کل: ${inv.totalAmount.toPersianPrice()}", style = MaterialTheme.typography.bodySmall)
            Text("دریافتی: ${inv.amountPaid.toPersianPrice()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
        }

        if (!isSettled && inv.dueDate != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = Color.Red)
                Spacer(Modifier.width(6.dp))
                Text("سررسید نسیه: ${inv.dueDate.toPersianDateString()}", style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SettleDebtDialog(
    currentDebt: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val currentFontFamily = MaterialTheme.typography.bodyLarge.fontFamily

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "تسویه بدهی",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column {
                        Text("مبلغ بدهی فعلی: ${currentDebt.toPersianPrice()}")
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                val cleaned = input.cleanNumber()
                                if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                    amountText = cleaned
                                }
                            },
                            label = { Text("مبلغ دریافتی (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily)
                        )
                        TextButton(
                            onClick = { 
                                amountText = currentDebt.toLong().toString()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("تسویه کل بدهی")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amount = amountText.cleanNumber().toDoubleOrNull() ?: 0.0
                                if (amount > 0) onConfirm(amount)
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("تایید")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phoneNumber ?: "") }
    var landline by remember { mutableStateOf(customer.landline ?: "") }
    var address by remember { mutableStateOf(customer.address ?: "") }
    var type by remember { mutableStateOf(customer.type) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "ویرایش اطلاعات مشتری",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when(type) {
                                    CustomerType.PERSON -> "شخص حقیقی"
                                    CustomerType.COMPANY -> "شرکت / حقوقی"
                                    CustomerType.INSTITUTION -> "موسسه / سازمان"
                                    CustomerType.OTHER -> "سایر"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوع مشتری") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                CustomerType.entries.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(when(t) {
                                            CustomerType.PERSON -> "شخص حقیقی"
                                            CustomerType.COMPANY -> "شرکت / حقوقی"
                                            CustomerType.INSTITUTION -> "موسسه / سازمان"
                                            CustomerType.OTHER -> "سایر"
                                        }) },
                                        onClick = {
                                            type = t
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام و نام خانوادگی / نام شرکت") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره همراه") },
                            leadingIcon = { Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = landline.toPersianDigits(),
                            onValueChange = { landline = it.cleanNumber() },
                            label = { Text("تلفن ثابت") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("آدرس دقیق") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                if (name.isNotBlank()) onConfirm(customer.copy(
                                    name = name, 
                                    phoneNumber = phone.ifBlank { null },
                                    landline = landline.ifBlank { null },
                                    address = address.ifBlank { null },
                                    type = type
                                )) 
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("تایید")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, String?, CustomerType) -> Unit,
) {
    var name by remember { mutableStateOf(value = "") }
    var phone by remember { mutableStateOf(value = "") }
    var landline by remember { mutableStateOf(value = "") }
    var address by remember { mutableStateOf(value = "") }
    var type by remember { mutableStateOf(value = CustomerType.PERSON) }
    var expanded by remember { mutableStateOf(value = false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    var suggestedContacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }

    // Search contacts if name matches
    LaunchedEffect(name) {
        suggestedContacts = if (name.length > 1 && phone.isBlank()) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ContactHelper.getContactsByName(context, name)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "افزودن مشتری جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when(type) {
                                    CustomerType.PERSON -> "شخص حقیقی"
                                    CustomerType.COMPANY -> "شرکت / حقوقی"
                                    CustomerType.INSTITUTION -> "موسسه / سازمان"
                                    CustomerType.OTHER -> "سایر"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوع مشتری") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                CustomerType.entries.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(when(t) {
                                            CustomerType.PERSON -> "شخص حقیقی"
                                            CustomerType.COMPANY -> "شرکت / حقوقی"
                                            CustomerType.INSTITUTION -> "موسسه / سازمان"
                                            CustomerType.OTHER -> "سایر"
                                        }) },
                                        onClick = {
                                            type = t
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام و نام خانوادگی / نام شرکت") },
                            modifier = Modifier.fillMaxWidth()
                        )

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
                                    SuggestionChip(
                                        onClick = {
                                            name = contact.name
                                            phone = contact.phoneNumber
                                            suggestedContacts = emptyList()
                                        },
                                        label = {
                                            Column {
                                                Text(contact.name, style = MaterialTheme.typography.labelSmall)
                                                Text(contact.phoneNumber.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره همراه") },
                            leadingIcon = { Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = landline.toPersianDigits(),
                            onValueChange = { landline = it.cleanNumber() },
                            label = { Text("تلفن ثابت") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("آدرس دقیق") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                if (name.isNotBlank()) onConfirm(
                                    name, 
                                    phone.ifBlank { null },
                                    landline.ifBlank { null },
                                    address.ifBlank { null },
                                    type
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("تایید")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}
