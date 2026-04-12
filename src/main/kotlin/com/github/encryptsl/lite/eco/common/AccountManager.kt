package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.io
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.*

class AccountManager(private val liteEco: LiteEco) {

    suspend fun createOrUpdateAndCache(uuid: UUID, username: String) = io {
        val currencies = liteEco.currencyImpl.getCurrenciesKeys()

        val tasks = currencies.map { currency ->
            async {
                val startBalance = liteEco.currencyImpl.getCurrencyStartBalance(currency)
                liteEco.api.createOrUpdateAndCache(uuid, username, currency, startBalance)
            }
        }
        tasks.awaitAll()
    }

    fun syncAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            liteEco.api.syncAccount(uuid)
        }
    }
}