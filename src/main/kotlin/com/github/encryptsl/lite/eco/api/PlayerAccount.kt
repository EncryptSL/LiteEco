package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    internal val cache = mutableMapOf<UUID, MutableMap<String, BigDecimal>>()

    override fun startJanitor(liteEco: LiteEco) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(liteEco, Runnable {
            if (cache.isEmpty()) return@Runnable

            val offlineUUIDs = cache.keys.filter { uuid -> Bukkit.getPlayer(uuid) == null }

            offlineUUIDs.forEach { uuid ->
                if (Bukkit.getPlayer(uuid) == null) {
                    syncAccount(uuid)
                }
            }
        }, 20L * 300, 20L * 300)
    }

    override fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal) {
        if (!isAccountCached(uuid, currency)) {
            cache.computeIfAbsent(uuid) { mutableMapOf() } [currency] = value
        } else {
            cache[uuid]?.computeIfPresent(currency) { _, _ -> value }
        }
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    override fun syncAccount(uuid: UUID) {
        LiteEco.instance.logger.info("Attempting synchronization for UUID: $uuid")
        val userBalances = cache[uuid] ?: run {
            LiteEco.instance.logger.info("Cache for $uuid is empty, nothing to synchronize.")
            return
        }
        val failedCurrencies = mutableListOf<String>()

        userBalances.forEach { (currency, amount) ->
            try {
                databaseEcoModel.set(uuid, currency, amount)
                LiteEco.instance.debugger.debug(PlayerAccount::class.java, "Sync OK: $uuid -> $currency ($amount)")
            } catch (e: Exception) {
                failedCurrencies.add(currency)
                LiteEco.instance.logger.severe("Sync FAIL: $uuid -> $currency. Data preserved in cache for next cycle. Error: ${e.message}")
            }
        }

        if (failedCurrencies.isEmpty()) {
            cache.remove(uuid)
        } else {
            userBalances.keys.retainAll(failedCurrencies.toSet())
            if (userBalances.isEmpty()) {
                cache.remove(uuid)
            }
        }
    }

    override fun syncAccounts() {
        val uuids = cache.keys.toList()
        for (uuid in uuids) {
            val data = cache[uuid] ?: continue
            data.forEach { (currency, amount) ->
                try {
                    databaseEcoModel.set(uuid, currency, amount)
                } catch (_: Exception) {
                    LiteEco.instance.logger.severe("CRITICAL LOSS: Could not save $uuid during shutdown!")
                }
            }
        }
        cache.clear()
    }

    override fun clearFromCache(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID, currency: String?): Boolean {
       return cache.containsKey(uuid) && cache[uuid]?.containsKey(currency) == true
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}