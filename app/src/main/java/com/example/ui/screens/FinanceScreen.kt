package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.data.Shipment
import com.example.ui.TradeViewModel
import com.example.ui.theme.*

@Composable
fun FinanceScreen(
    viewModel: TradeViewModel
) {
    val totalCommission by viewModel.totalCommissionEarned.collectAsState()
    val totalTransport by viewModel.totalTransportCost.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val roi by viewModel.roiPercentage.collectAsState()

    val shipments by viewModel.allShipments.collectAsState()
    val inventoryItems by viewModel.allInventoryItems.collectAsState()

    // 1. Direct Sales = Sum of items quantity * sellPrice
    val totalDirectSales = shipments.sumOf { sh ->
        sh.items.sumOf { it.quantity * it.sellPrice }
    }

    // 2. Per-category profit calculation
    // Match shipped items to find corresponding buy price and calculate: profit = quantity * (sellPrice - buyPrice)
    val categoryProfits = remember(shipments, inventoryItems) {
        val profits = mutableMapOf<String, Double>()
        shipments.forEach { sh ->
            sh.items.forEach { sItem ->
                val matchedInvItem = inventoryItems.find { 
                    it.name.trim().lowercase() == sItem.itemName.trim().lowercase() 
                }
                val buyPrice = matchedInvItem?.buyPrice ?: 0.0
                val profit = sItem.quantity * (sItem.sellPrice - buyPrice)
                val cat = matchedInvItem?.category ?: "Other"
                profits[cat] = (profits[cat] ?: 0.0) + profit
            }
        }
        profits
    }

    // 3. Group shipments by month (e.g. "Jun 2026")
    val monthlySummary = remember(shipments) {
        val summaries = mutableMapOf<String, Double>()
        shipments.forEach { sh ->
            // Try extracting month from format like "27 Jun 2026" or "June 28, 2026"
            val parts = sh.date.trim().split(" ")
            val monthYear = if (parts.size >= 3) {
                "${parts[1]} ${parts[2]}"
            } else {
                "Jun 2026" // default fallback
            }
            summaries[monthYear] = (summaries[monthYear] ?: 0.0) + sh.netProfit
        }
        summaries
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 160.dp, top = 16.dp)
    ) {
        // Net Profit Header Hero
        item {
            val netProfitColor = if (netProfit >= 0) PrimaryGreenLight else MaterialTheme.colorScheme.error
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NET PROFIT (JUNE 2026)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatCurrency(netProfit),
                            style = MaterialTheme.typography.displayLarge,
                            color = netProfitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = netProfitColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (netProfit >= 0) "+12.4% from last month" else "-5% decrease",
                            color = netProfitColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Monthly Profit Trend Line Graph
        item {
            MonthlyProfitTrendChart(monthlySummary = monthlySummary)
        }

        // ROI percentage vs total stock invested
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ROI PERCENTAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", roi)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (roi.toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Capital Efficiency: " + if (roi > 20.0) "High" else "Moderate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Income vs Expenses detailed breakdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income breakdown Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryGreenLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INCOME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Total Commission", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(totalCommission), style = TabularMono.copy(fontSize = 12.sp), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Direct Sales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(totalDirectSales), style = TabularMono.copy(fontSize = 12.sp), fontWeight = FontWeight.Bold)
                    }
                }

                // Expenses breakdown Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPENSES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Transport Costs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(totalTransport), style = TabularMono.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Warehouse Fees", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("15,000.00 ETB", style = TabularMono.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Inventory Value Distribution Donut Chart
        item {
            InventoryValueDonutChart(inventoryItems = inventoryItems)
        }

        // Per-category profit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROFITS BY CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (categoryProfits.isEmpty()) {
                        Text(
                            text = "No shipments made yet to track profit breakdown.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categoryProfits.forEach { (cat, profit) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CategoryBadge(category = cat)
                                Text(
                                    text = FormatUtils.formatCurrency(profit),
                                    style = TabularMono,
                                    color = if (profit >= 0) PrimaryGreenLight else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Per-shipment profit list breakdown
        item {
            Text(
                text = "Shipment Profit Breakdown List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (shipments.isEmpty()) {
            item {
                Text(
                    text = "No shipment data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(shipments) { sh ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SHIPMENT #${sh.id.toString().padStart(4, '0')}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Buyer: ${sh.buyerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(FormatUtils.formatCurrency(sh.netProfit), style = TabularMono, color = PrimaryGreenLight, fontWeight = FontWeight.Bold)
                            val shROI = if (sh.transportCost == 0.0) 100.0 else (sh.netProfit / sh.transportCost) * 100.0
                            Text("Margin: ${String.format("%.1f", shROI)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Monthly Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONTHLY PROFIT AGGREGATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (monthlySummary.isEmpty()) {
                        Text(
                            text = "No monthly data captured yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        monthlySummary.forEach { (month, profit) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = month.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(profit),
                                    style = TabularMono,
                                    color = if (profit >= 0) PrimaryGreenLight else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
