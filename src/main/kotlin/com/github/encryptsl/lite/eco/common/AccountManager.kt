package com.github.encryptsl.lite.eco.common

import com.github.encryptsl.lite.eco.LiteEco
import org.bukkit.Bukkit
import java.util.*

class AccountManager(private val liteEco: LiteEco) {

    fun createAccount(uuid: UUID) {
        for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
            liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), currency, liteEco.currencyImpl.getCurrencyStartBalance(currency))
        }
    }

    fun syncAccount(uuid: UUID) {
        liteEco.api.syncAccount(uuid)
    }

    fun cachingAccount(uuid: UUID) {
        for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
            val future = liteEco.databaseEcoModel.getUserByUUID(uuid, currency).thenAccept {
                if (it.isPresent) {
                    liteEco.api.cacheAccount(uuid, currency, it.get().money)
                }
            }
            future.join()
        }
    }


}