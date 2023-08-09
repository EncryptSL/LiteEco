package encryptsl.cekuj.net.extensions
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow

private val units = charArrayOf('\u0000', 'K', 'M', 'B', 'T', 'Q')

fun String.toValidDecimal(): Double? {
    return decompressNumber(this)
}

fun Double.compactFormat(pattern: String, compactPattern: String, locale: String): String {
    val (value, unit) = compactNumber(this) ?: (null to null)

    return value?.let { formatNumber(value, compactPattern, locale) + unit }
        ?: formatNumber(this, pattern, locale)
}

fun Double.moneyFormat(pattern: String, locale: String): String {
    return formatNumber(this, pattern, locale)
}

private fun formatNumber(number: Double, pattern: String, locale: String, compacted: Boolean = false): String {
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

private fun decompressNumber(str: String): Double? {
    if (str.isBlank()) return null

    val lastChar = str.last().uppercaseChar()

    return if (lastChar in units) {
        val multiplier = 10.0.pow(units.indexOf(lastChar) * 3)
        str.dropLast(1).toDecimal()?.times(multiplier)
    }
    else str.toDecimal()
}

private fun compactNumber(number: Double): Pair<Double, Char>? {
    var value = number
    for (unit in units) {
        if (value < 1000) {
            return if (unit == units[0]) null
            else {
                return Pair(value, unit)
            }
        }
        value /= 1000
    }
    throw IllegalStateException("This shouldn't happen")
}

private fun getLocale(localeStr: String): Locale {
    val parts = localeStr.split("-", "_")
    return when (parts.size) {
        1 -> Locale(parts[0])
        2 -> Locale(parts[0], parts[1])
        else -> Locale(parts[0], parts[1], parts[2])
    }
}