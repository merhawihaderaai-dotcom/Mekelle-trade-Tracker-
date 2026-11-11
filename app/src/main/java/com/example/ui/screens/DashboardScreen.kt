package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Shipment
import com.example.ui.TradeViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: TradeViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val stockValue by viewModel.totalStockValue.collectAsState()
    val commissionEarned by viewModel.totalCommissionEarned.collectAsState()
    val transportCost by viewModel.totalTransportCost.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()

    val inventoryItems by viewModel.allInventoryItems.collectAsState()
    val shipments by viewModel.allShipments.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val totalProductsCount = inventoryItems.size
    val totalShipmentsCount = shipments.size
    val deliveredShipmentsCount = shipments.count { it.status == "Delivered" }

    // Group items by category to get breakdown count
    val categoryBreakdown = inventoryItems.groupBy { it.category }
        .mapValues { it.value.size }

    // Take 3 most recent shipments
    val recentShipments = shipments.take(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Hero banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = com.example.utils.TranslationHelper.translate("Addis Spice Hub Dashboard", appLanguage),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.utils.TranslationHelper.translate("Region: Mekelle & Addis Ababa Ecosystem", appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Finance Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = com.example.utils.TranslationHelper.translate("Financial Summary", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinanceSummaryCard(
                        title = com.example.utils.TranslationHelper.translate("Total Stock Value", appLanguage),
                        value = stockValue,
                        icon = Icons.Default.Inventory,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceSummaryCard(
                        title = com.example.utils.TranslationHelper.translate("Total Commission", appLanguage),
                        value = commissionEarned,
                        icon = Icons.Default.Payments,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinanceSummaryCard(
                        title = com.example.utils.TranslationHelper.translate("Total Transport Cost", appLanguage),
                        value = transportCost,
                        icon = Icons.Default.LocalShipping,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceSummaryCard(
                        title = com.example.utils.TranslationHelper.translate("Net Profit", appLanguage),
                        value = netProfit,
                        icon = Icons.Default.TrendingUp,
                        tint = if (netProfit >= 0) PrimaryGreenLight else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Row of 3 counters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CounterCard(
                    label = "Total Products",
                    count = totalProductsCount,
                    icon = Icons.Default.Category,
                    modifier = Modifier.weight(1f)
                )
                CounterCard(
                    label = "Shipments",
                    count = totalShipmentsCount,
                    icon = Icons.Default.LocalShipping,
                    modifier = Modifier.weight(1f)
                )
                CounterCard(
                    label = "Delivered",
                    count = deliveredShipmentsCount,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Stock Highlights by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No items in stock. Add some in the Inventory tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categoryBreakdown.forEach { (category, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = when (category.lowercase()) {
                                                    "spices" -> CategorySpices
                                                    "cloth" -> CategoryCloth
                                                    "garments" -> CategoryGarments
                                                    "grains" -> CategoryGrains
                                                    "electronics" -> CategoryElectronics
                                                    else -> CategoryOther
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "$count item" + (if (count > 1) "s" else ""),
                                    style = TabularMono,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent shipments list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Shipments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigateToTab("Shipments") },
                    modifier = Modifier.testTag("view_all_shipments_btn")
                ) {
                    Text("View All")
                }
            }
        }

        if (recentShipments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Empty Shipments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No shipments registered yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentShipments) { shipment ->
                RecentShipmentRow(shipment = shipment, onClick = { onNavigateToTab("Shipments") })
            }
        }
    }
}

@Composable
fun FinanceSummaryCard(
    title: String,
    value: Double,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = FormatUtils.formatCurrency(value),
                style = TabularMono.copy(fontSize = 16.sp),
                color = tint
            )
        }
    }
}

@Composable
fun CounterCard(
    label: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentShipmentRow(
    shipment: Shipment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val summaryText = shipment.items.joinToString(" + ") { "${it.quantity.toInt()}${it.unit} ${it.itemName}" }
                    Text(
                        text = if (summaryText.length > 25) summaryText.take(25) + "..." else summaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Buyer: ${shipment.buyerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = shipment.date,
                        style = TabularMono.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusBadge(status = shipment.status)
        }
    }
}
