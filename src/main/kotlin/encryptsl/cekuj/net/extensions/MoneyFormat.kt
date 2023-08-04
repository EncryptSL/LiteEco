package encryptsl.cekuj.net.extensions
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow

private val units = charArrayOf('\u0000', 'K', 'M', 'B', 'T', 'Q')

fun String.toValidDecimal(): Double? {
    return if (isNullOrBlank()) null
    else {
        decompressNumber(this)
    }
}

fun Double.moneyFormat(compact: Boolean = false): String {
    return if (compact) {
        compactNumber(this)
    }
    else formatNumber(this)
}

fun Double.moneyFormat(prefix: String, currencyName: String, compact: Boolean = false): String {
    val formattedNumber = if (compact) {
        compactNumber(this)
    }
    else formatNumber(this)

    return "$prefix$formattedNumber $currencyName"
}

private fun formatNumber(number: Double, compacted: Boolean = false): String {
    val formatter = DecimalFormat()
    formatter.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)

    if (compacted) {
        formatter.applyPattern("#,##0.###")
        formatter.roundingMode = RoundingMode.UNNECESSARY
    } else {
        formatter.applyPattern("#,##0.00")
        formatter.roundingMode = RoundingMode.HALF_UP
    }
    return formatter.format(number)
}

private fun decompressNumber(str: String): Double? {
    val lastChar = str.last().uppercaseChar()

    if (lastChar !in units) return str.toDecimal()

    val multiplier = 10.0.pow(units.indexOf(lastChar) * 3)

    return str.dropLast(1).toDecimal()?.times(multiplier)
}

private fun compactNumber(number: Double): String {
    var value = number
    for (unit in units) {
        if (value >= 1000) {
            value /= 1000
        } else {
            if (unit != units[0]) {
                return formatNumber(value, true) + unit
            }
            return formatNumber(number)
        }
    }
    throw IllegalStateException("This shouldn't happen")
}