package com.github.encryptsl.lite.eco.common.extensions
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private val units = charArrayOf('\u0000', 'K', 'M', 'B', 'T', 'Q')

fun String.toValidDecimal(): BigDecimal? {
    return decompressNumber(this)
}

fun BigDecimal.compactFormat(pattern: String, compactPattern: String, locale: String): String {
    val (value, unit) = compactNumber(this) ?: (null to null)

    return value?.let { formatNumber(value, compactPattern, locale) + unit }
        ?: formatNumber(this, pattern, locale)
}

fun BigDecimal.moneyFormat(pattern: String, locale: String): String {
    return formatNumber(this, pattern, locale)
}

private fun formatNumber(number: BigDecimal, pattern: String, locale: String, compacted: Boolean = false): String {
    val formatter = DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(getLocale(locale))
        roundingMode = if (compacted) {
            RoundingMode.UNNECESSARY
        } else {
            RoundingMode.HALF_UP
        }
        applyPattern(pattern)
    }
    return formatter.format(number)
}

private fun decompressNumber(str: String): BigDecimal? {
    if (str.isBlank()) return null

    val lastChar = str.last().uppercaseChar()

    return if (lastChar in units) {
        val multiplier = BigDecimal.valueOf(10.0).pow(units.indexOf(lastChar) * 3)
        str.dropLast(1).toBigDecimal().times(multiplier)
    }
    else str.toBigDecimal()
}

private fun compactNumber(number: BigDecimal): Pair<BigDecimal, Char>? {
    var value = number
    for (unit in units) {
        if (value < BigDecimal.valueOf(1000)) {
            return if (unit == units[0]) null
            else {
                return Pair(value, unit)
            }
        }
        value /= BigDecimal.valueOf(1000)
    }
    throw IllegalStateException("This shouldn't happen")
}

private fun getLocale(localeStr: String): Locale {
    val parts = localeStr.split("-", "_")
    return when (parts.size) {
        1 -> Locale.of(parts[0])
        2 -> Locale.of(parts[0], parts[1])
        else -> Locale.of(parts[0], parts[1], parts[2])
    }
}