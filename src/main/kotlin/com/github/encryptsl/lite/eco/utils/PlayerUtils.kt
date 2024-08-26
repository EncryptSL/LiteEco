package com.github.encryptsl.lite.eco.utils

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

object PlayerUtils {
    fun getOfflinePlayer(uuid: UUID): OfflinePlayer {
        return Bukkit.getPlayer(uuid) ?: Bukkit.getOfflinePlayer(uuid)
    }

    fun getUniqueId(offlinePlayer: OfflinePlayer): UUID {
        return Optional.ofNullable(offlinePlayer.player).map(Player::getUniqueId).orElseGet(offlinePlayer::getUniqueId)
    }
}