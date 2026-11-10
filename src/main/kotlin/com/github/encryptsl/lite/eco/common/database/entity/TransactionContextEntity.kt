package com.github.encryptsl.lite.eco.common.database.entity

import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.enums.TypeLogger
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import kotlin.time.Clock
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
data class TransactionContextEntity(
    val type: TypeLogger,
    val sender: String,
    val target: String,
    val currency: String,
    val previousBalance: BigDecimal,
    val newBalance: BigDecimal,
    val timestamp: Instant = Clock.System.now()
) {
    companion object {
        fun fromRow(row: ResultRow): TransactionContextEntity {
            return TransactionContextEntity(
                type = TypeLogger.valueOf(row[MonologTable.action]),
                sender = row[MonologTable.sender],
                target = row[MonologTable.target],
                currency = row[MonologTable.currency],
                previousBalance = row[MonologTable.previousBalance],
                newBalance = row[MonologTable.newBalance],
                timestamp = row[MonologTable.timestamp]
            )
        }
    }
}