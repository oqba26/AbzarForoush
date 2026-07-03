package com.oqba26.abzarforoush.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.abzarforoush.data.Product
import com.oqba26.abzarforoush.data.BundleWithProducts
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
    isInCart: Boolean = false,
    isSaleMode: Boolean = false,
    onDelete: () -> Unit, 
    onEdit: () -> Unit, 
    onAddToCart: () -> Unit
) {
    val isLowStock = product.minStock > 0 && product.stock > 0 && product.stock <= product.minStock
    val isOutOfStock = product.stock <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
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

                // Price Section
                Text(
                    text = "قیمت هر ${product.unit}: ${product.price.toPersianPrice()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32) // Dark Green for price
                    )
                )

                Spacer(Modifier.height(4.dp))

                // Stock Status Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val stockColor = when {
                        isOutOfStock -> Color.Red
                        isLowStock -> Color(0xFFE65100) // Deep Orange
                        else -> Color(0xFF4CAF50) // Material Green
                    }
                    
                    val stockIcon = when {
                        isOutOfStock -> Icons.Default.Warning
                        isLowStock -> Icons.Default.Warning
                        else -> Icons.Default.Inventory
                    }

                    Icon(
                        imageVector = stockIcon,
                        contentDescription = null,
                        tint = stockColor,
                        modifier = Modifier.size(16.dp)
                    )

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

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit", 
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete", 
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
