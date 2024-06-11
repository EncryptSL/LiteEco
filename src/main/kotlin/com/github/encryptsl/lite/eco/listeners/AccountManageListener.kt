package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.OperationType
import com.github.encryptsl.lite.eco.api.events.AccountManageEvent
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountManageListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val player: OfflinePlayer = event.offlinePlayer

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> liteEco.api.createAccount(player, liteEco.config.getDouble("economy.starting_balance"))
            OperationType.CACHING_ACCOUNT -> {
                liteEco.databaseEcoModel.getUserByUUID(player.uniqueId)
                    .thenAccept { liteEco.api.cacheAccount(player, it.money) }
            }
            OperationType.SYNC_ACCOUNT -> liteEco.api.syncAccount(player)
        }
    }
}