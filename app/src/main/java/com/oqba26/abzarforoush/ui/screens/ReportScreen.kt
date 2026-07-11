package com.oqba26.abzarforoush.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateString
import com.oqba26.abzarforoush.util.toPersianNumber
import com.oqba26.abzarforoush.util.toPersianPrice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val invoiceItems by viewModel.allInvoiceItems.collectAsState(initial = emptyList())
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())

    // بهینه‌سازی محاسبات سنگین با استفاده از derivedStateOf
    val totalSales by remember(invoices) { derivedStateOf { invoices.sumOf { it.invoice.totalAmount } } }
    val totalReceived by remember(invoices) { derivedStateOf { invoices.sumOf { it.invoice.amountPaid } } }
    val totalDebt by remember(customers) { derivedStateOf { customers.sumOf { it.totalDebt } } }
    val totalExpenses by remember(expenses) { derivedStateOf { expenses.sumOf { it.amount } } }
    
    val aiInsights by viewModel.aiInsights.collectAsState()
    
    val grossProfit by remember(invoices, invoiceItems) { 
        derivedStateOf {
            invoiceItems.sumOf { 
                (it.priceAtSale - it.purchasePriceAtSale) * it.quantity - it.discount 
            } - invoices.sumOf { it.invoice.totalDiscount }
        }
    }
    
    val netProfit by remember(grossProfit, totalExpenses) { derivedStateOf { grossProfit - totalExpenses } }
    
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارشات و آمار", style = MaterialTheme.typography.titleMedium) },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "دستیار هوشمند فروشگاه",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (!isAnalyzing) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    isAnalyzing = true
                                    delay(3000.milliseconds) // شبیه‌سازی تحلیل عمیق
                                    isAnalyzing = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("تحلیل عمیق (بتا)", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (isAnalyzing) {
                    AiScanningLoader()
                } else {
                    AiInsightsSection(aiInsights)
                }
            }

            item {
                Text("خلاصه وضعیت مالی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        insights.forEach { insight ->
            val color = when {
                insight.contains("📈") || insight.contains("🏆") -> Color(0xFFE8F5E9) // سبز برای موفقیت
                insight.contains("☀️") || insight.contains("🔮") -> Color(0xFFE3F2FD) // آبی برای فرصت و پیش‌بینی
                insight.contains("⚠️") -> Color(0xFFFFF3E0) // نارنجی برای هشدار
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            
            val icon = when {
                insight.contains("📈") -> Icons.AutoMirrored.Filled.TrendingUp
                insight.contains("🔮") -> Icons.Default.Psychology
                insight.contains("☀️") -> Icons.Default.AutoAwesome
                else -> Icons.Default.Lightbulb
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.DarkGray
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AiScanningLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(48.dp).alpha(alpha),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "در حال تحلیل عمیق داده‌های فروشگاه...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
