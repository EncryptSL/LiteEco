package com.github.encryptsl.lite.eco.common.manager.monolog

import java.math.BigDecimal

data class MonologFilter(
    val action: String? = null,
    val sender: String? = null,
    val target: String? = null,
    val currency: String? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
) {
    fun toCommandString(): String {
        val parts = mutableListOf<String>()

        target?.let { parts.add("t:$it") }
        sender?.let { parts.add("s:$it") }
        action?.let { parts.add("a:$it") }
        currency?.let { parts.add("c:$it") }
        minAmount?.let { parts.add("am:$it") }

        if (parts.isEmpty()) return ""

        return "[" + parts.joinToString(",") + "]"
    }
}