package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianDateTimeString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val customer = customers.find { it.id == customerId }
    val allInvoices by viewModel.allInvoices.collectAsState()
    val debtTransactions by viewModel.getDebtTransactions(customerId).collectAsState(initial = emptyList())
    
    val customerInvoices = remember(allInvoices, customerId) {
        allInvoices.filter { it.invoice.customerId == customerId }.sortedByDescending { it.invoice.timestamp }
    }

    val installments = remember(debtTransactions) {
        debtTransactions.filter { 
            (it.type == com.oqba26.abzarforoush.data.TransactionType.DEBT_INCREASE) && 
            (it.dueDate != null) && 
            (it.description?.startsWith("قسط") == true) 
        }.sortedBy { it.dueDate!! }
    }

    val totalPurchased = customerInvoices.sumOf { it.invoice.totalAmount }
    val totalPaid = customerInvoices.sumOf { it.invoice.amountPaid }
    val remainingDebt = (totalPurchased - totalPaid).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "جزئیات مشتری") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (customer != null && customerInvoices.isNotEmpty()) {
                        IconButton(onClick = { 
                            viewModel.shareCustomerStatementAsPdf(context, customer, customerInvoices) 
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF ریزحساب", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (customer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("مشتری یافت نشد")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
            ) {
                // Customer Quick Info
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = customer.phoneNumber?.toPersianDigits() ?: "بدون شماره تماس",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Financial Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("خلاصه وضعیت مالی", style = MaterialTheme.typography.labelSmall)
                            FinancialRow("کل خریدها:", totalPurchased.toPersianPrice(), MaterialTheme.colorScheme.onSurface)
                            FinancialRow("مجموع پرداختی:", totalPaid.toPersianPrice(), Color(0xFF2E7D32))
                            
                            HorizontalDivider()
                            FinancialRow("مانده بدهی کل:", remainingDebt.toPersianPrice(), if (remainingDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32), isBold = true)
                        }
                    }
                }

                item {
                    Text(
                        text = "تاریخچه فاکتورها و اقساط",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (customerInvoices.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("هیچ فاکتوری برای این مشتری ثبت نشده است.")
                        }
                    }
                } else {
                    items(customerInvoices) { invoiceWithItems ->
                        val invoiceInstallments = installments.filter { it.invoiceId == invoiceWithItems.invoice.id }
                        InvoiceAndInstallmentGroup(
                            invoiceWithItems = invoiceWithItems,
                            installments = invoiceInstallments,
                            onPayInstallment = { viewModel.payInstallment(it) },
                            onRevertInstallment = { viewModel.revertInstallmentPayment(it) },
                        ) { viewModel.deleteInstallment(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceAndInstallmentGroup(
    invoiceWithItems: com.oqba26.abzarforoush.data.InvoiceWithItems,
    installments: List<com.oqba26.abzarforoush.data.DebtTransaction>,
    onPayInstallment: (com.oqba26.abzarforoush.data.DebtTransaction) -> Unit,
    onRevertInstallment: (com.oqba26.abzarforoush.data.DebtTransaction) -> Unit,
    onDeleteInstallment: (com.oqba26.abzarforoush.data.DebtTransaction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            // Header Invoice
            InvoiceHistoryCard(invoiceWithItems, showDueDate = installments.isEmpty())
            
            if (installments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "اقساط این فاکتور:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    installments.forEach { installment ->
                        InstallmentRow(
                            transaction = installment,
                            onPayClick = { onPayInstallment(installment) },
                            onRevertClick = { onRevertInstallment(installment) },
                            onDeleteClick = { onDeleteInstallment(installment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentRow(
    transaction: com.oqba26.abzarforoush.data.DebtTransaction,
    onPayClick: () -> Unit,
    onRevertClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(value = false) }
    var showRevertDialog by remember { mutableStateOf(value = false) }
    var showDeleteDialog by remember { mutableStateOf(value = false) }

    if (showConfirmDialog) {
        SimpleConfirmDialog(
            title = "تایید پرداخت قسط",
            message = "آیا از دریافت مبلغ ${transaction.amount.toPersianPrice()} بابت این قسط اطمینان دارید؟",
            onConfirm = { 
                onPayClick()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showRevertDialog) {
        SimpleConfirmDialog(
            title = "لغو وضعیت پرداخت",
            message = "آیا می‌خواهید وضعیت این قسط را به «پرداخت نشده» برگردانید؟ (مبلغ به بدهی مشتری اضافه خواهد شد)",
            onConfirm = { 
                onRevertClick()
                showRevertDialog = false
            },
            onDismiss = { showRevertDialog = false }
        )
    }

    if (showDeleteDialog) {
        SimpleConfirmDialog(
            title = "حذف قسط",
            message = "آیا از حذف کامل این قسط اطمینان دارید؟",
            onConfirm = { 
                onDeleteClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            isDanger = true
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isPaid) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, if (transaction.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.description ?: "قسط",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.isPaid) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = transaction.dueDate?.toPersianDateString() ?: "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (transaction.isPaid) Color(0xFF2E7D32).copy(alpha = 0.7f) else Color.Gray
                )
                if (transaction.isPaid && transaction.paymentTimestamp != null) {
                    Text(
                        text = "وصول: ${transaction.paymentTimestamp.toPersianDateTimeString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount.toPersianPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
                
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (transaction.isPaid) {
                        IconButton(onClick = { showRevertDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, "اصلاح", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                        Button(
                            onClick = { showConfirmDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("تسویه", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = if (isDanger) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
                    ) { Text("تایید") }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                }
            }
        }
    }
}

@Composable
fun FinancialRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun InvoiceHistoryCard(invoiceWithItems: com.oqba26.abzarforoush.data.InvoiceWithItems, showDueDate: Boolean) {
    val inv = invoiceWithItems.invoice
    val isSettled = inv.amountPaid >= inv.totalAmount
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "فاکتور #${inv.id}", fontWeight = FontWeight.Bold)
                Text(
                    text = if (isSettled) "تسویه شده" else "نسیه/مانده دار",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
            
            Text(
                text = inv.timestamp.toPersianDateString(), // این متد شامل ساعت هم هست
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("مبلغ فاکتور", style = MaterialTheme.typography.labelSmall)
                    Text(inv.totalAmount.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("پرداختی", style = MaterialTheme.typography.labelSmall)
                    Text(inv.amountPaid.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                }
            }
            
            if (!isSettled && inv.dueDate != null && showDueDate) {
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "سررسید بدهی: ${inv.dueDate.toPersianDateString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
