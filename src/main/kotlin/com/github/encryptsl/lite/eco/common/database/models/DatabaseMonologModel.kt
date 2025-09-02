package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.api.interfaces.TransactionLogger
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.convertInstant
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.github.encryptsl.lite.eco.common.extensions.runBlockingIO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.math.BigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DatabaseMonologModel(val plugin: Plugin) : TransactionLogger {

    override suspend fun logging(
        typeLogger: TypeLogger,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    ) {
        log(typeLogger, sender, target, currency, previousBalance, newBalance)
    }

    override fun clearLogs() {
        loggedTransaction { MonologTable.deleteAll() }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getLog(): List<EconomyLog> {
        return loggedTransaction {
            MonologTable.selectAll().orderBy(MonologTable.timestamp, SortOrder.DESC).mapNotNull {
                EconomyLog(it[MonologTable.action], it[MonologTable.sender], it[MonologTable.target], it[MonologTable.currency], it[MonologTable.previousBalance], it[MonologTable.newBalance], it[MonologTable.timestamp])
            }
        }
    }

    fun message(
        translation: String,
        typeLogger: TypeLogger,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal,
        instant: Instant,
    ): Component {
        return LiteEco.instance.locale.translation(translation, TagResolver.resolver(
            Placeholder.parsed("action", typeLogger.name),
            Placeholder.parsed("sender", sender),
            Placeholder.parsed("target", target),
            Placeholder.parsed("currency", currency),
            Placeholder.parsed("previous_balance", LiteEco.instance.currencyImpl.fullFormatting(previousBalance)),
            Placeholder.parsed("new_balance", LiteEco.instance.currencyImpl.fullFormatting(newBalance)),
            Placeholder.parsed("timestamp", convertInstant(instant)),
        ))
    }

    private suspend fun log(
        typeLogger: TypeLogger,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    ) {
        if (!plugin.config.getBoolean("economy.monolog_activity", true)) return
        runBlockingIO {
            loggedTransaction {
                try {
                    MonologTable.insert {
                        it[action] = typeLogger.name
                        it[MonologTable.sender] = sender
                        it[MonologTable.target] = target
                        it[MonologTable.currency] = currency
                        it[MonologTable.previousBalance] = previousBalance
                        it[MonologTable.newBalance] = newBalance
                    }
                } catch (e : Exception) {
                    plugin.componentLogger.warn(e.message ?: e.localizedMessage)
                }
            }
        }
    }

}