package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.AccountAPI
import org.bukkit.Bukkit
import java.util.*

class PlayerAccount(private val liteEco: LiteEco) : AccountAPI {

    private val cache: MutableMap<UUID, Double> = HashMap()

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache.put(uuid, value).also {
                liteEco.logger.info("Account $uuid with ${liteEco.api.formatted(value)} was cached successfully !")
            }
        } else {
            cache.put(uuid, value)?.also {
                liteEco.logger.info("Account $uuid with ${it.let { a -> liteEco.api.formatted(a) }} was changed successfully  !")
            }
        }
    }

    override fun syncAccount(uuid: UUID) {
        getAccount()[uuid]?.let { liteEco.preparedStatements.setMoney(uuid, it) }.also {
            liteEco.logger.info("Account $uuid was synced with database  !")
            removeAccount(uuid)
        }
    }

    override fun syncAccounts() {
        getAccount().toList().forEach { a ->
            liteEco.preparedStatements.setMoney(a.first, a.second)
        }.also {
            liteEco.logger.info("Accounts are synced with database !")
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