package encryptsl.cekuj.net.extensions

fun Double.isNegative(): Boolean {
    return this < 0
}

fun Double.isZero(): Boolean {
    return this == 0.0
}
fun String.isNumeric(): Boolean {
    if (this.isEmpty()) return false
    return this.toIntOrNull() != null
}

fun String.isDecimal(): Boolean {
    if (this.isEmpty()) return false
    val value = this.toDoubleOrNull()
    return value != null && !value.isFinite()
}