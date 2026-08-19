package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
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

    override suspend fun createOrUpdateAndCache(uuid: UUID, username: String, currency: String, start: BigDecimal) {
        AccountCache.withLock(uuid) {
            val user = try {
                account().getUserByUUID(uuid, currency)
            } catch (e: Exception) {
                LiteEco.instance.logger.error("Error in createOrUpdateAndCache for $uuid: ${e.message}")
                return@withLock
            }

            val balanceToCache = user?.money ?: start

            io {
                if (user == null) {
                    LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, start)
                } else {
                    LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
                }
            }

            cacheAccount(uuid, currency, balanceToCache)
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

    private fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.cache(uuid, currency, amount)
    }
}