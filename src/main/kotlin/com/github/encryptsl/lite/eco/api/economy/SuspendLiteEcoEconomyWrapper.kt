package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.runBlockingIO
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class SuspendLiteEcoEconomyWrapper {

    private val economyImpl: ModernLiteEcoEconomyImpl = ModernLiteEcoEconomyImpl()

    suspend fun getUserByUUID(uuid: UUID, currency: String = "dollars"): Optional<User> =
        runBlockingIO { economyImpl.getUserByUUID(uuid, currency) }

    suspend fun createAccount(player: OfflinePlayer, currency: String = "dollars", startAmount: BigDecimal): Boolean =
        runBlockingIO {
            val userOpt = economyImpl.getUserByUUID(player.uniqueId, currency)
            if (!userOpt.isPresent) {
                LiteEco.instance.databaseEcoModel.createPlayerAccount(
                    player.name.toString(),
                    player.uniqueId,
                    currency,
                    startAmount
                )
                true
            } else {
                LiteEco.instance.databaseEcoModel.updatePlayerName(
                    player.uniqueId,
                    player.name.toString(),
                    currency
                )
                false
            }
        }

    suspend fun createAccount(uuid: UUID, username: String, currency: String = "dollars", startAmount: BigDecimal): Boolean =
        runBlockingIO {
            val userOpt = economyImpl.getUserByUUID(uuid, currency)
            if (!userOpt.isPresent) {
                LiteEco.instance.databaseEcoModel.createPlayerAccount(
                    username,
                    uuid,
                    currency,
                    startAmount
                )
                true
            } else {
                LiteEco.instance.databaseEcoModel.updatePlayerName(
                    uuid,
                    username,
                    currency
                )
                false
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

    suspend fun syncAccount(uuid: UUID, currency: String = "dollars") {
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