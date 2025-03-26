package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnector(private val liteEco: LiteEco) : DatabaseConnectorProvider {

    override fun load() {
        initConnect(
            liteEco.config.getString("database.connection.driverClassName") ?: "org.sqlite.JDBC",
            liteEco.config.getString("database.connection.jdbc_url") ?: "jdbc:sqlite:plugins/LiteEco/database.db",
            liteEco.config.getString("database.connection.username") ?: "root",
            liteEco.config.getString("database.connection.password") ?: "admin"
        )
    }

    override fun initConnect(driver: String, jdbcHost: String, user: String, pass: String) {
        Database.connect(HikariDataSource().apply {
            jdbcUrl = jdbcHost
            driverClassName = driver
            username = user
            password = pass
            poolName = liteEco.javaClass.simpleName
            maximumPoolSize = 20
            isReadOnly = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }, databaseConfig = DatabaseConfig {
            @OptIn(ExperimentalKeywordApi::class)
            preserveKeywordCasing = true
        },connectionAutoRegistration = ExposedConnectionImpl())

        val currencyIterator = liteEco.currencyImpl.getCurrenciesKeys().iterator()

        while (currencyIterator.hasNext()) {
            val currency = currencyIterator.next()
            loggedTransaction {
                SchemaUtils.create(Account(currency), MonologTable)
            }
        }
    }
}