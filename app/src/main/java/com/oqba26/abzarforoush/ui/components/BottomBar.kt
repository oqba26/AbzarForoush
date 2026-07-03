package com.oqba26.abzarforoush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.abzarforoush.util.toPersianDigits

@Composable
fun AppBottomBar(
    currentScreen: String = "products",
    cartItemCount: Int,
    onNavigateToProducts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onShowCart: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ترتیب از راست به چپ (در حالت RTL سیستم):
            // محصولات، مشتریان، گزارشات، تاریخچه، سبد، تنظیمات
            
            RowNavigationItem(
                icon = Icons.Default.Inventory,
                label = "محصولات",
                selected = currentScreen == "products",
                onClick = onNavigateToProducts
            )

            RowNavigationItem(
                icon = Icons.Default.People,
                label = "مشتریان",
                selected = currentScreen == "customers",
                onClick = onNavigateToCustomers
            )

            RowNavigationItem(
                icon = Icons.Default.BarChart,
                label = "گزارشات",
                selected = currentScreen == "reports",
                onClick = onNavigateToReports
            )

            RowNavigationItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = "حسابداری",
                selected = currentScreen == "accounting",
                onClick = onNavigateToAccounting
            )

            RowNavigationItem(
                icon = Icons.Default.History,
                label = "تاریخچه",
                selected = currentScreen == "history",
                onClick = onNavigateToHistory
            )

            RowNavigationItem(
                icon = Icons.Default.ShoppingCart,
                label = "سبد",
                selected = false,
                onClick = onShowCart,
                badgeCount = cartItemCount
            )

            RowNavigationItem(
                icon = Icons.Default.Settings,
                label = "تنظیمات",
                selected = currentScreen == "settings",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun RowScope.RowNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val indicatorColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) 
        else Color.Transparent,
        label = "color"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(56.dp)
                .clip(CircleShape)
                .background(indicatorColor),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) { Text(text = badgeCount.toString().toPersianDigits()) }
                    }
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1
        )
    }
}
