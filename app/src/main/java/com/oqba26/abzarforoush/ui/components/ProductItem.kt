package com.oqba26.abzarforoush.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.abzarforoush.data.Product
import com.oqba26.abzarforoush.data.BundleWithProducts
import com.oqba26.abzarforoush.data.InvoiceWithItems
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.util.toPersianDateTimeString
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianNumber
import com.oqba26.abzarforoush.util.toPersianPrice

@Composable
fun BundleItemCard(
    bundleWithProducts: BundleWithProducts,
    isSaleMode: Boolean = false,
    onAddToCart: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📦 " + bundleWithProducts.bundle.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                bundleWithProducts.bundle.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "محتویات پکیج:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                bundleWithProducts.bundleItems.forEach { item ->
                    val product = bundleWithProducts.products.find { it.id == item.productId }
                    Text(
                        text = "• ${product?.name ?: "نامعلوم"} (${item.quantity.toPersianNumber()} ${product?.unit ?: ""})",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSaleMode) {
                    IconButton(onClick = onAddToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product, 
    viewModel: ProductViewModel,
    isInCart: Boolean = false,
    isSaleMode: Boolean = false,
    onDelete: () -> Unit, 
    onEdit: () -> Unit, 
    onAddToCart: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // به جای دریافت کل فاکتورها در هر آیتم، فقط وقتی باز شد محاسبات را انجام می‌دهیم
    val allInvoices by viewModel.allInvoices.collectAsState()
    
    val productMovement = remember(allInvoices, product.name, isExpanded) {
        if (!isExpanded) emptyList()
        else {
            allInvoices.flatMap { invoiceWithItems ->
                invoiceWithItems.items.filter { it.productName == product.name }.map { item ->
                    MovementRecord(
                        type = invoiceWithItems.invoice.type,
                        quantity = item.quantity,
                        price = item.priceAtSale,
                        timestamp = invoiceWithItems.invoice.timestamp,
                        partyName = invoiceWithItems.invoice.customerId?.toString() ?: "مشتری نقدی"
                    )
                }
            }.sortedByDescending { it.timestamp }
        }
    }

    val isLowStock = product.minStock > 0 && product.stock > 0 && product.stock <= product.minStock
    val isOutOfStock = product.stock <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isExpanded) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name, 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ), 
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isInCart) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "In Cart",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    product.barcode?.let {
                        Text(
                            text = "بارکد: ${it.toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    Text(
                        text = "دسته: ${product.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "قیمت فعلی: ${product.price.toPersianPrice()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    StockStatus(isOutOfStock, isLowStock, product)
                }

                // Action Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isSaleMode) {
                        IconButton(
                            onClick = { onAddToCart() },
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                containerColor = if (isInCart) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isInCart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Add to Cart")
                        }
                    }
                    
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Text(
                        text = "📊 گردش کالا و سابقه قیمت:", 
                        style = MaterialTheme.typography.labelLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (productMovement.isEmpty()) {
                        Text("هنوز گردشی برای این کالا ثبت نشده است.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        Spacer(Modifier.height(8.dp))
                        productMovement.take(5).forEach { record ->
                            MovementItemRow(record, product.unit)
                        }
                    }
                }
            }
        }
    }
}

data class MovementRecord(
    val type: com.oqba26.abzarforoush.data.InvoiceType,
    val quantity: Double,
    val price: Double,
    val timestamp: Long,
    val partyName: String
)

@Composable
fun MovementItemRow(record: MovementRecord, unit: String) {
    val isSale = record.type == com.oqba26.abzarforoush.data.InvoiceType.SALE
    
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isSale) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = if (isSale) "خروج (فروش)" else "ورود (خرید)",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        color = if (isSale) Color.Red else Color(0xFF2E7D32)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(text = record.timestamp.toPersianDateTimeString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(
                text = "${record.quantity.toPersianNumber()} $unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "قیمت ثبت شده: ${record.price.toPersianPrice()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Party name can be added here if available
        }
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun StockStatus(isOutOfStock: Boolean, isLowStock: Boolean, product: Product) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val stockColor = when {
            isOutOfStock -> Color.Red
            isLowStock -> Color(0xFFE65100)
            else -> Color(0xFF4CAF50)
        }
        
        val stockIcon = when {
            isOutOfStock -> Icons.Default.Warning
            isLowStock -> Icons.Default.Warning
            else -> Icons.Default.Inventory
        }

        Icon(stockIcon, null, tint = stockColor, modifier = Modifier.size(16.dp))

        val stockText = when {
            isOutOfStock -> "ناموجود در انبار"
            isLowStock -> "فقط ${product.stock.toPersianNumber()} ${product.unit} باقی مانده!"
            else -> "موجودی: ${product.stock.toPersianNumber()} ${product.unit}"
        }

        Text(
            text = stockText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isLowStock || isOutOfStock) FontWeight.Bold else FontWeight.Normal
            ),
            color = stockColor
        )
    }
}
