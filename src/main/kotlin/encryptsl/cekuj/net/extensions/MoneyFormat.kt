package encryptsl.cekuj.net.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Double.moneyFormat(prefix: String, currencyName: String): String {
    val formatter = DecimalFormat("$prefix###,###,##0.00 $currencyName", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = 2
    return formatter.format(this)
}

fun Double.moneyFormat(): String {
    val formatter = DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.maximumFractionDigits = 2
    return formatter.format(this)
}
fun String.toValidNumber(): Double? {
    val units = mapOf('K' to 1000.0, 'M' to 1_000_000.0, 'B' to 1_000_000_000.0, 'T' to 1_000_000_000_000.0)

    val lastChar = this.lastOrNull()?.uppercaseChar()
    if (lastChar == null || !units.containsKey(lastChar)) {
        return toDoubleOrNull()
    }
    val multiplier = units[lastChar]!!
    val value = dropLast(1)?.toDoubleOrNull()!!

    return value * multiplier
}