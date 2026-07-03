package com.oqba26.abzarforoush.util

import java.text.NumberFormat
import java.util.Locale
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

fun Long.toPersianDateString(pattern: String = "Y/m/d"): String {
    val pDate = PersianDate(this)
    val pDateFormat = PersianDateFormat(pattern)
    return pDateFormat.format(pDate).toPersianDigits()
}

fun Long.toPersianDateTimeString(): String {
    return this.toPersianDateString("Y/m/d H:i")
}

fun Double.toPersianPrice(): String {
    val formatter = NumberFormat.getInstance(Locale("fa", "IR"))
    return formatter.format(this) + " تومان"
}

fun Number.toPersianNumber(): String {
    val formatter = NumberFormat.getInstance(Locale("fa", "IR"))
    return if (this is Double && this == this.toLong().toDouble()) {
        formatter.format(this.toLong())
    } else {
        formatter.format(this)
    }
}

fun String.toPersianDigits(): String {
    var result = this
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in 0..9) {
        result = result.replace(i.toString(), persianDigits[i])
    }
    return result
}

fun String.formatThousandSeparator(): String {
    if (this.isEmpty()) return ""
    val cleanString = this.replace(",", "").replace("٫", "")
    val doubleValue = cleanString.toDoubleOrNull() ?: return this
    val formatter = NumberFormat.getInstance(Locale.US) // Use US for standard grouping
    return formatter.format(doubleValue)
}

fun String.fromPersianDigits(): String {
    var result = this
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in 0..9) {
        result = result.replace(persianDigits[i], i.toString())
    }
    return result
}

fun String.cleanNumber(): String {
    return this.replace(",", "").replace("٫", "").fromPersianDigits()
}

class PersianNumberVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }

        val hasDot = originalText.contains(".")
        val formatted = if (hasDot) originalText.toPersianDigits() else formatWithCommas(originalText).toPersianDigits()

        val offsetMapping = if (hasDot) {
            androidx.compose.ui.text.input.OffsetMapping.Identity
        } else {
            object : androidx.compose.ui.text.input.OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    val len = originalText.length
                    var commasBefore = 0
                    for (i in 1 until offset) {
                        if ((len - i) % 3 == 0) commasBefore++
                    }
                    return offset + commasBefore
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 0) return 0
                    val len = originalText.length
                    var digitsFound = 0
                    var currentOffset = 0
                    while (currentOffset < offset && digitsFound < len) {
                        val isComma = (len - digitsFound) % 3 == 0 && digitsFound != 0
                        if (isComma) {
                            currentOffset++
                            if (currentOffset > offset) break
                        }
                        digitsFound++
                        currentOffset++
                    }
                    return digitsFound
                }
            }
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(formatted),
            offsetMapping
        )
    }

    private fun formatWithCommas(s: String): String {
        val sb = StringBuilder()
        for (i in s.indices) {
            sb.append(s[i])
            val digitsFromRight = s.length - 1 - i
            if (digitsFromRight > 0 && digitsFromRight % 3 == 0) {
                sb.append(",")
            }
        }
        return sb.toString()
    }

    private fun String.toPersianDigits(): String {
        var result = this
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) {
            result = result.replace(i.toString(), persianDigits[i])
        }
        return result
    }
}
