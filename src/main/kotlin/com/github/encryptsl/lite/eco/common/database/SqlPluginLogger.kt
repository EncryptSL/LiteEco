package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.expandArgs
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

class SqlPluginLogger : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        if (!LiteEco.instance.config.getBoolean("database.sql-plugin-logger")) return
        LiteEco.instance.logger.info(ModernText.miniModernText("<gold>${context.expandArgs(TransactionManager.current())}"))
    }
}