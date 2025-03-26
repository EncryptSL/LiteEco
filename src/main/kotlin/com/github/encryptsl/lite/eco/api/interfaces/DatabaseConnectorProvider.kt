package com.github.encryptsl.lite.eco.api.interfaces

interface DatabaseConnectorProvider {
    fun load()
    fun initConnect(driver: String, jdbcHost: String, user: String, pass: String)
}