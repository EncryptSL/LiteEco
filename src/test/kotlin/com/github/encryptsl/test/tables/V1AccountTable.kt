package com.github.encryptsl.lite.eco.common.database.com.github.encryptsl.test.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable


class V1AccountTable(currency: String) : LongIdTable("lite_eco_$currency") {
    val username = varchar("username", 36)
    val uuid = uuid("uuid")
    val money = decimal("money", 18, 9)
}