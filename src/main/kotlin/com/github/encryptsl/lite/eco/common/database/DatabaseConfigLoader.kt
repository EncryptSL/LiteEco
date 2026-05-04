package com.github.encryptsl.lite.eco.common.database

import com.github.encryptsl.lite.eco.common.config.BaseConfig

class DatabaseConfigLoader(
    private val config: BaseConfig
) {

    data class DbConfig(
        val driver: String,
        val jdbcUrl: String,
        val username: String,
        val password: String
    )

    fun load(): DbConfig {
        val driver = config.database.connection.driverClassName

        val jdbcUrl = config.database.connection.jdbcUrl

        val username = config.database.connection.username

        val password = config.database.connection.password

        return DbConfig(
            driver = driver,
            jdbcUrl = jdbcUrl,
            username = username,
            password = password
        )
    }
}