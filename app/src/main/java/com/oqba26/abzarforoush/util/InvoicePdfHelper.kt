package com.oqba26.abzarforoush.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.oqba26.abzarforoush.data.Customer
import com.oqba26.abzarforoush.data.DebtTransaction
import com.oqba26.abzarforoush.data.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream

object InvoicePdfHelper {

    fun generateAndShareInvoice(
        context: Context, 
        invoiceWithItems: InvoiceWithItems,
        installments: List<DebtTransaction> = emptyList(),
        shopName: String = "",
        shopPhone: String = "",
        shopAddress: String = "",
        shopTaxId: String = ""
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { strokeWidth = 1f }
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            textAlign = Paint.Align.CENTER
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
        val footerPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        var currentY = 60f

        // Header - Dynamic Shop Name
        val fullTitle = if (shopName.isNotBlank()) "ابزار فروشی $shopName" else "ابزار فروشی"
        canvas.drawText(fullTitle, pageWidth / 2, currentY, titlePaint)
        
        currentY = 110f 

        // Invoice Metadata (Top Right)
        val dateStr = invoiceWithItems.invoice.timestamp.toPersianDateTimeString()
        canvas.drawText("شماره فاکتور: ${invoiceWithItems.invoice.id.toString().toPersianDigits()}", pageWidth - margin, currentY, textPaint)
        currentY += 20f
        canvas.drawText("تاریخ: $dateStr", pageWidth - margin, currentY, textPaint)
        
        if (shopTaxId.isNotBlank()) {
            currentY += 20f
            canvas.drawText("شناسه اقتصادی: ${shopTaxId.toPersianDigits()}", pageWidth - margin, currentY, textPaint)
        }
        
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

        // Total info
        canvas.drawText("جمع کل فاکتور: ${invoiceWithItems.invoice.totalAmount.toPersianPrice()}", pageWidth - margin, currentY, headerPaint)
        currentY += 25f
        canvas.drawText("مبلغ پرداختی: ${invoiceWithItems.invoice.amountPaid.toPersianPrice()}", pageWidth - margin, currentY, textPaint)
        
        val remaining = invoiceWithItems.invoice.totalAmount - invoiceWithItems.invoice.amountPaid
        if (remaining > 0) {
            currentY += 25f
            canvas.drawText("مانده بدهی: ${remaining.toPersianPrice()}", pageWidth - margin, currentY, textPaint.apply { 
                color = Color.RED 
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            })
            textPaint.color = Color.BLACK 
            textPaint.typeface = Typeface.DEFAULT
            
            if (installments.isNotEmpty()) {
                currentY += 40f
                canvas.drawText("جزئیات اقساط:", pageWidth - margin, currentY, headerPaint)
                currentY += 20f
                installments.forEach { inst ->
                    val status = if (inst.isPaid) "✅ وصول" else "⏳ معوق"
                    val line = "${inst.amount.toPersianPrice()} - سررسید: ${inst.dueDate?.toPersianDateString() ?: "-"} ($status)"
                    canvas.drawText(line, pageWidth - margin, currentY, textPaint.apply { textSize = 10f })
                    currentY += 18f
                }
            }
        }

        // --- FOOTER SECTION (Shop Info at Bottom Right) ---
        currentY = pageInfo.pageHeight - 160f
        
        if (shopAddress.isNotBlank()) {
            canvas.drawText("آدرس فروشگاه: $shopAddress", pageWidth - margin, currentY, footerPaint)
            currentY += 18f
        }
        if (shopPhone.isNotBlank()) {
            canvas.drawText("شماره تماس فروشگاه: ${shopPhone.toPersianDigits()}", pageWidth - margin, currentY, footerPaint)
        }

        // Signature Line
        currentY = pageInfo.pageHeight - 80f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 30f
        canvas.drawText("مهر و امضای فروشگاه", pageWidth - margin - 80f, currentY, headerPaint)
        canvas.drawText("امضای خریدار", margin + 80f, currentY, headerPaint)

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

    fun generateAndShareCustomerStatement(
        context: Context,
        customer: Customer,
        invoices: List<InvoiceWithItems>,
        allTransactions: List<DebtTransaction>,
        shopName: String = "",
        shopPhone: String = "",
        shopAddress: String = ""
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { strokeWidth = 1f }
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val textPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }
        val footerPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        var currentY = 60f

        // Header
        val fullTitle = if (shopName.isNotBlank()) "ابزار فروشی $shopName" else "ابزار فروشی"
        canvas.drawText(fullTitle, pageWidth / 2, currentY, titlePaint)
        
        currentY = 110f
        canvas.drawText("ریزحساب مشتری: ${customer.name}", pageWidth - margin, currentY, headerPaint)
        currentY += 20f
        canvas.drawText("تلفن مشتری: ${customer.phoneNumber?.toPersianDigits() ?: "-"}", pageWidth - margin, currentY, textPaint)
        currentY += 25f
        canvas.drawText("گزارش تهیه شده در: ${System.currentTimeMillis().toPersianDateTimeString()}", pageWidth - margin, currentY, textPaint)
        currentY += 35f

        // Table Headers
        val colDate = margin + 140f
        val colAmount = margin + 80f
        val colStatus = margin + 20f
        
        canvas.drawText("شرح (فاکتور / قسط)", pageWidth - margin, currentY, headerPaint)
        canvas.drawText("تاریخ", colDate + 40f, currentY, headerPaint)
        canvas.drawText("مبلغ", colAmount + 40f, currentY, headerPaint)
        canvas.drawText("وضعیت", colStatus + 40f, currentY, headerPaint)
        
        currentY += 10f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 25f

        // Combined Rows
        invoices.forEach { group ->
            val inv = group.invoice
            canvas.drawText("فاکتور #${inv.id}", pageWidth - margin, currentY, textPaint.apply { typeface = Typeface.DEFAULT_BOLD })
            canvas.drawText(inv.timestamp.toPersianDateString(), colDate + 40f, currentY, textPaint)
            canvas.drawText(inv.totalAmount.toPersianPrice(), colAmount + 40f, currentY, textPaint)
            canvas.drawText(if (inv.amountPaid >= inv.totalAmount) "تسویه" else "مانده‌دار", colStatus + 40f, currentY, textPaint)
            currentY += 20f
            
            val invInstallments = allTransactions.filter { it.invoiceId == inv.id }
            invInstallments.forEach { inst ->
                canvas.drawText("  - ${inst.description ?: "قسط"}", pageWidth - margin, currentY, textPaint.apply { typeface = Typeface.DEFAULT; textSize = 9f })
                canvas.drawText(inst.dueDate?.toPersianDateString() ?: "-", colDate + 40f, currentY, textPaint)
                canvas.drawText(inst.amount.toPersianPrice(), colAmount + 40f, currentY, textPaint)
                canvas.drawText(if (inst.isPaid) "✅ وصول" else "⏳ معوق", colStatus + 40f, currentY, textPaint)
                currentY += 15f
            }
            currentY += 10f
            textPaint.textSize = 10f
            
            if (currentY > 700) return@forEach // Simple limit
        }

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 30f

        val totalPurchased = invoices.sumOf { it.invoice.totalAmount }
        val totalPaid = invoices.sumOf { it.invoice.amountPaid }
        val balance = totalPurchased - totalPaid

        canvas.drawText("مجموع کل خریدها: ${totalPurchased.toPersianPrice()}", pageWidth - margin, currentY, headerPaint)
        currentY += 25f
        canvas.drawText("مجموع کل پرداختی‌ها: ${totalPaid.toPersianPrice()}", pageWidth - margin, currentY, textPaint)
        currentY += 25f
        canvas.drawText("مانده بدهی نهایی: ${balance.toPersianPrice()}", pageWidth - margin, currentY, headerPaint.apply { 
            color = if (balance > 0) Color.RED else Color.BLACK
        })

        // --- FOOTER (Shop Info) ---
        currentY = pageInfo.pageHeight - 100f
        if (shopAddress.isNotBlank()) {
            canvas.drawText("آدرس فروشگاه: $shopAddress", pageWidth - margin, currentY, footerPaint)
            currentY += 18f
        }
        if (shopPhone.isNotBlank()) {
            canvas.drawText("شماره تماس فروشگاه: ${shopPhone.toPersianDigits()}", pageWidth - margin, currentY, footerPaint)
        }

        pdfDocument.finishPage(page)

        val fileName = "Statement_${customer.name.replace(" ", "_")}.pdf"
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
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری گزارش PDF"))
    }
}
