package com.github.encryptsl.lite.eco.utils

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import java.util.Optional
import java.util.UUID

object PlayerUtils {
    fun getOfflinePlayer(uuid: UUID): OfflinePlayer {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid)).orElseGet { Bukkit.getPlayer(uuid) }
    }

    fun getUniqueId(offlinePlayer: OfflinePlayer): UUID {
        return Optional.ofNullable(offlinePlayer.getPlayer()).map(Entity::getUniqueId).orElseGet(offlinePlayer::getUniqueId)
    }

    fun getUniqueId(player: String): UUID {
        return getUniqueId(Bukkit.getOfflinePlayer(player))
    }
}