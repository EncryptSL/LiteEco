package com.github.encryptsl.lite.eco.api.migrator.interfaces

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import java.text.SimpleDateFormat
import java.util.*

interface Export {
    val date_and_time: String
        get() = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())


    suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean
    enum class SQLDialect(val hexPrefix: String, val hexSuffix: String) {
        MARIADB("0x", ""),
        SQLITE("X'", "'");

        fun formatHex(uuidString: String): String {
            return "$hexPrefix${uuidString.replace("-", "")}$hexSuffix"
        }
    }
}