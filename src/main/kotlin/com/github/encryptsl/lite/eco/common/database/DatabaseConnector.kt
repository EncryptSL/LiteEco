package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.MonologTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.ExperimentalKeywordApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

class DatabaseConnector(private val liteEco: LiteEco) : DatabaseConnectorProvider {

    private var hikari: HikariDataSource? = null

    override fun onLoad() {
        val configLoader = DatabaseConfigLoader(liteEco.config)
        val (driver, jdbcUrl, username, password) = configLoader.load()

        try {
            initConnect(driver, jdbcUrl, username, password)
            liteEco.logger.info("✅ Database connection established successfully.")
        } catch (e: Exception) {
            liteEco.logger.severe("❌ Database failed to initialize: ${e.message}")
            liteEco.pluginManager.disablePlugin(liteEco)
        }
    }

    override fun initConnect(driver: String, jdbcHost: String, user: String, pass: String) {
        try {
            hikari = HikariDataSource().apply {
                this.driverClassName = driver
                this.jdbcUrl = jdbcHost
                this.username = user
                this.password = pass
                this.poolName = liteEco.javaClass.simpleName
                this.maximumPoolSize = 20
                this.isReadOnly = false
                this.transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }

            Flyway.configure()
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .dataSource(hikari)
                .load()
                .migrate()

            @OptIn(ExperimentalKeywordApi::class)
            Database.connect(hikari!!, databaseConfig = DatabaseConfig {
                preserveKeywordCasing = true
            })


            val tables = liteEco.currencyImpl.getCurrenciesKeys().map { Account(it) }.toTypedArray()

            loggedTransaction {
                SchemaUtils.create(*tables, MonologTable)
            }
        } catch (e: Exception) {
            liteEco.logger.severe("Database initialization failed: ${e.message}")
        }
    }

    override fun onDisable() {
        try {
            hikari?.close()
            liteEco.logger.info("✅ Database connection closed cleanly.")
        } catch (e: Exception) {
            liteEco.logger.warning("⚠️ Failed to close database connection: ${e.message}")
        } finally {
            hikari = null
        }
    }
}