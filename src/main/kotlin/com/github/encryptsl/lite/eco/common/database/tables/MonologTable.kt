package com.github.encryptsl.lite.eco.common.database.tables

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MonologTable : Table("lite_eco_monolog") {
    private val id = integer( "id").autoIncrement()
    val action = varchar("action", 8)
    val sender = varchar("sender", 36)
    val target = varchar("target", 36)
    val currency = varchar("currency", 16)
    val previousBalance = decimal("previous_balance", 18, 9)
    val newBalance = decimal("new_balance", 18, 9)
    val timestamp = timestamp("timestamp").default(Clock.System.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}