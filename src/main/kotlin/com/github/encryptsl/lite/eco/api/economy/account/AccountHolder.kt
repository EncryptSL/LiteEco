package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.IAccountHolder
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.extensions.io
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class AccountHolder : IAccountHolder {

    override suspend fun getUserByUUID(uuid: UUID, currency: String): UserEntity? = io {
        try {
            if (AccountCache.isAccountCached(uuid, currency)) {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val name = offlinePlayer.name ?: "Unknown"

                UserEntity(name, uuid, AccountCache.getBalance(uuid, currency))
            } else {
                LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency)
            }
        } catch (e: Exception) {
            LiteEco.instance.logger.error("Error in getUserByUUID for $uuid: ${e.message}")
            null
        }
    }

    override fun hasAccount(uuid: UUID, currency: String): Boolean =
        LiteEco.instance.databaseEcoModel.getExistPlayerAccount(uuid, currency)

    override fun has(uuid: UUID, currency: String, requiredAmount: BigDecimal): Boolean {
        if (AccountCache.isAccountCached(uuid, currency)) {
            return requiredAmount <= AccountCache.getBalance(uuid, currency)
        }

        val offlineBalance = LiteEco.instance.databaseEcoModel.getBalance(uuid, currency)
        return requiredAmount <= offlineBalance
    }

    override suspend fun delete(uuid: UUID, currency: String): Boolean {
        return AccountCache.withLock(uuid) {
            val user = getUserByUUID(uuid, currency)

            user?.let {
                AccountCache.clear(uuid)
                io { LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency) }
                true
            } ?: false
        }
    }

    override suspend fun withdraw(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.withLock(uuid) {
            withdrawUnsafe(uuid, currency, amount)
        }
    }

    override suspend fun deposit(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.withLock(uuid) {
            depositUnsafe(uuid, currency, amount)
        }
    }

    override suspend fun set(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.withLock(uuid) {
            if (AccountCache.isAccountCached(uuid, currency)) {
                cacheAccount(uuid, currency, amount)
            } else {
                io { LiteEco.instance.databaseEcoModel.set(uuid, currency, amount) }
            }
        }
    }

    override suspend fun sync(uuid: UUID): Boolean = io {
        AccountCache.withLock(uuid) {
            AccountCache.sync(uuid)
        }
    }

    override suspend fun transfer(sender: UUID, target: UUID, currency: String, amount: BigDecimal): Boolean {
        if (amount.signum() <= 0 || sender == target) return false

        val firstLock = if (sender < target) sender else target
        val secondLock = if (sender < target) target else sender

        return AccountCache.withLock(firstLock) {
            AccountCache.withLock(secondLock) {
                val senderBalance = getBalance(sender, currency)

                if (senderBalance < amount) {
                    return@withLock false
                }

                val targetBalance = getBalance(target, currency)
                if (LiteEco.instance.currencyImpl.getCheckBalanceLimit(targetBalance, currency, amount)) {
                    return@withLock false
                }

                withdrawUnsafe(sender, currency, amount)
                depositUnsafe(target, currency, amount)
                true
            }
        }
    }

    override suspend fun getBalance(uuid: UUID, currency: String): BigDecimal {
        if (AccountCache.isAccountCached(uuid, currency)) {
            return AccountCache.getBalance(uuid, currency)
        }
        val user = getUserByUUID(uuid, currency)
        return user?.money ?: BigDecimal.ZERO
    }

    override fun syncAccounts() {
        try {
            AccountCache.syncAccounts()
        } catch (e: Exception) {
            LiteEco.instance.logger.error(e.message ?: e.localizedMessage)
        }
    }

    private fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.cache(uuid, currency, amount)
    }

    private suspend fun withdrawUnsafe(uuid: UUID, currency: String, amount: BigDecimal) {
        if (AccountCache.isAccountCached(uuid, currency)) {
            val current = AccountCache.getBalance(uuid, currency)
            cacheAccount(uuid, currency, current.minus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.withdraw(uuid, currency, amount) }
        }
    }

    private suspend fun depositUnsafe(uuid: UUID, currency: String, amount: BigDecimal) {
        if (AccountCache.isAccountCached(uuid, currency)) {
            val current = AccountCache.getBalance(uuid, currency)
            cacheAccount(uuid, currency, current.plus(amount))
        } else {
            io { LiteEco.instance.databaseEcoModel.deposit(uuid, currency, amount) }
        }
    }
}