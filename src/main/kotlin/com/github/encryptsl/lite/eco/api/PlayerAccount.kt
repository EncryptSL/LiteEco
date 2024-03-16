package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.api.objects.BalanceCache
import com.github.encryptsl.lite.eco.common.database.models.PreparedStatements
import com.github.encryptsl.lite.eco.utils.DebugLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PlayerAccount(val plugin: Plugin) : AccountAPI {

    private val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    val debugLogger: DebugLogger by lazy { DebugLogger(plugin) }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            BalanceCache.cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully !")
        } else {
            BalanceCache.cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully  !")
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return BalanceCache.cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID, value: Double) {
        runCatching {
            preparedStatements.setMoney(uuid, value)
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Account $uuid was synced with database  !")
            removeAccount(uuid)
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccount(uuid: UUID) {
        runCatching {
            preparedStatements.setMoney(uuid, getBalance(uuid))
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Account $uuid was synced with database  !")
            removeAccount(uuid)
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccounts() {
        runCatching {
            val cache = BalanceCache.cache
            if (cache.isEmpty()) return
            for (c in cache) {
                syncAccount(c.key, c.value)
            }
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Accounts are synced with database !")
            BalanceCache.cache.clear()
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun removeAccount(uuid: UUID) {
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