package com.github.encryptsl.lite.eco.common.database.entity

import java.math.BigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class EconomyLog(
    val action: String,
    val sender: String,
    val target: String,
    val currency: String,
    val previousBalance: BigDecimal,
    val newBalance: BigDecimal,
    val timestamp: Instant
)
