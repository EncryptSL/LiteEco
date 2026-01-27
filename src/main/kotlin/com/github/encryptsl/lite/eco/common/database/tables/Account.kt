package com.github.encryptsl.lite.eco.common.database.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
class Account(currency: String) : LongIdTable("lite_eco_$currency") {
    val username = varchar("username", 36)
    val uuid = uuid("uuid").uniqueIndex("idx_${currency}_uuid")
    val money = decimal("money", 30, 2)
}