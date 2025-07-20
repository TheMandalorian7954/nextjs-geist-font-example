package com.businesscardscanner

import android.content.Context
import android.os.Environment
import android.widget.Toast
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {
    
    fun exportCards(context: Context, cards: List<BusinessCard>, callback: (Boolean, String) -> Unit) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Business Cards")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("Name", "Title", "Company", "Phone", "Email", "Address", "Date Added")
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                
                // Style header
                val font = workbook.createFont()
                font.bold = true
                val style = workbook.createCellStyle()
                style.setFont(font)
                style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                style.fillPattern = FillPatternType.SOLID_FOREGROUND
                cell.cellStyle = style
            }
            
            // Add data rows
            cards.forEachIndexed { index, card ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(card.name)
                row.createCell(1).setCellValue(card.title)
                row.createCell(2).setCellValue(card.company)
                row.createCell(3).setCellValue(card.phone)
                row.createCell(4).setCellValue(card.email)
                row.createCell(5).setCellValue(card.address)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dateString = dateFormat.format(Date(card.createdAt))
                row.createCell(6).setCellValue(dateString)
            }
            
            // Auto-size columns
            for (i in 0 until headers.size) {
                sheet.autoSizeColumn(i)
            }
            
            // Save file
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "BusinessCards_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
            val file = File(downloadsDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            
            workbook.close()
            callback(true, file.absolutePath)
            
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, e.message ?: "Unknown error")
        }
    }
}
