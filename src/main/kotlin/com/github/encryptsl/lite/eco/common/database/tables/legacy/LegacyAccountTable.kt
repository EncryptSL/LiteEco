package com.github.encryptsl.lite.eco.common.database.tables.legacy

import org.jetbrains.exposed.sql.Table

object LegacyAccountTable : Table("lite_eco") {
    private val id = integer("id").autoIncrement()
    val username = varchar("username", 36)
    val uuid = varchar("uuid", 36)
    val money = double("money")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}