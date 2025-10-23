package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.mainThread
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class SuspendLiteEcoEconomyWrapper : ModernLiteEcoEconomyImpl() {

    override suspend fun getUserByUUID(uuid: UUID, currency: String): Optional<User> = io {
        if (PlayerAccount.isPlayerOnline(uuid) || PlayerAccount.isAccountCached(uuid, currency)) {
            val name = mainThread(LiteEco.instance) {
                Bukkit.getPlayer(uuid)?.name.toString()
            }
            Optional.of(User(name, uuid, PlayerAccount.getBalance(uuid, currency)))
        } else {
            Optional.ofNullable(LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency))
        }
    }

    override suspend fun createOrUpdateAccount(uuid: UUID, username: String, currency: String, value: BigDecimal, ignoreUpdate: Boolean): Boolean {
        val userOpt = getUserByUUID(uuid, currency).orElse(null)
        return if (userOpt == null) {
            io { LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, value) }
            true
        } else {
            if (!ignoreUpdate) {
                io { LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency) }
            }
            false
        }
    }

    override suspend fun createOrUpdateAndCache(uuid: UUID, username: String, currency: String, start: BigDecimal) {
        val user = getUserByUUID(uuid, currency).orElse(null)
        if (user == null) {
            io { LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, start) }
            cacheAccount(uuid, currency, start)
        } else {
            io { LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency) }
            cacheAccount(uuid, currency, user.money)
        }
    }

    override suspend fun deleteAccount(uuid: UUID, currency: String): Boolean {
        val user = getUserByUUID(uuid, currency).orElse(null)
        return if (user == null) {
            false
        } else {
            PlayerAccount.clearFromCache(uuid)
            io { LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency) }
            true
        }
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
        if (PlayerAccount.isPlayerOnline(uuid) && PlayerAccount.isAccountCached(uuid, currency)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).minus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.withdraw(uuid, currency, amount) }
        }
    }

    override suspend fun deposit(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid) && PlayerAccount.isAccountCached(uuid, currency)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).plus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.deposit(uuid, currency, amount) }
        }
    }

    override suspend fun set(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid)) {
            cacheAccount(uuid, currency, amount)
        } else {
            io { LiteEco.instance.databaseEcoModel.set(uuid, currency, amount) }
        }
    }

    override suspend fun syncAccount(uuid: UUID) {
        PlayerAccount.syncAccount(uuid)
    }

    override suspend fun transfer(sender: UUID, target: UUID, currency: String, amount: BigDecimal) {
        withdraw(sender, currency, amount)
        deposit(target, currency, amount)
    }

    override suspend fun getBalance(uuid: UUID, currency: String): BigDecimal
        = getUserByUUID(uuid, currency).map { it.money }.orElse(BigDecimal.ZERO)

    private fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        PlayerAccount.cacheAccount(uuid, currency, amount)
    }
}