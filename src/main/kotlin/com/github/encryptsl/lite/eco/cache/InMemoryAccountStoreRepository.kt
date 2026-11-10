package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.AccountState
import com.github.encryptsl.lite.eco.api.interfaces.AccountStore
import com.github.encryptsl.lite.eco.common.database.models.AccountRepository
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.moneyScale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object InMemoryAccountStoreRepository : AccountStore {

    private const val SYNC_BLOCKED = "Sync BLOCKED for {}: Data integrity risk (isSuccessfullyLoaded = false)."
    private const val SYNC_FAIL_NO_ROWS_UPDATE = "Sync FAIL (No rows updated): {} -> {} ({})"
    private const val SYNC_FAIL_EXCEPTION = "Sync FAIL: {} -> {} ({}). Error: {}"
    private const val SYNC_SUCCESS_CLEARED = "Sync SUCCESS: Account for {} cleared from cache."
    private const val SYNC_SUCCESS_RETAINED = "Sync SUCCESS: Account for {} updated in DB (retained in cache)."
    private const val SYNC_HOLD_SAVE_FAILURE = "Sync HOLD: Account for {} retained in cache due to save failure."

    private const val SHUTDOWN_INIT = "Initiating global shutdown synchronization..."
    private const val SHUTDOWN_SKIP_UNLOADED = "Skipping shutdown sync for {}: Data integrity flag is FALSE."
    private const val SHUTDOWN_SUCCESS = "SUCCESS: All accounts successfully backed up to database. Cache cleared."
    private const val SHUTDOWN_ERROR_WARNING = "WARNING: Shutdown sync completed with errors. Cache was NOT cleared to prevent data loss."

    private val sqlAccountRepository: AccountRepository by lazy { AccountRepository() }
    internal val cache = ConcurrentHashMap<UUID, AccountState>()
    private val locks = ConcurrentHashMap<UUID, Mutex>()

    fun getLock(uuid: UUID): Mutex {
        return locks.computeIfAbsent(uuid) { Mutex() }
    }

    suspend inline fun <T> withLock(uuid: UUID, crossinline block: suspend () -> T): T {
        return getLock(uuid).withLock { block() }
    }

    override fun updateBalance(
        uuid: UUID,
        currency: String,
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal {
        val account = cache.getOrPut(uuid) { AccountState() }
        account.isSuccessfullyLoaded = true

        return account.balances.compute(currency) { _, currentAmount ->
            val present = currentAmount ?: BigDecimal.ZERO
            transform(present).moneyScale()
        } ?: BigDecimal.ZERO
    }

    override fun startAccountSyncEvictionWorker(liteEco: LiteEco) {
        val delay = 300L
        val period = 300L

        val janitorTask = Runnable {
            if (cache.isEmpty()) return@Runnable

            val offlineUUIDs = cache.keys.filter { uuid -> !isPlayerOnline(uuid) }

            offlineUUIDs.forEach { uuid ->
                liteEco.pluginScope.launch {
                    withLock(uuid) {
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
        val account = cache.getOrPut(uuid) { AccountState() }
        account.balances[currency] = value.moneyScale()
        username?.let { account.username = it }
        account.isSuccessfullyLoaded = true
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.balances?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    override suspend fun sync(uuid: UUID, shouldUnload: Boolean): Boolean {
        return withLock(uuid) {
            syncUnsafe(uuid, shouldUnload)
        }
    }

    suspend fun syncUnsafe(uuid: UUID, shouldUnload: Boolean): Boolean = io {
        val account = cache[uuid] ?: return@io true

        if (!account.isSuccessfullyLoaded) {
            LiteEco.instance.logger.error(SYNC_BLOCKED, uuid)
            return@io false
        }

        var isAllSavedSuccessfully = true

        account.balances.forEach { (currency, amount) ->
            if (amount < BigDecimal.ZERO) return@forEach

            try {
                val isSaved = sqlAccountRepository.set(uuid, currency, amount)
                if (!isSaved) {
                    isAllSavedSuccessfully = false
                    LiteEco.instance.logger.error(SYNC_FAIL_NO_ROWS_UPDATE, uuid, currency, amount)
                } else {
                    LiteEco.instance.debugger.debug(InMemoryAccountStoreRepository::class.java, "Sync OK: $uuid -> $currency ($amount)")
                }
            } catch (e: Exception) {
                isAllSavedSuccessfully = false
                LiteEco.instance.logger.error(SYNC_FAIL_EXCEPTION, uuid, currency, amount, e.message)
            }
        }

        if (isAllSavedSuccessfully) {
            if (shouldUnload) {
                cache.remove(uuid)
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
        var hasErrorOccurred = false

        cache.keys.toList().forEach { uuid ->
            val account = cache[uuid] ?: return@forEach

            if (!account.isSuccessfullyLoaded) {
                LiteEco.instance.logger.warn(SHUTDOWN_SKIP_UNLOADED, uuid)
                return@forEach
            }

            try {
                runBlocking {
                    val success = syncUnsafe(uuid, shouldUnload = false)
                    if (!success) hasErrorOccurred = true
                }
            } catch (e: Exception) {
                hasErrorOccurred = true
                LiteEco.instance.logger.error("Critical error syncing account $uuid during shutdown: ${e.message}", e)
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
    }

    override fun isAccountCached(uuid: UUID, currency: String?): Boolean {
        val accountState = cache[uuid] ?: return false
        return currency?.let { accountState.balances.containsKey(it) } ?: true
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}