package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.Expense
import com.oqba26.abzarforoush.data.ExpenseCategory
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToCheques: () -> Unit,
    onNavigateToCashbook: () -> Unit,
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    var showAddExpenseDialog by remember { mutableStateOf(value = false) }

    val totalIncome = remember(invoices) { invoices.sumOf { it.invoice.amountPaid } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val balance = totalIncome - totalExpenses

    val recentTransactions = remember(invoices, expenses) {
        val list = mutableListOf<CombinedTransaction>()
        invoices.forEach {
            list.add(CombinedTransaction(
                title = "فاکتور #${it.invoice.id} (${it.invoice.customerId ?: "نقدی"})",
                amount = it.invoice.amountPaid,
                timestamp = it.invoice.timestamp,
                isIncome = true
            ))
        }
        expenses.forEach {
            list.add(CombinedTransaction(
                title = getCategoryName(it.category),
                amount = it.amount,
                timestamp = it.timestamp,
                isIncome = false
            ))
        }
        list.sortedByDescending { it.timestamp }.take(20)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حسابداری و مدیریت مالی") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("خلاصه وضعیت مالی", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryItem("درآمد (دریافتی)", totalIncome.toPersianPrice(), Color(0xFF2E7D32))
                        SummaryItem("هزینه‌های جاری", totalExpenses.toPersianPrice(), MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("مانده صندوق (سود):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            balance.toPersianPrice(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (balance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Navigation Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountingNavCard(
                    title = "تامین‌کنندگان",
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onNavigateToSuppliers,
                    modifier = Modifier.weight(1f)
                )
                AccountingNavCard(
                    title = "دفتر چک",
                    icon = Icons.Default.AccountBalance,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = onNavigateToCheques,
                    modifier = Modifier.weight(1f)
                )
                AccountingNavCard(
                    title = "ریز تراکنش‌ها",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onNavigateToCashbook,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تراکنش‌های اخیر", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { showAddExpenseDialog = true }, shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("ثبت هزینه")
                }
            }

            if (recentTransactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("هیچ تراکنشی ثبت نشده است.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentTransactions) { transaction ->
                        TransactionItemRow(transaction)
                    }
                }
            }
        }
    }
    // ... (Dialog part remains same)

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false }
        ) { amount, category, desc ->
            viewModel.addExpense(amount, category, desc)
            showAddExpenseDialog = false
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

data class CombinedTransaction(val title: String, val amount: Double, val timestamp: Long, val isIncome: Boolean)

@Composable
fun TransactionItemRow(transaction: CombinedTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(transaction.timestamp.toPersianDateString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(
                text = (if (transaction.isIncome) "+" else "-") + transaction.amount.toPersianPrice(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AccountingNavCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getCategoryName(expense.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                expense.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = expense.timestamp.toPersianDateString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = expense.amount.toPersianPrice(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (Double, ExpenseCategory, String?) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var expanded by remember { mutableStateOf(value = false) }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "ثبت هزینه جدید",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.cleanNumber() },
                        label = { Text("مبلغ (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PersianNumberVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = getCategoryName(category),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("دسته بندی") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            ExpenseCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(getCategoryName(cat)) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیحات (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amt = amount.cleanNumber().toDoubleOrNull() ?: 0.0
                                if (amt > 0) onConfirm(amt, category, description)
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
                                containerColor = MaterialTheme.colorScheme.error
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

fun getCategoryName(category: ExpenseCategory): String {
    return when (category) {
        ExpenseCategory.RENT -> "اجاره مغازه"
        ExpenseCategory.BILLS -> "قبوض (آب، برق، گاز)"
        ExpenseCategory.SALARY -> "حقوق و دستمزد"
        ExpenseCategory.TAX -> "مالیات"
        ExpenseCategory.TRANSPORT -> "حمل و نقل"
        ExpenseCategory.REPAIR -> "تعمیرات و نگهداری"
        ExpenseCategory.MARKETING -> "تبلیغات"
        ExpenseCategory.OTHER -> "سایر هزینه‌ها"
    }
}
