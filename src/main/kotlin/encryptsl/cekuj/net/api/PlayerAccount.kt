package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.api.interfaces.AccountAPI
import encryptsl.cekuj.net.api.objects.BalanceCache
import encryptsl.cekuj.net.database.models.PreparedStatements
import encryptsl.cekuj.net.utils.DebugLogger
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
            for (c in BalanceCache.cache) {
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