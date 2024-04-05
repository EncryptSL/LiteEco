package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog

interface AdapterLogger {
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)
    fun clearLogs()
    fun getLog(): List<EconomyLog>
}