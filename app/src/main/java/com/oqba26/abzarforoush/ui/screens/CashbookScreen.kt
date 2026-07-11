package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())

    val transactions = remember(invoices, expenses) {
        val list = mutableListOf<TransactionItem>()
        invoices.forEach { 
            list.add(TransactionItem(
                title = "فروش - فاکتور #${it.invoice.id}",
                amount = it.invoice.amountPaid,
                timestamp = it.invoice.timestamp,
                isIncome = true
            ))
        }
        expenses.forEach {
            list.add(TransactionItem(
                title = "هزینه - ${getCashbookCategoryName(it.category)}",
                amount = it.amount,
                timestamp = it.timestamp,
                isIncome = false
            ))
        }
        list.sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دفتر صندوق", style = MaterialTheme.typography.titleMedium) },
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
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هنوز تراکنشی ثبت نشده است.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { item ->
                        TransactionRow(item)
                    }
                }
            }
        }
    }
}

data class TransactionItem(val title: String, val amount: Double, val timestamp: Long, val isIncome: Boolean)

@Composable
fun TransactionRow(item: TransactionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (item.isIncome) Color(0xFF4CAF50) else Color.Red
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = item.timestamp.toPersianDateString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Text(
                text = item.amount.toPersianPrice(),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isIncome) Color(0xFF2E7D32) else Color.Red,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun getCashbookCategoryName(category: com.oqba26.abzarforoush.data.ExpenseCategory): String {
    return when (category) {
        com.oqba26.abzarforoush.data.ExpenseCategory.RENT -> "اجاره"
        com.oqba26.abzarforoush.data.ExpenseCategory.BILLS -> "قبوض"
        com.oqba26.abzarforoush.data.ExpenseCategory.SALARY -> "حقوق"
        com.oqba26.abzarforoush.data.ExpenseCategory.TAX -> "مالیات"
        com.oqba26.abzarforoush.data.ExpenseCategory.TRANSPORT -> "حمل و نقل"
        com.oqba26.abzarforoush.data.ExpenseCategory.REPAIR -> "تعمیرات"
        com.oqba26.abzarforoush.data.ExpenseCategory.MARKETING -> "تبلیغات"
        com.oqba26.abzarforoush.data.ExpenseCategory.OTHER -> "متفرقه"
    }
}
