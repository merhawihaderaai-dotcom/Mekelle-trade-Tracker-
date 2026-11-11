package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.data.Shipment
import com.example.data.ShipmentItem
import com.example.ui.TradeViewModel
import com.example.ui.theme.*

@Composable
fun ShipmentsScreen(
    viewModel: TradeViewModel
) {
    val shipments by viewModel.allShipments.collectAsState()
    val inventoryItems by viewModel.allInventoryItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedShipmentForDetail by remember { mutableStateOf<Shipment?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_shipment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Shipment", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header title
            Text(
                text = "Shipments Tracking",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Quick Stats summary bar
            val activeCount = shipments.count { it.status == "In Transit" || it.status == "Pending" }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${shipments.size} Total Shipments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$activeCount Active Shipments",
                    style = TabularMono,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Shipments lists
            if (shipments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No shipments registered yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp)
                ) {
                    items(shipments) { shipment ->
                        ShipmentItemCard(
                            shipment = shipment,
                            onClick = { selectedShipmentForDetail = shipment },
                            onMarkInTransit = {
                                viewModel.updateShipment(shipment.copy(status = "In Transit"))
                            },
                            onMarkDelivered = {
                                viewModel.updateShipment(shipment.copy(status = "Delivered"))
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal to create shipment
    if (showAddDialog) {
        CreateShipmentDialog(
            inventoryItems = inventoryItems,
            onDismiss = { showAddDialog = false },
            onSave = { shipment ->
                viewModel.addShipment(shipment) {
                    showAddDialog = false
                }
            }
        )
    }

    // Detail modal view
    if (selectedShipmentForDetail != null) {
        val shipment = selectedShipmentForDetail!!
        ShipmentDetailDialog(
            shipment = shipment,
            onDismiss = { selectedShipmentForDetail = null },
            onMarkInTransit = {
                viewModel.updateShipment(shipment.copy(status = "In Transit"))
                selectedShipmentForDetail = null
            },
            onMarkDelivered = {
                viewModel.updateShipment(shipment.copy(status = "Delivered"))
                selectedShipmentForDetail = null
            }
        )
    }
}

@Composable
fun ShipmentItemCard(
    shipment: Shipment,
    onClick: () -> Unit,
    onMarkInTransit: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Date & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHIPMENT #${shipment.id.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = shipment.date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(status = shipment.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buyer Name
            Text(
                text = "Buyer: ${shipment.buyerName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-item list visualization
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shipment.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${FormatUtils.formatQuantity(item.quantity, item.unit)} ${item.itemName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@ ${FormatUtils.formatCurrency(item.sellPrice)}",
                            style = TabularMono.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats grid (Commission, Transport, Net Profit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("COMMISSION", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FormatUtils.formatCurrency(shipment.commission), style = TabularMono.copy(fontSize = 12.sp))
                }
                Column {
                    Text("TRANSPORT", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FormatUtils.formatCurrency(shipment.transportCost), style = TabularMono.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("NET PROFIT", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FormatUtils.formatCurrency(shipment.netProfit), style = TabularMono.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = PrimaryGreenLight)
                }
            }

            // Quick Actions Buttons for status transitions
            if (shipment.status != "Delivered") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (shipment.status == "Pending") {
                        Button(
                            onClick = onMarkInTransit,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark In Transit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = onMarkDelivered,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShipmentDialog(
    inventoryItems: List<InventoryItem>,
    onDismiss: () -> Unit,
    onSave: (Shipment) -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("28 Jun 2026") }
    var status by remember { mutableStateOf("Pending") }
    var transportCostStr by remember { mutableStateOf("") }
    var commissionStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dynamic list of items added to this shipment
    val addedItems = remember { mutableStateListOf<ShipmentItem>() }

    // Dropdown fields for picking item
    var selectedItemIndex by remember { mutableStateOf(-1) }
    var itemQtyStr by remember { mutableStateOf("") }
    var itemPriceStr by remember { mutableStateOf("") }

    // Errors
    var buyerError by remember { mutableStateOf<String?>(null) }
    var transportError by remember { mutableStateOf<String?>(null) }
    var commissionError by remember { mutableStateOf<String?>(null) }
    var itemError by remember { mutableStateOf<String?>(null) }

    var isItemDropdownExpanded by remember { mutableStateOf(false) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Pending", "In Transit", "Delivered")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Shipment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buyer Name
                item {
                    Column {
                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = {
                                buyerName = it
                                buyerError = if (it.trim().isEmpty()) "Buyer name is required" else null
                            },
                            label = { Text("Buyer Name") },
                            placeholder = { Text("e.g. Mekelle Buyer 1") },
                            isError = buyerError != null,
                            modifier = Modifier.fillMaxWidth().testTag("add_shipment_buyer"),
                            singleLine = true
                        )
                        if (buyerError != null) {
                            Text(buyerError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Date & Status
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            placeholder = { Text("28 Jun 2026") },
                            modifier = Modifier.weight(1.1f),
                            singleLine = true
                        )

                        Box(modifier = Modifier.weight(0.9f)) {
                            ExposedDropdownMenuBox(
                                expanded = isStatusDropdownExpanded,
                                onExpandedChange = { isStatusDropdownExpanded = !isStatusDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = status,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Status") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = isStatusDropdownExpanded,
                                    onDismissRequest = { isStatusDropdownExpanded = false }
                                ) {
                                    statusOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                status = opt
                                                isStatusDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Financials (Transport Cost & Commission)
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = transportCostStr,
                                onValueChange = {
                                    transportCostStr = it
                                    val parsed = it.toDoubleOrNull()
                                    transportError = if (parsed == null || parsed < 0.0) "Must be >= 0" else null
                                },
                                label = { Text("Transport Cost") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = transportError != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_shipment_transport")
                            )
                            if (transportError != null) {
                                  Text(transportError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = commissionStr,
                                onValueChange = {
                                    commissionStr = it
                                    val parsed = it.toDoubleOrNull()
                                    commissionError = if (parsed == null || parsed < 0.0) "Must be >= 0" else null
                                },
                                label = { Text("Commission") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = commissionError != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_shipment_commission")
                            )
                            if (commissionError != null) {
                                  Text(commissionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Shipment Notes") },
                        placeholder = { Text("e.g. carrier name, driver ph#") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Add cargo items section
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Cargo Items to Ship",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Inline form to select an item, input sell price and qty, check constraints
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Item Selector Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                val selectedItemText = if (selectedItemIndex >= 0) inventoryItems[selectedItemIndex].name else "Pick inventory item"
                                ExposedDropdownMenuBox(
                                    expanded = isItemDropdownExpanded,
                                    onExpandedChange = { isItemDropdownExpanded = !isItemDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedItemText,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Inventory Product") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isItemDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("add_shipment_item_select")
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isItemDropdownExpanded,
                                        onDismissRequest = { isItemDropdownExpanded = false }
                                    ) {
                                        inventoryItems.forEachIndexed { idx, item ->
                                            DropdownMenuItem(
                                                text = { Text("${item.name} (${FormatUtils.formatQuantity(item.quantity, item.unit)} left)") },
                                                onClick = {
                                                    selectedItemIndex = idx
                                                    itemPriceStr = item.buyPrice.toString() // prefill buy price
                                                    isItemDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Inline Qty and Sell Price fields
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = itemQtyStr,
                                    onValueChange = {
                                        itemQtyStr = it
                                        itemError = null
                                    },
                                    label = { Text("Qty") },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("add_shipment_item_qty")
                                )

                                OutlinedTextField(
                                    value = itemPriceStr,
                                    onValueChange = { itemPriceStr = it },
                                    label = { Text("Sell Price") },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("add_shipment_item_price")
                                )
                            }

                            // Dynamic available stock checker/warning
                            if (selectedItemIndex >= 0) {
                                val matched = inventoryItems[selectedItemIndex]
                                val userQty = itemQtyStr.toDoubleOrNull() ?: 0.0
                                if (userQty > matched.quantity) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "DANGER: Shipment quantity exceeds available stock (${matched.quantity}${matched.unit} remaining)",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (itemError != null) {
                                Text(itemError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            // Button to Add item line
                            Button(
                                onClick = {
                                    if (selectedItemIndex < 0) {
                                        itemError = "Please select a product first"
                                        return@Button
                                    }
                                    val matched = inventoryItems[selectedItemIndex]
                                    val userQty = itemQtyStr.toDoubleOrNull()
                                    val userPrice = itemPriceStr.toDoubleOrNull()

                                    if (userQty == null || userQty <= 0.0) {
                                        itemError = "Enter valid quantity > 0"
                                        return@Button
                                    }
                                    if (userPrice == null || userPrice <= 0.0) {
                                        itemError = "Enter valid sell price > 0"
                                        return@Button
                                    }

                                    // Add to temporary items list
                                    addedItems.add(
                                        ShipmentItem(
                                            itemName = matched.name,
                                            quantity = userQty,
                                            unit = matched.unit,
                                            sellPrice = userPrice
                                        )
                                    )

                                    // Reset entry fields
                                    itemQtyStr = ""
                                    selectedItemIndex = -1
                                    itemError = null
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_item_line_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Product to Cargo", color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                }

                // Currently added items breakdown visualization
                if (addedItems.isEmpty()) {
                    item {
                        Text(
                            "No cargo items added yet. Pick and add items above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(addedItems) { itm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(4.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(itm.itemName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${FormatUtils.formatQuantity(itm.quantity, itm.unit)} @ ${FormatUtils.formatCurrency(itm.sellPrice)}",
                                    style = TabularMono.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { addedItems.remove(itm) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete line", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Cargo Subtotal Summary
                    item {
                        val subTotalVal = addedItems.sumOf { it.quantity * it.sellPrice }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Cargo Value:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatCurrency(subTotalVal), style = TabularMono, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tc = transportCostStr.toDoubleOrNull()
                    val comm = commissionStr.toDoubleOrNull()

                    if (buyerName.trim().isEmpty()) {
                        buyerError = "Buyer name is required"
                    }
                    if (tc == null || tc < 0.0) {
                        transportError = "Must be >= 0"
                    }
                    if (comm == null || comm < 0.0) {
                        commissionError = "Must be >= 0"
                    }
                    if (addedItems.isEmpty()) {
                        itemError = "Please add at least one cargo item"
                    }

                    if (buyerName.trim().isNotEmpty() && tc != null && tc >= 0.0 && comm != null && comm >= 0.0 && addedItems.isNotEmpty()) {
                        onSave(
                            Shipment(
                                date = date,
                                buyerName = buyerName,
                                status = status,
                                items = addedItems.toList(),
                                transportCost = tc,
                                commission = comm,
                                notes = notes
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("save_shipment_btn")
            ) {
                Text("Create Shipment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun ShipmentDetailDialog(
    shipment: Shipment,
    onDismiss: () -> Unit,
    onMarkInTransit: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Shipment Detail",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "ID: #${shipment.id.toString().padStart(4, '0')}",
                    style = TabularMono.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(shipment.date, style = TabularMono, fontWeight = FontWeight.Bold)
                    StatusBadge(status = shipment.status)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Buyer Name: ${shipment.buyerName}", fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Cargo Content List:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainer, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    shipment.items.forEach { itm ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${FormatUtils.formatQuantity(itm.quantity, itm.unit)} ${itm.itemName}")
                            Text(FormatUtils.formatCurrency(itm.totalValue), style = TabularMono)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Commission Earned:", style = MaterialTheme.typography.bodyMedium)
                    Text(FormatUtils.formatCurrency(shipment.commission), style = TabularMono)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transport Cost:", style = MaterialTheme.typography.bodyMedium)
                    Text(FormatUtils.formatCurrency(shipment.transportCost), style = TabularMono, color = MaterialTheme.colorScheme.error)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Profit:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(FormatUtils.formatCurrency(shipment.netProfit), style = TabularMono, color = PrimaryGreenLight, fontWeight = FontWeight.Bold)
                }

                if (shipment.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Notes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(shipment.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shipment.status != "Delivered") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (shipment.status == "Pending") {
                            Button(
                                onClick = onMarkInTransit,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("In Transit")
                            }
                        }
                        Button(
                            onClick = onMarkDelivered,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delivered")
                        }
                    }
                }
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        com.example.utils.ReceiptPdfGenerator.generateAndShareReceipt(context, shipment)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_pdf_receipt_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share PDF", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share PDF Receipt", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        containerColor = SurfaceDark
    )
}
