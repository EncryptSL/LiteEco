package com.github.encryptsl.lite.eco.common.database.entity

import kotlinx.datetime.Instant
import java.math.BigDecimal

data class EconomyLog(
    val action: String,
    val sender: String,
    val target: String,
    val currency: String,
    val previousBalance: BigDecimal,
    val newBalance: BigDecimal,
    val timestamp: Instant
)
