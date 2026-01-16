package com.github.encryptsl.lite.eco.api.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.UUID
import kotlin.jvm.java

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    internal val cache = mutableMapOf<UUID, CachedAccount>()

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
        val account = cache.getOrPut(uuid) { CachedAccount() }
        account.balances[currency] = value
        account.isSuccessfullyLoaded = true
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.balances?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    override fun syncAccount(uuid: UUID) {
        LiteEco.instance.logger.info("Attempting synchronization for UUID: $uuid")
        val account = cache[uuid] ?: return

        if (!account.isSuccessfullyLoaded) {
            LiteEco.instance.logger.error("Sync BLOCKED for $uuid: Data integrity risk (isSuccessfullyLoaded = false).")
            return
        }

        val failedCurrencies = mutableListOf<String>()

        account.balances.forEach { (currency, amount) ->
            try {
                if (amount < BigDecimal.ZERO) return@forEach

                databaseEcoModel.set(uuid, currency, amount)
                LiteEco.instance.debugger.debug(PlayerAccount::class.java, "Sync OK: $uuid -> $currency ($amount)")
            } catch (e: Exception) {
                failedCurrencies.add(currency)
                LiteEco.instance.logger.error("Sync FAIL: $uuid -> $currency. Data preserved in cache for next cycle. Error: ${e.message}")
            }
        }

        if (failedCurrencies.isEmpty()) {
            cache.remove(uuid)
        } else {
            account.balances.keys.retainAll(failedCurrencies.toSet())
        }
    }

    override fun syncAccounts() {
        val uuids = cache.keys.toList()
        for (uuid in uuids) {
            val account = cache[uuid] ?: continue

            if (!account.isSuccessfullyLoaded) {
                LiteEco.instance.logger.warn("Skipping shutdown sync for $uuid: Data integrity flag is FALSE.")
                continue
            }
            account.balances.forEach { (currency, amount) ->
                try {
                    databaseEcoModel.set(uuid, currency, amount)
                } catch (e: Exception) {
                    LiteEco.instance.logger.error("CRITICAL LOSS: Could not save $uuid ($currency) during shutdown! Error: ${e.message}")
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
        val account = cache[uuid] ?: return false
        if (currency == null) return true
        return account.balances.containsKey(currency)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}