package com.github.encryptsl.lite.eco.api.migrator.interfaces

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances

interface Export {
    val timestamp: Long
        get() = System.currentTimeMillis()

    suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean
    enum class SQLDialect(val hexPrefix: String, val hexSuffix: String) {
        MARIADB("0x", ""),
        SQLITE("X'", "'");

        fun formatHex(uuidString: String): String {
            return "$hexPrefix${uuidString.replace("-", "")}$hexSuffix"
        }
    }
}