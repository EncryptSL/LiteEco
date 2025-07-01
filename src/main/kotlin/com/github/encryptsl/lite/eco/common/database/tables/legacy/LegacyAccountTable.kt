package com.github.encryptsl.lite.eco.common.database.tables.legacy

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable


object LegacyAccountTable : LongIdTable("lite_eco") {
    val username = varchar("username", 36)
    val uuid = varchar("uuid", 36)
    val money = double("money")
}