package com.github.encryptsl.lite.eco.common.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MonologTable : Table("lite_eco_monolog") {
    private val id = integer( "id").autoIncrement()
    val action = varchar("action", 8)
    val sender = varchar("sender", 36)
    val target = varchar("target", 36)
    val currency = varchar("currency", 16)
    val previousBalance = decimal("previous_balance", 30, 2)
    val newBalance = decimal("new_balance", 30, 2)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}