package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import java.math.BigDecimal

interface TransactionLogger {
    suspend fun logging(typeLogger: TypeLogger, sender:String, target: String, currency: String, previousBalance: BigDecimal, newBalance: BigDecimal)
    fun clearLogs()
    suspend fun getLog(): List<EconomyLog>
}