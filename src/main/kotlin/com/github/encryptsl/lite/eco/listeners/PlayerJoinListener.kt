package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.OperationType
import com.github.encryptsl.lite.eco.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.player
        liteEco.pluginManager.callEvent(AccountManageEvent(player.uniqueId, OperationType.CACHING_ACCOUNT))
    }
}