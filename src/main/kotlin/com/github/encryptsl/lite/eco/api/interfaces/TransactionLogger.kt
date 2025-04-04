package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import java.math.BigDecimal

interface TransactionLogger {
    fun logging(economyOperations: EconomyOperations, sender:String, target: String, currency: String, previousBalance: BigDecimal, newBalance: BigDecimal)
    fun clearLogs()
    fun getLog(): List<EconomyLog>
}