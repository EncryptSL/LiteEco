package com.github.encryptsl.lite.eco.common.database.tables

import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid


@OptIn(ExperimentalUuidApi::class)
class Account(currency: String) : LongIdTable("lite_eco_$currency") {
    val username = varchar("username", 36)
    val uuid = uuid("uuid").uniqueIndex("idx_${currency}_uuid")
    val money = decimal("money", 30, 2)
}

@OptIn(ExperimentalUuidApi::class)
fun ResultRow.toUserEntity(table: Account) = UserEntity(
    userName = this[table.username],
    uuid = this[table.uuid].toJavaUuid(),
    money = this[table.money]
)