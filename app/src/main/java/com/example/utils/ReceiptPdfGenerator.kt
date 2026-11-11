package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Shipment
import com.example.ui.screens.FormatUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ReceiptPdfGenerator {

    fun generateAndShareReceipt(context: Context, shipment: Shipment) {
        try {
            val file = generateReceiptPdf(context, shipment)
            if (file != null && file.exists()) {
                sharePdfFile(context, file)
            } else {
                Toast.makeText(context, "Failed to generate receipt PDF.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateReceiptPdf(context: Context, shipment: Shipment): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Setup Paints
        val paintPrimary = Paint().apply {
            color = 0xFF1E293B.toInt() // Slate 800
            isAntiAlias = true
        }
        val paintAccent = Paint().apply {
            color = 0xFF10B981.toInt() // Emerald 500
            isAntiAlias = true
        }
        val paintTextDark = Paint().apply {
            color = 0xFF0F172A.toInt() // Slate 900
            textSize = 10f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintTextBold = Paint().apply {
            color = 0xFF0F172A.toInt() // Slate 900
            textSize = 10f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintTextMuted = Paint().apply {
            color = 0xFF64748B.toInt() // Slate 500
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintLightGray = Paint().apply {
            color = 0xFFF1F5F9.toInt() // Slate 100
            isAntiAlias = true
        }
        val paintBorder = Paint().apply {
            color = 0xFFE2E8F0.toInt() // Slate 200
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // --- 1. Header Block ---
        // Navy top bar
        canvas.drawRect(0f, 0f, 595f, 90f, paintPrimary)

        // Title and branding
        val paintHeaderTitle = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintHeaderSubtitle = Paint().apply {
            color = 0xFF94A3B8.toInt() // Slate 400
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("AST COMMISSION TRADE", 40f, 40f, paintHeaderTitle)
        canvas.drawText("Official Shipment Receipt & Commercial Invoice", 40f, 56f, paintHeaderSubtitle)

        // Date and Invoice Number
        val paintHeaderMeta = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val paintHeaderMetaBold = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("INVOICE NO: #SH-${shipment.id.toString().padStart(4, '0')}", 555f, 40f, paintHeaderMetaBold)
        canvas.drawText("Date: ${shipment.date}", 555f, 56f, paintHeaderMeta)
        canvas.drawText("Status: ${shipment.status.uppercase()}", 555f, 72f, paintHeaderMeta)

        // --- 2. Customer / Shipment Meta section ---
        var currentY = 120f
        canvas.drawText("BILLED TO / BUYER DETAILS:", 40f, currentY, paintTextMuted)
        currentY += 18f
        paintTextBold.textSize = 13f
        canvas.drawText(shipment.buyerName, 40f, currentY, paintTextBold)
        paintTextBold.textSize = 10f // Reset

        // Draw a neat horizontal divider
        currentY += 15f
        canvas.drawLine(40f, currentY, 555f, currentY, paintBorder)

        // --- 3. Items Table Header ---
        currentY += 15f
        // Draw Table Header Background
        canvas.drawRect(40f, currentY, 555f, currentY + 24f, paintLightGray)

        val headerY = currentY + 16f
        canvas.drawText("S.No", 50f, headerY, paintTextBold)
        canvas.drawText("Item Description", 100f, headerY, paintTextBold)
        canvas.drawText("Qty / Unit", 300f, headerY, paintTextBold)
        canvas.drawText("Unit Price", 400f, headerY, paintTextBold)
        
        val paintRightAlignBold = Paint(paintTextBold).apply { textAlign = Paint.Align.RIGHT }
        val paintRightAlignNormal = Paint(paintTextDark).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("Total Value", 545f, headerY, paintRightAlignBold)

        currentY += 24f

        // --- 4. Items Table Rows ---
        var subTotalVal = 0.0
        shipment.items.forEachIndexed { idx, item ->
            currentY += 22f
            val itemTotal = item.quantity * item.sellPrice
            subTotalVal += itemTotal

            canvas.drawText((idx + 1).toString(), 50f, currentY, paintTextDark)
            
            val truncatedName = if (item.itemName.length > 25) {
                item.itemName.take(22) + "..."
            } else {
                item.itemName
            }
            canvas.drawText(truncatedName, 100f, currentY, paintTextDark)
            canvas.drawText(FormatUtils.formatQuantity(item.quantity, item.unit), 300f, currentY, paintTextDark)
            canvas.drawText(FormatUtils.formatCurrency(item.sellPrice), 400f, currentY, paintTextDark)
            canvas.drawText(FormatUtils.formatCurrency(itemTotal), 545f, currentY, paintRightAlignNormal)

            // Row bottom light gridline
            canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, paintBorder)
            currentY += 6f
        }

        // --- 5. Financial Summary Box ---
        currentY += 25f
        val boxTop = currentY
        val boxWidth = 220f
        val boxLeft = 555f - boxWidth

        // Draw a border and background for financial summary
        val paintSummaryBg = Paint().apply {
            color = 0xFFF8FAFC.toInt() // Slate 50
            isAntiAlias = true
        }
        canvas.drawRect(boxLeft, boxTop, 555f, boxTop + 110f, paintSummaryBg)
        canvas.drawRect(boxLeft, boxTop, 555f, boxTop + 110f, paintBorder)

        var summaryY = boxTop + 18f
        canvas.drawText("Total Cargo Value:", boxLeft + 12f, summaryY, paintTextDark)
        canvas.drawText(FormatUtils.formatCurrency(subTotalVal), 543f, summaryY, paintRightAlignNormal)

        summaryY += 22f
        canvas.drawText("Commission Earned:", boxLeft + 12f, summaryY, paintTextDark)
        val paintCommissionText = Paint(paintRightAlignNormal).apply { color = 0xFF10B981.toInt() } // Green for earnings
        canvas.drawText(FormatUtils.formatCurrency(shipment.commission), 543f, summaryY, paintCommissionText)

        summaryY += 22f
        canvas.drawText("Transport Cost:", boxLeft + 12f, summaryY, paintTextDark)
        val paintTransportText = Paint(paintRightAlignNormal).apply { color = 0xFFEF4444.toInt() } // Red for costs
        canvas.drawText("- " + FormatUtils.formatCurrency(shipment.transportCost), 543f, summaryY, paintTransportText)

        // Draw divider before Net Profit
        summaryY += 10f
        canvas.drawLine(boxLeft + 10f, summaryY, 545f, summaryY, paintBorder)

        summaryY += 18f
        val paintNetProfitLabel = Paint(paintTextBold).apply {
            textSize = 11f
            color = 0xFF1E293B.toInt()
        }
        canvas.drawText("Net Profit / Income:", boxLeft + 12f, summaryY, paintNetProfitLabel)

        val paintNetProfitVal = Paint(paintRightAlignBold).apply {
            textSize = 11f
            color = 0xFF10B981.toInt() // Emerald
        }
        canvas.drawText(FormatUtils.formatCurrency(shipment.netProfit), 543f, summaryY, paintNetProfitVal)

        // --- 6. Notes section on left of summary box ---
        if (shipment.notes.isNotEmpty()) {
            val notesLeft = 40f
            val notesWidth = boxLeft - 60f
            var notesY = boxTop + 14f
            canvas.drawText("SHIPMENT REMARKS / NOTES:", notesLeft, notesY, paintTextMuted)
            notesY += 16f
            
            // Simple word-wrapping logic for notes text
            val words = shipment.notes.split(" ")
            var line = ""
            val maxCharPerLine = 35
            for (word in words) {
                if ((line + word).length > maxCharPerLine) {
                    canvas.drawText(line.trim(), notesLeft, notesY, paintTextDark)
                    notesY += 14f
                    line = "$word "
                } else {
                    line += "$word "
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.trim(), notesLeft, notesY, paintTextDark)
            }
        }

        // --- 7. Signature Areas ---
        var sigY = 720f
        canvas.drawLine(40f, sigY, 200f, sigY, paintBorder)
        canvas.drawLine(395f, sigY, 555f, sigY, paintBorder)
        
        sigY += 14f
        canvas.drawText("Authorized Signature", 60f, sigY, paintTextMuted)
        canvas.drawText("Buyer Acknowledgement", 415f, sigY, paintTextMuted)

        // --- 8. Footer Block ---
        val footerY = 790f
        canvas.drawLine(40f, footerY - 10f, 555f, footerY - 10f, paintBorder)
        
        val paintFooter = Paint().apply {
            color = 0xFF94A3B8.toInt()
            textSize = 8f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for choosing AST Commission Trade Management System.", 297f, footerY, paintFooter)
        canvas.drawText("Document generated automatically by Commission Trade Android Client.", 297f, footerY + 12f, paintFooter)

        pdfDocument.finishPage(page)

        // Save PDF to App Cache directory
        val cacheFile = File(context.cacheDir, "AST_Shipment_Receipt_${shipment.id}.pdf")
        return try {
            val fos = FileOutputStream(cacheFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun sharePdfFile(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "AST Shipment Receipt #${file.name}")
            putExtra(Intent.EXTRA_TEXT, "Hello, please find attached the professional PDF receipt for Shipment ID: ${file.name.substringAfterLast("_").substringBeforeLast(".")}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Receipt PDF via")
        // Grant permission to resolve activities
        val resInfoList = context.packageManager.queryIntentActivities(chooser, 0)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(chooser)
    }
}
