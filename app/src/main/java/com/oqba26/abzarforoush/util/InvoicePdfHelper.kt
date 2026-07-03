package com.oqba26.abzarforoush.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.oqba26.abzarforoush.data.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object InvoicePdfHelper {

    fun generateAndShareInvoice(
        context: Context, 
        invoiceWithItems: InvoiceWithItems,
        shopName: String = "",
        shopPhone: String = "",
        shopAddress: String = "",
        shopTaxId: String = ""
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        val infoPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.LEFT
        }
        val textPaint = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        var currentY = 50f

        // Header / Shop Info
        if (shopName.isNotBlank()) {
            canvas.drawText(shopName, pageWidth / 2, currentY, titlePaint)
        } else {
            canvas.drawText("فاکتور فروش ابزارفروشی", pageWidth / 2, currentY, titlePaint)
        }
        currentY += 30f
        
        if (shopAddress.isNotBlank()) {
            canvas.drawText("آدرس: $shopAddress", margin, currentY, infoPaint)
            currentY += 15f
        }
        if (shopPhone.isNotBlank()) {
            canvas.drawText("تلفن: ${shopPhone.toPersianDigits()}", margin, currentY, infoPaint)
            currentY += 15f
        }
        if (shopTaxId.isNotBlank()) {
            canvas.drawText("شناسه اقتصادی: ${shopTaxId.toPersianDigits()}", margin, currentY, infoPaint)
            currentY += 15f
        }

        currentY = 110f // Reset Y for invoice metadata

        // Invoice Info
        val dateStr = invoiceWithItems.invoice.timestamp.toPersianDateTimeString()
        canvas.drawText("شماره فاکتور: ${invoiceWithItems.invoice.id.toString().toPersianDigits()}", pageWidth - margin, currentY, textPaint)
        currentY += 20f
        canvas.drawText("تاریخ: $dateStr", pageWidth - margin, currentY, textPaint)
        currentY += 40f

        // Table Header
        val colPrice = margin + 50f
        val colQty = margin + 150f
        val colName = pageWidth - margin - 50f
        
        canvas.drawText("ردیف", pageWidth - margin, currentY, headerPaint)
        canvas.drawText("نام کالا", colName, currentY, headerPaint)
        canvas.drawText("تعداد", colQty, currentY, headerPaint)
        canvas.drawText("قیمت واحد", colPrice + 50f, currentY, headerPaint)
        canvas.drawText("جمع", margin + 40f, currentY, headerPaint)
        
        currentY += 10f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 25f

        // Items
        invoiceWithItems.items.forEachIndexed { index, item ->
            val rowNum = (index + 1).toString().toPersianDigits()
            canvas.drawText(rowNum, pageWidth - margin, currentY, textPaint)
            canvas.drawText(item.productName, colName, currentY, textPaint)
            canvas.drawText(item.quantity.toPersianNumber(), colQty, currentY, textPaint)
            canvas.drawText(item.priceAtSale.toInt().toString().formatThousandSeparator().toPersianDigits(), colPrice + 50f, currentY, textPaint)
            val total = (item.quantity * item.priceAtSale).toInt().toString().formatThousandSeparator().toPersianDigits()
            canvas.drawText(total, margin + 40f, currentY, textPaint)
            
            currentY += 25f
        }

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 30f

        // Total
        val totalStr = invoiceWithItems.invoice.totalAmount.toPersianPrice()
        canvas.drawText("جمع کل فاکتور: $totalStr", pageWidth - margin, currentY, headerPaint)
        
        currentY += 25f
        val paidStr = invoiceWithItems.invoice.amountPaid.toPersianPrice()
        canvas.drawText("مبلغ پرداختی: $paidStr", pageWidth - margin, currentY, textPaint)
        
        currentY += 25f
        val remaining = invoiceWithItems.invoice.totalAmount - invoiceWithItems.invoice.amountPaid
        if (remaining > 0) {
            canvas.drawText("مانده بدهی: ${remaining.toPersianPrice()}", pageWidth - margin, currentY, textPaint.apply { color = Color.RED })
        }

        pdfDocument.finishPage(page)

        val fileName = "Invoice_${invoiceWithItems.invoice.id}.pdf"
        val file = File(context.cacheDir, fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور PDF"))
    }
}
