package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.api.interfaces.TransactionLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.math.ceil
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DatabaseMonologModel(
    val plugin: Plugin
) : TransactionLogger {

    private val isLoggingEnabled: Boolean
        get() = plugin.config.getBoolean("economy.monolog_activity", true)

    override suspend fun logging(ctx: TransactionContextEntity) {
        if (!isLoggingEnabled) return

        io {
            loggedTransaction {
                try {
                    MonologTable.insert {
                        it[action] = ctx.type.name
                        it[sender] = ctx.sender
                        it[target] = ctx.target
                        it[currency] = ctx.currency
                        it[previousBalance] = ctx.previousBalance
                        it[newBalance] = ctx.newBalance
                        it[timestamp] = ctx.timestamp
                    }
                } catch (e: Exception) {
                    plugin.componentLogger.warn("Failed to log transaction: ${e.message}")
                }
            }
        }
    }

    override fun clearLogs() {
        loggedTransaction { MonologTable.deleteAll() }
    }

    override suspend fun hasLogs(targetFilter: String?): Boolean = io {
        loggedTransaction {
            !createQuery(targetFilter).empty()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getLog(targetFilter: String?): List<TransactionContextEntity> = io {
        loggedTransaction {
            createQuery(targetFilter)
                .orderBy(MonologTable.timestamp, SortOrder.DESC)
                .map { it.toEconomyLog() }
        }
    }

    override suspend fun getLogPage(
        targetFilter: String?,
        page: Int,
        pageSize: Int
    ): Pair<List<TransactionContextEntity>, Int> = io {
        loggedTransaction {
            val query = createQuery(targetFilter)

            val totalCount = query.count()
            val totalPages = ceil(totalCount.toDouble() / pageSize).toInt().coerceAtLeast(1)
            val offsetValue = ((page - 1).coerceAtLeast(0) * pageSize).toLong()

            val logs = query.copy()
                .orderBy(MonologTable.timestamp, SortOrder.DESC)
                .limit(pageSize)
                .offset(offsetValue)
                .map { it.toEconomyLog() }

            Pair(logs, totalPages)
        }
    }

    private fun createQuery(targetFilter: String?): Query {
        return if (targetFilter != null && targetFilter != "all") {
            MonologTable.selectAll().where { MonologTable.target eq targetFilter }
        } else {
            MonologTable.selectAll()
        }
    }

    private fun ResultRow.toEconomyLog() = TransactionContextEntity(
        type = try {
            TypeLogger.valueOf(this[MonologTable.action])
        } catch (_ : Exception) {
            TypeLogger.UNKNOWN
        },
        sender = this[MonologTable.sender],
        target = this[MonologTable.target],
        currency = this[MonologTable.currency],
        previousBalance = this[MonologTable.previousBalance],
        newBalance = this[MonologTable.newBalance],
        timestamp = this[MonologTable.timestamp]
    )
}