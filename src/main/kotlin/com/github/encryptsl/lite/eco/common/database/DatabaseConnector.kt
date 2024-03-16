package com.github.encryptsl.lite.eco.common.database

import com.zaxxer.hikari.HikariDataSource
import com.github.encryptsl.lite.eco.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.jetbrains.exposed.sql.*

class DatabaseConnector : DatabaseConnectorProvider {
    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        loggedTransaction {
            SchemaUtils.create(Account)
        }
    }
}