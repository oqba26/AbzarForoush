package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.oqba26.abzarforoush.data.InvoiceType
import com.oqba26.abzarforoush.data.InvoiceWithItems
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.ui.components.InvoiceItemCard
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeRange by remember { mutableStateOf("امروز") }
    val timeRanges = listOf("امروز", "هفته اخیر", "ماه اخیر", "همه")

    val todayDate = remember { System.currentTimeMillis().toPersianDateString() }
    val now = System.currentTimeMillis()

    val filteredInvoices = remember(invoices, customers, searchQuery, selectedTimeRange) {
        invoices.filter { invoiceWithItems ->
            val invoice = invoiceWithItems.invoice
            val customerName = customers.find { it.id == invoice.customerId }?.name ?: "نقدی"
            
            // Search filter
            val matchesSearch = searchQuery.isBlank() || 
                                customerName.contains(searchQuery, ignoreCase = true) ||
                                invoice.id.toString().contains(searchQuery)
            
            // Time filter
            val matchesTime = when (selectedTimeRange) {
                "امروز" -> invoice.timestamp.toPersianDateString() == todayDate
                "هفته اخیر" -> invoice.timestamp > (now - 7L * 24 * 60 * 60 * 1000)
                "ماه اخیر" -> invoice.timestamp > (now - 30L * 24 * 60 * 60 * 1000)
                else -> true
            }

            matchesSearch && matchesTime
        }
    }

    val totalSales = filteredInvoices.filter { it.invoice.type == InvoiceType.SALE }.sumOf { it.invoice.totalAmount }
    val totalPurchases = filteredInvoices.filter { it.invoice.type == InvoiceType.PURCHASE }.sumOf { it.invoice.totalAmount }

    var invoiceToDelete by remember { mutableStateOf<InvoiceWithItems?>(null) }

    if (invoiceToDelete != null) {
        Dialog(onDismissRequest = { invoiceToDelete = null }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "حذف فاکتور",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "آیا از حذف فاکتور شماره #${invoiceToDelete?.invoice?.id.toString().toPersianDigits()} اطمینان دارید؟ با حذف فاکتور، موجودی کالاها به حالت قبل برمی‌گردد.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    invoiceToDelete?.let { viewModel.deleteInvoice(it) }
                                    invoiceToDelete = null
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
                                onClick = { invoiceToDelete = null },
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تاریخچه فروش", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.syncWithSupabase { isRefreshing = false }
            },
            state = pullRefreshState,
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("جستجوی فاکتور یا مشتری...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                // Time Range Filters
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeRanges) { range ->
                        val isSelected = range == selectedTimeRange
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTimeRange = range },
                            label = { Text(range) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "خلاصه وضعیت $selectedTimeRange", style = MaterialTheme.typography.titleMedium)
                                if (selectedTimeRange == "امروز") {
                                    Text(text = todayDate, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "فروش: ${totalSales.toPersianPrice()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (totalPurchases > 0) {
                                    Text(
                                        text = "خرید: ${totalPurchases.toPersianPrice()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredInvoices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (searchQuery.isBlank()) "فاکتوری در این بازه پیدا نشد." else "نتیجه‌ای برای جستجوی شما یافت نشد.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(filteredInvoices, key = { it.invoice.id }) { invoiceWithItems ->
                            InvoiceItemCard(
                                invoiceWithItems = invoiceWithItems, 
                                onSharePdf = { viewModel.shareInvoiceAsPdf(context, invoiceWithItems) },
                                onDelete = { invoiceToDelete = invoiceWithItems }
                            )
                        }
                    }
                }
            }
        }
    }
}
