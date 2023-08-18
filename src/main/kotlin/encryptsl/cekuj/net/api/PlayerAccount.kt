package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.api.interfaces.AccountAPI
import encryptsl.cekuj.net.database.models.PreparedStatements
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.*

class PlayerAccount(val plugin: Plugin) : AccountAPI {

    private val cache: MutableMap<UUID, Double> = HashMap()
    private val preparedStatements: PreparedStatements by lazy { PreparedStatements() }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache.put(uuid, value).also {
                plugin.logger.info("Account $uuid with $value was cached successfully !")
            }
        } else {
            cache.put(uuid, value)?.also {
                plugin.logger.info("Account $uuid with $value was changed successfully  !")
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
            plugin.logger.info("Account $uuid was synced with database  !")
        }.onFailure {
            plugin.logger.severe(it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccounts() {
        runCatching {
            cache.toList().forEach { a ->
                preparedStatements.setMoney(a.first, a.second)
            }
        }.onSuccess {
            plugin.logger.info("Accounts are synced with database !")
        }.onFailure {
            plugin.logger.severe(it.message ?: it.localizedMessage)
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
        return Bukkit.getOnlinePlayers().first { p -> runCatching { p.uniqueId == uuid }.getOrNull() == true } != null
    }
}