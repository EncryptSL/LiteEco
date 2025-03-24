package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.OperationType
import com.github.encryptsl.lite.eco.api.events.AccountManageEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountManageListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val uuid = event.uuid

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> {
                for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
                    liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), currency, liteEco.currencyImpl.getCurrencyStartBalance(currency))
                }
            }
            OperationType.CACHING_ACCOUNT -> {
                for (currency in liteEco.currencyImpl.getCurrenciesKeys()) {
                    val future = liteEco.databaseEcoModel.getUserByUUID(uuid, currency).thenAccept {
                        if (it.isPresent) {
                            liteEco.api.cacheAccount(uuid, currency, it.get().money)
                        }
                    }
                    future.join()
                }
            }
            OperationType.SYNC_ACCOUNT -> liteEco.api.syncAccount(uuid)
        }
    }
}