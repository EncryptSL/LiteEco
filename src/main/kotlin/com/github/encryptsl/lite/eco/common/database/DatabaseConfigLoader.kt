package com.github.encryptsl.lite.eco.common.database

import org.bukkit.configuration.ConfigurationSection

class DatabaseConfigLoader(
    private val config: ConfigurationSection
) {

    data class DbConfig(
        val driver: String,
        val jdbcUrl: String,
        val username: String,
        val password: String
    )

    fun load(): DbConfig {
        val driver = config.getString("database.connection.driverClassName")
            ?: throw IllegalArgumentException("Missing database.connection.driverClassName in config")

        val jdbcUrl = config.getString("database.connection.jdbc_url")
            ?: throw IllegalArgumentException("Missing database.connection.jdbc_url in config")

        val username = config.getString("database.connection.username")
            ?: throw IllegalArgumentException("Missing database.connection.username in config")

        val password = config.getString("database.connection.password")
            ?: throw IllegalArgumentException("Missing database.connection.password in config")

        return DbConfig(
            driver = driver,
            jdbcUrl = jdbcUrl,
            username = username,
            password = password
        )
    }
}