package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.transactions.TransactionManager

class SqlPluginLogger(private val liteEco: LiteEco) : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        if (!liteEco.config.getBoolean("database.sql-plugin-logger", false)) return

        liteEco.logger.info(context.sql(TransactionManager.current()))
    }
}