package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.EconomyService
import com.github.encryptsl.lite.eco.api.economy.account.InMemoryAccountStoreRepository
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import com.github.encryptsl.lite.eco.common.database.models.AccountRepository
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.migrator.entity.PlayerBalances
import java.math.BigDecimal
import java.util.*

class LiteEcoEconomy : LiteEconomyAPI {

    private val economyService: EconomyService = EconomyService()
    private val accountRepository: AccountRepository by lazy { AccountRepository() }

    override fun account(): EconomyService = economyService

    override fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String) {
        accountRepository.batchInsert(importData, currency)
    }

    override fun getTopBalance(currency: String): Map<String, BigDecimal> {
        val currencyData = LiteEco.instance.baseConfig.economy.currencies[currency.lowercase()]
        val topSettings = currencyData?.topBalances

        val isFilteringEnabled = topSettings?.filtering ?: false
        val blackList = if (isFilteringEnabled) topSettings.blacklist else emptyList()
        val combinedList = blackList.toSet().plus(setOf("NULL", "CONSOLE", "SERVER"))

        return accountRepository.getTopBalance(currency)
            .mapValues { e ->
                if (InMemoryAccountStoreRepository.isAccountCached(e.value.uuid, currency))
                    InMemoryAccountStoreRepository.getBalance(e.value.uuid, currency)
                else
                    e.value.money
            }
            .filterKeys { name ->
                combinedList.none { it.equals(name, ignoreCase = true) }
            }
            .toList()
            .sortedByDescending { (_, balance) -> balance }
            .toMap()
    }

    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> {
        return accountRepository.getUUIDNameMap(currency)
    }

    override suspend fun createOrUpdateAccount(
        uuid: UUID,
        username: String,
        currency: String,
        value: BigDecimal,
        ignoreUpdate: Boolean
    ): Boolean {
        return InMemoryAccountStoreRepository.withLock(uuid) {
            val user = economyService.getUserByUUID(uuid, currency)
            val userExists = user != null

            io {
                if (userExists) {
                    if (!ignoreUpdate) {
                        accountRepository.updatePlayerName(uuid, username, currency)
                    }
                } else {
                    accountRepository.createPlayerAccount(username, uuid, currency, value)
                }
            }

            !userExists
        }
    }

    override suspend fun createOrUpdateAndCache(
        uuid: UUID,
        username: String,
        currency: String,
        start: BigDecimal
    ) {
        InMemoryAccountStoreRepository.withLock(uuid) {
            val user = economyService.getUserByUUID(uuid, currency)
            val balanceToCache = user?.money ?: start

            if (user == null) {
                accountRepository.createPlayerAccount(username, uuid, currency, start)
            } else {
                accountRepository.updatePlayerName(uuid, username, currency)
            }

            InMemoryAccountStoreRepository.cache(uuid, username, currency, balanceToCache)
        }
    }

    override suspend fun purgeAccounts(currency: String): Int = io {
        accountRepository.purgeAccounts(currency)
    }

    override suspend fun purgeTestAccounts(): Int = io {
        accountRepository.purgeTestAccounts()
    }

    override suspend fun purgeInvalidAccounts(currency: String): Int = io {
        accountRepository.purgeInvalidAccounts(currency)
    }

    override suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal): Int = io {
        accountRepository.purgeDefaultAccounts(defaultValue, currency)
    }

    override suspend fun getBalancesForCurrency(currency: String): List<PlayerBalances.PlayerBalance> {
        val cleanCurrency = currency.lowercase()
        val dbBalances = accountRepository.getBalancesForCurrency(cleanCurrency)

        return dbBalances.map { player ->
            if (InMemoryAccountStoreRepository.isAccountCached(player.uuid, cleanCurrency)) {
                val cachedBalance = InMemoryAccountStoreRepository.getBalance(player.uuid, cleanCurrency)
                player.copy(money = cachedBalance)
            } else {
                player
            }
        }
    }
}