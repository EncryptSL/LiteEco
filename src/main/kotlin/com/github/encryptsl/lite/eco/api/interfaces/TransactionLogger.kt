package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologFilter
import com.github.encryptsl.lite.eco.enums.TypeLogger

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
     * Permanently deletes **all** recorded transaction logs from the database.
     *
     * *Use with extreme caution! This operation is irreversible.*
     *
     * @return The total number of transaction log entries deleted.
     */
    fun clearLogs(): Int

    suspend fun getLogPage(
        filter: MonologFilter,
        page: Int,
        pageSize: Int
    ): Pair<List<TransactionContextEntity>, Int>
}