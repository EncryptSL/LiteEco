package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.api.interfaces.AdapterLogger
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class DatabaseMonologModel(val plugin: Plugin) : AdapterLogger {
    override fun error(message: String) {
        log(Level.SEVERE, message)
    }

    override fun warning(message: String) {
        log(Level.WARNING, message)
    }

    override fun info(message: String) {
        log(Level.INFO, message)
    }

    override fun clearLogs() {
        loggedTransaction { MonologTable.deleteAll() }
    }

    override fun getLog(): CompletableFuture<List<EconomyLog>> {
        val future = CompletableFuture<List<EconomyLog>>()
        val log = loggedTransaction {
            MonologTable.selectAll().orderBy(MonologTable.timestamp, SortOrder.DESC).mapNotNull {
                     EconomyLog(it[MonologTable.level], it[MonologTable.log], it[MonologTable.timestamp])
            }
        }
        future.completeAsync { log }
        return future
    }

    private fun log(level: Level, message: String) {
        if (!plugin.config.getBoolean("economy.monolog_activity", true)) return
        loggedTransaction {
            MonologTable.insert {
                it[MonologTable.level] = level.name
                it[log] = message
            }
        }
    }

}