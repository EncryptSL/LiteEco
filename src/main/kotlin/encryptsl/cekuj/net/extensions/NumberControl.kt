package encryptsl.cekuj.net.extensions

fun Double.isNegative(): Boolean {
    return this < 0
}

fun Double.isZero(): Boolean {
    return this == 0.0
}

fun Double.isApproachingZero(): Boolean {
    return this < 0.01
}

fun String.isDecimal(): Boolean {
    return toDoubleOrNull()?.takeIf { it.isFinite() } != null
}

fun String.toDecimal(): Double? {
    return toDoubleOrNull()?.takeIf { it.isFinite() }
}