package com.github.encryptsl.lite.eco.common.database.tables

import org.jetbrains.exposed.sql.Table

class Account(currency: String) : Table("lite_eco_$currency") {
    private val id = integer("id").autoIncrement()
    val username = varchar("username", 36)
    val uuid = uuid("uuid")
    val money = decimal("money", 1, 2)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}