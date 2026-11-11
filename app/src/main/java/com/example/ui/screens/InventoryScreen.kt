package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import android.net.Uri
import com.example.data.InventoryItem
import com.example.ui.TradeViewModel
import com.example.ui.theme.*

@Composable
fun InventoryScreen(
    viewModel: TradeViewModel
) {
    val items by viewModel.filteredInventoryItems.collectAsState()
    val rawItems by viewModel.allInventoryItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<InventoryItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<InventoryItem?>(null) }

    var isBatchEditMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<Int>() }
    var showBatchPriceDialog by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }

    // Hardcoded unique list of existing categories to filter
    val categories = listOf("All", "Spices", "Cloth", "Garments", "Grains", "Electronics", "Cosmetics", "Oils", "Other")

    Scaffold(
        floatingActionButton = {
            if (!isBatchEditMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(bottom = 80.dp)
                        .testTag("add_item_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = {
            if (isBatchEditMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("batch_edit_actions_bar"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Batch Action",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${selectedItemIds.size} selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.testTag("batch_selected_count_text")
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showBatchPriceDialog = true },
                                enabled = selectedItemIds.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("batch_update_price_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Price", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showBatchCategoryDialog = true },
                                enabled = selectedItemIds.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("batch_update_category_btn")
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Category", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Search items by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = SurfaceDark,
                    focusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Category filter row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryFilter.lowercase() == category.lowercase()
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else SurfaceDark
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    val borderStroke = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(16.dp))
                            .run {
                                if (borderStroke != null) border(borderStroke, RoundedCornerShape(16.dp)) else this
                            }
                            .clickable {
                                viewModel.selectedCategoryFilter.value = category
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("filter_$category")
                    ) {
                        Text(
                            text = category,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Value and counts bar
            val totalValue = items.sumOf { it.quantity * it.buyPrice }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBatchEditMode) "${selectedItemIds.size} selected" else "Showing ${items.size} items",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isBatchEditMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (isBatchEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("inventory_showing_count")
                    )
                    if (!isBatchEditMode) {
                        Text(
                            text = "Valuation: ${FormatUtils.formatCurrency(totalValue)}",
                            style = TabularMono.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBatchEditMode) {
                        TextButton(
                            onClick = {
                                if (selectedItemIds.size == items.size) {
                                    selectedItemIds.clear()
                                } else {
                                    selectedItemIds.clear()
                                    selectedItemIds.addAll(items.map { it.id })
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("batch_select_all_btn")
                        ) {
                            Text(
                                if (selectedItemIds.size == items.size) "Deselect All" else "Select All",
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                isBatchEditMode = false
                                selectedItemIds.clear()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("batch_cancel_btn")
                        ) {
                            Text("Exit", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    } else if (items.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                isBatchEditMode = true
                                selectedItemIds.clear()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("batch_edit_toggle_btn")
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Batch Edit", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Inventory List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matching inventory items found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedCategoryFilter.value = "All"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp)
                ) {
                    items(items) { item ->
                        InventoryItemCard(
                            item = item,
                            isSelectionMode = isBatchEditMode,
                            isSelected = selectedItemIds.contains(item.id),
                            onSelectToggle = {
                                if (selectedItemIds.contains(item.id)) {
                                    selectedItemIds.remove(item.id)
                                } else {
                                    selectedItemIds.add(item.id)
                                }
                            },
                            onClick = { selectedItemForDetail = item }
                        )
                    }
                }
            }
        }
    }

    // Modal to Add New Item
    if (showAddDialog) {
        AddInventoryItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, unit, buyPrice, quantity, notes, imageUri ->
                viewModel.addInventoryItem(
                    InventoryItem(
                        name = name,
                        category = category,
                        unit = unit,
                        buyPrice = buyPrice,
                        quantity = quantity,
                        notes = notes,
                        imageUri = imageUri,
                        dateAdded = "2026-06-28" // default current date
                    )
                )
                showAddDialog = false
            }
        )
    }

    // Dialog for detail and edit / delete option
    if (selectedItemForDetail != null) {
        val currentId = selectedItemForDetail!!.id
        val item = rawItems.find { it.id == currentId } ?: selectedItemForDetail!!
        InventoryDetailDialog(
            item = item,
            onDismiss = { selectedItemForDetail = null },
            onDelete = {
                showDeleteConfirmDialog = item
                selectedItemForDetail = null
            },
            onUpdatePhoto = { newUri ->
                viewModel.updateInventoryItem(item.copy(imageUri = newUri))
            }
        )
    }

    // Confirmation dialog before deleting anything
    if (showDeleteConfirmDialog != null) {
        val item = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Inventory Item", color = MaterialTheme.colorScheme.error) },
            text = { Text("Are you sure you want to delete '${item.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInventoryItem(item)
                        showDeleteConfirmDialog = null
                    },
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    val context = LocalContext.current

    if (showBatchPriceDialog) {
        BatchUpdatePriceDialog(
            selectedCount = selectedItemIds.size,
            onDismiss = { showBatchPriceDialog = false },
            onSave = { newPrice ->
                val itemsToUpdate = rawItems.filter { selectedItemIds.contains(it.id) }
                    .map { it.copy(buyPrice = newPrice) }
                viewModel.updateInventoryItems(itemsToUpdate) {
                    Toast.makeText(context, "Updated price for ${itemsToUpdate.size} items", Toast.LENGTH_SHORT).show()
                }
                showBatchPriceDialog = false
                isBatchEditMode = false
                selectedItemIds.clear()
            }
        )
    }

    if (showBatchCategoryDialog) {
        BatchUpdateCategoryDialog(
            selectedCount = selectedItemIds.size,
            onDismiss = { showBatchCategoryDialog = false },
            onSave = { newCategory ->
                val itemsToUpdate = rawItems.filter { selectedItemIds.contains(it.id) }
                    .map { it.copy(category = newCategory) }
                viewModel.updateInventoryItems(itemsToUpdate) {
                    Toast.makeText(context, "Updated category for ${itemsToUpdate.size} items", Toast.LENGTH_SHORT).show()
                }
                showBatchCategoryDialog = false
                isBatchEditMode = false
                selectedItemIds.clear()
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onClick: () -> Unit
) {
    val isCritical = item.quantity < 10.0
    val quantityColor = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else if (isCritical) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) {
                    onSelectToggle()
                } else {
                    onClick()
                }
            }
            .border(
                border = cardBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f) else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("item_checkbox_${item.id}"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isCritical) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUri != null) {
                        coil.compose.AsyncImage(
                            model = item.imageUri,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryBadge(category = item.category)
                        if (item.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• " + if (item.notes.length > 15) item.notes.take(15) + "..." else item.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Stock: ${FormatUtils.formatQuantity(item.quantity, item.unit)}",
                    style = TabularMono.copy(fontWeight = FontWeight.Bold),
                    color = quantityColor
                )
                Text(
                    text = "Val: ${FormatUtils.formatCurrency(item.totalValue)}",
                    style = TabularMono.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, String, String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Spices") }
    var customCategory by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var buyPriceStr by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }
    var capturedImageUriStr by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                capturedImageUriStr = Uri.fromFile(file).toString()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = com.example.utils.ImageCaptureHelper.createImageFile(context)
                tempPhotoFile = file
                val uri = com.example.utils.ImageCaptureHelper.getUriForFile(context, file)
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take product photos", Toast.LENGTH_LONG).show()
        }
    }

    val triggerPhotoCapture = {
        val permissionCheckResult = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        )
        if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val file = com.example.utils.ImageCaptureHelper.createImageFile(context)
                tempPhotoFile = file
                val uri = com.example.utils.ImageCaptureHelper.getUriForFile(context, file)
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var buyPriceError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Spices", "Cloth", "Garments", "Grains", "Electronics", "Cosmetics", "Oils", "Other")
    val units = listOf("kg", "g", "meter", "piece", "bag", "bundle", "liter", "box", "roll")

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add New Inventory Item",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Name
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.trim().isEmpty()) "Name is required" else null
                        },
                        label = { Text("Item Name") },
                        placeholder = { Text("e.g. Berbere") },
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth().testTag("add_item_name"),
                        singleLine = true
                    )
                    if (nameError != null) {
                        Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Category selection dropdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("add_item_category_field")
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            isCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Unit dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = isUnitDropdownExpanded,
                            onExpandedChange = { isUnitDropdownExpanded = !isUnitDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isUnitDropdownExpanded,
                                onDismissRequest = { isUnitDropdownExpanded = false }
                            ) {
                                units.forEach { un ->
                                    DropdownMenuItem(
                                        text = { Text(un) },
                                        onClick = {
                                            unit = un
                                            isUnitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom category field if "Other" is selected
                if (category == "Other") {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Specify Category") },
                        placeholder = { Text("e.g. Oils") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Prices & Quantities
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = buyPriceStr,
                            onValueChange = {
                                buyPriceStr = it
                                val parsed = it.toDoubleOrNull()
                                buyPriceError = if (parsed == null || parsed <= 0.0) "Must be > 0" else null
                            },
                            label = { Text("Buy Price (ETB)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = buyPriceError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_item_price")
                        )
                        if (buyPriceError != null) {
                            Text(buyPriceError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = {
                                quantityStr = it
                                val parsed = it.toDoubleOrNull()
                                quantityError = if (parsed == null || parsed < 0.0) "Must be >= 0" else null
                            },
                            label = { Text("Quantity") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = quantityError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_item_quantity")
                        )
                        if (quantityError != null) {
                            Text(quantityError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Product Photo Section
                Column {
                    Text(
                        text = "Product Photo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedImageUriStr != null) {
                                coil.compose.AsyncImage(
                                    model = capturedImageUriStr,
                                    contentDescription = "Captured Product Image",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "No photo taken",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (capturedImageUriStr != null) "Photo captured!" else "Take a real photo of this product for visual identification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = triggerPhotoCapture,
                                    modifier = Modifier.height(36.dp).testTag("take_photo_btn"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (capturedImageUriStr != null) "Retake" else "Take Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (capturedImageUriStr != null) {
                                    TextButton(
                                        onClick = { capturedImageUriStr = null },
                                        modifier = Modifier.height(36.dp),
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. supplier, batch info") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCategory = if (category == "Other" && customCategory.isNotEmpty()) customCategory else category
                    val price = buyPriceStr.toDoubleOrNull()
                    val qty = quantityStr.toDoubleOrNull()

                    if (name.trim().isEmpty()) {
                        nameError = "Name is required"
                    }
                    if (price == null || price <= 0.0) {
                        buyPriceError = "Must be > 0"
                    }
                    if (qty == null || qty < 0.0) {
                        quantityError = "Must be >= 0"
                    }

                    if (name.trim().isNotEmpty() && price != null && price > 0.0 && qty != null && qty >= 0.0) {
                        onSave(name, finalCategory, unit, price, qty, notes, capturedImageUriStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("save_item_btn")
            ) {
                Text("Save to Inventory")
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
fun InventoryDetailDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePhoto: (String?) -> Unit
) {
    val context = LocalContext.current
    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                onUpdatePhoto(Uri.fromFile(file).toString())
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = com.example.utils.ImageCaptureHelper.createImageFile(context)
                tempPhotoFile = file
                val uri = com.example.utils.ImageCaptureHelper.getUriForFile(context, file)
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take product photos", Toast.LENGTH_LONG).show()
        }
    }

    val triggerPhotoCapture = {
        val permissionCheckResult = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        )
        if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val file = com.example.utils.ImageCaptureHelper.createImageFile(context)
                tempPhotoFile = file
                val uri = com.example.utils.ImageCaptureHelper.getUriForFile(context, file)
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Large product image if it exists, or placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUri != null) {
                        coil.compose.AsyncImage(
                            model = item.imageUri,
                            contentDescription = "${item.name} image",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Photo Available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Photo actions: Take Photo / Retake Photo / Remove Photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = triggerPhotoCapture,
                        modifier = Modifier.height(36.dp).testTag("detail_take_photo_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (item.imageUri != null) Icons.Default.Refresh else Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.imageUri != null) "Retake Photo" else "Take Photo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (item.imageUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onUpdatePhoto(null) },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category = item.category)
                    Text(
                        text = "Added: ${item.dateAdded}",
                        style = TabularMono.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Buy Price: ${FormatUtils.formatCurrency(item.buyPrice)} per ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Stock Balance: ${FormatUtils.formatQuantity(item.quantity, item.unit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.quantity < 10.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Valuation: ${FormatUtils.formatCurrency(item.totalValue)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notes:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun BatchUpdatePriceDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var priceStr by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Batch Update Price",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Update the Buy Price for $selectedCount selected items.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = {
                        priceStr = it
                        val parsed = it.toDoubleOrNull()
                        priceError = if (parsed == null || parsed <= 0.0) "Must be a valid price > 0" else null
                    },
                    label = { Text("New Buy Price (ETB)") },
                    placeholder = { Text("e.g. 150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("batch_price_input")
                )
                if (priceError != null) {
                    Text(priceError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    if (price == null || price <= 0.0) {
                        priceError = "Must be a valid price > 0"
                    } else {
                        onSave(price)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("batch_price_save_btn")
            ) {
                Text("Apply Price")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchUpdateCategoryDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var category by remember { mutableStateOf("Spices") }
    var customCategory by remember { mutableStateOf("") }
    val categories = listOf("Spices", "Cloth", "Garments", "Grains", "Electronics", "Cosmetics", "Oils", "Other")
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Batch Update Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Update the Category for $selectedCount selected items.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("batch_category_select_field")
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (category == "Other") {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Specify Category") },
                        placeholder = { Text("e.g. Oils") },
                        modifier = Modifier.fillMaxWidth().testTag("batch_category_custom_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCategory = if (category == "Other" && customCategory.isNotEmpty()) customCategory else category
                    onSave(finalCategory)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("batch_category_save_btn")
            ) {
                Text("Apply Category")
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
