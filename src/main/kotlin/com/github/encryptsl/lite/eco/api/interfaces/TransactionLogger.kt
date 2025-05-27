package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import java.math.BigDecimal

interface TransactionLogger {
    suspend fun logging(economyOperations: EconomyOperations, sender:String, target: String, currency: String, previousBalance: BigDecimal, newBalance: BigDecimal)
    fun clearLogs()
    suspend fun getLog(): List<EconomyLog>
}