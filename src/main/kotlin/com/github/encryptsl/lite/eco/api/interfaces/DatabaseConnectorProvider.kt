package com.github.encryptsl.lite.eco.api.interfaces

interface DatabaseConnectorProvider {
    fun initConnect(jdbcHost: String, user: String, pass: String)
}