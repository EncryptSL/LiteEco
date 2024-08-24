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
            jdbcUrl = jdbcHost
            username = user
            password = pass
            poolName = liteEco.javaClass.simpleName
            maximumPoolSize = 10
        }

        Database.connect(config)

        val currencyIterator = liteEco.currencyImpl.getCurrenciesKeys().iterator()

        while (currencyIterator.hasNext()) {
            val currency = currencyIterator.next()
            loggedTransaction {
                SchemaUtils.create(Account(currency), MonologTable)
            }
        }
    }
}