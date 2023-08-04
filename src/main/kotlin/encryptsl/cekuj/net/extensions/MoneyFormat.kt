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
    val formatter = DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
        roundingMode = if (compacted) {
            applyPattern("#,##0.###")
            RoundingMode.UNNECESSARY
        } else {
            applyPattern("#,##0.00")
            RoundingMode.HALF_UP
        }
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

private fun compactNumber(number: Double): String {
    var value = number
    for (unit in units) {
        if (value < 1000) {
            return if (unit == units[0]) formatNumber(number)
            else {
                return formatNumber(value, true) + unit
            }
        }
        value /= 1000
    }
    throw IllegalStateException("This shouldn't happen")
}