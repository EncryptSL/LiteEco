package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.objects.ModernText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerListeners(
    private val liteEco: LiteEco
) : Listener {

    private val preventSync: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    @EventHandler(priority = EventPriority.HIGH)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        val uuid = event.uniqueId
        val username = event.name

        try {
            if (AccountCache.isAccountCached(uuid, null)) {
                preventSync.add(uuid)
                liteEco.logger.warn("Duplicate login detected for $username ($uuid). Flagged preventSync.")
            }
            runBlocking(Dispatchers.IO) {
                liteEco.accountManager.createOrUpdateAndCache(uuid, username)
            }
        } catch (e: Exception) {
            liteEco.logger.error("Failed to load or create account for $username ($uuid): ${e.message}", e)

            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                ModernText.miniModernText("<red>Error while loading or creating your economy account.</red>")
            )
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onKick(event: PlayerKickEvent) {
        val reason = PlainTextComponentSerializer.plainText().serialize(event.reason())
        if (reason.contains("logged in from another location", ignoreCase = true)) {
            preventSync.add(event.player.uniqueId)
            liteEco.logger.warn("Player ${event.player.name} kicked due to duplicate login location.")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (preventSync.remove(uuid)) {
            liteEco.logger.warn("Skipping account sync for ${player.name} (Duplicate login detected - data protection).")
            return
        }

        if (event.reason == PlayerQuitEvent.QuitReason.ERRONEOUS_STATE) {
            liteEco.logger.warn("Skipping account sync for ${player.name} (Erroneous connection state).")
            return
        }

        liteEco.accountManager.syncAccount(uuid, shouldUnload = true)
    }
}