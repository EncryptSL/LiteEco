package encryptsl.cekuj.net.extensions

import java.text.DecimalFormat

fun Double.moneyFormat(): String {
    val formatter = DecimalFormat("###,###,##0.00")
    return formatter.format(this)
}