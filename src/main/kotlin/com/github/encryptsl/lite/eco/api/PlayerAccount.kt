package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.api.objects.BalanceCache
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.util.*

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            BalanceCache.cache[uuid] = value
        } else {
            BalanceCache.cache[uuid] = value
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return BalanceCache.cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID, value: Double) {
        try {
            databaseEcoModel.setMoney(uuid, value)
        } catch (e : Exception) {
            Bukkit.getServer().logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccount(uuid: UUID) {
        try {
            databaseEcoModel.setMoney(uuid, getBalance(uuid))
            clearFromCache(uuid)
        } catch (e : Exception) {
            Bukkit.getServer().logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccounts() {
        try {
            if (BalanceCache.cache.isEmpty()) return
            for (c in BalanceCache.cache) { syncAccount(c.key, c.value) }
            BalanceCache.cache.clear()
        } catch (e : Exception) {
            Bukkit.getServer().logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun clearFromCache(uuid: UUID) {
        val player = BalanceCache.cache.keys.find { key -> key == uuid } ?: return

        BalanceCache.cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return BalanceCache.cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}