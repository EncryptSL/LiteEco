package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.Wallet
import com.github.encryptsl.lite.eco.api.interfaces.IAccount
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.io
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AccountCache : IAccount {

    private const val SYNC_BLOCKED = "Sync BLOCKED for {}: Data integrity risk (isSuccessfullyLoaded = false)."
    private const val SYNC_FAIL_NO_ROWS_UPDATE = "Sync FAIL (No rows updated): {} -> {} ({})"
    private const val SYNC_FAIL_EXCEPTION = "Sync FAIL: {} -> {} ({}). Error: {}"
    private const val SYNC_SUCCESS_CLEARED = "Sync SUCCESS: Account for {} cleared from cache."
    private const val SYNC_SUCCESS_RETAINED = "Sync SUCCESS: Account for {} updated in DB (retained in cache)."
    private const val SYNC_HOLD_SAVE_FAILURE = "Sync HOLD: Account for {} retained in cache due to save failure."

    private const val SHUTDOWN_INIT = "Initiating global shutdown synchronization..."
    private const val SHUTDOWN_SKIP_UNLOADED = "Skipping shutdown sync for {}: Data integrity flag is FALSE."
    private const val SHUTDOWN_CRITICAL_LOSS = "CRITICAL LOSS: Could not save {} ({}) during shutdown! Error: {}"
    private const val SHUTDOWN_SUCCESS = "SUCCESS: All accounts successfully backed up to database. Cache cleared."
    private const val SHUTDOWN_ERROR_WARNING = "WARNING: Shutdown sync completed with errors. Cache was NOT cleared to prevent data loss."

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    internal val cache = ConcurrentHashMap<UUID, Wallet>()
    private val locks = ConcurrentHashMap<UUID, Mutex>()

    fun getLock(uuid: UUID): Mutex {
        return locks.computeIfAbsent(uuid) { Mutex() }
    }

    suspend inline fun <T> withLock(uuid: UUID, crossinline block: suspend () -> T): T {
        return getLock(uuid).withLock {
            block()
        }
    }

    fun removeLock(uuid: UUID) {
        locks.remove(uuid)
    }

    override fun updateBalance(
        uuid: UUID,
        currency: String,
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal {
        val account = cache.getOrPut(uuid) { Wallet() }
        account.isSuccessfullyLoaded = true

        return account.balances.compute(currency) { _, currentAmount ->
            val present = currentAmount ?: BigDecimal.ZERO
            transform(present.setScale(2, RoundingMode.HALF_UP))
        } ?: BigDecimal.ZERO
    }

    override fun startJanitor(liteEco: LiteEco) {
        val delay = 20L * 300
        val period = 20L * 300

        val janitorTask = Runnable {
            if (cache.isEmpty()) return@Runnable

            val offlineUUIDs = cache.keys.filter { uuid -> !isPlayerOnline(uuid) }

            offlineUUIDs.forEach { uuid ->
                liteEco.pluginScope.launch {
                    withLock(uuid) {
                        // Re-check player status inside lock to prevent race conditions on reconnect
                        if (!isPlayerOnline(uuid)) {
                            syncUnsafe(uuid, shouldUnload = true)
                        }
                    }
                }
            }
        }

        liteEco.schedulerHelper.runAsyncTimer(delay, period, janitorTask)
    }

    override fun cache(uuid: UUID, username: String?, currency: String, value: BigDecimal) {
        val account = cache.getOrPut(uuid) { Wallet() }
        account.balances[currency] = value
        username?.let { account.username = it }
        account.isSuccessfullyLoaded = true
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.balances?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    /**
     * Suspendable thread-safe sync method to be called from exterior scopes.
     */
    override suspend fun sync(uuid: UUID, shouldUnload: Boolean): Boolean {
        return withLock(uuid) {
            syncUnsafe(uuid, shouldUnload)
        }
    }

    /**
     * Unsafe sync implementation to be executed ONLY within an existing lock scope.
     */
    suspend fun syncUnsafe(uuid: UUID, shouldUnload: Boolean): Boolean = io {
        val account = cache[uuid] ?: return@io false

        if (!account.isSuccessfullyLoaded) {
            LiteEco.instance.logger.error(SYNC_BLOCKED, uuid)
            return@io false
        }

        var isAllSavedSuccessfully = true

        account.balances.forEach { (currency, amount) ->
            if (amount < BigDecimal.ZERO) return@forEach

            try {
                val isSaved = databaseEcoModel.set(uuid, currency, amount)

                if (!isSaved) {
                    isAllSavedSuccessfully = false
                    LiteEco.instance.logger.error(SYNC_FAIL_NO_ROWS_UPDATE, uuid, currency, amount)
                } else {
                    LiteEco.instance.debugger.debug(AccountCache::class.java, "Sync OK: $uuid -> $currency ($amount)")
                }
            } catch (e: Exception) {
                isAllSavedSuccessfully = false
                LiteEco.instance.logger.error(SYNC_FAIL_EXCEPTION, uuid, currency, amount, e.message)
            }
        }

        if (isAllSavedSuccessfully) {
            if (shouldUnload) {
                cache.remove(uuid)
                removeLock(uuid) // Clean up unused mutex
                LiteEco.instance.logger.info(SYNC_SUCCESS_CLEARED, uuid)
            } else {
                LiteEco.instance.logger.info(SYNC_SUCCESS_RETAINED, uuid)
            }
        } else {
            LiteEco.instance.logger.warn(SYNC_HOLD_SAVE_FAILURE, uuid)
        }

        return@io isAllSavedSuccessfully
    }

    override fun syncAccounts() {
        LiteEco.instance.logger.info(SHUTDOWN_INIT)

        val uuids = cache.keys.toList()
        var hasErrorOccurred = false

        for (uuid in uuids) {
            val account = cache[uuid] ?: continue

            if (!account.isSuccessfullyLoaded) {
                LiteEco.instance.logger.warn(SHUTDOWN_SKIP_UNLOADED, uuid)
                continue
            }

            account.balances.forEach { (currency, amount) ->
                if (amount < BigDecimal.ZERO) return@forEach

                try {
                    val isSaved = databaseEcoModel.set(uuid, currency, amount)

                    if (!isSaved) {
                        hasErrorOccurred = true
                        LiteEco.instance.logger.error(SYNC_FAIL_NO_ROWS_UPDATE, uuid, currency, amount)
                    }
                } catch (e: Exception) {
                    hasErrorOccurred = true
                    LiteEco.instance.logger.error(SHUTDOWN_CRITICAL_LOSS, uuid, currency, e.message, e)
                }
            }
        }

        if (!hasErrorOccurred) {
            cache.clear()
            locks.clear()
            LiteEco.instance.logger.info(SHUTDOWN_SUCCESS)
        } else {
            LiteEco.instance.debugger.dumpUnsavedAccounts(
                cache,
                "Shutdown sync completed with errors. Cache was NOT cleared to prevent data loss.",
                LiteEco.instance.databaseConnector.isSqlite
            )
            LiteEco.instance.logger.error(SHUTDOWN_ERROR_WARNING)
        }
    }

    override fun clear(uuid: UUID) {
        cache.remove(uuid)
        removeLock(uuid)
    }

    override fun isAccountCached(uuid: UUID, currency: String?): Boolean {
        val wallet = cache[uuid] ?: return false
        return currency?.let { wallet.balances.containsKey(it) } ?: true
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}