package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianNumber
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val invoiceItems by viewModel.allInvoiceItems.collectAsState(initial = emptyList())
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())

    val totalSales = invoices.sumOf { it.invoice.totalAmount }
    val totalReceived = invoices.sumOf { it.invoice.amountPaid }
    val totalDebt = customers.sumOf { it.totalDebt }
    val totalExpenses = expenses.sumOf { it.amount }
    val aiInsights by viewModel.aiInsights.collectAsState()
    
    val grossProfit = invoiceItems.sumOf { 
        (it.priceAtSale - it.purchasePriceAtSale) * it.quantity - it.discount 
    } - invoices.sumOf { it.invoice.totalDiscount }
    
    val netProfit = grossProfit - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارشات و آمار") },
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("خلاصه وضعیت مالی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                SmartAnalysisCard(invoices.map { it.invoice }, invoiceItems, customers, netProfit)
            }

            item {
                Text("توصیه‌های هوشمند (دستیار آفلاین)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AiInsightsSection(aiInsights)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("کل فروش", totalSales.toPersianPrice(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                    SummaryCard("سود ناخالص", grossProfit.toPersianPrice(), Color(0xFFE8F5E9), Modifier.weight(1f))
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("کل هزینه‌ها", totalExpenses.toPersianPrice(), Color(0xFFFFEBEE), Modifier.weight(1f))
                    SummaryCard("سود واقعی (خالص)", netProfit.toPersianPrice(), Color(0xFFE1F5FE), Modifier.weight(1f))
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("دریافتی", totalReceived.toPersianPrice(), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                    SummaryCard("طلب مشتریان", totalDebt.toPersianPrice(), MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f))
                }
            }

            item {
                Text("فروش ۷ روز اخیر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Last7DaysChart(invoices.map { it.invoice })
            }

            item {
                Text("پر‌فروش‌ترین کالاها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            val topProducts = invoiceItems
                .groupBy { it.productName }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            items(topProducts) { (name, qty) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                    Text("${qty.toPersianNumber()} واحد", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun AiInsightsSection(insights: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        insights.forEach { insight ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null, 
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmartAnalysisCard(
    invoices: List<com.oqba26.abzarforoush.data.Invoice>,
    items: List<com.oqba26.abzarforoush.data.InvoiceItem>,
    customers: List<com.oqba26.abzarforoush.data.Customer>,
    totalProfit: Double
) {
    val analysis = remember(invoices, items, customers, totalProfit) {
        val topProduct = items.groupBy { it.productName }
            .mapValues { it.value.sumOf { i -> i.quantity } }
            .maxByOrNull { it.value }?.key ?: "نامشخص"
        
        val topCustomer = customers.maxByOrNull { it.totalDebt }
        val debtStatus = if (topCustomer != null && topCustomer.totalDebt > 0) {
            "بیشترین طلب شما از «${topCustomer.name}» است."
        } else "خوشبختانه طلب معوقه سنگینی ندارید."

        val avgSale = if (invoices.isNotEmpty()) invoices.sumOf { it.totalAmount } / invoices.size else 0.0
        val profitMargin = if (invoices.sumOf { it.totalAmount } > 0) (totalProfit / invoices.sumOf { it.totalAmount }) * 100 else 0.0
        
        "محبوب‌ترین کالای شما «$topProduct» بوده است. $debtStatus میانگین هر فاکتور شما ${avgSale.toPersianPrice()} و حاشیه سودتان حدود ${profitMargin.toInt().toPersianNumber()}% است."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = analysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, containerColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun Last7DaysChart(invoices: List<com.oqba26.abzarforoush.data.Invoice>) {
    val last7Days = (0..6).reversed().map { daysAgo ->
        val timestamp = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)
        timestamp.toPersianDateString()
    }

    val dailySales = last7Days.associateWith { date ->
        invoices.filter { it.timestamp.toPersianDateString() == date }.sumOf { it.totalAmount }
    }

    val maxSale = dailySales.values.maxOrNull() ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        last7Days.forEach { date ->
            val sale = dailySales[date] ?: 0.0
            val barHeight = (sale / maxSale).toFloat().coerceAtLeast(0.05f)
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (sale > 0) {
                    Text(
                        text = (sale / 1000).toInt().toPersianNumber() + "k",
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(barHeight)
                        .background(
                            if (barHeight > 0.1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Text(
                    text = date.substring(8), // Just show the day
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
