package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.TransactionLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologFilter
import com.github.encryptsl.lite.eco.enums.TypeLogger
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.math.ceil

class MonologRepository(
    val liteEco: LiteEco
) : TransactionLogger {

    private val isLoggingEnabled: Boolean
        get() = liteEco.baseConfig.economy.monologActivity

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
                    liteEco.logger.warn("Failed to log transaction: ${e.message}")
                }
            }
        }
    }

    override fun clearLogs(): Int {
        return loggedTransaction { MonologTable.deleteAll() }
    }


    override suspend fun getLogPage(
        filter: MonologFilter,
        page: Int,
        pageSize: Int
    ): Pair<List<TransactionContextEntity>, Int> = io {
        loggedTransaction {
            val query = createQuery(filter)

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

    private fun createQuery(filter: MonologFilter): Query {
        val query = MonologTable.selectAll()

        filter.action?.let { action ->
            query.andWhere { MonologTable.action eq action }
        }
        filter.sender?.let { sender ->
            query.andWhere { MonologTable.sender eq sender }
        }
        filter.target?.let { target ->
            query.andWhere { MonologTable.target eq target }
        }
        filter.currency?.let { currency ->
            query.andWhere { MonologTable.currency eq currency }
        }
        filter.minAmount?.let { minAmount ->
            query.andWhere { (MonologTable.newBalance - MonologTable.previousBalance) greaterEq minAmount }
        }

        return query
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