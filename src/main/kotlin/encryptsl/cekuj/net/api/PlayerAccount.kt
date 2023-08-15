package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
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
                plugin.logger.info("Account $uuid with ${it.let { a -> value }} was changed successfully  !")
            }
        }
    }

    override fun syncAccount(uuid: UUID) {
        getAccount()[uuid]?.let { preparedStatements.setMoney(uuid, it) }.also {
            plugin.logger.info("Account $uuid was synced with database  !")
            removeAccount(uuid)
        }
    }

    override fun syncAccounts() {
        getAccount().toList().forEach { a ->
            preparedStatements.setMoney(a.first, a.second)
        }.also {
            plugin.logger.info("Accounts are synced with database !")
            getAccount().clear()
        }
    }

    override fun removeAccount(uuid: UUID) {
        val player = getAccount().keys.find { key -> key == uuid } ?: return

        getAccount().remove(player)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return getAccount().containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getOnlinePlayers().find { player -> player.uniqueId == uuid } != null
    }

    override fun getAccount(): MutableMap<UUID, Double> {
        return cache
    }
}