package encryptsl.cekuj.net.extensions
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private val units = charArrayOf('K', 'M', 'B', 'T', 'Q')
private val unitsMap = mapOf('K' to 1000.0, 'M' to 1_000_000.0, 'B' to 1_000_000_000.0, 'T' to 1_000_000_000_000.0, 'Q' to 1_000_000_000_000_000.0)


fun Double.moneyFormat(prefix: String, currencyName: String, compact: Boolean = false): String {
    val (formattedNumber, compactChar) = formatNumber(this, compact)
    val suffix = compactChar?.let { "$it $currencyName" } ?: " $currencyName"
    return "$prefix$formattedNumber$suffix"
}

fun Double.moneyFormat(compact: Boolean = false): String {
    val (formattedNumber, compactChar) = formatNumber(this, compact)
    val suffix = compactChar?.let { "$it" } ?: ""
    return "$formattedNumber$suffix"
}

fun String.parseValidNumber(): Double? {
    if (isNullOrBlank()) return null
    val lastChar = last().uppercaseChar()
    return if (units.contains(lastChar)) {
        decompressNumber(this, lastChar)
    } else {
        toDecimal()
    }
}

private fun formatNumber(number: Double, compact: Boolean): Pair<String, Char?> {
    return if (compact) {
        val (compactNumber, compactChar) = compactNumber(number) ?: (number to null)
        removeTrailingZeros(formatter(roundingMode = RoundingMode.DOWN).format(compactNumber)) to compactChar
    } else {
        formatter(roundingMode = RoundingMode.HALF_UP).format(number) to null
    }
}

private fun formatter(fractionDigits: Int = 2, roundingMode: RoundingMode): DecimalFormat {
    val formatter = DecimalFormat("###,###,##0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = fractionDigits
    formatter.roundingMode = roundingMode
    return formatter
}

private fun removeTrailingZeros(numberStr: String): String {
    return if (numberStr.contains('.') && numberStr.endsWith("0")) {
        var i = numberStr.length - 1
        while (i >= 0 && numberStr[i] == '0') {
            i--
        }
        if (numberStr[i] == '.') {
            numberStr.substring(0, i)
        } else {
            numberStr.substring(0, i + 1)
        }
    } else {
        numberStr
    }
}

private fun decompressNumber(str: String, metric: Char): Double? {
    val multiplier = unitsMap[metric] ?: 1.0
    val value = str.dropLast(1).toDecimal()
    return value?.times(multiplier)
}

private fun compactNumber(number: Double): Pair<Double, Char>? {
    if (number < 1000) return null
    var unitIndex = 0
    var value = number
    while (value >= 1000 && unitIndex < units.size) {
        value /= 1000
        unitIndex++
    }
    return unitIndex.takeIf { it > 0 }?.let { Pair(value, units[it - 1]) }
}