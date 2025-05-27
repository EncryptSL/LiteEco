package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.interfaces.TransactionLogger
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.convertInstant
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.github.encryptsl.lite.eco.common.extensions.runBlockingIO
import kotlinx.datetime.Instant
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

class DatabaseMonologModel(val plugin: Plugin) : TransactionLogger {

    override suspend fun logging(
        economyOperations: EconomyOperations,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    ) {
        log(economyOperations, sender, target, currency, previousBalance, newBalance)
    }

    override fun clearLogs() {
        loggedTransaction { MonologTable.deleteAll() }
    }

    override suspend fun getLog(): List<EconomyLog> {
        return loggedTransaction {
            MonologTable.selectAll().orderBy(MonologTable.timestamp, SortOrder.DESC).mapNotNull {
                EconomyLog(it[MonologTable.action], it[MonologTable.sender], it[MonologTable.target], it[MonologTable.currency], it[MonologTable.previousBalance], it[MonologTable.newBalance], it[MonologTable.timestamp])
            }
        }
    }

    fun message(
        translation: String,
        economyOperations: EconomyOperations,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal,
        instant: Instant,
    ): Component {
        return LiteEco.instance.locale.translation(translation, TagResolver.resolver(
            Placeholder.parsed("action", economyOperations.name),
            Placeholder.parsed("sender", sender),
            Placeholder.parsed("target", target),
            Placeholder.parsed("currency", currency),
            Placeholder.parsed("previous_balance", LiteEco.instance.api.fullFormatting(previousBalance)),
            Placeholder.parsed("new_balance", LiteEco.instance.api.fullFormatting(newBalance)),
            Placeholder.parsed("timestamp", convertInstant(instant)),
        ))
    }

    private suspend fun log(
        economyOperations: EconomyOperations,
        sender: String,
        target: String,
        currency: String,
        previousBalance: BigDecimal,
        newBalance: BigDecimal
    ) {
        if (!plugin.config.getBoolean("economy.monolog_activity", true)) return
        runBlockingIO {
            loggedTransaction {
                MonologTable.insert {
                    it[action] = economyOperations.name
                    it[MonologTable.sender] = sender
                    it[MonologTable.target] = target
                    it[MonologTable.currency] = currency
                    it[MonologTable.previousBalance] = previousBalance
                    it[MonologTable.newBalance] = newBalance
                }
            }
        }
    }

}