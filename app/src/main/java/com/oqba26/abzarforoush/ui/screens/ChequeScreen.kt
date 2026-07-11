package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.Cheque
import com.oqba26.abzarforoush.data.ChequeStatus
import com.oqba26.abzarforoush.data.ChequeType
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequeScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val cheques by viewModel.allCheques.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<ChequeStatus?>(null) }
    var dueTodayOnly by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در چک‌ها...", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    isSearchVisible = false 
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                                }
                            }
                        )
                    } else {
                        Text("دفتر چک", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    Surface(
                        onClick = { showAddDialog = true },
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
                                contentDescription = "Add Cheque",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ثبت چک",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("دریافتی (مشتری)") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("پرداختی (تامین‌کننده)") })
            }

            // Filter Bar
            ScrollableTabRow(
                selectedTabIndex = if (statusFilter == null && !dueTodayOnly) 0 else -1,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {},
                indicator = {}
            ) {
                FilterChip(
                    selected = statusFilter == null && !dueTodayOnly,
                    onClick = { statusFilter = null; dueTodayOnly = false },
                    label = { Text("همه") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilterChip(
                    selected = dueTodayOnly,
                    onClick = { dueTodayOnly = !dueTodayOnly; statusFilter = null },
                    label = { Text("سررسید امروز") },
                    leadingIcon = { if (dueTodayOnly) Icon(Icons.Default.FilterList, null, Modifier.size(16.dp)) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                ChequeStatus.entries.forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = if (statusFilter == status) null else status; dueTodayOnly = false },
                        label = { Text(status.toPersian()) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            val filteredCheques = cheques.filter { cheque ->
                val typeMatch = if (selectedTab == 0) cheque.type == ChequeType.RECEIVABLE else cheque.type == ChequeType.PAYABLE
                val searchMatch = searchQuery.isEmpty() || 
                        cheque.personName.contains(searchQuery, ignoreCase = true) || 
                        cheque.chequeNumber.contains(searchQuery) ||
                        cheque.bankName.contains(searchQuery, ignoreCase = true)
                
                val statusMatch = statusFilter == null || cheque.status == statusFilter
                
                val todayMatch = if (dueTodayOnly) {
                    cheque.dueDate == viewModel.timeProvider.getToday()
                } else true

                typeMatch && searchMatch && statusMatch && todayMatch
            }

            if (filteredCheques.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ چکی در این بخش ثبت نشده است.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredCheques) { cheque ->
                        ChequeItem(
                            cheque = cheque,
                            onStatusChange = { newStatus -> viewModel.updateCheque(cheque.copy(status = newStatus)) },
                            onDelete = { viewModel.deleteCheque(cheque) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddChequeDialog(
            today = viewModel.timeProvider.getToday(),
            onDismiss = { showAddDialog = false },
            onConfirm = { num, bank, amount, date, person, type ->
                viewModel.addCheque(num, bank, amount, date, person, type)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ChequeItem(cheque: Cheque, onStatusChange: (ChequeStatus) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "بانک ${cheque.bankName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = cheque.status.toPersian(),
                    style = MaterialTheme.typography.labelSmall,
                    color = when(cheque.status) {
                        ChequeStatus.CLEARED -> Color(0xFF4CAF50)
                        ChequeStatus.BOUNCED -> Color.Red
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(text = "شماره چک: ${cheque.chequeNumber.toPersianDigits()}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "طرف حساب: ${cheque.personName}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "تاریخ سررسید: ${cheque.dueDate.toPersianDateString()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = cheque.amount.toPersianPrice(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                Row {
                    if (cheque.status == ChequeStatus.PENDING) {
                        TextButton(onClick = { onStatusChange(ChequeStatus.CLEARED) }) { Text("پاس شد") }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChequeDialog(
    today: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, LocalDate, String, ChequeType) -> Unit
) {
    var chequeNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ChequeType.RECEIVABLE) }
    
    var dueDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val currentMilli = dueDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        com.oqba26.abzarforoush.ui.components.ShamsiDatePicker(
            initialTimestamp = currentMilli,
            onDismiss = { showDatePicker = false },
            onDateSelected = { timestamp ->
                dueDate = java.time.Instant.ofEpochMilli(timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                showDatePicker = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "ثبت چک جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = type == ChequeType.RECEIVABLE,
                                onClick = { type = ChequeType.RECEIVABLE },
                                label = { Text("دریافتی") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = type == ChequeType.PAYABLE,
                                onClick = { type = ChequeType.PAYABLE },
                                label = { Text("پرداختی") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(value = chequeNumber, onValueChange = { chequeNumber = it }, label = { Text("شماره چک") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("نام بانک") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = personName, onValueChange = { personName = it }, label = { Text("نام شخص (مشتری/تامین‌کننده)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = amount, 
                            onValueChange = { amount = it.cleanNumber() }, 
                            label = { Text("مبلغ (تومان)") }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dueDate.toPersianDateString(), 
                            onValueChange = { }, 
                            label = { Text("تاریخ سررسید") }, 
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            },
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
                                val amt = amount.cleanNumber().toDoubleOrNull() ?: 0.0
                                if (chequeNumber.isNotBlank() && amt > 0) {
                                    onConfirm(chequeNumber, bankName, amt, dueDate, personName, type)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("تایید")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
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

fun ChequeStatus.toPersian(): String = when(this) {
    ChequeStatus.PENDING -> "در انتظار"
    ChequeStatus.CLEARED -> "پاس شده"
    ChequeStatus.BOUNCED -> "برگشت خورده"
    ChequeStatus.CANCELLED -> "ابطال شده"
}
