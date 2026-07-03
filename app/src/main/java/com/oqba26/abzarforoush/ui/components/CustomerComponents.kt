package com.oqba26.abzarforoush.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.Customer
import com.oqba26.abzarforoush.data.DebtTransaction
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.data.TransactionType
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDateTimeString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@Composable
fun CustomerItemCard(
    customer: Customer, 
    onSettleDebt: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNewInvoice: () -> Unit,
    onViewHistory: () -> Unit
) {
    var showSettleDialog by remember { mutableStateOf(false) }

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
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.name, style = MaterialTheme.typography.titleLarge)
                    customer.phoneNumber?.let {
                        Text(text = "تلفن: ${it.toPersianDigits()}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Row {
                    IconButton(onClick = onNewInvoice) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "New Invoice", tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Default.History, contentDescription = "Debt History", tint = Color(0xFF9C27B0))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = Color.Red)
                    }
                    if (customer.totalDebt > 0) {
                        IconButton(onClick = { showSettleDialog = true }) {
                            Icon(Icons.Default.Payments, contentDescription = "Settle Debt", tint = Color(0xFF4CAF50))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "کل بدهی:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = customer.totalDebt.toPersianPrice(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (customer.totalDebt > 0) Color.Red else Color.Unspecified,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewHistory() }
                )
            }
        }
    }
}

@Composable
fun DebtHistoryDialog(
    customer: Customer,
    viewModel: ProductViewModel,
    onDismiss: () -> Unit
) {
    val transactions by viewModel.getDebtTransactions(customer.id).collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "تاریخچه تراکنش‌های ${customer.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "بدهی فعلی: ${customer.totalDebt.toPersianPrice()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("تراکنشی یافت نشد.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(transactions) { transaction ->
                                TransactionItem(transaction)
                                HorizontalDivider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: DebtTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description ?: (if (transaction.type == TransactionType.DEBT_INCREASE) "بدهی جدید" else "پرداختی"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.timestamp.toPersianDateTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = (if (transaction.amount > 0) "+" else "") + transaction.amount.toPersianPrice(),
            style = MaterialTheme.typography.titleMedium,
            color = if (transaction.amount > 0) Color.Red else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
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
                Column(modifier = Modifier.padding(24.dp)) {
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
                                amountText = currentDebt.toInt().toString()
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
                            shape = MaterialTheme.shapes.small,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("تایید", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Button(
                            onClick = onDismiss,
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

@Composable
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phoneNumber ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "ویرایش اطلاعات مشتری",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام مشتری") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره تلفن") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { if (name.isNotBlank()) onConfirm(customer.copy(name = name, phoneNumber = phone.ifBlank { null })) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("تایید", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Button(
                            onClick = onDismiss,
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

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "افزودن مشتری جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام مشتری") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره تلفن") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { if (name.isNotBlank()) onConfirm(name, phone.ifBlank { null }) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("تایید", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Button(
                            onClick = onDismiss,
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
