package com.github.encryptsl.lite.eco.common.database.entity

import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents the complete context of an economy transaction for logging purposes.
 *
 * @property type The [TypeLogger] action performed.
 * @property sender The initiator of the transaction.
 * @property target The recipient or affected party.
 * @property currency The currency identifier.
 * @property previousBalance Balance before the transaction.
 * @property newBalance Balance after the transaction.
 * @property timestamp The [Instant] when the transaction was recorded.
 */
@OptIn(ExperimentalTime::class)
data class TransactionContextEntity(
    val type: TypeLogger,
    val sender: String,
    val target: String,
    val currency: String,
    val previousBalance: BigDecimal,
    val newBalance: BigDecimal,
    val timestamp: Instant = Clock.System.now()
)