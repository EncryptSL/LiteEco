package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.Currency
import com.github.encryptsl.lite.eco.api.enums.OperationType
import com.github.encryptsl.lite.eco.api.events.AccountManageEvent
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountManageListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val player: OfflinePlayer = event.offlinePlayer
        val currencies = Currency(liteEco)

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> {
                for (currency in currencies.getCurrenciesNames()!!) {
                    liteEco.api.createAccount(player, currency, liteEco.currencyImpl.getCurrencyStartBalance(currency))
                }
            }
            OperationType.CACHING_ACCOUNT -> {
                for (currency in currencies.getCurrenciesNames()!!) {
                    liteEco.databaseEcoModel.getUserByUUID(player.uniqueId, currency)
                        .thenAccept { liteEco.api.cacheAccount(player, currency, it.money) }
                }
            }
            OperationType.SYNC_ACCOUNT -> liteEco.api.syncAccount(player)
        }
    }
}