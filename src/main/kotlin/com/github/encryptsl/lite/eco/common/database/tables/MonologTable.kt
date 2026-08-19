package com.github.encryptsl.lite.eco.common.database.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp


object MonologTable : LongIdTable("lite_eco_monolog") {
    val action = varchar("action", 8)
    val sender = varchar("sender", 36)
    val target = varchar("target", 36)
    val currency = varchar("currency", 16)
    val previousBalance = decimal("previous_balance", 30, 2)
    val newBalance = decimal("new_balance", 30, 2)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}