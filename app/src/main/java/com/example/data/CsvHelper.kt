package com.example.data

import java.io.BufferedReader
import java.io.StringReader

object CsvHelper {

    fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    fun parseCsv(csvText: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentField = StringBuilder()
        var inQuotes = false
        var currentLine = mutableListOf<String>()
        
        var i = 0
        val n = csvText.length
        while (i < n) {
            val c = csvText[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < n && csvText[i + 1] == '"') {
                        currentField.append('"')
                        i++ // skip next quote
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    ',' -> {
                        currentLine.add(currentField.toString())
                        currentField = StringBuilder()
                    }
                    '\n', '\r' -> {
                        currentLine.add(currentField.toString())
                        currentField = StringBuilder()
                        if (currentLine.isNotEmpty()) {
                            result.add(currentLine)
                            currentLine = mutableListOf()
                        }
                        if (c == '\r' && i + 1 < n && csvText[i + 1] == '\n') {
                            i++ // skip LF
                        }
                    }
                    '"' -> {
                        inQuotes = true
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
            i++
        }
        if (currentField.isNotEmpty() || currentLine.isNotEmpty()) {
            currentLine.add(currentField.toString())
            result.add(currentLine)
        }
        return result
    }

    fun exportInventoryToCsv(items: List<InventoryItem>): String {
        val sb = StringBuilder()
        sb.append("ID,Name,Category,Unit,BuyPrice,Quantity,DateAdded,Notes\n")
        for (item in items) {
            sb.append("${item.id},")
              .append("${escapeCsv(item.name)},")
              .append("${escapeCsv(item.category)},")
              .append("${escapeCsv(item.unit)},")
              .append("${item.buyPrice},")
              .append("${item.quantity},")
              .append("${escapeCsv(item.dateAdded)},")
              .append("${escapeCsv(item.notes)}\n")
        }
        return sb.toString()
    }

    fun importInventoryFromCsv(csvText: String): List<InventoryItem>? {
        val rows = parseCsv(csvText)
        if (rows.size < 2) return null
        
        val headers = rows[0].map { it.trim().lowercase() }
        val nameIdx = headers.indexOf("name")
        val categoryIdx = headers.indexOf("category")
        val unitIdx = headers.indexOf("unit")
        val buyPriceIdx = headers.indexOf("buyprice")
        val quantityIdx = headers.indexOf("quantity")
        val dateAddedIdx = headers.indexOf("dateadded")
        val notesIdx = headers.indexOf("notes")
        
        if (nameIdx == -1 || categoryIdx == -1 || unitIdx == -1 || buyPriceIdx == -1 || quantityIdx == -1) {
            return null
        }
        
        val items = mutableListOf<InventoryItem>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= nameIdx) continue
            
            val name = row[nameIdx]
            if (name.trim().isEmpty()) continue
            
            val category = row.getOrNull(categoryIdx) ?: "Other"
            val unit = row.getOrNull(unitIdx) ?: "pcs"
            val buyPrice = row.getOrNull(buyPriceIdx)?.toDoubleOrNull() ?: 0.0
            val quantity = row.getOrNull(quantityIdx)?.toDoubleOrNull() ?: 0.0
            val dateAdded = row.getOrNull(dateAddedIdx) ?: "2026-06-29"
            val notes = row.getOrNull(notesIdx) ?: ""
            
            items.add(
                InventoryItem(
                    id = 0,
                    name = name,
                    category = category,
                    unit = unit,
                    buyPrice = buyPrice,
                    quantity = quantity,
                    dateAdded = dateAdded,
                    notes = notes
                )
            )
        }
        return items
    }

    fun exportShipmentsToCsv(shipments: List<Shipment>): String {
        val sb = StringBuilder()
        sb.append("ShipmentID,Date,Status,BuyerName,TransportCost,Commission,ShipmentNotes,ItemName,Quantity,Unit,SellPrice\n")
        for (sh in shipments) {
            val notes = sh.notes
            if (sh.items.isEmpty()) {
                sb.append("${sh.id},")
                  .append("${escapeCsv(sh.date)},")
                  .append("${escapeCsv(sh.status)},")
                  .append("${escapeCsv(sh.buyerName)},")
                  .append("${sh.transportCost},")
                  .append("${sh.commission},")
                  .append("${escapeCsv(notes)},")
                  .append(",,,0.0\n")
            } else {
                for (item in sh.items) {
                    sb.append("${sh.id},")
                      .append("${escapeCsv(sh.date)},")
                      .append("${escapeCsv(sh.status)},")
                      .append("${escapeCsv(sh.buyerName)},")
                      .append("${sh.transportCost},")
                      .append("${sh.commission},")
                      .append("${escapeCsv(notes)},")
                      .append("${escapeCsv(item.itemName)},")
                      .append("${item.quantity},")
                      .append("${escapeCsv(item.unit)},")
                      .append("${item.sellPrice}\n")
                }
            }
        }
        return sb.toString()
    }

