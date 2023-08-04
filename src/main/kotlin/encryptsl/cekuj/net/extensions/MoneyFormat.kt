package encryptsl.cekuj.net.extensions
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow

private val units = charArrayOf('\u0000', 'K', 'M', 'B', 'T', 'Q')

fun Double.moneyFormat(prefix: String, currencyName: String, compact: Boolean = false): String {
    val formattedNumber = formatNumber(this, compact)
    return "$prefix$formattedNumber $currencyName"
}

fun Double.moneyFormat(compact: Boolean = false): String {
    return formatNumber(this, compact)
}

fun String.toValidDecimal(): Double? {
    if (isNullOrBlank()) return null
    return decompressNumber(this)
}

private fun formatNumber(number: Double, compact: Boolean): String {
    return if (compact) {
        val (compactNumber, compactChar) = compactNumber(number)
        removeTrailingZeros(formatter(3, RoundingMode.DOWN).format(compactNumber)) + compactChar
    } else {
        formatter(2, RoundingMode.HALF_UP).format(number)
    }
}

private fun formatter(fractionDigits: Int, roundingMode: RoundingMode): DecimalFormat {
    val formatter = DecimalFormat("###,###,##0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = fractionDigits
    formatter.roundingMode = roundingMode
    return formatter
}

private fun removeTrailingZeros(numberStr: String): String {
    if (!numberStr.contains('.') || !numberStr.endsWith('0')) return numberStr

    val builder = StringBuilder(numberStr)
    var i = builder.length - 1

    while (i >= 0 && builder[i] == '0') {
        builder.deleteCharAt(i)
        i--
    }
    if (builder[i] == '.') {
        builder.deleteCharAt(i)
    }
    return builder.toString()
}

private fun decompressNumber(str: String): Double? {
    val lastChar = str.last().uppercaseChar()

    if (lastChar !in units) return str.toDecimal()

    val value = str.dropLast(1).toDecimal()

    val multiplier = 10.0.pow(units.indexOf(lastChar) * 3)
    return value?.times(multiplier)
}

private fun compactNumber(number: Double): Pair<Double, Char> {
    var value = number
    for (unit in units) {
        if (value >= 1000) {
            value /= 1000
        } else {
            return Pair(value, unit)
        }
    }
    throw IllegalStateException("This shouldn't happen")}
}