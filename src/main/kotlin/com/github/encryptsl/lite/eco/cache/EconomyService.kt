package com.github.encryptsl.lite.eco.api.economy.account

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountService
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.moneyScale
import java.math.BigDecimal
import java.util.*

class EconomyService : AccountService {

    override suspend fun updateBalance(
        uuid: UUID,
        currency: String,
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal {
        return InMemoryAccountStoreRepository.withLock(uuid) {
            updateBalanceUnsafe(uuid, currency, transform)
        }
    }

    private suspend fun updateBalanceUnsafe(
        uuid: UUID,
        currency: String,
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal {
        if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency) && LiteEco.instance.baseConfig.plugin.cache) {
            return InMemoryAccountStoreRepository.updateBalance(uuid, currency, transform)
        } else {
            val current = getBalance(uuid, currency)
            val newBalance = transform(current)
            io { LiteEco.instance.accountRepository.set(uuid, currency, newBalance) }
            return newBalance
        }
    }

    override suspend fun withdraw(uuid: UUID, currency: String, amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) return
        updateBalance(uuid, currency) { it.minus(amount) }
    }

    override suspend fun deposit(uuid: UUID, currency: String, amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) return
        updateBalance(uuid, currency) { it.plus(amount) }
    }

    override suspend fun set(uuid: UUID, currency: String, amount: BigDecimal) {
        InMemoryAccountStoreRepository.withLock(uuid) {
            if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency) && LiteEco.instance.baseConfig.plugin.cache) {
                InMemoryAccountStoreRepository.cache(uuid, null, currency, amount)
            } else {
                //InMemoryAccountStoreRepository.cache(uuid, null, currency, amount)
                io { LiteEco.instance.accountRepository.set(uuid, currency, amount) }
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

        val firstLock = if (sender.compareTo(target) < 0) sender else target
        val secondLock = if (sender.compareTo(target) < 0) target else sender

        return InMemoryAccountStoreRepository.withLock(firstLock) {
            InMemoryAccountStoreRepository.withLock(secondLock) {
                val senderBalance = getBalance(sender, currency)
                if (senderBalance < amount) return@withLock false

                updateBalanceUnsafe(sender, currency) { it.minus(amount) }
                updateBalanceUnsafe(target, currency) { it.plus(amount) }

                true
            }
        }
    }

    override suspend fun getBalance(uuid: UUID, currency: String): BigDecimal {
        if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency)) {
            return InMemoryAccountStoreRepository.getBalance(uuid, currency)
        }

        val userEntity = io { LiteEco.instance.accountRepository.getUserByUUID(uuid, currency) }
        return (userEntity?.money ?: BigDecimal.ZERO).moneyScale()
    }

    override suspend fun getUserByUUID(uuid: UUID, currency: String): UserEntity? {
        if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency) && LiteEco.instance.baseConfig.plugin.cache) {
            val balance = InMemoryAccountStoreRepository.getBalance(uuid, currency)
            val account = InMemoryAccountStoreRepository.cache[uuid]
            return UserEntity(account?.username ?: "Unknown", uuid, balance)
        }
        return io { LiteEco.instance.accountRepository.getUserByUUID(uuid, currency) }
    }

    override suspend fun delete(uuid: UUID, currency: String): Boolean {
        return InMemoryAccountStoreRepository.withLock(uuid) {
            val exists = hasAccount(uuid, currency)
            if (LiteEco.instance.baseConfig.plugin.cache) {
                InMemoryAccountStoreRepository.clear(uuid)
            }
            io { LiteEco.instance.accountRepository.deletePlayerAccount(uuid, currency) }
            exists
        }
    }

    override suspend fun sync(uuid: UUID, shouldUnload: Boolean): Boolean {
        if (!LiteEco.instance.baseConfig.plugin.cache) {
            return false
        }

        return InMemoryAccountStoreRepository.sync(uuid, shouldUnload)
    }

    override fun syncAccounts() {
        InMemoryAccountStoreRepository.syncAccounts()
    }

    override fun hasAccount(uuid: UUID, currency: String): Boolean {
        if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency) && LiteEco.instance.baseConfig.plugin.cache) {
            return true
        }

        return LiteEco.instance.accountRepository.getExistPlayerAccount(uuid, currency)
    }

    override fun has(uuid: UUID, currency: String, requiredAmount: BigDecimal): Boolean {
        if (InMemoryAccountStoreRepository.isAccountCached(uuid, currency) && LiteEco.instance.baseConfig.plugin.cache) {
            val cachedBalance = InMemoryAccountStoreRepository.getBalance(uuid, currency)
            return cachedBalance >= requiredAmount
        } else {
            val dbUser = LiteEco.instance.accountRepository.getUserByUUID(uuid, currency)
            val balance = dbUser?.money ?: BigDecimal.ZERO

            return balance >= requiredAmount
        }
    }
}