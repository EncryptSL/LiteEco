package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.io
import kotlinx.coroutines.*
import java.util.*

class AccountManager(private val liteEco: LiteEco) {

    suspend fun createOrUpdateAndCache(uuid: UUID, username: String) = io {
        val currencies = liteEco.currencyImpl.getCurrenciesKeys()

        coroutineScope {
            currencies.map { currency ->
                async {
                    val startBalance = liteEco.currencyImpl.getCurrencyStartBalance(currency)
                    liteEco.api.createOrUpdateAndCache(uuid, username, currency, startBalance)
                }
            }.awaitAll()
        }
    }

    fun syncAccount(uuid: UUID, shouldUnload: Boolean) {
        liteEco.pluginScope.launch(Dispatchers.IO) {
            // AccountCache handles lock acquisition, DB write, cache eviction, and lock cleanup internally
            liteEco.api.account().sync(uuid, shouldUnload)
        }
    }
}