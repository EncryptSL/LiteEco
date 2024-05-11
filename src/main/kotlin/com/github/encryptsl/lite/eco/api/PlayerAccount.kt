package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.util.*

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    private val cache: HashMap<UUID, Double> = HashMap()

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache[uuid] = value
        } else {
            cache[uuid] = value
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID, value: Double) {
        try {
            databaseEcoModel.setMoney(uuid, value)
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccount(uuid: UUID) {
        try {
            databaseEcoModel.setMoney(uuid, getBalance(uuid))
            clearFromCache(uuid)
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccounts() {
        try {
            if (cache.isEmpty()) return
            for (c in cache) { syncAccount(c.key, c.value) }
            cache.clear()
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun clearFromCache(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}