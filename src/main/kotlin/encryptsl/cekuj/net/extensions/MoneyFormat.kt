package encryptsl.cekuj.net.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Double.moneyFormat(prefix: String, currencyName: String): String {
    val formatter = DecimalFormat("$prefix###,###,##0.00 $currencyName", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    return formatter.format(this)
}