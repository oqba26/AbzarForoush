package com.oqba26.abzarforoush.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.oqba26.abzarforoush.data.Product
import com.oqba26.abzarforoush.util.PersianNumberVisualTransformation
import com.oqba26.abzarforoush.util.cleanNumber
import com.oqba26.abzarforoush.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    categories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Double, Double, Double, Double, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var partnerPrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("عدد") }
    var barcode by remember { mutableStateOf("") }
    var showScannerInsideDialog by remember { mutableStateOf(false) }
    var isCategoryManuallyEdited by remember { mutableStateOf(false) }
    var isUnitManuallyEdited by remember { mutableStateOf(false) }
    
    val units = listOf("عدد", "متر", "کیلو", "جین", "بسته", "لیتر", "گالن", "شاخه", "حلقه", "جفت", "قوطی", "جعبه", "رول", "ست")
    var expanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val currentFontFamily = MaterialTheme.typography.bodyLarge.fontFamily
    // ... rest of logic

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScannerInsideDialog = true
    }

    if (showScannerInsideDialog) {
        BarcodeScannerDialog(
            onDismiss = { showScannerInsideDialog = false },
            onBarcodeScanned = {
                barcode = it
                showScannerInsideDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 650.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "افزودن محصول جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                if (!isCategoryManuallyEdited) {
                                    category = com.oqba26.abzarforoush.util.CategorizationHelper.suggestCategory(it)
                                }
                                if (!isUnitManuallyEdited) {
                                    unit = com.oqba26.abzarforoush.util.CategorizationHelper.suggestUnit(it)
                                }
                            },
                            label = { Text("نام کالا") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { input -> 
                                val cleaned = input.cleanNumber()
                                if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                    price = cleaned
                                }
                            },
                            label = { Text("قیمت تک‌فروشی (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = wholesalePrice,
                                onValueChange = { input -> 
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) wholesalePrice = cleaned
                                },
                                label = { Text("قیمت عمده") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = partnerPrice,
                                onValueChange = { input -> 
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) partnerPrice = cleaned
                                },
                                label = { Text("قیمت همکار") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { input -> 
                                val cleaned = input.cleanNumber()
                                if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                    purchasePrice = cleaned
                                }
                            },
                            label = { Text("قیمت خرید (تومان) - اختیاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { input ->
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                        stock = cleaned
                                    }
                                },
                                label = { Text("موجودی") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = minStock,
                                onValueChange = { input ->
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                        minStock = cleaned
                                    }
                                },
                                label = { Text("حدِاقل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                        }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { 
                                    category = it
                                    isCategoryManuallyEdited = it.isNotBlank() && it != "بدون دسته بندی"
                                },
                                label = { Text("انتخاب یا تایپ دسته بندی") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            val filteredCategories = categories.filter { 
                                it.contains(category, ignoreCase = true) && it != "همه" && it != "بدون دسته بندی" 
                            }
                            if (filteredCategories.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    filteredCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                isCategoryManuallyEdited = true
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {
                                    unit = it
                                    isUnitManuallyEdited = true
                                },
                                label = { Text("واحد") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                units.filter { it.contains(unit, ignoreCase = true) }.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = { 
                                            unit = u
                                            isUnitManuallyEdited = true
                                            expanded = false 
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = barcode.toPersianDigits(),
                            onValueChange = { barcode = it.cleanNumber() },
                            label = { Text("بارکد کالا") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        showScannerInsideDialog = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && price.isNotBlank() && stock.isNotBlank()) {
                                    onConfirm(
                                        name, 
                                        price.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        wholesalePrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        partnerPrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        purchasePrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        stock.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        minStock.cleanNumber().toDoubleOrNull() ?: 0.0,
                                        category.ifBlank { "بدون دسته بندی" },
                                        unit,
                                        barcode.ifBlank { null }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("تایید", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("انصراف", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    categories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toLong().toString()) }
    var wholesalePrice by remember { mutableStateOf(product.wholesalePrice.toLong().toString()) }
    var partnerPrice by remember { mutableStateOf(product.partnerPrice.toLong().toString()) }
    var purchasePrice by remember { mutableStateOf(product.purchasePrice.toLong().toString()) }
    var stock by remember { mutableStateOf(if (product.stock == product.stock.toLong().toDouble()) product.stock.toLong().toString() else product.stock.toString()) }
    var minStock by remember { mutableStateOf(if (product.minStock == product.minStock.toLong().toDouble()) product.minStock.toLong().toString() else product.minStock.toString()) }
    var category by remember { mutableStateOf(product.category) }
    var unit by remember { mutableStateOf(product.unit) }
    var expanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isUnitManuallyEdited by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val units = listOf("عدد", "متر", "کیلو", "جین", "بسته", "لیتر", "گالن", "شاخه", "حلقه", "جفت", "قوطی", "جعبه", "رول", "ست")

    val currentFontFamily = MaterialTheme.typography.bodyLarge.fontFamily

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 650.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "ویرایش محصول",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام کالا") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { input -> 
                                val cleaned = input.cleanNumber()
                                if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                    price = cleaned
                                }
                            },
                            label = { Text("قیمت تک‌فروشی (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = wholesalePrice,
                                onValueChange = { input -> 
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) wholesalePrice = cleaned
                                },
                                label = { Text("قیمت عمده") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = partnerPrice,
                                onValueChange = { input -> 
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) partnerPrice = cleaned
                                },
                                label = { Text("قیمت همکار") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { input -> 
                                val cleaned = input.cleanNumber()
                                if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                    purchasePrice = cleaned
                                }
                            },
                            label = { Text("قیمت خرید (تومان) - اختیاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = currentFontFamily),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { input ->
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                        stock = cleaned
                                    }
                                },
                                label = { Text("موجودی") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = minStock,
                                onValueChange = { input ->
                                    val cleaned = input.cleanNumber()
                                    if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null) {
                                        minStock = cleaned
                                    }
                                },
                                label = { Text("حدِاقل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation(),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                        }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("انتخاب یا تایپ دسته بندی") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            val filteredCategories = categories.filter { 
                                it.contains(category, ignoreCase = true) && it != "همه" && it != "بدون دسته بندی" 
                            }
                            if (filteredCategories.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    filteredCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {
                                    unit = it
                                    isUnitManuallyEdited = true
                                },
                                label = { Text("واحد") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                textStyle = TextStyle(fontFamily = currentFontFamily),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                units.filter { it.contains(unit, ignoreCase = true) }.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = { 
                                            unit = u
                                            isUnitManuallyEdited = true
                                            expanded = false 
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && price.isNotBlank() && stock.isNotBlank()) {
                                    onConfirm(product.copy(
                                        name = name,
                                        price = price.cleanNumber().toDoubleOrNull() ?: product.price,
                                        wholesalePrice = wholesalePrice.cleanNumber().toDoubleOrNull() ?: product.wholesalePrice,
                                        partnerPrice = partnerPrice.cleanNumber().toDoubleOrNull() ?: product.partnerPrice,
                                        purchasePrice = purchasePrice.cleanNumber().toDoubleOrNull() ?: product.purchasePrice,
                                        stock = stock.cleanNumber().toDoubleOrNull() ?: product.stock,
                                        minStock = minStock.cleanNumber().toDoubleOrNull() ?: product.minStock,
                                        category = category.ifBlank { "بدون دسته بندی" },
                                        unit = unit
                                    ))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("تایید", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("انصراف", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
