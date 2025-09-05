package com.github.encryptsl.lite.eco.common.extensions
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private val units = arrayOf(
    "", "K", "M", "B", "T", "Q", "Qn", "S", "Se", "O", "N", "D"
)

/**
 * Converts a string to BigDecimal, including truncated units (e.g., "1.5M" → 1500000).
 */
fun String.toValidDecimal(): BigDecimal? {
    if (this.isBlank() || this.contains(" ")) return null
    return decompressNumber(this)
}

/**
 * Returns a number in abbreviated format (e.g., 1500 → "1.5K").
 */
fun BigDecimal.compactFormat(pattern: String, compactPattern: String, locale: String): String {
    val (value, unit) = compactNumber(this) ?: (null to null)
    return value?.let {
        formatNumber(it, compactPattern, locale, compacted = true) + unit
    } ?: formatNumber(this, pattern, locale)
}

/**
 * Returns a number as currency according to the specified pattern and locale.
 */
fun BigDecimal.moneyFormat(pattern: String, locale: String): String {
    return formatNumber(this, pattern, locale)
}

/**
 * Formats a number according to a pattern and locale.
 */
private fun formatNumber(number: BigDecimal, pattern: String, locale: String, compacted: Boolean = false): String {
    val formatter = DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(getLocale(locale))
        roundingMode = if (compacted) RoundingMode.UNNECESSARY else RoundingMode.HALF_UP
        applyPattern(pattern)
    }
    return formatter.format(number)
}

/**
 * Decompress string → BigDecimal (e.g., "2.5M" → 2500000).
 */
private fun decompressNumber(str: String): BigDecimal? {
    val upper = str.uppercase(Locale.getDefault())

    // find suffix
    val unit = units
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.length } // so that "Qn" takes precedence over "Q"
        .firstOrNull { upper.endsWith(it) }

    return if (unit != null) {
        val index = units.indexOf(unit)
        val numberPart = upper.removeSuffix(unit)
        numberPart.toBigDecimalOrNull()?.multiply(BigDecimal.TEN.pow(index * 3))
    } else {
        str.toBigDecimalOrNull()
    }
}

/**
 * Compact BigDecimal → Pair(value, unit).
 */
private fun compactNumber(number: BigDecimal): Pair<BigDecimal, String>? {
    if (number == BigDecimal.ZERO) return null

    val absNum = number.abs()
    val exp = absNum.toBigInteger().toString().length - 1 // log10 over string length
    val unitIndex = exp / 3

    if (unitIndex == 0) return null

    if (unitIndex >= units.size) {
        // fallback: use the last known unit
        val divisor = BigDecimal.TEN.pow((units.size - 1) * 3)
        return number.divide(divisor) to units.last()
    }

    val divisor = BigDecimal.TEN.pow(unitIndex * 3)
    return number.divide(divisor) to units[unitIndex]
}

/**
 * Returns the locale based on a string (e.g., "en", "en-US", "zh_CN").
 */
private fun getLocale(localeStr: String): Locale {
    val parts = localeStr.split("-", "_")
    return when (parts.size) {
        1 -> Locale.of(parts[0])
        2 -> Locale.of(parts[0], parts[1])
        else -> Locale.of(parts[0], parts[1], parts[2])
    }
}