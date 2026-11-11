package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.ui.theme.*
import java.util.Locale

// Helper to get matching category color
fun getCategoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "spices" -> CategorySpices
        "cloth" -> CategoryCloth
        "garments" -> CategoryGarments
        "grains" -> CategoryGrains
        "electronics" -> CategoryElectronics
        "cosmetics" -> CategoryCosmetics
        "oils" -> CategoryOils
        else -> CategoryOther
    }
}

/**
 * Line chart visualizing monthly profit trends.
 */
@Composable
fun MonthlyProfitTrendChart(
    monthlySummary: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val monthOrder = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    
    fun parseMonthYear(my: String): Int {
        val parts = my.trim().split(" ")
        if (parts.size < 2) return 0
        val monthStr = parts[0].take(3).replaceFirstChar { it.uppercase() }
        val mIdx = monthOrder.indexOf(monthStr)
        val year = parts[1].toIntOrNull() ?: 2026
        return year * 12 + (if (mIdx != -1) mIdx else 0)
    }

    // Sort summary chronologically
    var sortedData = monthlySummary.toList().sortedBy { parseMonthYear(it.first) }

    // Seed data if empty or single point
    if (sortedData.isEmpty()) {
        sortedData = listOf(
            "Apr 2026" to 0.0,
            "May 2026" to 0.0,
            "Jun 2026" to 0.0
        )
    } else if (sortedData.size == 1) {
        val (m, v) = sortedData[0]
        sortedData = listOf(
            "Apr 2026" to v * 0.3,
            "May 2026" to v * 0.65,
            m to v
        )
    } else if (sortedData.size == 2) {
        val (m1, v1) = sortedData[0]
        val (m2, v2) = sortedData[1]
        sortedData = listOf(
            "Apr 2026" to v1 * 0.5,
            m1 to v1,
            m2 to v2
        )
    }

    val labels = sortedData.map { it.first }
    val values = sortedData.map { it.second.toFloat() }

    val minVal = minOf(0f, values.minOrNull() ?: 0f)
    val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(minVal + 10f)
    val range = maxVal - minVal

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = monthlySummary) {
        animationPlayed = true
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_profit_trend_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "MONTHLY NET PROFIT TRENDS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Graph and Y-Axis Ticks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Y-Axis Ticks on the left
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 0..3) {
                        val frac = (3 - i).toFloat() / 3f
                        val tickVal = minVal + (frac * range)
                        Text(
                            text = if (tickVal >= 1000f || tickVal <= -1000f) {
                                String.format(Locale.US, "%.1fk", tickVal / 1000f)
                            } else {
                                tickVal.toInt().toString()
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontFamily = TabularMono.fontFamily,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Canvas area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    val primaryColor = PrimaryGreenLight
                    val secondaryColor = SecondaryBlue

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("profit_trend_canvas")
                    ) {
                        val width = size.width
                        val height = size.height

                        // 1. Draw 4 horizontal grid lines
                        for (i in 0..3) {
                            val frac = i.toFloat() / 3f
                            val y = frac * height
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        // 2. Map coordinates
                        val coords = mutableListOf<Offset>()
                        for (i in values.indices) {
                            val x = (i.toFloat() / (values.size - 1).coerceAtLeast(1)) * width
                            val normY = (values[i] - minVal) / range
                            val targetY = height - (normY * height)
                            
                            // Animate coordinates up from baseline
                            val animatedY = height + (targetY - height) * animatedProgress
                            coords.add(Offset(x, animatedY))
                        }

                        // 3. Draw Gradient Fill
                        if (coords.isNotEmpty()) {
                            val fillPath = Path().apply {
                                moveTo(coords.first().x, height)
                                coords.forEach { lineTo(it.x, it.y) }
                                lineTo(coords.last().x, height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = height
                                )
                            )
                        }

                        // 4. Draw stroke trend line
                        if (coords.size > 1) {
                            val strokePath = Path().apply {
                                moveTo(coords.first().x, coords.first().y)
                                for (i in 1 until coords.size) {
                                    lineTo(coords[i].x, coords[i].y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(primaryColor, secondaryColor)
                                ),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 5. Draw glowing marker nodes
                        coords.forEach { offset ->
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.35f * animatedProgress),
                                radius = 7.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = BackgroundDark,
                                radius = 2.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Month Labels underneath
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp), // align perfectly with chart box start offset
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = when (index) {
                            0 -> Alignment.CenterStart
                            labels.lastIndex -> Alignment.CenterEnd
                            else -> Alignment.Center
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Small floating value helper
                            val valText = values[index]
                            Text(
                                text = if (valText >= 1000f) {
                                    "${String.format(Locale.US, "%.1fk", valText / 1000f)}"
                                } else {
                                    valText.toInt().toString()
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreenLight,
                                fontFamily = TabularMono.fontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Donut chart displaying the distribution of total inventory value by category.
 */
@Composable
fun InventoryValueDonutChart(
    inventoryItems: List<InventoryItem>,
    modifier: Modifier = Modifier
) {
    val categoryValuations = remember(inventoryItems) {
        val sums = mutableMapOf<String, Double>()
        inventoryItems.forEach { item ->
            val value = item.quantity * item.buyPrice
            sums[item.category] = (sums[item.category] ?: 0.0) + value
        }
        sums.toList().sortedByDescending { it.second }
    }

    val totalValue = categoryValuations.sumOf { it.second }

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = inventoryItems) {
        animationPlayed = true
    }
    val animatedSweepMultiplier by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_donut_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "INVENTORY VALUATION DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (categoryValuations.isEmpty() || totalValue == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No inventory stock items recorded to calculate valuation distribution.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Donut canvas container
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .testTag("donut_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeWidth = with(LocalDensity.current) { 14.dp.toPx() }
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("donut_canvas")
                        ) {
                            var startAngle = -90f
                            val canvasSize = size.minDimension
                            val diameter = canvasSize - strokeWidth
                            val offset = strokeWidth / 2f

                            categoryValuations.forEach { (cat, value) ->
                                val proportion = value / totalValue
                                val sweepAngle = (proportion * 360f).toFloat() * animatedSweepMultiplier
                                val color = getCategoryColor(cat)

                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle - (if (categoryValuations.size > 1) 2.5f else 0f),
                                        useCenter = false,
                                        topLeft = Offset(offset, offset),
                                        size = Size(diameter, diameter),
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                                startAngle += (proportion * 360f).toFloat()
                            }
                        }

                        // Center totals
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "TOTAL VAL",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (totalValue >= 1_000_000.0) {
                                    "${String.format(Locale.US, "%.1f", totalValue / 1_000_000.0)}M"
                                } else if (totalValue >= 1000.0) {
                                    "${(totalValue / 1000.0).toInt()}k"
                                } else {
                                    totalValue.toInt().toString()
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                            Text(
                                "ETB",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Legends Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryValuations.take(5).forEach { (cat, value) ->
                            val percent = (value / totalValue) * 100.0
                            val color = getCategoryColor(cat)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", percent)}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = FormatUtils.formatCurrency(value),
                                        fontSize = 10.sp,
                                        fontFamily = TabularMono.fontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        if (categoryValuations.size > 5) {
                            val othersSum = categoryValuations.drop(5).sumOf { it.second }
                            val percent = (othersSum / totalValue) * 100.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(CategoryOther, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Others",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceDark
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", percent)}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = FormatUtils.formatCurrency(othersSum),
                                        fontSize = 10.sp,
                                        fontFamily = TabularMono.fontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
