package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity

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
     * @param ctx The [TransactionContextEntity] containing all details of the transaction,
     * including [TypeLogger], parties involved, currency, and balance changes.
     */
    suspend fun logging(
        ctx: TransactionContextEntity
    )

    /**
     * Clears and permanently deletes all recorded transaction logs from the storage.
     *
     * **Warning:** This operation is irreversible and should be used with caution.
     */
    fun clearLogs()

    suspend fun hasLogs(targetFilter: String?): Boolean

    /**
     * Asynchronously retrieves stored transaction logs, optionally filtered by a target identifier.
     *
     * @param targetFilter The identifier (e.g., player UUID) to filter logs by.
     * If null or "all", retrieves logs for all entities.
     * @return A list of [TransactionContextEntity] objects representing the transaction history,
     * typically ordered from newest to oldest.
     */
    suspend fun getLog(targetFilter: String? = null): List<TransactionContextEntity>

    suspend fun getLogPage(targetFilter: String?, page: Int, pageSize: Int): Pair<List<TransactionContextEntity>, Int>
}