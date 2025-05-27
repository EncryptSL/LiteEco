package com.github.encryptsl.lite.eco.api.migrator.entity

import java.math.BigDecimal
import java.util.*

class PlayerBalances {
    data class PlayerBalance(val id: Int, val uuid: UUID, val username: String?, val money: BigDecimal)
}