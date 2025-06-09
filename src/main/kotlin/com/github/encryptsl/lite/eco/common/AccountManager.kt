package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.map

class AccountManager(private val liteEco: LiteEco) {

    fun createOrUpdateAndCache(uuid: UUID, username: String) {
        liteEco.pluginScope.launch {
            liteEco.currencyImpl.getCurrenciesKeys().map {
                it to liteEco.currencyImpl.getCurrencyStartBalance(it)
            }.forEach { (currency, amount) ->
                liteEco.suspendApiWrapper.createOrUpdateAndCache(uuid, username, currency, amount)
            }
        }
    }

    fun syncAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            liteEco.suspendApiWrapper.syncAccount(uuid)
        }
    }
}