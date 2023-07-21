package encryptsl.cekuj.net.extensions

fun Double.isNegative(): Boolean {
    return this < 0
}

fun Double.isZero(): Boolean {
    return this == 0.0
}
fun String.isNumeric(): Boolean {
    return this.toIntOrNull() != null
}

fun String.isDecimal(): Boolean {
    return toDoubleOrNull()?.takeIf { it.isFinite() } != null
}