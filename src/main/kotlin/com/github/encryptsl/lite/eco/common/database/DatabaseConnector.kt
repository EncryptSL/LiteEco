package com.github.encryptsl.lite.eco.common.database

import com.zaxxer.hikari.HikariDataSource
import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnector(private val liteEco: LiteEco) : DatabaseConnectorProvider {

    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        transaction {
            addLogger(SqlPluginLogger(liteEco))
            SchemaUtils.create(Account)
        }
    }
}