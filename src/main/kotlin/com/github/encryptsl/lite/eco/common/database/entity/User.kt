package com.github.encryptsl.lite.eco.common.database.entity

import java.math.BigDecimal
import java.util.*

data class User(
    val userName: String,
    val uuid: UUID,
    val money: BigDecimal
)