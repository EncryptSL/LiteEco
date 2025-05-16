package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.OperationType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener(
    private val liteEco: LiteEco
) : Listener {

    @EventHandler
    fun onLoginEvent(event: PlayerLoginEvent) {
        val player = event.player
        liteEco.accountManager.createAccount(player.uniqueId)
    }

}