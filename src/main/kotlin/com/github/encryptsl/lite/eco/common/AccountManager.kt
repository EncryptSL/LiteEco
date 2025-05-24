package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import java.util.*

class AccountManager(private val liteEco: LiteEco) {

    fun createAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
                liteEco.suspendApiWrapper.createAccount(Bukkit.getOfflinePlayer(uuid), currency, liteEco.currencyImpl.getCurrencyStartBalance(currency))
            }
        }
    }

    fun syncAccount(uuid: UUID) {
        liteEco.api.syncAccount(uuid)
    }

    fun cachingAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
                val result = liteEco.databaseEcoModel.getUserByUUID(uuid, currency).await()
                if (result.isPresent) {
                    liteEco.api.cacheAccount(uuid, currency, result.get().money)
                }
            }
        }
    }


}