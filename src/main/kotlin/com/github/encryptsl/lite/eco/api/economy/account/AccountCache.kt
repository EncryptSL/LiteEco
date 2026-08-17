package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.Wallet
import com.github.encryptsl.lite.eco.api.interfaces.IAccount
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AccountCache : IAccount {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    internal val cache = ConcurrentHashMap<UUID, Wallet>()

    private val locks = Array(64) { Mutex() }

    fun getLock(uuid: UUID): Mutex {
        val index = (uuid.hashCode() and Int.MAX_VALUE) % locks.size
        return locks[index]
    }

    suspend inline fun <T> withLock(uuid: UUID, crossinline block: suspend () -> T): T {
        return getLock(uuid).withLock {
            block()
        }
    }

    override fun startJanitor(liteEco: LiteEco) {

        val delay = 20L * 300
        val period = 20L * 300

        val janitorTask = Runnable {
            if (cache.isEmpty()) return@Runnable

            val offlineUUIDs = cache.keys.filter { uuid -> Bukkit.getPlayer(uuid) == null }

            offlineUUIDs.forEach { uuid ->
                liteEco.pluginScope.launch {
                    withLock(uuid) {
                        sync(uuid)
                    }
                }
            }
        }

        liteEco.schedulerHelper.runAsyncTimer(delay, period, janitorTask)
    }

    override fun cache(uuid: UUID, currency: String, value: BigDecimal) {
        val account = cache.getOrPut(uuid) { Wallet() }
        account.balances[currency] = value
        account.isSuccessfullyLoaded = true
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.balances?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    override fun sync(uuid: UUID): Boolean {
        LiteEco.instance.logger.info("Attempting synchronization for UUID: $uuid")
        val account = cache[uuid] ?: return false

        if (!account.isSuccessfullyLoaded) {
            LiteEco.instance.logger.error("Sync BLOCKED for $uuid: Data integrity risk (isSuccessfullyLoaded = false).")
            return false
        }

        var isAllSavedSuccessfully = true

        account.balances.forEach { (currency, amount) ->
            if (amount < BigDecimal.ZERO) return@forEach

            try {
                databaseEcoModel.set(uuid, currency, amount)

                val dbBalance = databaseEcoModel.getBalance(uuid, currency)

                if (dbBalance != amount) {
                    isAllSavedSuccessfully = false
                    LiteEco.instance.logger.error("Sync FAIL (Mismatched DB value): $uuid -> $currency. Expected: $amount, DB has: $dbBalance")
                } else {
                    LiteEco.instance.debugger.debug(AccountCache::class.java, "Sync OK: $uuid -> $currency ($amount)")
                }
            } catch (e: Exception) {
                isAllSavedSuccessfully = false
                LiteEco.instance.logger.error("Sync FAIL: $uuid -> $currency. Data preserved in cache. Error: ${e.message}")
            }
        }

        return if (isAllSavedSuccessfully) {
            cache.remove(uuid)
            LiteEco.instance.logger.info("Sync SUCCESS: Account for $uuid cleared from cache.")
            true
        } else {
            LiteEco.instance.logger.warn("Sync HOLD: Account for $uuid retained in cache due to save failure.")
            false
        }
    }

    override fun syncAccounts() {
        LiteEco.instance.logger.info("Initiating global shutdown synchronization...")

        val uuids = cache.keys.toList()
        var hasErrorOccurred = false

        for (uuid in uuids) {
            val account = cache[uuid] ?: continue

            if (!account.isSuccessfullyLoaded) {
                LiteEco.instance.logger.warn("Skipping shutdown sync for $uuid: Data integrity flag is FALSE.")
                continue
            }

            account.balances.forEach { (currency, amount) ->
                if (amount < BigDecimal.ZERO) return@forEach

                try {
                    databaseEcoModel.set(uuid, currency, amount)

                    val dbBalance = databaseEcoModel.getBalance(uuid, currency)

                    if (dbBalance != amount) {
                        hasErrorOccurred = true
                        LiteEco.instance.logger.error("CRITICAL LOSS: Mismatched DB value for $uuid ($currency) during shutdown! Expected: $amount, DB has: $dbBalance")
                    }
                } catch (e: Exception) {
                    hasErrorOccurred = true
                    LiteEco.instance.logger.error("CRITICAL LOSS: Could not save $uuid ($currency) during shutdown! Error: ${e.message}", e)
                }
            }
        }

        if (!hasErrorOccurred) {
            cache.clear()
            LiteEco.instance.logger.info("SUCCESS: All accounts successfully backed up to database. Cache cleared.")
        } else {
            LiteEco.instance.logger.error("WARNING: Shutdown sync completed with errors. Cache was NOT cleared to prevent data loss.")
        }
    }

    override fun clear(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID, currency: String?): Boolean {
        val wallet = cache[uuid] ?: return false
        return currency?.let { wallet.balances.containsKey(it) } ?: true
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}