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