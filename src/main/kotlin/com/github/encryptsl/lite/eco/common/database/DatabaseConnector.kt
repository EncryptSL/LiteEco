package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi
import org.jetbrains.exposed.sql.SchemaUtils

class DatabaseConnector(private val liteEco: LiteEco) : DatabaseConnectorProvider {

    override fun load() {
        val configLoader = DatabaseConfigLoader(liteEco.config)
        val (driver, jdbcUrl, username, password) = configLoader.load()

        initConnect(driver, jdbcUrl, username, password)
    }

    override fun initConnect(driver: String, jdbcHost: String, user: String, pass: String) {
        try {
            Database.connect(HikariDataSource().apply {
                this.driverClassName = driver
                this.jdbcUrl = jdbcHost
                this.username = user
                this.password = pass
                this.poolName = liteEco.javaClass.simpleName
                this.maximumPoolSize = 20
                this.isReadOnly = false
                this.transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }, databaseConfig = DatabaseConfig {
                @OptIn(ExperimentalKeywordApi::class)
                preserveKeywordCasing = true
            }, connectionAutoRegistration = ExposedConnectionImpl())

            liteEco.currencyImpl.getCurrenciesKeys().forEach { currency ->
                loggedTransaction {
                    SchemaUtils.create(Account(currency), MonologTable)
                }
            }
        } catch (e: Exception) {
            liteEco.logger.severe("Database initialization failed: ${e.message}")
        }
    }
}