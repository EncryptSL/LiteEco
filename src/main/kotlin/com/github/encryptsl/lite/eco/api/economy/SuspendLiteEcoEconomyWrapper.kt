package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.common.extensions.io
import java.math.BigDecimal
import java.util.*

class SuspendLiteEcoEconomyWrapper : ModernLiteEcoEconomyImpl() {

    override suspend fun createOrUpdateAccount(uuid: UUID, username: String, currency: String, value: BigDecimal, ignoreUpdate: Boolean): Boolean {
        return AccountCache.withLock(uuid) {
            val user = account().getUserByUUID(uuid, currency)
            val userExists = user != null

            io {
                if (userExists) {
                    if (!ignoreUpdate) {
                        LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
                    }
                } else {
                    LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, value)
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
        AccountCache.withLock(uuid) {
            val user = account().getUserByUUID(uuid, currency)

            val balanceToCache = user?.money ?: start

            if (user == null) {
                LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, start)
            } else {
                LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
            }

            cacheAccount(uuid, username, currency, balanceToCache)
        }
    }

    override suspend fun purgeAccounts(currency: String) = io {
        LiteEco.instance.databaseEcoModel.purgeAccounts(currency)
    }

    override suspend fun purgeTestAccounts() {
        LiteEco.instance.databaseEcoModel.purgeTestAccounts()
    }

    override suspend fun purgeInvalidAccounts(currency: String) = io {
        LiteEco.instance.databaseEcoModel.purgeInvalidAccounts(currency)
    }

    override suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal) = io {
        LiteEco.instance.databaseEcoModel.purgeDefaultAccounts(defaultValue, currency)
    }

    override suspend fun getBalancesForCurrency(currency: String): List<PlayerBalances.PlayerBalance> {
        val cleanCurrency = currency.lowercase()
        val dbBalances = LiteEco.instance.databaseEcoModel.getBalancesForCurrency(cleanCurrency)

        return dbBalances.map { player ->
            if (AccountCache.isAccountCached(player.uuid, cleanCurrency)) {
                player.copy(money = AccountCache.getBalance(player.uuid, cleanCurrency))
            } else {
                player
            }
        }
    }

    private fun cacheAccount(uuid: UUID, username: String, currency: String, amount: BigDecimal) {
        AccountCache.cache(uuid, username, currency, amount)
    }
}