    fun importShipmentsFromCsv(csvText: String): List<Shipment>? {
        val rows = parseCsv(csvText)
        if (rows.size < 2) return null
        
        val headers = rows[0].map { it.trim().lowercase() }
        val shIdIdx = headers.indexOf("shipmentid")
        val dateIdx = headers.indexOf("date")
        val statusIdx = headers.indexOf("status")
        val buyerNameIdx = headers.indexOf("buyername")
        val transportIdx = headers.indexOf("transportcost")
        val commissionIdx = headers.indexOf("commission")
        val shNotesIdx = headers.indexOf("shipmentnotes")
        val itemNameIdx = headers.indexOf("itemname")
        val qtyIdx = headers.indexOf("quantity")
        val unitIdx = headers.indexOf("unit")
        val sellPriceIdx = headers.indexOf("sellprice")
        
        if (dateIdx == -1 || statusIdx == -1 || buyerNameIdx == -1 || itemNameIdx == -1 || qtyIdx == -1) {
            return null
        }
        
        data class TempShipmentRow(
            val shipmentId: String,
            val date: String,
            val status: String,
            val buyerName: String,
            val transportCost: Double,
            val commission: Double,
            val notes: String,
            val itemName: String,
            val quantity: Double,
            val unit: String,
            val sellPrice: Double
        )
        
        val tempRows = mutableListOf<TempShipmentRow>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= buyerNameIdx) continue
            
            val date = row[dateIdx]
            if (date.trim().isEmpty()) continue
            
            val shipmentId = row.getOrNull(shIdIdx) ?: ""
            val status = row.getOrNull(statusIdx) ?: "Pending"
            val buyerName = row[buyerNameIdx]
            val transportCost = row.getOrNull(transportIdx)?.toDoubleOrNull() ?: 0.0
            val commission = row.getOrNull(commissionIdx)?.toDoubleOrNull() ?: 0.0
            val notes = row.getOrNull(shNotesIdx) ?: ""
            
            val itemName = row.getOrNull(itemNameIdx) ?: ""
            val quantity = row.getOrNull(qtyIdx)?.toDoubleOrNull() ?: 0.0
            val unit = row.getOrNull(unitIdx) ?: "pcs"
            val sellPrice = row.getOrNull(sellPriceIdx)?.toDoubleOrNull() ?: 0.0
            
            tempRows.add(
                TempShipmentRow(
                    shipmentId = shipmentId,
                    date = date,
                    status = status,
                    buyerName = buyerName,
                    transportCost = transportCost,
                    commission = commission,
                    notes = notes,
                    itemName = itemName,
                    quantity = quantity,
                    unit = unit,
                    sellPrice = sellPrice
                )
            )
        }
        
        val grouped = tempRows.groupBy { row ->
            if (row.shipmentId.isNotEmpty()) row.shipmentId else "${row.buyerName}_${row.date}"
        }
        
        val shipments = mutableListOf<Shipment>()
        for ((_, groupRows) in grouped) {
            val firstRow = groupRows.first()
            
            val items = groupRows.filter { it.itemName.isNotEmpty() && it.quantity > 0.0 }.map { r ->
                ShipmentItem(
                    itemName = r.itemName,
                    quantity = r.quantity,
                    unit = r.unit,
                    sellPrice = r.sellPrice
                )
            }
            
            shipments.add(
                Shipment(
                    id = 0,
                    date = firstRow.date,
                    status = firstRow.status,
                    items = items,
                    transportCost = firstRow.transportCost,
                    commission = firstRow.commission,
                    buyerName = firstRow.buyerName,
                    notes = firstRow.notes
                )
            )
        }
        
        return shipments
    }
}
