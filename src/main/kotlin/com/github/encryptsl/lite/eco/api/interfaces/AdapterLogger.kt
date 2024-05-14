package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import java.util.concurrent.CompletableFuture

interface AdapterLogger {
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)
    fun clearLogs()
    fun getLog(): CompletableFuture<List<EconomyLog>>
}