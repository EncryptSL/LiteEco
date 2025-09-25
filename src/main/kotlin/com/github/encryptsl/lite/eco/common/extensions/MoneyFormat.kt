package com.github.encryptsl.lite.eco.common.extensions
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private val units = arrayOf(
    "", "K", "M", "B", "T", "Q", "Qn", "S", "Se", "O", "N", "D"
)

fun String.toValidDecimal(): BigDecimal? {
    val s = this.trim()
    if (s.isBlank() || s.contains(" ")) return null
    return decompressNumber(s)
}

fun BigDecimal.compactFormat(pattern: String, compactPattern: String, locale: String): String {
    val pair = compactNumber(this)
    return if (pair != null) {
        val (value, unit) = pair
        formatNumber(value, compactPattern, locale, compacted = true) + unit
    } else {
        formatNumber(this, pattern, locale)
    }
}

fun BigDecimal.moneyFormat(pattern: String, locale: String): String {
    return formatNumber(this, pattern, locale)
}

private fun formatNumber(number: BigDecimal, pattern: String, locale: String, compacted: Boolean = false): String {
    val formatter = DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(getLocale(locale))
        // always safe rounding; UNNECESSARY caused exceptions
        roundingMode = if (compacted) RoundingMode.HALF_UP else RoundingMode.HALF_UP
        applyPattern(pattern)
    }
    return formatter.format(number)
}

private fun decompressNumber(str: String): BigDecimal? {
    val upper = str.trim().uppercase(Locale.getDefault())

    val unit = units
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.length }
        .firstOrNull { upper.endsWith(it) }

    return if (unit != null) {
        val index = units.indexOf(unit)
        val numberPart = upper.removeSuffix(unit)
        numberPart.toBigDecimalOrNull()?.multiply(BigDecimal.TEN.pow(index * 3))
    } else {
        str.toBigDecimalOrNull()
    }
}

private fun compactNumber(number: BigDecimal): Pair<BigDecimal, String>? {
    if (number.compareTo(BigDecimal.ZERO) == 0) return null

    // use integer part to determine number of digits -> stable decision unit
    val absIntPart: BigInteger = number.abs().toBigInteger()
    if (absIntPart == BigInteger.ZERO) return null

    val digits = absIntPart.toString().length
    val unitIndex = (digits - 1) / 3
    if (unitIndex == 0) return null

    val index = if (unitIndex >= units.size) units.size - 1 else unitIndex
    val divisor = BigDecimal.TEN.pow(index * 3)

    // scale = 1 -> display up to 1 decimal place (for 44_888 -> 44.9K)
    // If you always want no decimal places (44K), use scale = 0 and RoundingMode.DOWN
    val scaled = number.divide(divisor, 1, RoundingMode.HALF_UP)

    return scaled to units[index]
}

private fun getLocale(localeStr: String): Locale {
    // compatible and safe: "en", "en-US", "zh_CN" -> "en", "en-US", "zh-CN"
    return Locale.forLanguageTag(localeStr.replace('_', '-'))
}