package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.runBlockingIO
import java.math.BigDecimal
import java.util.*

class SuspendLiteEcoEconomyWrapper {

    private val economyImpl: ModernLiteEcoEconomyImpl = ModernLiteEcoEconomyImpl()

    suspend fun getUserByUUID(uuid: UUID, currency: String = "dollars"): Optional<User> =
        runBlockingIO { economyImpl.getUserByUUID(uuid, currency) }

    suspend fun createOrUpdateAccount(uuid: UUID, username: String, currency: String = "dollars", value: BigDecimal, ignoreUpdate: Boolean = true): Boolean {
        return runBlockingIO {
            val userOpt = economyImpl.getUserByUUID(uuid, currency).orElse(null)
            if (userOpt == null) {
                LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, value)
                true
            } else {
                if (!ignoreUpdate) {
                    LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
                }
                false
            }
        }
    }

    suspend fun createOrUpdateAndCache(uuid: UUID, username: String, currency: String = "dollars", start: BigDecimal) {
        runBlockingIO {
            val user = economyImpl.getUserByUUID(uuid, currency).orElse(null)
            if (user == null) {
                LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, start)
            } else {
                LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
                LiteEco.instance.api.cacheAccount(uuid, currency, user.money)
            }
        }
    }

    suspend fun deleteAccount(uuid: UUID, currency: String = "dollars"): Boolean = runBlockingIO {
        val user = economyImpl.getUserByUUID(uuid, currency).orElse(null)
        if (user == null) {
            false
        } else {
            PlayerAccount.clearFromCache(uuid)
            LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency)
            true
        }
    }

    suspend fun purgeAccounts(currency: String) = runBlockingIO {
        LiteEco.instance.databaseEcoModel.purgeAccounts(currency)
    }

    suspend fun purgeInvalidAccounts(currency: String) = runBlockingIO {
        LiteEco.instance.databaseEcoModel.purgeInvalidAccounts(currency)
    }

    suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal) = runBlockingIO {
        LiteEco.instance.databaseEcoModel.purgeDefaultAccounts(defaultValue, currency)
    }

    suspend fun withdraw(uuid: UUID, currency: String = "dollars", amount: BigDecimal) = runBlockingIO {
        economyImpl.withDrawMoney(uuid, currency, amount)
    }

    suspend fun deposit(uuid: UUID, currency: String = "dollars", amount: BigDecimal) = runBlockingIO {
        economyImpl.depositMoney(uuid, currency, amount)
    }

    suspend fun set(uuid: UUID, currency: String = "dollars", amount: BigDecimal) = runBlockingIO {
        economyImpl.setMoney(uuid, currency, amount)
    }

    suspend fun syncAccount(uuid: UUID, currency: String = "dollars") = runBlockingIO {
        economyImpl.syncAccount(uuid, currency)
    }

    suspend fun transfer(sender: UUID, target: UUID, currency: String = "dollars", amount: BigDecimal) = runBlockingIO {
        economyImpl.transfer(sender, target, currency, amount)
    }

    suspend fun hasAccount(uuid: UUID, currency: String = "dollars"): Boolean = runBlockingIO {
        economyImpl.hasAccount(uuid, currency)
    }

    suspend fun getBalance(uuid: UUID, currency: String = "dollars"): BigDecimal = runBlockingIO {
        economyImpl.getBalance(uuid, currency)
    }
}