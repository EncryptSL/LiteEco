package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.Account
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.extensions.io
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class SuspendLiteEcoEconomyWrapper : ModernLiteEcoEconomyImpl() {

    override suspend fun getUserByUUID(uuid: UUID, currency: String): UserEntity? = io {
        try {
            if (Account.isPlayerOnline(uuid) || Account.isAccountCached(uuid, currency)) {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val name = offlinePlayer.name ?: "Unknown"

                UserEntity(name, uuid, Account.getBalance(uuid, currency))
            } else {
                LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency)
            }
        } catch (e: Exception) {
            LiteEco.instance.logger.error("Error in getUserByUUID for $uuid: ${e.message}")
            null
        }
    }

    override suspend fun createOrUpdateAccount(uuid: UUID, username: String, currency: String, value: BigDecimal, ignoreUpdate: Boolean): Boolean {
        val user = getUserByUUID(uuid, currency)
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

        return !userExists
    }

    override suspend fun createOrUpdateAndCache(uuid: UUID, username: String, currency: String, start: BigDecimal) {
        val user = try {
            getUserByUUID(uuid, currency)
        } catch (e: Exception) {
            LiteEco.instance.logger.error("Error in createOrUpdateAndCache for $uuid: ${e.message}")
            return
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

    override suspend fun delete(uuid: UUID, currency: String): Boolean {
        val user = getUserByUUID(uuid, currency)

        return user?.let {
            Account.clear(uuid)
            io { LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency) }
            true
        } ?: false
    }

    override suspend fun purgeAccounts(currency: String) = io {
        LiteEco.instance.databaseEcoModel.purgeAccounts(currency)
    }

    override suspend fun purgeInvalidAccounts(currency: String) = io {
        LiteEco.instance.databaseEcoModel.purgeInvalidAccounts(currency)
    }

    override suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal) = io {
        LiteEco.instance.databaseEcoModel.purgeDefaultAccounts(defaultValue, currency)
    }

    override suspend fun withdraw(uuid: UUID, currency: String, amount: BigDecimal) {
        if (Account.isPlayerOnline(uuid) && Account.isAccountCached(uuid, currency)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).minus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.withdraw(uuid, currency, amount) }
        }
    }

    override suspend fun deposit(uuid: UUID, currency: String, amount: BigDecimal) {
        if (Account.isPlayerOnline(uuid) && Account.isAccountCached(uuid, currency)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).plus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.deposit(uuid, currency, amount) }
        }
    }

    override suspend fun set(uuid: UUID, currency: String, amount: BigDecimal) {
        if (Account.isPlayerOnline(uuid)) {
            cacheAccount(uuid, currency, amount)
        } else {
            io { LiteEco.instance.databaseEcoModel.set(uuid, currency, amount) }
        }
    }

    override suspend fun sync(uuid: UUID) {
        Account.sync(uuid)
    }

    override suspend fun transfer(sender: UUID, target: UUID, currency: String, amount: BigDecimal) {
        withdraw(sender, currency, amount)
        deposit(target, currency, amount)
    }

    override suspend fun getBalance(uuid: UUID, currency: String): BigDecimal {
        val user = getUserByUUID(uuid, currency)
        return user?.let {
            user.money
        } ?: BigDecimal.ZERO
    }

    private fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        Account.cache(uuid, currency, amount)
    }
}