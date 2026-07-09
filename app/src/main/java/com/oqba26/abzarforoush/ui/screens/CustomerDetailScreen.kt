package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.allCustomers.collectAsState()
    val customer = customers.find { it.id == customerId }
    val allInvoices by viewModel.allInvoices.collectAsState()
    
    val customerInvoices = remember(allInvoices, customerId) {
        allInvoices.filter { it.invoice.customerId == customerId }.sortedByDescending { it.invoice.timestamp }
    }

    val totalPurchased = customerInvoices.sumOf { it.invoice.totalAmount }
    val totalPaid = customerInvoices.sumOf { it.invoice.amountPaid }
    val remainingDebt = totalPurchased - totalPaid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "جزئیات مشتری") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Quick Info
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

                // Financial Summary
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

                Text(
                    text = "سابقه فاکتورها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (customerInvoices.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("هیچ فاکتوری برای این مشتری ثبت نشده است.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(customerInvoices) { item ->
                            InvoiceHistoryCard(item)
                        }
                    }
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
fun InvoiceHistoryCard(invoiceWithItems: com.oqba26.abzarforoush.data.InvoiceWithItems) {
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
            
            if (!isSettled && inv.dueDate != null) {
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
