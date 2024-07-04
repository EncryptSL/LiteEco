package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import com.zaxxer.hikari.HikariDataSource
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.jetbrains.exposed.sql.*

class DatabaseConnector(private val liteEco: LiteEco) : DatabaseConnectorProvider {
    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        val currencyIterator = liteEco.config.getConfigurationSection("economy.currencies")?.getKeys(false)?.iterator()

        while (currencyIterator?.hasNext() == true) {
            val currency = liteEco.config.getString("economy.currencies.${currencyIterator.next()}.currency_name", "dollar").toString()
            loggedTransaction {
                SchemaUtils.create(Account(currency), MonologTable)
            }
        }
    }
}