package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import java.util.*

class AccountManager(private val liteEco: LiteEco) {

    fun createAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            liteEco.currencyImpl.getCurrenciesKeys()
                .map { it to liteEco.currencyImpl.getCurrencyStartBalance(it) }
                .forEach { (currency, amount) ->
                    liteEco.suspendApiWrapper.createAccount(Bukkit.getOfflinePlayer(uuid), currency, amount)
                }
        }
    }

    fun syncAccount(uuid: UUID) {
        liteEco.api.syncAccount(uuid)
    }

    fun cachingAccount(uuid: UUID) {
        liteEco.pluginScope.launch {
            liteEco.currencyImpl.getCurrenciesKeys()
                .mapNotNull { currency ->
                    liteEco.databaseEcoModel.getUserByUUID(uuid, currency)?.let { user -> currency to user.money }
                }
                .forEach { (currency, money) -> liteEco.api.cacheAccount(uuid, currency, money) }
        }
    }


}