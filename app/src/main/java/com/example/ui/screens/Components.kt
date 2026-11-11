package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return "${formatter.format(amount)} ETB"
    }

    fun formatQuantity(quantity: Double, unit: String): String {
        val formatted = if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", quantity)
        }
        return "$formatted$unit"
    }
}

@Composable
fun CategoryBadge(category: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (category.trim().lowercase()) {
        "spices" -> CategorySpices.copy(alpha = 0.15f) to CategorySpices
        "cloth" -> CategoryCloth.copy(alpha = 0.15f) to CategoryCloth
        "garments" -> CategoryGarments.copy(alpha = 0.15f) to CategoryGarments
        "grains" -> CategoryGrains.copy(alpha = 0.15f) to CategoryGrains
        "electronics" -> CategoryElectronics.copy(alpha = 0.15f) to CategoryElectronics
        "cosmetics" -> CategoryCosmetics.copy(alpha = 0.15f) to CategoryCosmetics
        "oils" -> CategoryOils.copy(alpha = 0.15f) to CategoryOils
        else -> CategoryOther.copy(alpha = 0.15f) to CategoryOther
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.trim().lowercase()) {
        "delivered" -> PrimaryGreenLight.copy(alpha = 0.15f) to PrimaryGreenLight
        "in transit" -> SecondaryBlue.copy(alpha = 0.15f) to SecondaryBlue
        "pending" -> CategorySpices.copy(alpha = 0.15f) to CategorySpices // yellow/orange
        else -> OnSurfaceVariantDark.copy(alpha = 0.15f) to OnSurfaceVariantDark
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
