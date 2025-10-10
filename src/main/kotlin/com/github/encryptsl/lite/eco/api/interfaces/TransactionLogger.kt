package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import java.math.BigDecimal

/**
 * Interface defining the contract for logging economic transactions.
 *
 * This logger is designed to handle asynchronous logging operations (indicated by `suspend`)
 * to ensure that transaction logging does not block the main application thread.
 */
interface TransactionLogger {

    /**
     * Asynchronously records a financial transaction log entry.
     *
     * @param typeLogger The type of transaction that occurred (e.g., deposit, withdraw, set).
     * @param sender The identifier of the entity initiating the transaction (maybe a player UUID or a system name).
     * @param target The identifier of the entity whose balance was affected (e.g., a player UUID).
     * @param currency The key/name of the currency involved in the transaction.
     * @param previousBalance The account balance immediately before the transaction occurred.
     * @param newBalance The account balance immediately after the transaction completed.
     */
    suspend fun logging(
        typeLogger: TypeLogger,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    )

    /**
     * Clears and permanently deletes all recorded transaction logs from the storage.
     *
     * *Note: This operation is synchronous, implying a quick, non-blocking clean-up, or a blocking operation is acceptable.*
     */
    fun clearLogs()

    /**
     * Asynchronously retrieves all stored transaction logs.
     *
     * @return A list of [EconomyLog] objects representing the complete transaction history.
     */
    suspend fun getLog(): List<EconomyLog>
}