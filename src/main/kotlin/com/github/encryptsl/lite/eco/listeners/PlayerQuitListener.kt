package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.OperationType
import com.github.encryptsl.lite.eco.api.events.AccountManageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        liteEco.pluginManager.callEvent(AccountManageEvent(player, OperationType.SYNC_ACCOUNT))
    }

}