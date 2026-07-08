package com.oqba26.abzarforoush.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.data.Supplier
import androidx.compose.material.icons.filled.ShoppingCart
import com.oqba26.abzarforoush.util.toPersianDigits
import com.oqba26.abzarforoush.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(
    viewModel: ProductViewModel, 
    onNavigateBack: () -> Unit,
    onStartPurchase: () -> Unit
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت تامین‌کنندگان") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier")
                    }
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
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            if (suppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هنوز تامین‌کننده‌ای ثبت نشده است.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(suppliers) { supplier ->
                        SupplierItem(
                            supplier = supplier,
                            onEdit = { supplierToEdit = supplier },
                            onDelete = { viewModel.deleteSupplier(supplier) },
                            onPurchase = {
                                viewModel.selectSupplierForCart(supplier.id)
                                onStartPurchase()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addSupplier(name, phone, address)
                showAddDialog = false
            }
        )
    }

    supplierToEdit?.let { supplier ->
        SupplierDialog(
            supplier = supplier,
            onDismiss = { supplierToEdit = null },
            onConfirm = { name, phone, address ->
                viewModel.updateSupplier(supplier.copy(name = name, phoneNumber = phone, address = address))
                supplierToEdit = null
            }
        )
    }
}

@Composable
fun SupplierItem(supplier: Supplier, onEdit: () -> Unit, onDelete: () -> Unit, onPurchase: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = supplier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                supplier.phoneNumber?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(text = it.toPersianDigits(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                if (supplier.totalDebtToSupplier != 0.0) {
                    Text(
                        text = "بدهی ما: ${supplier.totalDebtToSupplier.toPersianPrice()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row {
                IconButton(onClick = onPurchase) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "ثبت خرید", tint = Color(0xFF2E7D32))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun SupplierDialog(
    supplier: Supplier? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }

    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Auto-fill phone number from contacts if name matches
    LaunchedEffect(name) {
        if (name.length > 2 && phone.isBlank()) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val foundPhone = com.oqba26.abzarforoush.util.ContactHelper.getPhoneNumberByName(context, name)
                if (foundPhone != null) {
                    phone = foundPhone
                }
            }
        }
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
                        text = if (supplier == null) "افزودن تامین‌کننده" else "ویرایش تامین‌کننده",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("تلفن") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس") }, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { if (name.isNotBlank()) onConfirm(name, phone, address) },
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
