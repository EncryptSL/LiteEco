package com.github.encryptsl.lite.eco.api.errors

import java.math.BigDecimal
import java.util.*

class CouldNotTransferredException(
    val sender: UUID,
    val target: UUID,
    val currency: String,
    val amount: BigDecimal
) : RuntimeException("Could not transfer $amount $currency from $sender to $target")