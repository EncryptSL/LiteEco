package com.github.encryptsl.lite.eco.common.extensions

import java.math.BigDecimal

fun BigDecimal.isNegative(): Boolean {
    return this < BigDecimal.ZERO
}

fun BigDecimal.isApproachingZero(): Boolean {
    return this < BigDecimal.valueOf(0.01)
}