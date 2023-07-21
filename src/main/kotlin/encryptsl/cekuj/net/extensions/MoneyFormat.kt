package encryptsl.cekuj.net.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Double.moneyFormat(prefix: String, currencyName: String, compact: Boolean = false): String {
    val formatter = DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = 2

    val compactData = toCompact(this)
    val number = compactData?.first ?: this
    var suffix = " $currencyName"
    if (compact) {
        suffix = compactData?.second?.toString() + suffix
    }

    return "$prefix${formatter.format(number)}$suffix"
}

fun Double.moneyFormat(): String {
    val formatter = DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = 2
    return formatter.format(this)
}

private fun toCompact(number: Double): Pair<Double, Char>? {
    val units = charArrayOf('K', 'M', 'B', 'T')

    if (number < 1000) {
        return null
    }

    var unitIndex = 0
    var value = number
    while (value >= 1000 && unitIndex < units.size) {
        value /= 1000
        unitIndex++
    }

    return if (unitIndex > 0) {
        Pair(value, units[unitIndex - 1])
    } else {
        null
    }
}

fun String.toValidNumber(): Double? {
    val units = mapOf('K' to 1000.0, 'M' to 1_000_000.0, 'B' to 1_000_000_000.0, 'T' to 1_000_000_000_000.0, 'Q' to 1_000_000_000_000_000.0)

    val lastChar = this.lastOrNull()?.uppercaseChar()
    if (lastChar == null || !units.containsKey(lastChar)) {
        return toDoubleOrNull()
    }
    val multiplier = units[lastChar]!!
    val value = dropLast(1).toDoubleOrNull()!!

    return value * multiplier
}