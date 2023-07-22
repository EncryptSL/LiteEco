package encryptsl.cekuj.net.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


private fun formatter(): DecimalFormat {
    val formatter = DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = 2
    return formatter
}

private fun formatNumber(number: Double, compact: Boolean): Pair<String, Char?> {
    return if (compact) {
        val (compactNumber, compactChar) = compactNumber(number) ?: (number to null)
        formatter().format(compactNumber) to compactChar
    } else {
        formatter().format(number) to null
    }
}

fun Double.moneyFormat(prefix: String, currencyName: String, compact: Boolean = false): String {
    val (formattedNumber, compactChar) = formatNumber(this, compact)
    val suffix = compactChar?.let { " $it $currencyName" } ?: " $currencyName"

    return "$prefix$formattedNumber$suffix"
}

fun Double.moneyFormat(compact: Boolean = false): String {
    val (formattedNumber, compactChar) = formatNumber(this, compact)
    val suffix = compactChar?.let { " $it" } ?: ""

    return "$formattedNumber$suffix"
}

private fun compactNumber(number: Double): Pair<Double, Char>? {
    val units = charArrayOf('K', 'M', 'B', 'T', 'Q')

    if (number < 1000) return null

    var unitIndex = 0
    var value = number
    while (value >= 1000 && unitIndex < units.size) {
        value /= 1000
        unitIndex++
    }

    return unitIndex.takeIf { it > 0 }?.let { Pair(value, units[it - 1]) }
}

fun String.parseValidNumber(): Double? {
    val units = mapOf('K' to 1000.0, 'M' to 1_000_000.0, 'B' to 1_000_000_000.0, 'T' to 1_000_000_000_000.0, 'Q' to 1_000_000_000_000_000.0)

    val lastChar = lastOrNull()?.uppercaseChar()
    val multiplier = units.getOrDefault(lastChar, 1.0)
    if (multiplier == 1.0) {
        return takeIf { it.isDecimal() }?.toDoubleOrNull()
    }

    val value = dropLast(1).takeIf { it.isDecimal() }?.toDoubleOrNull()

    return value?.times(multiplier)
}