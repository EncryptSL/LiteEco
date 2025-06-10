package com.github.encryptsl.lite.eco.common.database

import MigrationUtils
import com.github.encryptsl.lite.eco.common.database.com.github.encryptsl.test.tables.V1AccountTable
import com.github.encryptsl.lite.eco.common.database.com.github.encryptsl.test.tables.V2AccountTable
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test

class FlyWayMigrationScript {

    private var hikari: HikariDataSource? = null

    private val driver = "org.mariadb.jdbc.Driver"
    private val jdbcUrl = "jdbc:mariadb://127.0.0.1:3306/minecraft_server"
    private val username = "minecraft_server"
    private val password = "minecraft_3"

    @Test
    fun onLoad() {
        try {
            initConnect(driver, jdbcUrl, username, password)
            println("✅ Database connection established successfully.")
        } catch (e: Exception) {
            println("❌ Database failed to initialize: ${e.message}")
        }
    }

    fun initConnect(driver: String, jdbcHost: String, user: String, pass: String) {
        try {
            hikari = HikariDataSource().apply {
                this.driverClassName = driver
                this.jdbcUrl = jdbcHost
                this.username = user
                this.password = pass
                this.poolName = this::class.java.simpleName
                this.maximumPoolSize = 20
                this.isReadOnly = false
                this.transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }

            Database.connect(hikari!!, databaseConfig = DatabaseConfig {
                @OptIn(ExperimentalKeywordApi::class)
                preserveKeywordCasing = true
            }, connectionAutoRegistration = ExposedConnectionImpl())
        } catch (e: Exception) {
            println("Database initialization failed: ${e.message}")
        }

        transaction {

            val oldTables = arrayOf(V1AccountTable("dollars"), V2AccountTable("credits"))
            SchemaUtils.create(*oldTables)
            commit()

            println("--- GENERATING MIGRATION SCRIPTS ---")
            println("Connecting to temporary in-memory database and simulating old schema.")

            val newTables = arrayOf(V2AccountTable("dollars"), V2AccountTable("credits"))
            val statements = MigrationUtils.statementsRequiredForDatabaseMigration(*newTables)

            if (statements.isEmpty()) {
                println("No migration statements required. Current schema matches the desired new schema.")
            } else {
                println("\n--- SQL Migration Statements for Flyway ---")
                statements.forEach { sql ->
                    println("$sql;")
                }
                println("------------------------------------------\n")
            }

            newTables.forEach {
                println("--- CREATE TABLE statement for ${it.tableName} ---")
                SchemaUtils.createStatements(it).forEach { sql ->
                    println("$sql;")
                }
                println("------------------------------------------------------------------")
            }
        }
    }

    fun onDisable() {
        try {
            hikari?.close()
            println("✅ Database connection closed cleanly.")
        } catch (e: Exception) {
            println("⚠️ Failed to close database connection: ${e.message}")
        } finally {
            hikari = null
        }
    }
}