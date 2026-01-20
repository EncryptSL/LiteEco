package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class PlayerListeners(
    private val liteEco: LiteEco
) : Listener {

    private val preventSync = mutableSetOf<UUID>()

    @EventHandler
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId
        val username = event.name
        liteEco.accountManager.createOrUpdateAndCache(uuid, username)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onKick(event: PlayerKickEvent) {
        val reason = PlainTextComponentSerializer.plainText().serialize(event.reason())
        if (reason.contains("logged in from another location", ignoreCase = true)) {
            preventSync.add(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (preventSync.contains(uuid)) {
            preventSync.remove(uuid)
            liteEco.logger.info("Skipping account sync for ${player.name} (Duplicate login detected - data protection).")
            return
        }

        if (event.reason == PlayerQuitEvent.QuitReason.ERRONEOUS_STATE) {
            liteEco.logger.warn("Skipping account sync for ${player.name} (Erroneous connection state).")
            return
        }

        liteEco.accountManager.syncAccount(uuid)
    }

}