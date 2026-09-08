package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.IAccountHolder
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.extensions.io
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class AccountHolder : IAccountHolder {

    override suspend fun updateBalance(
        uuid: UUID,
        currency: String,
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal {
        return AccountCache.withLock(uuid) {
            if (AccountCache.isAccountCached(uuid, currency)) {
                AccountCache.updateBalance(uuid, currency, transform)
            } else {
                val current = getBalance(uuid, currency)
                val newBalance = transform(current)
                io { LiteEco.instance.databaseEcoModel.set(uuid, currency, newBalance) }
                newBalance
            }
        }
    }

    override suspend fun withdraw(uuid: UUID, currency: String, amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) return
        updateBalance(uuid, currency) { current -> current.minus(amount) }
    }

    override suspend fun deposit(uuid: UUID, currency: String, amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) return
        updateBalance(uuid, currency) { current -> current.plus(amount) }
    }

    override suspend fun set(uuid: UUID, currency: String, amount: BigDecimal) {
        AccountCache.withLock(uuid) {
            if (AccountCache.isAccountCached(uuid, currency)) {
                AccountCache.cache(uuid, null, currency, amount)
            } else {
                io { LiteEco.instance.databaseEcoModel.set(uuid, currency, amount) }
            }
        }
    }

    override suspend fun transfer(
        sender: UUID,
        target: UUID,
        currency: String,
        amount: BigDecimal
    ): Boolean {
        if (amount <= BigDecimal.ZERO || sender == target) return false

        val firstLock = if (sender < target) sender else target
        val secondLock = if (sender < target) target else sender

        return AccountCache.withLock(firstLock) {
            AccountCache.withLock(secondLock) {
                val senderBalance = getBalance(sender, currency)
                if (senderBalance < amount) return@withLock false

                if (AccountCache.isAccountCached(sender, currency)) {
                    AccountCache.updateBalance(sender, currency) { current -> current.minus(amount) }
                } else {
                    io { LiteEco.instance.databaseEcoModel.withdraw(sender, currency, amount) }
                }

                if (AccountCache.isAccountCached(target, currency)) {
                    AccountCache.updateBalance(target, currency) { current -> current.plus(amount) }
                } else {
                    io { LiteEco.instance.databaseEcoModel.deposit(target, currency, amount) }
                }

                true
            }
        }
    }

    override suspend fun getBalance(uuid: UUID, currency: String): BigDecimal {
        if (AccountCache.isAccountCached(uuid, currency)) {
            return AccountCache.getBalance(uuid, currency)
        }

        val userEntity = io { LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency) }
        val balance = userEntity?.money ?: BigDecimal.ZERO

        return balance
    }

    override suspend fun getUserByUUID(uuid: UUID, currency: String): UserEntity? {
        if (AccountCache.isAccountCached(uuid, currency)) {
            val balance = AccountCache.getBalance(uuid, currency)
            val account = AccountCache.cache[uuid]
            return UserEntity(account?.username ?: "Unknown", uuid, balance)
        }
        return io { LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency) }
    }

    override suspend fun delete(uuid: UUID, currency: String): Boolean {
        return AccountCache.withLock(uuid) {
            val exists = hasAccount(uuid, currency)
            AccountCache.clear(uuid)

            io { LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency) }

            exists
        }
    }

    override suspend fun sync(uuid: UUID, shouldUnload: Boolean): Boolean {
        return AccountCache.sync(uuid, shouldUnload)
    }

    override fun syncAccounts() {
        AccountCache.syncAccounts()
    }

    override fun hasAccount(uuid: UUID, currency: String): Boolean {
        return AccountCache.isAccountCached(uuid, currency)
    }

    override fun has(uuid: UUID, currency: String, requiredAmount: BigDecimal): Boolean {
        val currentBalance = AccountCache.getBalance(uuid, currency)
        return currentBalance >= requiredAmount
    }
}