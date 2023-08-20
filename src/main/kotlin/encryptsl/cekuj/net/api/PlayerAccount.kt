package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.api.interfaces.AccountAPI
import encryptsl.cekuj.net.database.models.PreparedStatements
import encryptsl.cekuj.net.utils.DebugLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PlayerAccount(val plugin: Plugin) : AccountAPI {

    private val cache: MutableMap<UUID, Double> = HashMap()
    private val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    val debugLogger: DebugLogger by lazy { DebugLogger(plugin) }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache.put(uuid, value).also {
                debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully !")
            }
        } else {
            cache.put(uuid, value)?.also {
                debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully  !")
            }
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID) {
        runCatching {
            cache[uuid]?.let { preparedStatements.setMoney(uuid, it) }
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Account $uuid was synced with database  !")
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccounts() {
        runCatching {
            cache.toList().forEach { a ->
                preparedStatements.setMoney(a.first, a.second)
            }
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Accounts are synced with database !")
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun removeAccount(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getOnlinePlayers().first { p -> p.uniqueId == uuid } != null
    }
}